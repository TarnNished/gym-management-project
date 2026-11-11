package com.muro_akhaladze.gym_task.repository;

import com.muro_akhaladze.gym_task.entity.Trainee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TraineeRepo extends JpaRepository<Trainee, Integer> {

    @Query("select t from Trainee t where  t.user.userName = :username")
    Optional<Trainee> findByUsername(@Param("username")String username);

    @Modifying
    @Query("DELETE FROM User u WHERE u.userName = :username")
    void deleteByUsername(@Param("username") String username);

    @Query("""
    SELECT t FROM Trainee t
    JOIN Training tr ON tr.trainee.id = t.id
    WHERE tr.trainer.user.userName = :username
""")
    List<Trainee> findByTrainerUsername(@Param("username") String username);

}
