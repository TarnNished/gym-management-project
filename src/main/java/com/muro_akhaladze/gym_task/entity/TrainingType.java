package com.muro_akhaladze.gym_task.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrainingType {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int trainingTypeId;
    @Column(nullable = false, length = 50)
    private String trainingTypeName;
}
