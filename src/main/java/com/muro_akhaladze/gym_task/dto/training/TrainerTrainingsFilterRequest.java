package com.muro_akhaladze.gym_task.dto.training;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Optional query-string filters for GET /trainers/{username}/trainings (bound with @ModelAttribute). */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainerTrainingsFilterRequest {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;

    @Size(max = 50, message = "Trainee username cannot exceed 50 characters")
    private String traineeName;

    @AssertTrue(message = "fromDate must be on or before toDate")
    public boolean isDateRangeValid() {
        return fromDate == null || toDate == null || !fromDate.isAfter(toDate);
    }
}
