package com.muro_akhaladze.gym_task.repository;

import com.muro_akhaladze.gym_task.entity.Trainee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TraineeRepo extends JpaRepository<Trainee, Integer> {

    // Method-name query (navigates Trainee.user.userName)
    Optional<Trainee> findByUserUserName(String userName);

    // Custom JPQL query over the many-to-many relationship
    @Query("""
            SELECT t FROM Trainee t
            JOIN t.trainers tr
            WHERE tr.user.userName = :username
            ORDER BY t.user.lastName
            """)
    List<Trainee> findByTrainerUsername(@Param("username") String username);
}
