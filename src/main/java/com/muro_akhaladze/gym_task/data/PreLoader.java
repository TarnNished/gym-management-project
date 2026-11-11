package com.muro_akhaladze.gym_task.data;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.muro_akhaladze.gym_task.entity.Trainee;
import com.muro_akhaladze.gym_task.entity.Trainer;
import com.muro_akhaladze.gym_task.entity.Training;
import com.muro_akhaladze.gym_task.entity.TrainingType;
import com.muro_akhaladze.gym_task.service.TraineeService;
import com.muro_akhaladze.gym_task.service.TrainerService;
import com.muro_akhaladze.gym_task.service.TrainingService;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PreLoader {

    private final TrainingService trainingService;
    private final TrainerService trainerService;
    private final TraineeService traineeService;

    @Value("${data.storage}")
    private String dataFilePath;

    @PostConstruct
    public void loadData() {
        log.info("Loading data from: {} " , dataFilePath);

        try {
            File file = new File(dataFilePath);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            DataWrapper dataWrapper = objectMapper.readValue(file,DataWrapper.class);

            for (Trainee traineeData : dataWrapper.getTrainees()) {
                Trainee trainee = new Trainee(
                        traineeData.getUser(),
                        traineeData.getDateOfBirth(),
                        traineeData.getAddress()
                );
                Trainee savedTrainee = traineeService.createTrainee(trainee);
                log.info("Created Trainee: {}", savedTrainee.getUser().getUsername());
            }
            for (Trainer trainerData : dataWrapper.getTrainers()) {
                Trainer trainer = new Trainer(
                        trainerData.getUser(),
                        trainerData.getSpecialization()
                );
                Trainer savedTrainer = trainerService.createTrainer(trainer);
                log.info("Created Trainer: {}", savedTrainer.getUser().getUsername());
            }
            for (TrainingData trainingData : dataWrapper.getTrainings()) {
                Training training = new Training(
                        trainingData.getTrainingId(),
                        trainingData.getTrainee(),
                        trainingData.getTrainer(),
                        trainingData.getTrainingType(),
                        trainingData.getTrainingType().getTrainingTypeName(),
                        trainingData.getTrainingDate(),
                        trainingData.getTrainingDurationMinutes()

                );
                Training savedTraining = trainingService.createTraining(training);
                log.info("Created Training: {}", savedTraining.getTrainingName());

            }


        } catch (IOException e) {
            log.error("Error reading data file: {}", e.getMessage());
        }
    }
    // this class is used to map the structure of json file
    // and ensures it is properly structured before being used
    @Getter
    static class DataWrapper {
        private List<Trainee> trainees;
        private List<Trainer> trainers;
        private List<TrainingData> trainings;
    }

    @Getter
    static class TrainingData {
        private int trainingId;
        private Trainee Trainee;
        private Trainer Trainer;
        private String trainingName;
        private TrainingType trainingType;
        private LocalDate trainingDate;
        private Long trainingDurationMinutes;
    }

}
