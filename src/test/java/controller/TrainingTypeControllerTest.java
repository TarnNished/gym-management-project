package controller;

import com.muro_akhaladze.gym_task.controller.TrainingTypeController;
import com.muro_akhaladze.gym_task.entity.TrainingType;
import com.muro_akhaladze.gym_task.repository.TrainingTypeRepo;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.*;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@ContextConfiguration(classes = com.muro_akhaladze.gym_task.Main.class)
@WebMvcTest(TrainingTypeController.class)
class TrainingTypeControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private TrainingTypeRepo trainingTypeRepo;

    @Test
    void testGetAllTrainingTypes() throws Exception {
        TrainingType type1 = new TrainingType();
        type1.setTrainingTypeId(1);
        type1.setTrainingTypeName("Cardio");

        TrainingType type2 = new TrainingType();
        type2.setTrainingTypeId(2);
        type2.setTrainingTypeName("Strength");

        Mockito.when(trainingTypeRepo.findAll()).thenReturn(List.of(type1, type2));

        mockMvc.perform(get("/training-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Cardio"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Strength"));
    }
}
