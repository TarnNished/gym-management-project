package service;

import com.muro_akhaladze.gym_task.entity.Trainer;
import com.muro_akhaladze.gym_task.entity.TrainingType;
import com.muro_akhaladze.gym_task.entity.User;
import com.muro_akhaladze.gym_task.repository.TrainerRepo;
import com.muro_akhaladze.gym_task.service.TrainerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TrainerServiceTest {

    @Mock
    private TrainerRepo trainerRepo;

    @InjectMocks
    private TrainerService trainerService;

    private Trainer trainer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        User user = new User(
                1,
                "Oliver",
                "Martinez",
                "Oliver.Martinez",
                "securePass452",
                true);
        TrainingType trainingType = new TrainingType(1,"Yoga & Flexibility");
        trainer = new Trainer(user, trainingType);
    }

    @Test
    void testCreateTrainer_Success() {
        when(trainerRepo.save(any(Trainer.class))).thenReturn(trainer);

        Trainer createdTrainer = trainerService.createTrainer(trainer);

        assertNotNull(createdTrainer);
        verify(trainerRepo, times(1)).save(any(Trainer.class));
    }

    @Test
    void testGetTrainerByUsername_Found() {
        when(trainerRepo.findByUsername("Oliver.Martinez")).thenReturn(Optional.of(trainer));

        Optional<Trainer> result = trainerService.getTrainerByUsername("Oliver.Martinez");

        assertTrue(result.isPresent());
        assertEquals("Oliver.Martinez", result.get().getUser().getUserName()); // Fixed
        verify(trainerRepo, times(1)).findByUsername("Oliver.Martinez");
    }

    @Test
    void testGetTrainerByUsername_NotFound() {
        when(trainerRepo.findByUsername("UnknownUser")).thenReturn(Optional.empty());

        Optional<Trainer> result = trainerService.getTrainerByUsername("UnknownUser");

        assertTrue(result.isEmpty());
        verify(trainerRepo, times(1)).findByUsername("UnknownUser");
    }

    @Test
    void testUpdateTrainer_Success() {
        String username = "john_doe";
        User user = new User();
        user.setUserName(username);
        TrainingType trainingType = new TrainingType();
        trainingType.setTrainingTypeName("Yoga & Flexibility");

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(trainingType);

        when(trainerRepo.findByUsername(username)).thenReturn(Optional.of(trainer));
        when(trainerRepo.save(any(Trainer.class))).thenReturn(trainer);

        Trainer updatedTrainer = trainerService.updateTrainer(trainer);

        assertNotNull(updatedTrainer);
        assertEquals("Yoga & Flexibility", updatedTrainer.getSpecialization().getTrainingTypeName());
        verify(trainerRepo, times(1)).findByUsername(username);
        verify(trainerRepo, times(1)).save(any(Trainer.class));
    }

}
