package com.muro_akhaladze.gym_task.dto.training;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AddTrainingRequest {
    @NotBlank(message = "Trainee username is required")
    @Size(max = 50, message = "Trainee username cannot exceed 50 characters")
    private String traineeUsername;

    @NotBlank(message = "Trainer username is required")
    @Size(max = 50, message = "Trainer username cannot exceed 50 characters")
    private String trainerUsername;

    @NotBlank(message = "Training name is required")
    @Size(max = 50, message = "Training name cannot exceed 50 characters")
    private String trainingName;

    @NotNull(message = "Training date is required")
    private LocalDate trainingDate;

    @NotNull(message = "Training duration is required")
    @Positive(message = "Training duration must be a positive number")
    @Max(value = 600, message = "Training duration cannot exceed 600 minutes")
    private Integer trainingDuration;
}
