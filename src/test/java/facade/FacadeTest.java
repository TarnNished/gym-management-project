package facade;

import com.muro_akhaladze.gym_task.entity.*;
import com.muro_akhaladze.gym_task.facade.Facade;
import com.muro_akhaladze.gym_task.service.TraineeService;
import com.muro_akhaladze.gym_task.service.TrainerService;
import com.muro_akhaladze.gym_task.service.TrainingService;
import com.muro_akhaladze.gym_task.service.UserService;
import org.checkerframework.common.reflection.qual.NewInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FacadeTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @Mock
    private UserService userService;

    @InjectMocks
    private Facade facade;

    private Trainee trainee;
    private Trainer trainer;
    private Training training;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        User traineeUser = new User(1,"Liam", "Anderson", "Liam.Anderson", "securePass456",true);
        trainee = new Trainee(traineeUser, LocalDate.of(1998, 7, 15), "456 Elm Street");

        User trainerUser = new User(
                2,
                "Oliver",
                "Martinez",
                "Oliver.Martinez",
                "securePass452",

                true);
        TrainingType trainingType2 = new TrainingType(2,"Yoga & Flexibility");
        trainer = new Trainer(trainerUser, trainingType2);

        TrainingType trainingType1 = new TrainingType(1,"Strength");

        training = new Training(
                1,
                trainee,
                trainer,
                trainingType1,
                "Strength Training",
                LocalDate.of(2024, 2, 20),
                60L
        );
    }

    @Test
    void testCreateTrainee_Success() {
        when(traineeService.createTrainee(any(Trainee.class))).thenReturn(trainee);

        Trainee createdTrainee = facade.createTrainee(trainee);

        assertNotNull(createdTrainee);
        verify(traineeService, times(1)).createTrainee(any(Trainee.class));
    }

    @Test
    void testCreateTrainer_Success() {
        when(trainerService.createTrainer(any(Trainer.class))).thenReturn(trainer);

        Trainer createdTrainer = facade.createTrainer(trainer);

        assertNotNull(createdTrainer);
        verify(trainerService, times(1)).createTrainer(any(Trainer.class));
    }

    @Test
    void testCreateTraining_Success() {
        when(trainingService.createTraining(any(Training.class))).thenReturn(training);

        Training result = facade.createTraining(training);

        assertNotNull(result);
        assertEquals(training, result);
        verify(trainingService, times(1)).createTraining(any(Training.class));
    }

    @Test
    void testGetTraineeByUsername_ValidCredentials() {
        when(userService.isInvalidPassword("user1", "password123")).thenReturn(false);
        when(traineeService.getTraineeByUsername("user1")).thenReturn(Optional.of(trainee));

        Optional<Trainee> result = facade.getTraineeByUsername("user1", "password123");

        assertTrue(result.isPresent());
        verify(traineeService, times(1)).getTraineeByUsername("user1");
    }

    @Test
    void testGetTraineeByUsername_InvalidCredentials() {
        when(userService.isInvalidPassword("user1", "wrongPass")).thenReturn(true);

        Optional<Trainee> result = facade.getTraineeByUsername("user1", "wrongPass");

        assertTrue(result.isEmpty());
        verify(traineeService, never()).getTraineeByUsername(anyString());
    }

    @Test
    void testGetTrainerByUsername_ValidCredentials() {
        when(userService.isInvalidPassword("trainer1", "password456")).thenReturn(false);
        when(trainerService.getTrainerByUsername("trainer1")).thenReturn(Optional.of(trainer));

        Optional<Trainer> result = facade.getTrainerByUsername("trainer1", "password456");

        assertTrue(result.isPresent());
        verify(trainerService, times(1)).getTrainerByUsername("trainer1");
    }

    @Test
    void testGetTrainerByUsername_InvalidCredentials() {
        when(userService.isInvalidPassword("trainer1", "wrongPass")).thenReturn(true);

        Optional<Trainer> result = facade.getTrainerByUsername("trainer1", "wrongPass");

        assertTrue(result.isEmpty());
        verify(trainerService, never()).getTrainerByUsername(anyString());
    }

    @Test
    void testUpdateTrainee_ValidCredentials() {
        when(userService.isInvalidPassword("user1", "password123")).thenReturn(false);
        when(traineeService.updateTrainee(any(Trainee.class))).thenReturn(trainee);

        Trainee result = facade.updateTrainee(trainee, "password123", "user1");

        assertNotNull(result);
        verify(traineeService, times(1)).updateTrainee(any(Trainee.class));
    }

    @Test
    void testUpdateTrainee_InvalidCredentials() {
        when(userService.isInvalidPassword("user1", "wrongPass")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> facade.updateTrainee(trainee, "wrongPass", "user1"));
        verify(traineeService, never()).updateTrainee(any());
    }

    @Test
    void testUpdateTrainer_ValidCredentials() {
        when(userService.isInvalidPassword("trainer1", "password456")).thenReturn(false);
        when(trainerService.updateTrainer(any(Trainer.class))).thenReturn(trainer);

        Trainer result = facade.updateTrainer(trainer, "password456", "trainer1");

        assertNotNull(result);
        verify(trainerService, times(1)).updateTrainer(any(Trainer.class));
    }

    @Test
    void testUpdateTrainer_InvalidCredentials() {
        when(userService.isInvalidPassword("trainer1", "wrongPass")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> facade.updateTrainer(trainer, "wrongPass", "trainer1"));
        verify(trainerService, never()).updateTrainer(any());
    }

    @Test
    void testDeleteTrainee_ValidCredentials() {
        when(userService.isInvalidPassword("user1", "password123")).thenReturn(false);

        facade.deleteTrainee("password123", "user1");

        verify(traineeService, times(1)).deleteTrainee("user1");
    }

    @Test
    void testDeleteTrainee_InvalidCredentials() {
        when(userService.isInvalidPassword("user1", "wrongPass")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> facade.deleteTrainee( "wrongPass", "user1"));
        verify(traineeService, never()).deleteTrainee(anyString());
    }

    @Test
    void testUpdateUserActivationStatus_ValidCredentials() {
        when(userService.isInvalidPassword("user1", "password123")).thenReturn(false);

        facade.updateUserActivationStatus("user1", "password123", true);

        verify(userService, times(1)).updateUserActivationStatus("user1", true);
    }

    @Test
    void testUpdateUserActivationStatus_InvalidCredentials() {
        when(userService.isInvalidPassword("user1", "wrongPass")).thenReturn(true);

        facade.updateUserActivationStatus("user1", "wrongPass", true);

        verify(userService, never()).updateUserActivationStatus(anyString(), anyBoolean());
    }

    @Test
    void testUpdatePassword_ValidCredentials() {
        when(userService.isInvalidPassword("user1", "oldPass")).thenReturn(false);

        facade.updatePassword("user1", "oldPass", "newPass");

        verify(userService, times(1)).updatePassword("user1", "oldPass", "newPass");
    }

    @Test
    void testUpdatePassword_InvalidCredentials() {
        when(userService.isInvalidPassword("user1", "wrongPass")).thenReturn(true);

        facade.updatePassword("user1", "wrongPass", "newPass");

        verify(userService, never()).updatePassword(anyString(), anyString(), anyString());
    }
}
