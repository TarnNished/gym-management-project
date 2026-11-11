package com.muro_akhaladze.gym_task.dto.training;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainingResponse {
    private int id;
    private String trainingName;
    private LocalDate trainingDate;
    private String trainingType;
    private Long trainingDuration;
    private String traineeUsername;
    private String trainerUsername;
}
