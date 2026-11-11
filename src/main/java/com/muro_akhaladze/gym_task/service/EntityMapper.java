package com.muro_akhaladze.gym_task.service;

import com.muro_akhaladze.gym_task.dto.trainee.TraineeProfileResponse;
import com.muro_akhaladze.gym_task.dto.trainee.TraineeShortInfo;
import com.muro_akhaladze.gym_task.dto.trainer.TrainerProfileResponse;
import com.muro_akhaladze.gym_task.dto.trainer.TrainerShortInfo;
import com.muro_akhaladze.gym_task.dto.training.TrainingResponse;
import com.muro_akhaladze.gym_task.entity.Trainee;
import com.muro_akhaladze.gym_task.entity.Trainer;
import com.muro_akhaladze.gym_task.entity.Training;
import org.springframework.stereotype.Component;

import java.util.Comparator;

/** Converts entities to response DTOs so entities are never serialized directly. */
@Component
public class EntityMapper {

    public TrainerShortInfo toShortInfo(Trainer trainer) {
        return new TrainerShortInfo(
                trainer.getUser().getUsername(),
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getSpecialization().getTrainingTypeName());
    }

    public TraineeShortInfo toShortInfo(Trainee trainee) {
        return new TraineeShortInfo(
                trainee.getUser().getUsername(),
                trainee.getUser().getFirstName(),
                trainee.getUser().getLastName());
    }

    public TraineeProfileResponse toProfile(Trainee trainee) {
        return new TraineeProfileResponse(
                trainee.getUser().getUsername(),
                trainee.getUser().getFirstName(),
                trainee.getUser().getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                trainee.getUser().isActive(),
                trainee.getUser().getPhotoPath(),
                trainee.getTrainers().stream()
                        .sorted(Comparator.comparing(t -> t.getUser().getUsername()))
                        .map(this::toShortInfo)
                        .toList());
    }

    public TrainerProfileResponse toProfile(Trainer trainer) {
        return new TrainerProfileResponse(
                trainer.getUser().getUsername(),
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getSpecialization().getTrainingTypeName(),
                trainer.getUser().isActive(),
                trainer.getUser().getPhotoPath(),
                trainer.getTrainees().stream()
                        .sorted(Comparator.comparing(t -> t.getUser().getUsername()))
                        .map(this::toShortInfo)
                        .toList());
    }

    public TrainingResponse toResponse(Training training) {
        return new TrainingResponse(
                training.getId(),
                training.getTrainingName(),
                training.getTrainingDate(),
                training.getTrainingType().getTrainingTypeName(),
                training.getTrainingDuration(),
                training.getTrainee().getUser().getUsername(),
                training.getTrainer().getUser().getUsername());
    }
}
