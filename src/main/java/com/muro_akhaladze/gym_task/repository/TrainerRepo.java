package com.muro_akhaladze.gym_task.repository;

import com.muro_akhaladze.gym_task.entity.Trainee;
import com.muro_akhaladze.gym_task.entity.Trainer;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrainerRepo extends JpaRepository<Trainer, Integer> {
    @Query(value = """
    SELECT t.* FROM trainer t
    JOIN trainer_trainee tt ON t.id = tt.trainer_id
    JOIN trainee tr ON tr.id = tt.trainee_id
    JOIN users u ON u.user_id = tr.user_id
    WHERE u.user_name = :username
""", nativeQuery = true)
    List<Trainer> findByTraineeUsername(@Param("username") String username);


    @Query("SELECT t FROM Trainer t WHERE t.user.userName = :username")
    Optional<Trainer> findByUsername(@Param("username") String username);


    @Query("SELECT tr FROM Trainer tr " +
            "LEFT JOIN Training t ON tr = t.trainer AND t.trainee.user.userName = :traineeUsername " +
            "WHERE t.id IS NULL")
    List<Trainer> findTrainersNotAssignedToTrainee(@Param("traineeUsername") String traineeUsername);


}
