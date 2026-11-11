package com.muro_akhaladze.gym_task.dto.trainee;

import com.muro_akhaladze.gym_task.dto.trainer.TrainerShortInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TraineeProfileResponse {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private LocalDate dateOfBirth;

    private String address;

    @NotNull(message = "Active status must be provided")
    private Boolean isActive;

    private List<TrainerShortInfo> trainers;
}
