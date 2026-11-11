package service;

import com.muro_akhaladze.gym_task.entity.Trainee;
import com.muro_akhaladze.gym_task.entity.User;
import com.muro_akhaladze.gym_task.repository.TraineeRepo;
import com.muro_akhaladze.gym_task.service.TraineeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TraineeServiceTest {

    @Mock
    private TraineeRepo traineeRepo;

    @InjectMocks
    private TraineeService traineeService;

    private Trainee trainee;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        User user = new User(
                1,
                "Liam",
                "Anderson",
                "Liam.Anderson",
                "securePass123",
                true);

        trainee = new Trainee(user, LocalDate.of(1998, 7, 15), "456 Elm Street");
    }


    @Test
    void testCreateTrainee_Success() {
        when(traineeRepo.save(any(Trainee.class))).thenReturn(trainee);

        Trainee createdTrainee = traineeService.createTrainee(trainee);

        assertNotNull(createdTrainee);
        verify(traineeRepo, times(1)).save(any(Trainee.class));
    }

    @Test
    void testGetTraineeByUsername_Found() {
        when(traineeRepo.findByUsername("Liam.Anderson")).thenReturn(Optional.of(trainee));

        Optional<Trainee> result = traineeService.getTraineeByUsername("Liam.Anderson");

        assertTrue(result.isPresent());
        assertEquals("Liam.Anderson", result.get().getUser().getUserName());
        verify(traineeRepo, times(1)).findByUsername("Liam.Anderson");
    }

    @Test
    void testGetTraineeByUsername_NotFound() {
        when(traineeRepo.findByUsername("UnknownUser")).thenReturn(Optional.empty());

        Optional<Trainee> result = traineeService.getTraineeByUsername("UnknownUser");

        assertTrue(result.isEmpty());
        verify(traineeRepo, times(1)).findByUsername("UnknownUser");
    }

    @Test
    void testUpdateTrainee_Success() {
        String username = "john_doe";
        User user = new User();
        user.setUserName(username);

        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setAddress("456 Elm Street");

        when(traineeRepo.findByUsername(username)).thenReturn(Optional.of(trainee));
        when(traineeRepo.save(any(Trainee.class))).thenReturn(trainee);

        Trainee updatedTrainee = traineeService.updateTrainee(trainee);

        assertNotNull(updatedTrainee);
        assertEquals("456 Elm Street", updatedTrainee.getAddress());

        verify(traineeRepo, times(1)).findByUsername(username);
        verify(traineeRepo, times(1)).save(any(Trainee.class));
    }


    @Test
    void testDeleteTrainee_Success() {
        doNothing().when(traineeRepo).deleteByUsername(anyString());

        traineeService.deleteTrainee("Liam.Anderson");

        verify(traineeRepo, times(1)).deleteByUsername("Liam.Anderson");
    }
}
