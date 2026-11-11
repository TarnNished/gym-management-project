package com.muro_akhaladze.gym_task.service;

import com.muro_akhaladze.gym_task.dto.training.AddTrainingRequest;
import com.muro_akhaladze.gym_task.entity.Trainee;
import com.muro_akhaladze.gym_task.entity.Trainer;
import com.muro_akhaladze.gym_task.entity.Training;
import com.muro_akhaladze.gym_task.exceptions.ApiException;
import com.muro_akhaladze.gym_task.repository.TraineeRepo;
import com.muro_akhaladze.gym_task.repository.TrainerRepo;
import com.muro_akhaladze.gym_task.repository.TrainingRepo;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Log
@RequiredArgsConstructor
public class TrainingService {
    private final TrainingRepo trainingRepo;
    private final TraineeRepo traineeRepo;
    private final TrainerRepo trainerRepo;

    public Training createTraining(@Valid Training training) {
        log.info("created Training");
        return trainingRepo.save(training);
    }
    public Optional<Training> getTraining(int trainingId) {
        Optional<Training> training = trainingRepo.findById(trainingId);
        if (training.isPresent()) {
            log.info("select trainee with username");
        }else
            log.warning("no user found");
        return training;
    }
    public List<Training> getTraineeTrainings(String traineeUsername, LocalDate fromDate, LocalDate toDate, String trainerName, String trainingType) {
        return trainingRepo.findTraineeTrainings(traineeUsername, fromDate, toDate, trainerName, trainingType);
    }

    public List<Training> getTrainerTrainings(String trainerUsername, LocalDate fromDate, LocalDate toDate, String traineeName) {
        return trainingRepo.findTrainerTrainings(trainerUsername, fromDate, toDate, traineeName);
    }
    public void createTraining(@Valid AddTrainingRequest request) {
        Trainee trainee = traineeRepo.findByUsername(request.getTraineeUsername())
                .orElseThrow(() -> new ApiException("NOT_FOUND", "Trainee not found", HttpStatus.NOT_FOUND));

        Trainer trainer = trainerRepo.findByUsername(request.getTrainerUsername())
                .orElseThrow(() -> new ApiException("NOT_FOUND", "Trainer not found", HttpStatus.NOT_FOUND));

        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(trainer.getSpecialization());
        training.setTrainingName(request.getTrainingName());
        training.setTrainingDate(request.getTrainingDate());
        training.setTrainingDuration(request.getTrainingDuration().longValue());

        trainingRepo.save(training);
    }

}
