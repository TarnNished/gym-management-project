package com.muro_akhaladze.gym_task.service;

import com.muro_akhaladze.gym_task.dto.auth.RegistrationResponse;
import com.muro_akhaladze.gym_task.dto.trainee.TraineeProfileResponse;
import com.muro_akhaladze.gym_task.dto.trainee.TraineeRegistrationRequest;
import com.muro_akhaladze.gym_task.dto.trainee.TraineeUpdateRequest;
import com.muro_akhaladze.gym_task.dto.trainer.TrainerShortInfo;
import com.muro_akhaladze.gym_task.entity.Trainee;
import com.muro_akhaladze.gym_task.entity.Trainer;
import com.muro_akhaladze.gym_task.entity.User;
import com.muro_akhaladze.gym_task.exceptions.ApiException;
import com.muro_akhaladze.gym_task.repository.TraineeRepo;
import com.muro_akhaladze.gym_task.repository.TrainerRepo;
import com.muro_akhaladze.gym_task.repository.TrainingRepo;
import com.muro_akhaladze.gym_task.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor // generates the constructor used for constructor injection
public class TraineeService {
    private final TraineeRepo traineeRepo;
    private final TrainerRepo trainerRepo;
    private final TrainingRepo trainingRepo;
    private final UserRepo userRepo;
    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final EntityMapper mapper;

    @Transactional
    public RegistrationResponse register(TraineeRegistrationRequest request) {
        String rawPassword = userService.generatePassword();
        RegistrationResponse response = register(request, rawPassword);
        response.setPassword(rawPassword);
        return response;
    }

    /** Registration with a known password (used by the demo data loader). */
    @Transactional
    public RegistrationResponse register(TraineeRegistrationRequest request, String rawPassword) {
        User user = userRepo.save(userService.newUser(request.getFirstName(), request.getLastName(), rawPassword));
        Trainee trainee = new Trainee(user, request.getDateOfBirth(), request.getAddress());
        traineeRepo.save(trainee);
        log.info("Registered trainee {}", user.getUsername());
        return new RegistrationResponse(user.getUsername(), null);
    }

    @Transactional(readOnly = true)
    public TraineeProfileResponse getProfile(String username) {
        return mapper.toProfile(getEntity(username));
    }

    @Transactional
    public TraineeProfileResponse update(String username, TraineeUpdateRequest request) {
        Trainee trainee = getEntity(username);
        User user = trainee.getUser();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setActive(request.getIsActive());
        trainee.setDateOfBirth(request.getDateOfBirth());
        trainee.setAddress(request.getAddress());
        log.info("Updated trainee {}", username);
        return mapper.toProfile(trainee);
    }

    /** Deletes the trainee, its trainings, its trainer links, its user row and its photo file. */
    @Transactional
    public void delete(String username) {
        Trainee trainee = getEntity(username);
        String photo = trainee.getUser().getPhotoPath();
        trainingRepo.deleteAllByTrainee(trainee);
        trainee.clearTrainers();
        traineeRepo.delete(trainee); // CascadeType.REMOVE also deletes the User
        if (photo != null) {
            fileStorageService.delete(photo);
        }
        log.info("Deleted trainee {}", username);
    }

    /** Replaces the trainee's trainers (many-to-many) with the given list. */
    @Transactional
    public List<TrainerShortInfo> updateTrainers(String username, List<String> trainerUsernames) {
        Trainee trainee = getEntity(username);
        Set<String> wanted = new HashSet<>(trainerUsernames);
        List<Trainer> trainers = trainerRepo.findByUserUserNameIn(List.copyOf(wanted));

        if (trainers.size() != wanted.size()) {
            Set<String> found = trainers.stream().map(t -> t.getUser().getUsername()).collect(Collectors.toSet());
            wanted.removeAll(found);
            throw new ApiException("NOT_FOUND", "Trainers not found: " + wanted, HttpStatus.NOT_FOUND);
        }

        trainee.clearTrainers();
        trainers.forEach(trainee::addTrainer);
        log.info("Trainee {} now has trainers {}", username, wanted);
        return mapper.toProfile(trainee).getTrainers();
    }

    @Transactional(readOnly = true)
    public List<TrainerShortInfo> getUnassignedTrainers(String username) {
        getEntity(username); // 404 if the trainee does not exist
        return trainerRepo.findTrainersNotAssignedToTrainee(username).stream()
                .map(mapper::toShortInfo)
                .toList();
    }

    Trainee getEntity(String username) {
        return traineeRepo.findByUserUserName(username)
                .orElseThrow(() -> new ApiException("NOT_FOUND", "Trainee '" + username + "' not found", HttpStatus.NOT_FOUND));
    }
}
