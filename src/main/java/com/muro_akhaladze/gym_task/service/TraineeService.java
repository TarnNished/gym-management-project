package com.muro_akhaladze.gym_task.service;


import com.muro_akhaladze.gym_task.dto.trainee.TraineeProfileResponse;
import com.muro_akhaladze.gym_task.dto.trainer.TrainerShortInfo;
import com.muro_akhaladze.gym_task.entity.Trainee;
import com.muro_akhaladze.gym_task.entity.Trainer;
import com.muro_akhaladze.gym_task.entity.User;
import com.muro_akhaladze.gym_task.repository.TraineeRepo;
import com.muro_akhaladze.gym_task.repository.TrainerRepo;
import com.muro_akhaladze.gym_task.repository.UserRepo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@Log
@RequiredArgsConstructor
public class TraineeService {
    private final TraineeRepo traineeRepo;
    private final TrainerRepo trainerRepo;
    private final UserRepo userRepo;
    private final UserService userService;

    public Trainee createTrainee(@Valid Trainee trainee) {
        log.info("Creating new trainee");
        User user = trainee.getUser();
        user.setUserName(userService.generateUserName(trainee.getUser().getFirstName(), trainee.getUser().getLastName()));
        user.setPassword(userService.generatePassword());
        User savedUser = userRepo.save(user);

        trainee.setUser(savedUser);


        return traineeRepo.save(trainee);
    }
    public void deleteTrainee(String username) {
        traineeRepo.deleteByUsername(username);
        log.info("deleting trainee with ID");
    }
    public Optional<Trainee> getTraineeByUsername(String username) {
            Optional<Trainee> trainee = traineeRepo.findByUsername(username);
            if (trainee.isPresent()) {
                log.info("select trainee with username");
            }else
                log.warning("no user found");
        return trainee;
    }
    public Trainee updateTrainee(@Valid Trainee trainee) {
        Trainee existingTrainee = traineeRepo.findByUsername(trainee.getUser().getUsername())
                .orElseThrow(() -> {
                    log.warning("No trainee found with username '" + trainee.getUser().getUsername() + "'");
                    return new IllegalArgumentException("Trainee not found");
                });

        log.info("Updating trainee with username");

        existingTrainee.setAddress(trainee.getAddress());
        existingTrainee.setDateOfBirth(trainee.getDateOfBirth());
        existingTrainee.setUser(trainee.getUser());

        return traineeRepo.save(existingTrainee);
    }
    public Optional<TraineeProfileResponse> getTraineeProfile(String username) {
        return traineeRepo.findByUsername(username)
                .map(this::mapToDto);
    }
    private TraineeProfileResponse mapToDto(Trainee trainee) {
        TraineeProfileResponse dto = new TraineeProfileResponse();
        dto.setFirstName(trainee.getUser().getFirstName());
        dto.setLastName(trainee.getUser().getLastName());
        dto.setDateOfBirth(trainee.getDateOfBirth());
        dto.setAddress(trainee.getAddress());

        List<Trainer> trainers = trainerRepo.findByTraineeUsername(trainee.getUser().getUsername());

        dto.setTrainers(
                trainers.stream().map(trainer -> {
                    TrainerShortInfo info = new TrainerShortInfo();
                    info.setUsername(trainer.getUser().getUsername());
                    info.setFirstName(trainer.getUser().getFirstName());
                    info.setLastName(trainer.getUser().getLastName());
                    info.setSpecialization(trainer.getSpecialization().getTrainingTypeName());
                    return info;
                }).toList()
        );

        return dto;
    }





}
