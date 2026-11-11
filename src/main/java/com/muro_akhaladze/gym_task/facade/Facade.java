package com.muro_akhaladze.gym_task.facade;

import com.muro_akhaladze.gym_task.entity.Trainee;
import com.muro_akhaladze.gym_task.entity.Trainer;
import com.muro_akhaladze.gym_task.entity.Training;
import com.muro_akhaladze.gym_task.service.TraineeService;
import com.muro_akhaladze.gym_task.service.TrainerService;
import com.muro_akhaladze.gym_task.service.TrainingService;
import com.muro_akhaladze.gym_task.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Log
@Component
@RequiredArgsConstructor
public class Facade {
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final UserService userService;

    public Trainee createTrainee(Trainee trainee){
        return traineeService.createTrainee(trainee);
    }
    public Trainer createTrainer(Trainer trainer){
        return trainerService.createTrainer(trainer);
    }
    public Training createTraining(Training training){
        return trainingService.createTraining(training);
    }

    public Optional<Trainee> getTraineeByUsername(String username, String password) {
        if (userService.isInvalidPassword(username, password)) {
            log.warning("Invalid credentials for user: " + username);
            return Optional.empty();
        }
        return traineeService.getTraineeByUsername(username);
    }

    public Optional<Trainer> getTrainerByUsername(String username, String password) {
        if (userService.isInvalidPassword(username, password)) {
            log.warning("Invalid credentials for user: " + username);
            return Optional.empty();
        }
        return trainerService.getTrainerByUsername(username);
    }

    public List<Trainer> getNotAssigned(String username, String password) {
        if (userService.isInvalidPassword(username, password)) {
            log.warning("Invalid credentials for user: " + username);
            return Collections.emptyList();
        }
        return trainerService.getNotAssigned(username);
    }



    public Optional<Training> getTraining(int trainingId, String password, String username) {
        if (userService.isInvalidPassword(username, password)) {
            log.warning("Invalid credentials for user: " + username);
            return Optional.empty();
        }
        return trainingService.getTraining(trainingId);
    }

    public List<Training> getTraineeTrainings(String traineeUsername, String password, LocalDate fromDate, LocalDate toDate, String trainerName, String trainingType) {
        if (userService.isInvalidPassword(traineeUsername, password)) {
            log.warning("Invalid credentials for user: " + traineeUsername);
            return List.of();
        }
        return trainingService.getTraineeTrainings(traineeUsername, fromDate, toDate, trainerName, trainingType);
    }

    public List<Training> getTrainerTrainings(String trainerUsername, String password, LocalDate fromDate, LocalDate toDate, String traineeName) {
        if (userService.isInvalidPassword(trainerUsername, password)) {
            log.warning("Invalid credentials for user: " + trainerUsername);
            return List.of();
        }
        return trainingService.getTrainerTrainings(trainerUsername, fromDate, toDate, traineeName);
    }


    public Trainee updateTrainee(Trainee trainee, String password, String username) {
        if (userService.isInvalidPassword(username, password)) {
            log.warning("Invalid credentials for user: " + username);
            throw new IllegalArgumentException("Invalid username or password.");
        }
        return traineeService.updateTrainee(trainee);
    }

    public Trainer updateTrainer(Trainer trainer, String password, String username) {
        if (userService.isInvalidPassword(username, password)) {
            log.warning("Invalid credentials for user: " + username);
            throw new IllegalArgumentException("Invalid username or password.");
        }
        return trainerService.updateTrainer(trainer);
    }



    public void deleteTrainee( String password, String username) {
        if (userService.isInvalidPassword(username, password)) {
            log.warning("Invalid credentials for user: " + username);
            throw new IllegalArgumentException();
        }
        traineeService.deleteTrainee(username);
    }

    public void updateUserActivationStatus(String username, String password, boolean isActive) {
        if (userService.isInvalidPassword(username, password)) {
            log.warning("Invalid credentials for user: " + username);
            return;
        }
        userService.updateUserActivationStatus(username, isActive);
    }

    public void updatePassword(String username, String currentPassword, String newPassword) {
        if (userService.isInvalidPassword(username, currentPassword)) {
            log.warning("Invalid current password for user: " + username);
            return;
        }
        userService.updatePassword(username, currentPassword, newPassword);
    }





}
