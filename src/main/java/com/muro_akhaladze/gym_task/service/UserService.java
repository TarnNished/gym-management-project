package com.muro_akhaladze.gym_task.service;

import com.muro_akhaladze.gym_task.repository.UserRepo;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;
    private static int traineeId = 1;
    private static int trainerId = 1;



    public String generateUserName(@NotBlank(message = "First name is required") String firstName,
                                   @NotBlank(message = "Last name is required") String lastName) {
        if (firstName == null || lastName == null || firstName.trim().isEmpty() || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name and last name cannot be empty");
        }

        String temp = firstName + "." + lastName;
        String generated = temp;
        int i = 1;

        while (userRepo.existsByUserName(generated)) {
            generated = temp + i;
            i++;
        }
        return generated;
    }
    public boolean isInvalidPassword(@NotBlank(message = "Username is required") String username,
                                     @NotBlank(message = "Password is required") String password) {
        return !userRepo.findByUsername(username)
                .map(user -> user.getPassword().equals(password))
                .orElse(false);
    }
    @Transactional
    public void updateUserActivationStatus(@NotBlank(message = "Username is required") String username, boolean isActive) {
        userRepo.findByUsername(username).ifPresent(user -> {
            user.setActive(isActive);
            userRepo.save(user);
        });
    }

    public synchronized Integer getTrainerId() {
        return trainerId++;
    }
    public synchronized Integer getTraineeId() {
        return traineeId++;
    }

    public String generatePassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        int password_length = 10;
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(password_length);
        for (int i = 0; i < password_length; i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));
        }
        return password.toString();
    }
    @Transactional
    public void updatePassword(@NotBlank(message = "Username is required") String username,
                               @NotBlank(message = "Current password is required") String currentPassword,
                               @NotBlank(message = "New password is required") String newPassword) {
        if (isInvalidPassword(username, currentPassword)) {
            throw new IllegalArgumentException("Invalid current password");
        }

        userRepo.findByUsername(username).ifPresent(user -> {
            user.setPassword(newPassword);
            userRepo.save(user);
        });
    }

}
