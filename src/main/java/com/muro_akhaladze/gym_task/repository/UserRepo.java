package com.muro_akhaladze.gym_task.repository;

import com.muro_akhaladze.gym_task.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.nio.channels.FileChannel;
import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Integer> {
    boolean existsByUserName(String generated);

    @Query("SELECT u FROM User u WHERE u.userName = :username")
    Optional<User> findByUsername(@Param("username")String username);
}
