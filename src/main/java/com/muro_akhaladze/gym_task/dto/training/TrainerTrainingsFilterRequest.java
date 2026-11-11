package com.muro_akhaladze.gym_task.dto.training;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainerTrainingsFilterRequest {
    private String trainingName;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Long trainingTypeId;
    private Long trainingDuration;
    private String traineeName;
    private String trainerUsername;
}
