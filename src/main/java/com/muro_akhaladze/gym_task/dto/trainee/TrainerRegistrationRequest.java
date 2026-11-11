package com.muro_akhaladze.gym_task.dto.trainee;

import com.muro_akhaladze.gym_task.entity.TrainingType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainerRegistrationRequest {
    @NotBlank(message = "First name is required")
    private String firstName;
    @NotBlank(message = "Last name is required")
    private String lastName;
    @NotBlank(message = "specialization id  is required")
    private TrainingType specialization;
}
