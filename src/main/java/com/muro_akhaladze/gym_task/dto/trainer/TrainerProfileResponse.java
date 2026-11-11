package com.muro_akhaladze.gym_task.dto.trainer;

import com.muro_akhaladze.gym_task.dto.trainee.TraineeShortInfo;
import com.muro_akhaladze.gym_task.entity.TrainingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainerProfileResponse {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private TrainingType specialization;

    @NotNull(message = "Active status must be specified")
    private Boolean isActive;

    private List<TraineeShortInfo> trainees;
}
