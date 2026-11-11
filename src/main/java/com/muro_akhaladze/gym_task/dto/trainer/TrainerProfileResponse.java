package com.muro_akhaladze.gym_task.dto.trainer;

import com.muro_akhaladze.gym_task.dto.trainee.TraineeShortInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainerProfileResponse {
    private String username;
    private String firstName;
    private String lastName;
    private String specialization;
    private Boolean isActive;
    private String photoPath;
    private List<TraineeShortInfo> trainees;
}
