package service;

import com.muro_akhaladze.gym_task.entity.User;
import com.muro_akhaladze.gym_task.repository.UserRepo;
import com.muro_akhaladze.gym_task.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateUserName_Unique() {
        when(userRepo.existsByUserName("Unique.Username")).thenReturn(false);

        String username = userService.generateUserName("Unique", "Username");

        assertEquals("Unique.Username", username);
        verify(userRepo, times(1)).existsByUserName("Unique.Username");
    }

    @Test
    void testGenerateUserName_AlreadyExists() {
        when(userRepo.existsByUserName("John.Doe")).thenReturn(true);
        when(userRepo.existsByUserName("John.Doe1")).thenReturn(false);

        String username = userService.generateUserName("John", "Doe");

        assertEquals("John.Doe1", username);
        verify(userRepo, times(2)).existsByUserName(anyString());
    }

    @Test
    void testIsInvalidPassword_CorrectPassword() {
        User user = new User(1,"John", "Doe", "John.Doe", "password123",true);
        when(userRepo.findByUsername("John.Doe")).thenReturn(Optional.of(user));

        boolean result = userService.isInvalidPassword("John.Doe", "password123");

        assertFalse(result);
        verify(userRepo, times(1)).findByUsername("John.Doe");
    }

    @Test
    void testIsInvalidPassword_WrongPassword() {
        User user = new User(1,"John", "Doe", "John.Doe", "password123",true);
        when(userRepo.findByUsername("John.Doe")).thenReturn(Optional.of(user));

        boolean result = userService.isInvalidPassword("John.Doe", "wrongPassword");

        assertTrue(result);
        verify(userRepo, times(1)).findByUsername("John.Doe");
    }

    @Test
    void testIsInvalidPassword_UserNotFound() {
        when(userRepo.findByUsername("NonExistent")).thenReturn(Optional.empty());

        boolean result = userService.isInvalidPassword("NonExistent", "anyPassword");

        assertTrue(result);
        verify(userRepo, times(1)).findByUsername("NonExistent");
    }

    @Test
    void testUpdateUserActivationStatus_ValidUser() {
        User user = new User(1,"John", "Doe", "John.Doe", "password123", true);
        when(userRepo.findByUsername("John.Doe")).thenReturn(Optional.of(user));

        userService.updateUserActivationStatus("John.Doe", false);

        assertFalse(user.isActive());
        verify(userRepo, times(1)).save(user);
    }

    @Test
    void testUpdateUserActivationStatus_UserNotFound() {
        when(userRepo.findByUsername("NonExistent")).thenReturn(Optional.empty());

        userService.updateUserActivationStatus("NonExistent", false);

        verify(userRepo, never()).save(any());
    }

    @Test
    void testUpdatePassword_Success() {
        User user = new User(1,"John", "Doe", "John.Doe", "oldPassword", true);
        when(userRepo.findByUsername("John.Doe")).thenReturn(Optional.of(user));

        userService.updatePassword("John.Doe", "oldPassword", "newPassword");

        assertEquals("newPassword", user.getPassword());
        verify(userRepo, times(1)).save(user);
    }

    @Test
    void testUpdatePassword_InvalidOldPassword() {
        User user = new User(1,"John", "Doe", "John.Doe", "oldPassword", true);
        when(userRepo.findByUsername("John.Doe")).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> {
            userService.updatePassword("John.Doe", "wrongPassword", "newPassword");
        });

        verify(userRepo, never()).save(any());
    }

    @Test
    void testUpdatePassword_UserNotFound() {
        when(userRepo.findByUsername("NonExistent")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            userService.updatePassword("NonExistent", "anyPassword", "newPassword");
        });

        verify(userRepo, never()).save(any());
    }

    @Test
    void testGeneratePassword_Length() {
        String password = userService.generatePassword();
        assertEquals(10, password.length());
    }

    @Test
    void testGetTrainerId_Increments() {
        int firstId = userService.getTrainerId();
        int secondId = userService.getTrainerId();

        assertEquals(firstId + 1, secondId);
    }

    @Test
    void testGetTraineeId_Increments() {
        int firstId = userService.getTraineeId();
        int secondId = userService.getTraineeId();

        assertEquals(firstId + 1, secondId);
    }
}
