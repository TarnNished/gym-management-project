package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muro_akhaladze.gym_task.controller.TrainerController;
import com.muro_akhaladze.gym_task.dto.trainee.ActivateUserRequest;
import com.muro_akhaladze.gym_task.dto.trainee.TrainerRegistrationRequest;
import com.muro_akhaladze.gym_task.dto.trainer.TrainerProfileResponse;
import com.muro_akhaladze.gym_task.entity.*;
import com.muro_akhaladze.gym_task.service.TrainerService;
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
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@ContextConfiguration(classes = com.muro_akhaladze.gym_task.Main.class)
@WebMvcTest(TrainerController.class)
class TrainerControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private TrainerService trainerService;
    @MockBean private UserService userService;
    @MockBean private TrainingService trainingService;

    private final String username = "trainer1";

    private RequestPostProcessor authHeaders() {
        return request -> {
            request.addHeader("X-Username", username);
            request.addHeader("X-Password", "pass");
            return request;
        };
    }

    @Test
    void testRegisterTrainer() throws Exception {
        TrainingType trainingType = new TrainingType(1,"CARDIO");
        TrainerRegistrationRequest request = new TrainerRegistrationRequest("John", "Doe", trainingType);
        Trainer trainer = new Trainer();

        Mockito.when(trainerService.createTrainer(any())).thenReturn(trainer);

        mockMvc.perform(post("/trainers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetTrainerProfileAuthorized() throws Exception {
        Mockito.when(userService.isInvalidPassword(username, "pass")).thenReturn(false);

        TrainerProfileResponse response = new TrainerProfileResponse();
        response.setUsername(username);

        Mockito.when(trainerService.getTrainerProfile(username)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/trainers/{username}", username).with(authHeaders()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));
    }

    @Test
    void testUpdateTrainerProfile() throws Exception {
        Mockito.when(userService.isInvalidPassword(username, "pass")).thenReturn(false);
        TrainingType trainingType = new TrainingType(1,"CARDIO");

        TrainerProfileResponse request = new TrainerProfileResponse();
        request.setUsername(username);
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setIsActive(true);
        request.setSpecialization(trainingType);

        Trainer updatedTrainer = new Trainer();
        User user = new User();
        user.setUserName(username);
        updatedTrainer.setUser(user);

        Mockito.when(trainerService.updateTrainer(any())).thenReturn(updatedTrainer);
        Mockito.when(trainerService.getTrainerProfile(username)).thenReturn(Optional.of(request));

        mockMvc.perform(put("/trainers/{username}", username)
                .with(authHeaders())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));
    }

    @Test
    void testToggleTrainerActivation() throws Exception {
        Mockito.when(userService.isInvalidPassword(username, "pass")).thenReturn(false);

        ActivateUserRequest request = new ActivateUserRequest(true);

        mockMvc.perform(patch("/trainers/{username}", username)
                .with(authHeaders())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Mockito.verify(userService).updateUserActivationStatus(username, true);
    }

    @Test
    void testGetTrainerTrainings() throws Exception {
        Mockito.when(userService.isInvalidPassword(username, "pass")).thenReturn(false);

        Training training = new Training();
        Mockito.when(trainingService.getTrainerTrainings(any(), any(), any(), any()))
                .thenReturn(List.of(training));

        mockMvc.perform(get("/trainers/{username}/trainings", username)
                .with(authHeaders()))
                .andExpect(status().isOk());
    }
}
