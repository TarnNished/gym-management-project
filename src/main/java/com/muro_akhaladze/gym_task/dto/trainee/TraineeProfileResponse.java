package com.muro_akhaladze.gym_task.dto.trainee;

import com.muro_akhaladze.gym_task.dto.trainer.TrainerShortInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TraineeProfileResponse {
    private String username;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String address;
    private Boolean isActive;
    private String photoPath;
    private List<TrainerShortInfo> trainers;
}
