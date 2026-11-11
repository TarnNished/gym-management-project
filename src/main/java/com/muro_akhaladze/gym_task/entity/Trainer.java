package com.muro_akhaladze.gym_task.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
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

    /** Inverse side of the Trainee <-> Trainer many-to-many relationship. */
    @JsonIgnore
    @ManyToMany(mappedBy = "trainers")
    private Set<Trainee> trainees = new HashSet<>();

    public Trainer(User user, TrainingType specialization) {
        this.user = user;
        this.specialization = specialization;
    }

    /** Removes this trainer from every trainee (the owning side of the many-to-many). */
    public void detachFromTrainees() {
        trainees.forEach(trainee -> trainee.getTrainers().remove(this));
        trainees.clear();
    }
}
