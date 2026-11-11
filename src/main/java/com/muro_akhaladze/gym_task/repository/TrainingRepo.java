package com.muro_akhaladze.gym_task.repository;

import com.muro_akhaladze.gym_task.entity.Trainer;
import com.muro_akhaladze.gym_task.entity.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TrainingRepo extends JpaRepository<Training, Integer> {

    @Query("SELECT t FROM Training t " +
            "WHERE t.trainee.user.userName = :traineeUsername " +
            "AND (:fromDate IS NULL OR t.trainingDate >= :fromDate) " +
            "AND (:toDate IS NULL OR t.trainingDate <= :toDate) " +
            "AND (:trainerName IS NULL OR t.trainer.user.userName = :trainerName) " +
            "AND (:trainingType IS NULL OR t.trainingType.trainingTypeName = :trainingType)")
    List<Training> findTraineeTrainings(
            @Param("traineeUsername") String traineeUsername,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("trainerName") String trainerName,
            @Param("trainingType") String trainingType
    );

    @Query("SELECT t FROM Training t " +
            "WHERE t.trainer.user.userName = :trainerUsername " +
            "AND (:fromDate IS NULL OR t.trainingDate >= :fromDate) " +
            "AND (:toDate IS NULL OR t.trainingDate <= :toDate) " +
            "AND (:traineeName IS NULL OR t.trainee.user.userName = :traineeName)")
    List<Training> findTrainerTrainings(
            @Param("trainerUsername") String trainerUsername,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("traineeName") String traineeName
    );

}
