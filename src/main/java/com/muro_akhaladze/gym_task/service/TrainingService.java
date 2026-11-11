package com.muro_akhaladze.gym_task.service;

import com.muro_akhaladze.gym_task.dto.training.AddTrainingRequest;
import com.muro_akhaladze.gym_task.dto.training.TraineeTrainingsFilterRequest;
import com.muro_akhaladze.gym_task.dto.training.TrainerTrainingsFilterRequest;
import com.muro_akhaladze.gym_task.dto.training.TrainingResponse;
import com.muro_akhaladze.gym_task.entity.Trainee;
import com.muro_akhaladze.gym_task.entity.Trainer;
import com.muro_akhaladze.gym_task.entity.Training;
import com.muro_akhaladze.gym_task.exceptions.ApiException;
import com.muro_akhaladze.gym_task.repository.TrainingRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor // generates the constructor used for constructor injection
public class TrainingService {

    /** Public sort keys -> entity property paths. Anything else is rejected with 400. */
    private static final Map<String, String> SORT_FIELDS = Map.of(
            "trainingDate", "trainingDate",
            "trainingName", "trainingName",
            "trainingDuration", "trainingDuration",
            "trainingType", "trainingType.trainingTypeName");

    private final TrainingRepo trainingRepo;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final EntityMapper mapper;

    /**
     * Creates a training and makes sure the trainer is linked to the trainee.
     * Only the trainee or the trainer themselves may create it, and both must be active.
     */
    @Transactional
    public TrainingResponse createTraining(AddTrainingRequest request, String callerUsername) {
        if (!request.getTraineeUsername().equals(callerUsername) && !request.getTrainerUsername().equals(callerUsername)) {
            throw new ApiException("FORBIDDEN", "You must be the trainee or the trainer of this training", HttpStatus.FORBIDDEN);
        }
        return createTraining(request);
    }

    /** Creates a training without the caller check (used by the demo data loader). */
    @Transactional
    public TrainingResponse createTraining(AddTrainingRequest request) {
        Trainee trainee = traineeService.getEntity(request.getTraineeUsername());
        Trainer trainer = trainerService.getEntity(request.getTrainerUsername());
        if (!trainee.getUser().isActive() || !trainer.getUser().isActive()) {
            throw new ApiException("INACTIVE_USER", "Both trainee and trainer must be active", HttpStatus.CONFLICT);
        }

        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(trainer.getSpecialization());
        training.setTrainingName(request.getTrainingName().trim());
        training.setTrainingDate(request.getTrainingDate());
        training.setTrainingDuration(request.getTrainingDuration().longValue());
        Training saved = trainingRepo.save(training);

        if (!trainee.getTrainers().contains(trainer)) {
            trainee.addTrainer(trainer);
        }
        log.info("Created training '{}' for {} with {}", saved.getTrainingName(),
                trainee.getUser().getUsername(), trainer.getUser().getUsername());
        return mapper.toResponse(saved);
    }

    /** Deletes a training; only its trainee or trainer may do this. */
    @Transactional
    public void deleteTraining(int id, String callerUsername) {
        Training training = trainingRepo.findById(id)
                .orElseThrow(() -> new ApiException("NOT_FOUND", "Training " + id + " not found", HttpStatus.NOT_FOUND));
        boolean isParticipant = training.getTrainee().getUser().getUsername().equals(callerUsername)
                || training.getTrainer().getUser().getUsername().equals(callerUsername);
        if (!isParticipant) {
            throw new ApiException("FORBIDDEN", "You must be the trainee or the trainer of this training", HttpStatus.FORBIDDEN);
        }
        trainingRepo.delete(training);
        log.info("Deleted training {}", id);
    }

    @Transactional(readOnly = true)
    public Page<TrainingResponse> getTraineeTrainings(String traineeUsername, TraineeTrainingsFilterRequest filter, Pageable pageable) {
        traineeService.getEntity(traineeUsername); // 404 if missing
        return trainingRepo.findTraineeTrainings(
                        traineeUsername,
                        filter.getFromDate(),
                        filter.getToDate(),
                        blankToNull(filter.getTrainerName()),
                        blankToNull(filter.getTrainingTypeName()),
                        safePageable(pageable))
                .map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<TrainingResponse> getTrainerTrainings(String trainerUsername, TrainerTrainingsFilterRequest filter, Pageable pageable) {
        trainerService.getEntity(trainerUsername); // 404 if missing
        return trainingRepo.findTrainerTrainings(
                        trainerUsername,
                        filter.getFromDate(),
                        filter.getToDate(),
                        blankToNull(filter.getTraineeName()),
                        safePageable(pageable))
                .map(mapper::toResponse);
    }

    /**
     * Only allows whitelisted sort keys (so clients cannot sort by e.g. trainee.user.password)
     * and maps them to entity paths. Unknown keys -> 400.
     */
    private Pageable safePageable(Pageable pageable) {
        List<Sort.Order> orders = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            String property = SORT_FIELDS.get(order.getProperty());
            if (property == null) {
                throw new ApiException("INVALID_SORT",
                        "sort must be one of " + SORT_FIELDS.keySet(), HttpStatus.BAD_REQUEST);
            }
            orders.add(new Sort.Order(order.getDirection(), property));
        }
        Sort sort = orders.isEmpty() ? Sort.by(Sort.Direction.DESC, "trainingDate") : Sort.by(orders);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort.and(Sort.by("id")));
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
