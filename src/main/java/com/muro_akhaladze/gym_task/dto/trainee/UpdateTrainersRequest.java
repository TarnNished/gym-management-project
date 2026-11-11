package com.muro_akhaladze.gym_task.dto.trainee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Replaces the set of trainers assigned to a trainee (many-to-many). */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTrainersRequest {
    @NotNull(message = "Trainer list is required (can be empty)")
    @Size(max = 20, message = "A trainee can have at most 20 trainers")
    private List<@NotBlank(message = "Trainer username cannot be blank") String> trainerUsernames;
}
