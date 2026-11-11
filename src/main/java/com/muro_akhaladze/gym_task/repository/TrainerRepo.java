package com.muro_akhaladze.gym_task.repository;

import com.muro_akhaladze.gym_task.entity.Trainer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepo extends JpaRepository<Trainer, Integer> {

    // Method-name query
    Optional<Trainer> findByUserUserName(String userName);

    // Method-name query with And + pagination: active trainers of one specialization
    Page<Trainer> findBySpecializationTrainingTypeNameIgnoreCaseAndUserIsActiveTrue(String specialization, Pageable pageable);

    // Method-name query with pagination: all active trainers
    Page<Trainer> findByUserIsActiveTrue(Pageable pageable);

    // Method-name query used for the many-to-many assignment endpoint
    List<Trainer> findByUserUserNameIn(List<String> userNames);

    // Custom JPQL query over the many-to-many relationship
    @Query("""
            SELECT t FROM Trainer t
            JOIN t.trainees tr
            WHERE tr.user.userName = :username
            ORDER BY t.user.lastName
            """)
    List<Trainer> findByTraineeUsername(@Param("username") String username);

    // Custom JPQL query with a sub-select: active trainers NOT yet assigned to the trainee
    @Query("""
            SELECT tr FROM Trainer tr
            WHERE tr.user.isActive = true
              AND tr NOT IN (
                  SELECT t FROM Trainee te JOIN te.trainers t
                  WHERE te.user.userName = :traineeUsername)
            ORDER BY tr.user.lastName
            """)
    List<Trainer> findTrainersNotAssignedToTrainee(@Param("traineeUsername") String traineeUsername);
}
