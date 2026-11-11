package com.muro_akhaladze.gym_task.repository;

import com.muro_akhaladze.gym_task.entity.TrainingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainingTypeRepo extends JpaRepository<TrainingType, Integer> {

    // Method-name query
    Optional<TrainingType> findByTrainingTypeNameIgnoreCase(String trainingTypeName);
}
