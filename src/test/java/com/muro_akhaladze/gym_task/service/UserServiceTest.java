package com.muro_akhaladze.gym_task.service;

import com.muro_akhaladze.gym_task.entity.User;
import com.muro_akhaladze.gym_task.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepo userRepo;
    @Mock FileStorageService fileStorageService;
    PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepo, encoder, fileStorageService);
    }

    @Test
    void usernameGetsSuffixWhenTaken() {
        when(userRepo.existsByUserName("John.Doe")).thenReturn(true);
        when(userRepo.existsByUserName("John.Doe1")).thenReturn(true);
        when(userRepo.existsByUserName("John.Doe2")).thenReturn(false);
        assertThat(userService.generateUserName("John", "Doe")).isEqualTo("John.Doe2");
    }

    @Test
    void newUserHasHashedPassword() {
        User user = userService.newUser("John", "Doe", "secret123");
        assertThat(user.getPassword()).isNotEqualTo("secret123");
        assertThat(encoder.matches("secret123", user.getPassword())).isTrue();
    }

    @Test
    void generatedPasswordHasTenChars() {
        assertThat(userService.generatePassword()).hasSize(10);
    }

    @Test
    void changePasswordChecksOldPassword() {
        User user = userService.newUser("John", "Doe", "oldpass");
        when(userRepo.findByUsername("John.Doe")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.updatePassword("John.Doe", "wrong", "newpass123"))
                .isInstanceOf(IllegalArgumentException.class);

        userService.updatePassword("John.Doe", "oldpass", "newpass123");
        assertThat(encoder.matches("newpass123", user.getPassword())).isTrue();
    }

    @Test
    void newPhotoReplacesAndDeletesOldOne() {
        User user = userService.newUser("John", "Doe", "x");
        user.setPhotoPath("old.png");
        when(userRepo.findByUsername("John.Doe")).thenReturn(Optional.of(user));
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", new byte[]{1});
        when(fileStorageService.store(file)).thenReturn("new.png");

        userService.updatePhoto("John.Doe", file);

        assertThat(user.getPhotoPath()).isEqualTo("new.png");
        verify(userRepo).save(user);
        verify(fileStorageService).delete("old.png");
    }
}
