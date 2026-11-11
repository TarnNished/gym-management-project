package com.muro_akhaladze.gym_task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Validation, sorting, upload and security edge cases. */
@SpringBootTest
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class EdgeCaseIntegrationTest {

    @TempDir
    static Path uploadDir;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("app.upload-dir", () -> uploadDir.toString());
    }

    private final MockMvc mvc;
    private final ObjectMapper json;

    EdgeCaseIntegrationTest(MockMvc mvc, ObjectMapper json) {
        this.mvc = mvc;
        this.json = json;
    }

    private String login(String username, String password) throws Exception {
        String body = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(body).get("token").asText();
    }

    private String[] register(String path, Map<String, String> body) throws Exception {
        String res = mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = json.readTree(res);
        String username = node.get("username").asText();
        return new String[]{username, login(username, node.get("password").asText())};
    }

    private String[] newTrainee() throws Exception {
        return register("/api/v1/trainees", Map.of("firstName", "Edge", "lastName", "T" + UUID.randomUUID().toString().substring(0, 8),
                "dateOfBirth", "2000-01-01", "address", "Somewhere 1"));
    }

    private String[] newTrainer() throws Exception {
        return register("/api/v1/trainers", Map.of("firstName", "Edge", "lastName", "R" + UUID.randomUUID().toString().substring(0, 8),
                "specialization", "HIIT"));
    }

    private MockHttpServletRequestBuilder addTraining(String token, String trainee, String trainer, String name, int minutes) {
        return post("/api/v1/trainings")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"traineeUsername\":\"" + trainee + "\",\"trainerUsername\":\"" + trainer + "\","
                        + "\"trainingName\":\"" + name + "\",\"trainingDate\":\"2025-05-01\",\"trainingDuration\":" + minutes + "}");
    }

    // ---------- validation ----------

    @Test
    void trainingNameMustNotBeBlankOrTooLong() throws Exception {
        String[] t = newTrainee();
        mvc.perform(addTraining(t[1], t[0], "Henry.Moore", "   ", 30))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.trainingName").exists());
        mvc.perform(addTraining(t[1], t[0], "Henry.Moore", "x".repeat(80), 30))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.trainingName").exists());
        mvc.perform(addTraining(t[1], t[0], "Henry.Moore", "Ok", 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.trainingDuration").exists());
    }

    @Test
    void filterDateRangeIsValidated() throws Exception {
        String token = login("Liam.Anderson", "password123");
        mvc.perform(get("/api/v1/trainees/Liam.Anderson/trainings")
                        .param("fromDate", "2025-05-01").param("toDate", "2025-01-01")
                        .header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILURE"));
    }

    // ---------- sorting ----------

    @Test
    void unknownOrSensitiveTrainingSortIsRejected() throws Exception {
        String token = login("Liam.Anderson", "password123");
        for (String sort : new String[]{"bogus,asc", "trainee.user.password,asc"}) {
            mvc.perform(get("/api/v1/trainees/Liam.Anderson/trainings").param("sort", sort).header("Authorization", token))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("INVALID_SORT"));
        }
        mvc.perform(get("/api/v1/trainees/Liam.Anderson/trainings").param("sort", "trainingType,asc").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].trainingType").value("Cardio Training"));
    }

    // ---------- upload ----------

    @Test
    void fileWithFakeImageContentTypeIsRejected() throws Exception {
        String[] t = newTrainee();
        MockMultipartFile fake = new MockMultipartFile("file", "evil.png", "image/png", "not really a png".getBytes());
        mvc.perform(multipart("/api/v1/users/" + t[0] + "/photo").file(fake).header("Authorization", t[1]))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error").value("INVALID_FILE_CONTENT"));
    }

    // ---------- security ----------

    @Test
    void failedLoginsFromOneIpDoNotLockOutOtherIps() throws Exception {
        String attackerBody = "{\"username\":\"Mia.Johnson\",\"password\":\"wrong\"}";
        for (int i = 0; i < 3; i++) {
            mvc.perform(post("/api/v1/auth/login").with(r -> { r.setRemoteAddr("10.0.0.66"); return r; })
                            .contentType(MediaType.APPLICATION_JSON).content(attackerBody))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
        }
        // the attacker's IP is now blocked, even with the right password
        mvc.perform(post("/api/v1/auth/login").with(r -> { r.setRemoteAddr("10.0.0.66"); return r; })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"Mia.Johnson\",\"password\":\"password123\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("TOO_MANY_ATTEMPTS"));
        // the real user from their own machine (MockMvc default 127.0.0.1) can still log in
        login("Mia.Johnson", "password123");
    }

    // ---------- business rules / deletes ----------

    @Test
    void cannotCreateTrainingForSomeoneElse() throws Exception {
        String[] t = newTrainee();
        mvc.perform(addTraining(t[1], "Ethan.Williams", "Henry.Moore", "Sneaky", 30))
                .andExpect(status().isForbidden());
    }

    @Test
    void cannotTrainWithInactiveTrainer() throws Exception {
        String[] trainer = newTrainer();
        mvc.perform(patch("/api/v1/trainers/" + trainer[0]).header("Authorization", trainer[1])
                        .contentType(MediaType.APPLICATION_JSON).content("{\"isActive\":false}"))
                .andExpect(status().isOk());
        String[] t = newTrainee();
        mvc.perform(addTraining(t[1], t[0], trainer[0], "Nope", 30))
                .andExpect(status().isConflict());
    }

    @Test
    void trainingCanBeDeletedOnlyByParticipants() throws Exception {
        String[] t = newTrainee();
        String res = mvc.perform(addTraining(t[1], t[0], "Henry.Moore", "Temp", 30))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        int id = json.readTree(res).get("id").asInt();

        String other = login("Ethan.Williams", "password123");
        mvc.perform(delete("/api/v1/trainings/" + id).header("Authorization", other)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/v1/trainings/" + id).header("Authorization", t[1])).andExpect(status().isNoContent());
        mvc.perform(delete("/api/v1/trainings/" + id).header("Authorization", t[1])).andExpect(status().isNotFound());
    }

    @Test
    void deleteTrainerRemovesTrainingsAndLinks() throws Exception {
        String[] trainer = newTrainer();
        String[] t = newTrainee();
        mvc.perform(addTraining(t[1], t[0], trainer[0], "Last one", 30)).andExpect(status().isCreated());

        mvc.perform(delete("/api/v1/trainers/" + trainer[0]).header("Authorization", trainer[1]))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/trainees/" + t[0]).header("Authorization", t[1]))
                .andExpect(jsonPath("$.trainers.length()").value(0));
        mvc.perform(get("/api/v1/trainees/" + t[0] + "/trainings").header("Authorization", t[1]))
                .andExpect(jsonPath("$.page.totalElements").value(0));
    }
}
