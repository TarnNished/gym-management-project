package com.muro_akhaladze.gym_task.service;

import com.muro_akhaladze.gym_task.dto.user.PhotoUploadResponse;
import com.muro_akhaladze.gym_task.entity.User;
import com.muro_akhaladze.gym_task.exceptions.ApiException;
import com.muro_akhaladze.gym_task.repository.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;

@Service
@Slf4j
public class UserService {

    private static final String PASSWORD_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 10;

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final SecureRandom random = new SecureRandom();

    // Constructor injection
    public UserService(UserRepo userRepo, PasswordEncoder passwordEncoder, FileStorageService fileStorageService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Builds a new (not yet saved) user with a unique "First.Last" username and
     * a BCrypt-hashed version of {@code rawPassword}.
     */
    public User newUser(String firstName, String lastName, String rawPassword) {
        User user = new User();
        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setUserName(generateUserName(firstName, lastName));
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setActive(true);
        return user;
    }

    public String generateUserName(String firstName, String lastName) {
        if (firstName == null || lastName == null || firstName.isBlank() || lastName.isBlank()) {
            throw new IllegalArgumentException("First name and last name cannot be empty");
        }
        String base = firstName.trim() + "." + lastName.trim();
        String candidate = base;
        int i = 1;
        while (userRepo.existsByUserName(candidate)) {
            candidate = base + i++;
        }
        return candidate;
    }

    public String generatePassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        return password.toString();
    }

    @Transactional(readOnly = true)
    public boolean hasAnyUsers() {
        return userRepo.count() > 0;
    }

    @Transactional(readOnly = true)
    public User getByUsername(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new ApiException("NOT_FOUND", "User '" + username + "' not found", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void updateUserActivationStatus(String username, boolean isActive) {
        User user = getByUsername(username);
        user.setActive(isActive);
        log.info("User {} active={}", username, isActive);
    }

    @Transactional
    public void updatePassword(String username, String currentPassword, String newPassword) {
        User user = getByUsername(username);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid current password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        log.info("Password changed for {}", username);
    }

    /** Stores the uploaded photo on disk and saves its file name in the users table. */
    @Transactional
    public PhotoUploadResponse updatePhoto(String username, MultipartFile file) {
        User user = getByUsername(username);
        String storedName = fileStorageService.store(file);
        String oldPhoto = user.getPhotoPath();
        user.setPhotoPath(storedName);
        userRepo.save(user);
        if (oldPhoto != null) {
            fileStorageService.delete(oldPhoto);
        }
        log.info("Photo for {} stored as {}", username, storedName);
        return new PhotoUploadResponse(
                username,
                storedName,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                "/api/v1/users/" + username + "/photo");
    }

    @Transactional(readOnly = true)
    public Resource loadPhoto(String username) {
        User user = getByUsername(username);
        if (user.getPhotoPath() == null) {
            throw new ApiException("NOT_FOUND", "User '" + username + "' has no photo", HttpStatus.NOT_FOUND);
        }
        return fileStorageService.load(user.getPhotoPath());
    }

    @Transactional
    public void deletePhoto(String username) {
        User user = getByUsername(username);
        if (user.getPhotoPath() != null) {
            fileStorageService.delete(user.getPhotoPath());
            user.setPhotoPath(null);
        }
    }
}
