package com.muro_akhaladze.gym_task.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muro_akhaladze.gym_task.dto.trainee.TraineeRegistrationRequest;
import com.muro_akhaladze.gym_task.dto.trainer.TrainerRegistrationRequest;
import com.muro_akhaladze.gym_task.dto.training.AddTrainingRequest;
import com.muro_akhaladze.gym_task.service.TraineeService;
import com.muro_akhaladze.gym_task.service.TrainerService;
import com.muro_akhaladze.gym_task.service.TrainingService;
import com.muro_akhaladze.gym_task.service.TrainingTypeService;
import com.muro_akhaladze.gym_task.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

/** Loads demo data from data.json on startup, but only when the database has no users yet. */
@Component
@Slf4j
public class PreLoader implements CommandLineRunner {

    private final TrainingTypeService trainingTypeService;
    private final UserService userService;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final Resource seedFile;

    // Constructor injection
    public PreLoader(TrainingTypeService trainingTypeService,
                     UserService userService,
                     TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService,
                     ObjectMapper objectMapper,
                     @Value("${app.seed.enabled:true}") boolean enabled,
                     @Value("${app.seed.file:classpath:data.json}") Resource seedFile) {
        this.trainingTypeService = trainingTypeService;
        this.userService = userService;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.seedFile = seedFile;
    }

    @Override
    public void run(String... args) throws Exception {
        boolean hasUsers = userService.hasAnyUsers();
        if (!enabled || hasUsers) {
            log.info("Skipping demo data (enabled={}, users already present={})", enabled, hasUsers);
            return;
        }
        SeedData data;
        try (InputStream in = seedFile.getInputStream()) {
            data = objectMapper.readValue(in, SeedData.class);
        }

        data.trainingTypes().forEach(trainingTypeService::createIfMissing);

        data.trainees().forEach(t -> traineeService.register(
                new TraineeRegistrationRequest(t.firstName(), t.lastName(), t.dateOfBirth(), t.address()),
                data.demoPassword()));

        data.trainers().forEach(t -> trainerService.register(
                new TrainerRegistrationRequest(t.firstName(), t.lastName(), t.specialization()),
                data.demoPassword()));

        data.trainings().forEach(t -> {
            AddTrainingRequest request = new AddTrainingRequest();
            request.setTraineeUsername(t.trainee());
            request.setTrainerUsername(t.trainer());
            request.setTrainingName(t.trainingName());
            request.setTrainingDate(t.trainingDate());
            request.setTrainingDuration(t.trainingDuration());
            trainingService.createTraining(request);
        });

        log.info("Demo data loaded: {} training types, {} trainees, {} trainers, {} trainings. "
                        + "All demo users have the password '{}' (e.g. login as Liam.Anderson)",
                data.trainingTypes().size(), data.trainees().size(), data.trainers().size(),
                data.trainings().size(), data.demoPassword());
    }

    // Shape of data.json
    record SeedData(String demoPassword, List<String> trainingTypes, List<SeedTrainee> trainees,
                    List<SeedTrainer> trainers, List<SeedTraining> trainings) {
    }

    record SeedTrainee(String firstName, String lastName, LocalDate dateOfBirth, String address) {
    }

    record SeedTrainer(String firstName, String lastName, String specialization) {
    }

    record SeedTraining(String trainee, String trainer, String trainingName, LocalDate trainingDate,
                        Integer trainingDuration) {
    }
}
