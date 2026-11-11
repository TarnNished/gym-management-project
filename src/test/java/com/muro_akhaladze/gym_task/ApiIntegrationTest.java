package com.muro_akhaladze.gym_task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.context.TestConstructor;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end tests over HTTP against the in-memory H2 database with the demo data from data.json.
 * Tests that change data register their own users so they don't depend on each other.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class ApiIntegrationTest {

    @TempDir
    static Path uploadDir;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("app.upload-dir", () -> uploadDir.toString());
    }

    private final MockMvc mvc;
    private final ObjectMapper json;

    // constructor injection (enabled by @TestConstructor)
    ApiIntegrationTest(MockMvc mvc, ObjectMapper json) {
        this.mvc = mvc;
        this.json = json;
    }

    // ---------- helpers ----------

    private String login(String username, String password) throws Exception {
        String body = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(body).get("token").asText();
    }

    /** Registers a fresh trainee and returns [username, bearerToken]. */
    private String[] newTrainee() throws Exception {
        String last = "T" + UUID.randomUUID().toString().substring(0, 8);
        String body = mvc.perform(post("/api/v1/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "firstName", "Test", "lastName", last,
                                "dateOfBirth", "2000-01-01", "address", "Somewhere 1"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = json.readTree(body);
        String username = node.get("username").asText();
        return new String[]{username, login(username, node.get("password").asText())};
    }

    // ---------- security ----------

    @Test
    void protectedEndpointWithoutTokenReturns401() throws Exception {
        mvc.perform(get("/api/v1/trainers")).andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPasswordReturns401() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"Ethan.Williams\",\"password\":\"nope\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cannotReadSomeoneElsesProfile() throws Exception {
        String token = login("Ethan.Williams", "password123");
        mvc.perform(get("/api/v1/trainees/Mia.Johnson").header("Authorization", token))
                .andExpect(status().isForbidden());
    }

    // ---------- validation ----------

    @Test
    void invalidRegistrationReturnsFieldErrors() throws Exception {
        mvc.perform(post("/api/v1/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"\",\"lastName\":\"X\",\"dateOfBirth\":\"2999-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILURE"))
                .andExpect(jsonPath("$.fieldErrors.firstName").exists())
                .andExpect(jsonPath("$.fieldErrors.dateOfBirth").exists())
                .andExpect(jsonPath("$.fieldErrors.address").exists());
    }

    @Test
    void trainerRegistrationWithUnknownSpecializationReturns404() throws Exception {
        mvc.perform(post("/api/v1/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"A\",\"lastName\":\"B\",\"specialization\":\"Chess\"}"))
                .andExpect(status().isNotFound());
    }

    // ---------- pagination & sorting ----------

    @Test
    void trainersArePagedAndSorted() throws Exception {
        String token = login("Ethan.Williams", "password123");
        mvc.perform(get("/api/v1/trainers")
                        .param("page", "0").param("size", "3")
                        .param("sort", "lastName,asc")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].lastName").value("Clark"))
                .andExpect(jsonPath("$.content[1].lastName").value("Davis"))
                .andExpect(jsonPath("$.page.size").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(4));
    }

    @Test
    void trainersCanBeFilteredBySpecialization() throws Exception {
        String token = login("Ethan.Williams", "password123");
        mvc.perform(get("/api/v1/trainers")
                        .param("specialization", "strength training")
                        .param("sort", "firstName")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.content[0].firstName").value("Grace"));
    }

    @Test
    void invalidSortFieldReturns400() throws Exception {
        String token = login("Ethan.Williams", "password123");
        mvc.perform(get("/api/v1/trainers").param("sort", "user.password").header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_SORT"));
    }

    @Test
    void traineeTrainingsArePagedSortedAndFiltered() throws Exception {
        String token = login("Liam.Anderson", "password123");
        mvc.perform(get("/api/v1/trainees/Liam.Anderson/trainings")
                        .param("size", "2").param("sort", "trainingDuration,desc")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].trainingDuration").value(75))
                .andExpect(jsonPath("$.page.totalElements").value(3));

        mvc.perform(get("/api/v1/trainees/Liam.Anderson/trainings")
                        .param("trainerName", "Sophia.Taylor")
                        .header("Authorization", token))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].trainingName").value("Morning Run"));
    }

    // ---------- many-to-many ----------

    @Test
    void trainersCanBeAssignedToTrainee() throws Exception {
        String[] trainee = newTrainee();
        mvc.perform(put("/api/v1/trainees/" + trainee[0] + "/trainers")
                        .header("Authorization", trainee[1])
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trainerUsernames\":[\"Emma.Davis\",\"Oliver.Martinez\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mvc.perform(get("/api/v1/trainees/" + trainee[0]).header("Authorization", trainee[1]))
                .andExpect(jsonPath("$.trainers.length()").value(2))
                .andExpect(jsonPath("$.trainers[0].username").value("Emma.Davis"));

        // the inverse side (trainer profile) sees the trainee too
        String trainerToken = login("Emma.Davis", "password123");
        String trainerProfile = mvc.perform(get("/api/v1/trainers/Emma.Davis").header("Authorization", trainerToken))
                .andReturn().getResponse().getContentAsString();
        assertThat(trainerProfile).contains(trainee[0]);

        mvc.perform(get("/api/v1/trainees/" + trainee[0] + "/unassigned-trainers").header("Authorization", trainee[1]))
                .andExpect(jsonPath("$.length()").value(10));
    }

    @Test
    void assigningUnknownTrainerReturns404() throws Exception {
        String[] trainee = newTrainee();
        mvc.perform(put("/api/v1/trainees/" + trainee[0] + "/trainers")
                        .header("Authorization", trainee[1])
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trainerUsernames\":[\"Nobody.Here\"]}"))
                .andExpect(status().isNotFound());
    }

    // ---------- file upload ----------

    @Test
    void photoUploadStoresFileAndSavesNameInDatabase() throws Exception {
        String[] trainee = newTrainee();
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3};
        MockMultipartFile file = new MockMultipartFile("file", "me.png", "image/png", png);

        String body = mvc.perform(multipart("/api/v1/users/" + trainee[0] + "/photo").file(file)
                        .header("Authorization", trainee[1]))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalFileName").value("me.png"))
                .andReturn().getResponse().getContentAsString();
        String stored = json.readTree(body).get("storedFileName").asText();

        // physically on disk
        assertThat(Files.readAllBytes(uploadDir.resolve(stored))).isEqualTo(png);

        // file name reflected in the database (shown in the profile)
        mvc.perform(get("/api/v1/trainees/" + trainee[0]).header("Authorization", trainee[1]))
                .andExpect(jsonPath("$.photoPath").value(stored));

        // downloadable
        mvc.perform(get("/api/v1/users/" + trainee[0] + "/photo"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/users/" + trainee[0] + "/photo").header("Authorization", trainee[1]))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(png));
    }

    @Test
    void nonImageUploadIsRejected() throws Exception {
        String[] trainee = newTrainee();
        MockMultipartFile file = new MockMultipartFile("file", "notes.txt", "text/plain", "hi".getBytes());
        mvc.perform(multipart("/api/v1/users/" + trainee[0] + "/photo").file(file)
                        .header("Authorization", trainee[1]))
                .andExpect(status().isUnsupportedMediaType());
    }

    // ---------- update / delete (transactions) ----------

    @Test
    void deleteTraineeRemovesUserTrainingsAndLinks() throws Exception {
        String[] trainee = newTrainee();
        mvc.perform(post("/api/v1/trainings")
                        .header("Authorization", trainee[1])
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"traineeUsername\":\"" + trainee[0] + "\",\"trainerUsername\":\"Henry.Moore\","
                                + "\"trainingName\":\"Squats\",\"trainingDate\":\"2025-04-01\",\"trainingDuration\":30}"))
                .andExpect(status().isCreated());

        mvc.perform(delete("/api/v1/trainees/" + trainee[0]).header("Authorization", trainee[1]))
                .andExpect(status().isNoContent());

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + trainee[0] + "\",\"password\":\"whatever\"}"))
                .andExpect(status().isUnauthorized());

        String henry = login("Henry.Moore", "password123");
        String profile = mvc.perform(get("/api/v1/trainers/Henry.Moore").header("Authorization", henry))
                .andReturn().getResponse().getContentAsString();
        assertThat(profile).doesNotContain(trainee[0]);
    }

    @Test
    void updateTraineeProfile() throws Exception {
        String[] trainee = newTrainee();
        mvc.perform(put("/api/v1/trainees/" + trainee[0])
                        .header("Authorization", trainee[1])
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"New\",\"lastName\":\"Name\",\"dateOfBirth\":\"1999-09-09\","
                                + "\"address\":\"New street 5\",\"isActive\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("New"))
                .andExpect(jsonPath("$.address").value("New street 5"));
    }
}
