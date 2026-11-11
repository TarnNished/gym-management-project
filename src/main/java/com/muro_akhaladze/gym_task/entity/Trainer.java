package com.muro_akhaladze.gym_task.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Trainer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @NotNull(message = "User is required")
    private User user;

    @ManyToOne
    @JoinColumn(name = "training_type_id", nullable = false)
    @NotNull(message = "Specialization is required")
    private TrainingType specialization;


    public Trainer(User user, TrainingType specialization) {
        this.user = user;
        this.specialization = specialization;
    }
}

