package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muro_akhaladze.gym_task.controller.TrainingController;
import com.muro_akhaladze.gym_task.dto.training.AddTrainingRequest;
import com.muro_akhaladze.gym_task.service.TrainingService;
import com.muro_akhaladze.gym_task.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.*;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@ContextConfiguration(classes = com.muro_akhaladze.gym_task.Main.class)
@WebMvcTest(TrainingController.class)
class TrainingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private TrainingService trainingService;

    private String validJsonBody() throws Exception {
        AddTrainingRequest req = new AddTrainingRequest();
        req.setTrainerUsername("trainer1");
        req.setTraineeUsername("trainee1");
        req.setTrainingName("Strength");
        req.setTrainingDate(LocalDate.of(2024, 4, 1));
        req.setTrainingDuration(60);
        return objectMapper.writeValueAsString(req);
    }

    @Test
    void testAddTrainingSuccess() throws Exception {
        AddTrainingRequest req = new AddTrainingRequest();
        req.setTrainerUsername("trainer1");
        req.setTraineeUsername("trainee1");
        req.setTrainingName("Strength");
        req.setTrainingDate(LocalDate.of(2024, 4, 1));
        req.setTrainingDuration(60);

        Mockito.when(userService.isInvalidPassword("trainer1", "pass")).thenReturn(false);
        Mockito.doNothing().when(trainingService).createTraining(Mockito.any(AddTrainingRequest.class));

        mockMvc.perform(post("/training")
                        .header("X-Username", "trainer1")
                        .header("X-Password", "pass")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void testAddTrainingUnauthorized() throws Exception {
        String json = validJsonBody();

        Mockito.when(userService.isInvalidPassword("trainer1", "wrongpass")).thenReturn(true);

        mockMvc.perform(post("/training")
                        .header("X-Username", "trainer1")
                        .header("X-Password", "wrongpass")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void testAddTrainingForbidden() throws Exception {
        AddTrainingRequest req = new AddTrainingRequest();
        req.setTrainerUsername("trainerX");
        req.setTraineeUsername("traineeX");
        req.setTrainingName("Strength");
        req.setTrainingDate(LocalDate.of(2024, 4, 1));
        req.setTrainingDuration(60);

        Mockito.when(userService.isInvalidPassword("hacker", "pass")).thenReturn(false);

        mockMvc.perform(post("/training")
                        .header("X-Username", "hacker")
                        .header("X-Password", "pass")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

}
