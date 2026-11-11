package service;

import com.muro_akhaladze.gym_task.entity.Training;
import com.muro_akhaladze.gym_task.entity.TrainingType;
import com.muro_akhaladze.gym_task.repository.TrainingRepo;
import com.muro_akhaladze.gym_task.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TrainingServiceTest {

    @Mock
    private TrainingRepo trainingRepo;

    @InjectMocks
    private TrainingService trainingService;

    private Training training;
    private LocalDate fromDate;
    private LocalDate toDate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        TrainingType trainingType = new TrainingType(1, "Strength");

        training = new Training(
                1,
                null,
                null,
                trainingType,
                "Strength Training",
                LocalDate.of(2024, 2, 20),
                60L
        );

        fromDate = LocalDate.of(2024, 2, 1);
        toDate = LocalDate.of(2024, 2, 28);
    }

    @Test
    void testCreateTraining_Success() {
        when(trainingRepo.save(any(Training.class))).thenReturn(training);

        Training createdTraining = trainingService.createTraining(training);

        assertNotNull(createdTraining);
        assertEquals(training.getTrainingName(), createdTraining.getTrainingName());
        assertEquals(training.getTrainingType(), createdTraining.getTrainingType());

        verify(trainingRepo, times(1)).save(any(Training.class));
    }

    @Test
    void testGetTraining_Found() {
        when(trainingRepo.findById(training.getId())).thenReturn(Optional.of(training));

        Optional<Training> retrievedTraining = trainingService.getTraining(training.getId());

        assertTrue(retrievedTraining.isPresent());
        assertEquals(training.getTrainingName(), retrievedTraining.get().getTrainingName());

        verify(trainingRepo, times(1)).findById(training.getId());
    }

    @Test
    void testGetTraining_NotFound_ReturnsEmpty() {
        when(trainingRepo.findById(999)).thenReturn(Optional.empty());

        Optional<Training> retrievedTraining = trainingService.getTraining(999);

        assertTrue(retrievedTraining.isEmpty());
        verify(trainingRepo, times(1)).findById(999);
    }

    @Test
    void testGetTraineeTrainings_Success() {
        List<Training> expectedTrainings = List.of(training);

        when(trainingRepo.findTraineeTrainings("Liam.Anderson", fromDate, toDate, "Oliver.Martinez", "Strength"))
                .thenReturn(expectedTrainings);

        List<Training> retrievedTrainings = trainingService.getTraineeTrainings(
                "Liam.Anderson", fromDate, toDate, "Oliver.Martinez", "Strength");

        assertFalse(retrievedTrainings.isEmpty());
        assertEquals(1, retrievedTrainings.size());
        assertEquals("Strength Training", retrievedTrainings.get(0).getTrainingName());

        verify(trainingRepo, times(1)).findTraineeTrainings("Liam.Anderson", fromDate, toDate, "Oliver.Martinez", "Strength");
    }

    @Test
    void testGetTraineeTrainings_EmptyList() {
        when(trainingRepo.findTraineeTrainings("UnknownUser", fromDate, toDate, "UnknownTrainer", "UnknownType"))
                .thenReturn(List.of());

        List<Training> retrievedTrainings = trainingService.getTraineeTrainings(
                "UnknownUser", fromDate, toDate, "UnknownTrainer", "UnknownType");

        assertTrue(retrievedTrainings.isEmpty());
        verify(trainingRepo, times(1)).findTraineeTrainings("UnknownUser", fromDate, toDate, "UnknownTrainer", "UnknownType");
    }

    @Test
    void testGetTrainerTrainings_Success() {
        List<Training> expectedTrainings = List.of(training);

        when(trainingRepo.findTrainerTrainings("Oliver.Martinez", fromDate, toDate, "Liam.Anderson"))
                .thenReturn(expectedTrainings);

        List<Training> retrievedTrainings = trainingService.getTrainerTrainings(
                "Oliver.Martinez", fromDate, toDate, "Liam.Anderson");

        assertFalse(retrievedTrainings.isEmpty());
        assertEquals(1, retrievedTrainings.size());
        assertEquals("Strength Training", retrievedTrainings.get(0).getTrainingName());

        verify(trainingRepo, times(1)).findTrainerTrainings("Oliver.Martinez", fromDate, toDate, "Liam.Anderson");
    }

    @Test
    void testGetTrainerTrainings_EmptyList() {
        when(trainingRepo.findTrainerTrainings("UnknownTrainer", fromDate, toDate, "UnknownTrainee"))
                .thenReturn(List.of());

        List<Training> retrievedTrainings = trainingService.getTrainerTrainings(
                "UnknownTrainer", fromDate, toDate, "UnknownTrainee");

        assertTrue(retrievedTrainings.isEmpty());
        verify(trainingRepo, times(1)).findTrainerTrainings("UnknownTrainer", fromDate, toDate, "UnknownTrainee");
    }
}
