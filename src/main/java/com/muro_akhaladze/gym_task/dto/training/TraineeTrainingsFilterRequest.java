package com.muro_akhaladze.gym_task.dto.training;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Optional query-string filters for GET /trainees/{username}/trainings (bound with @ModelAttribute). */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TraineeTrainingsFilterRequest {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;

    @Size(max = 50, message = "Trainer username cannot exceed 50 characters")
    private String trainerName;

    @Size(max = 50, message = "Training type name cannot exceed 50 characters")
    private String trainingTypeName;

    @AssertTrue(message = "fromDate must be on or before toDate")
    public boolean isDateRangeValid() {
        return fromDate == null || toDate == null || !fromDate.isAfter(toDate);
    }
}
