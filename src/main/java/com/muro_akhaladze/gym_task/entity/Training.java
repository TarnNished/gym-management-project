package com.muro_akhaladze.gym_task.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Training {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "trainee_id", nullable = false)
    @NotNull(message = "Trainee is required")
    private Trainee trainee;

    @ManyToOne
    @JoinColumn(name = "trainer_id", nullable = false)
    @NotNull(message = "Trainer is required")
    private Trainer trainer;

    @ManyToOne
    @JoinColumn(name = "training_type_id", nullable = false)
    @NotNull(message = "Training Type is required")
    private TrainingType trainingType;

    @Column(nullable = false, length = 50)
    @NotNull(message = "Training name is required")
    @Size(max = 50, message = "Training name cannot exceed 50 characters")
    private String trainingName;

    @Column(nullable = false)
    @NotNull(message = "Training date is required")
    private LocalDate trainingDate;

    @Column(nullable = false)
    @NotNull(message = "Training duration is required")
    @Min(value = 1, message = "Training duration must be at least 1")
    private Long trainingDuration;


}
