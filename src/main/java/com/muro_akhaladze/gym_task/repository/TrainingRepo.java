package com.muro_akhaladze.gym_task.repository;

import com.muro_akhaladze.gym_task.entity.Trainee;
import com.muro_akhaladze.gym_task.entity.Trainer;
import com.muro_akhaladze.gym_task.entity.Training;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface TrainingRepo extends JpaRepository<Training, Integer> {

    // Custom JPQL query with optional filters + pagination/sorting
    @Query("""
            SELECT t FROM Training t
            WHERE t.trainee.user.userName = :traineeUsername
              AND (:fromDate IS NULL OR t.trainingDate >= :fromDate)
              AND (:toDate IS NULL OR t.trainingDate <= :toDate)
              AND (:trainerName IS NULL OR t.trainer.user.userName = :trainerName)
              AND (:trainingType IS NULL OR t.trainingType.trainingTypeName = :trainingType)
            """)
    Page<Training> findTraineeTrainings(
            @Param("traineeUsername") String traineeUsername,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("trainerName") String trainerName,
            @Param("trainingType") String trainingType,
            Pageable pageable);

    // Custom JPQL query with optional filters + pagination/sorting
    @Query("""
            SELECT t FROM Training t
            WHERE t.trainer.user.userName = :trainerUsername
              AND (:fromDate IS NULL OR t.trainingDate >= :fromDate)
              AND (:toDate IS NULL OR t.trainingDate <= :toDate)
              AND (:traineeName IS NULL OR t.trainee.user.userName = :traineeName)
            """)
    Page<Training> findTrainerTrainings(
            @Param("trainerUsername") String trainerUsername,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("traineeName") String traineeName,
            Pageable pageable);

    // Custom modifying JPQL query, used when a trainee is deleted
    @Modifying
    @Query("DELETE FROM Training t WHERE t.trainee = :trainee")
    void deleteAllByTrainee(@Param("trainee") Trainee trainee);

    // Custom modifying JPQL query, used when a trainer is deleted
    @Modifying
    @Query("DELETE FROM Training t WHERE t.trainer = :trainer")
    void deleteAllByTrainer(@Param("trainer") Trainer trainer);
}
