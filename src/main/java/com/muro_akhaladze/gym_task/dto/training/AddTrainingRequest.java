package com.muro_akhaladze.gym_task.dto.training;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AddTrainingRequest {
    @NotNull(message = "Trainee username is required")
    private String traineeUsername;

    @NotNull(message = "Trainer username is required")
    private String trainerUsername;

    @NotNull(message = "Training name is required")
    private String trainingName;

    @NotNull(message = "Training date is required")
    private LocalDate trainingDate;

    @NotNull(message = "Training duration is required")
    @Positive(message = "Training duration must be a positive number")
    private Integer trainingDuration;
}
