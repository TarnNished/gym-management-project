package com.muro_akhaladze.gym_task.service;

import com.muro_akhaladze.gym_task.dto.auth.RegistrationResponse;
import com.muro_akhaladze.gym_task.dto.trainer.TrainerProfileResponse;
import com.muro_akhaladze.gym_task.dto.trainer.TrainerRegistrationRequest;
import com.muro_akhaladze.gym_task.dto.trainer.TrainerShortInfo;
import com.muro_akhaladze.gym_task.dto.trainer.TrainerUpdateRequest;
import com.muro_akhaladze.gym_task.entity.Trainer;
import com.muro_akhaladze.gym_task.entity.TrainingType;
import com.muro_akhaladze.gym_task.entity.User;
import com.muro_akhaladze.gym_task.exceptions.ApiException;
import com.muro_akhaladze.gym_task.repository.TrainerRepo;
import com.muro_akhaladze.gym_task.repository.TrainingRepo;
import com.muro_akhaladze.gym_task.repository.TrainingTypeRepo;
import com.muro_akhaladze.gym_task.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor // generates the constructor used for constructor injection
public class TrainerService {

    /** Public sort keys -> entity property paths. Anything else is rejected with 400. */
    private static final Map<String, String> SORT_FIELDS = Map.of(
            "firstName", "user.firstName",
            "lastName", "user.lastName",
            "username", "user.userName",
            "specialization", "specialization.trainingTypeName");

    private static final int MAX_PAGE_SIZE = 100;

    private final TrainerRepo trainerRepo;
    private final TrainingTypeRepo trainingTypeRepo;
    private final TrainingRepo trainingRepo;
    private final FileStorageService fileStorageService;
    private final UserRepo userRepo;
    private final UserService userService;
    private final EntityMapper mapper;

    @Transactional
    public RegistrationResponse register(TrainerRegistrationRequest request) {
        String rawPassword = userService.generatePassword();
        RegistrationResponse response = register(request, rawPassword);
        response.setPassword(rawPassword);
        return response;
    }

    /** Registration with a known password (used by the demo data loader). */
    @Transactional
    public RegistrationResponse register(TrainerRegistrationRequest request, String rawPassword) {
        TrainingType specialization = findTrainingType(request.getSpecialization());
        User user = userRepo.save(userService.newUser(request.getFirstName(), request.getLastName(), rawPassword));
        trainerRepo.save(new Trainer(user, specialization));
        log.info("Registered trainer {}", user.getUsername());
        return new RegistrationResponse(user.getUsername(), null);
    }

    @Transactional(readOnly = true)
    public TrainerProfileResponse getProfile(String username) {
        return mapper.toProfile(getEntity(username));
    }

    @Transactional
    public TrainerProfileResponse update(String username, TrainerUpdateRequest request) {
        Trainer trainer = getEntity(username);
        User user = trainer.getUser();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setActive(request.getIsActive());
        trainer.setSpecialization(findTrainingType(request.getSpecialization()));
        log.info("Updated trainer {}", username);
        return mapper.toProfile(trainer);
    }

    /** Deletes the trainer, its trainings, its trainee links, its user row and its photo file. */
    @Transactional
    public void delete(String username) {
        Trainer trainer = getEntity(username);
        String photo = trainer.getUser().getPhotoPath();
        trainingRepo.deleteAllByTrainer(trainer);
        trainer.detachFromTrainees();
        trainerRepo.delete(trainer); // CascadeType.REMOVE also deletes the User
        if (photo != null) {
            fileStorageService.delete(photo);
        }
        log.info("Deleted trainer {}", username);
    }

    /**
     * Server-side pagination + sorting of active trainers, optionally filtered by specialization.
     *
     * @param sortBy    one of firstName, lastName, username, specialization
     * @param direction asc or desc
     */
    @Transactional(readOnly = true)
    public Page<TrainerShortInfo> listTrainers(String specialization, int page, int size, String sortBy, String direction) {
        String property = SORT_FIELDS.get(sortBy);
        if (property == null) {
            throw new ApiException("INVALID_SORT",
                    "sortBy must be one of " + SORT_FIELDS.keySet(), HttpStatus.BAD_REQUEST);
        }
        Sort.Direction dir = Sort.Direction.fromOptionalString(direction)
                .orElseThrow(() -> new ApiException("INVALID_SORT", "direction must be asc or desc", HttpStatus.BAD_REQUEST));

        Sort sort = Sort.by(dir, property).and(Sort.by("id"));
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE), sort);

        Page<Trainer> trainers = (specialization == null || specialization.isBlank())
                ? trainerRepo.findByUserIsActiveTrue(pageable)
                : trainerRepo.findBySpecializationTrainingTypeNameIgnoreCaseAndUserIsActiveTrue(specialization, pageable);

        return trainers.map(mapper::toShortInfo);
    }

    Trainer getEntity(String username) {
        return trainerRepo.findByUserUserName(username)
                .orElseThrow(() -> new ApiException("NOT_FOUND", "Trainer '" + username + "' not found", HttpStatus.NOT_FOUND));
    }

    private TrainingType findTrainingType(String name) {
        return trainingTypeRepo.findByTrainingTypeNameIgnoreCase(name.trim())
                .orElseThrow(() -> new ApiException("NOT_FOUND",
                        "Training type '" + name + "' not found (see GET /api/v1/training-types)", HttpStatus.NOT_FOUND));
    }
}
