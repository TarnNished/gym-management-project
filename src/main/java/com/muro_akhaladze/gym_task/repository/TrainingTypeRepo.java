package com.muro_akhaladze.gym_task.repository;

import com.muro_akhaladze.gym_task.entity.TrainingType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingTypeRepo extends JpaRepository<TrainingType, Long> {
}
