package com.muro_akhaladze.gym_task.dto.training;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TraineeTrainingsFilterRequest {
    private String trainingName;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Long trainingTypeId;
    private Long trainingDuration;
    private String trainingTypeName;
    private String trainerName;
}
