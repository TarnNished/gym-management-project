package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muro_akhaladze.gym_task.controller.TraineeController;
import com.muro_akhaladze.gym_task.dto.trainee.*;
import com.muro_akhaladze.gym_task.dto.trainer.*;
import com.muro_akhaladze.gym_task.entity.*;
import com.muro_akhaladze.gym_task.service.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@ContextConfiguration(classes = com.muro_akhaladze.gym_task.Main.class)
@WebMvcTest(TraineeController.class)
class TraineeControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private TraineeService traineeService;
    @MockBean private TrainerService trainerService;
    @MockBean private UserService userService;
    @MockBean private TrainingService trainingService;

    private final String username = "john";

    private RequestPostProcessor authHeaders() {
        return request -> {
            request.addHeader("X-Username", username);
            request.addHeader("X-Password", "pass");
            return request;
        };
    }

    @Test
    void testRegisterTrainee() throws Exception {
        TraineeRegistrationRequest req = new TraineeRegistrationRequest("John", "Doe", LocalDate.of(2000, 1, 1), "Address");
        Trainee trainee = new Trainee();

        Mockito.when(traineeService.createTrainee(any())).thenReturn(trainee);

        mockMvc.perform(post("/trainees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetTraineeProfileAuthorized() throws Exception {
        Mockito.when(userService.isInvalidPassword(eq(username), eq("pass"))).thenReturn(false);

        TraineeProfileResponse profile = new TraineeProfileResponse();
        profile.setUsername(username);
        Mockito.when(traineeService.getTraineeProfile(eq(username))).thenReturn(Optional.of(profile));

        mockMvc.perform(get("/trainees/{username}", username)
                .with(authHeaders()))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateTraineeProfile() throws Exception {
        Mockito.when(userService.isInvalidPassword(eq(username), eq("pass"))).thenReturn(false);

        TraineeProfileResponse req = new TraineeProfileResponse();
        req.setUsername(username);
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setIsActive(true);
        req.setAddress("123 St");
        req.setDateOfBirth(LocalDate.of(2000, 1, 1));

        Trainee trainee = new Trainee();
        User user = new User();
        user.setUserName(username);
        trainee.setUser(user);

        Mockito.when(traineeService.updateTrainee(any())).thenReturn(trainee);
        Mockito.when(traineeService.getTraineeProfile(eq(username))).thenReturn(Optional.of(req));

        mockMvc.perform(put("/trainees/{username}", username)
                .with(authHeaders())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteTrainee() throws Exception {
        Mockito.when(userService.isInvalidPassword(eq(username), eq("pass"))).thenReturn(false);

        mockMvc.perform(delete("/trainees/{username}", username).with(authHeaders()))
                .andExpect(status().isOk());

        Mockito.verify(traineeService).deleteTrainee(username);
    }

    @Test
    void testToggleActivationTrainee() throws Exception {
        Mockito.when(userService.isInvalidPassword(eq(username), eq("pass"))).thenReturn(false);
        ActivateUserRequest req = new ActivateUserRequest(true);

        mockMvc.perform(patch("/trainees/{username}", username)
                .with(authHeaders())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        Mockito.verify(userService).updateUserActivationStatus(username, true);
    }

    @Test
    void testGetTraineeTrainings() throws Exception {
        Mockito.when(userService.isInvalidPassword(eq(username), eq("pass"))).thenReturn(false);

        Training training = new Training();
        Mockito.when(trainingService.getTraineeTrainings(any(), any(), any(), any(), any()))
                .thenReturn(List.of(training));

        mockMvc.perform(get("/trainees/{username}/trainings", username)
                .with(authHeaders()))
                .andExpect(status().isOk());
    }

    @Test
    void testGetUnassignedTrainers() throws Exception {
        Mockito.when(userService.isInvalidPassword(eq(username), eq("pass"))).thenReturn(false);

        Trainer trainer = new Trainer();
        User trainerUser = new User();
        TrainingType trainingType = new TrainingType(1,"strength");
        trainerUser.setUserName("trainer1");
        trainerUser.setFirstName("Mike");
        trainerUser.setLastName("Smith");
        trainer.setUser(trainerUser);
        trainer.setSpecialization(trainingType);

        Mockito.when(trainerService.getNotAssigned(username)).thenReturn(List.of(trainer));

        mockMvc.perform(get("/trainees/{username}/unassigned-trainers", username)
                .with(authHeaders()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("trainer1"));
    }
}
