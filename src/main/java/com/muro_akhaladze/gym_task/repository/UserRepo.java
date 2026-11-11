package com.muro_akhaladze.gym_task.repository;

import com.muro_akhaladze.gym_task.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Integer> {

    // Method-name query
    boolean existsByUserName(String userName);

    // Custom JPQL query
    @Query("SELECT u FROM User u WHERE u.userName = :username")
    Optional<User> findByUsername(@Param("username") String username);
}
