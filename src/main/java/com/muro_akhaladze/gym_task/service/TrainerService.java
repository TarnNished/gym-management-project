package com.muro_akhaladze.gym_task.service;

import com.muro_akhaladze.gym_task.dto.trainee.TraineeShortInfo;
import com.muro_akhaladze.gym_task.dto.trainer.TrainerProfileResponse;
import com.muro_akhaladze.gym_task.entity.Trainee;
import com.muro_akhaladze.gym_task.entity.Trainer;
import com.muro_akhaladze.gym_task.entity.User;
import com.muro_akhaladze.gym_task.repository.TraineeRepo;
import com.muro_akhaladze.gym_task.repository.TrainerRepo;
import com.muro_akhaladze.gym_task.repository.UserRepo;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Log
@RequiredArgsConstructor
public class TrainerService {
    private final TrainerRepo trainerRepo;
    private final TraineeRepo traineeRepo;
    private final UserRepo userRepo;
    private final UserService userService;

    public Trainer createTrainer(@Valid Trainer trainer) {

        log.info("Create trainer");
        User user = trainer.getUser();
        user.setUserName(userService.generateUserName(trainer.getUser().getFirstName(), trainer.getUser().getLastName()));
        user.setPassword(userService.generatePassword());
        User savedUser = userRepo.save(user);

        trainer.setUser(savedUser);

        return trainerRepo.save(trainer);
    }
    public Optional<Trainer> getTrainerByUsername(String username) {
        Optional<Trainer> trainer = trainerRepo.findByUsername(username);
        if (trainer.isPresent()) {
            log.info("select trainee with username");
        }else
            log.warning("no user found");
        return trainer;
    }
    public Trainer updateTrainer(@Valid Trainer trainer) {
        Trainer existingTrainer = trainerRepo.findByUsername(trainer.getUser().getUsername())
                .orElseThrow(() -> {
                    log.warning("No trainer found with username '" + trainer.getUser().getUsername() + "'");
                    return new IllegalArgumentException("Trainer not found");
                });

        log.info("Updating trainer with username");

        existingTrainer.setSpecialization(trainer.getSpecialization());
        existingTrainer.setUser(trainer.getUser());

        return trainerRepo.save(existingTrainer);
    }
    public List<Trainer> getNotAssigned(String username) {
        return trainerRepo.findTrainersNotAssignedToTrainee(username);
    }

    public Optional<TrainerProfileResponse> getTrainerProfile(String username) {
        return trainerRepo.findByUsername(username)
                .map(this::mapToDto);
    }
    private TrainerProfileResponse mapToDto(Trainer trainer) {
        TrainerProfileResponse dto = new TrainerProfileResponse();
        dto.setFirstName(trainer.getUser().getFirstName());
        dto.setLastName(trainer.getUser().getLastName());
        dto.setIsActive(trainer.getUser().isActive());
        dto.setSpecialization(trainer.getSpecialization());

        List<TraineeShortInfo> trainees = traineeRepo.findByTrainerUsername(trainer.getUser().getUsername())
                .stream()
                .map(trainee -> {
                    TraineeShortInfo info = new TraineeShortInfo();
                    info.setUsername(trainee.getUser().getUsername());
                    info.setFirstName(trainee.getUser().getFirstName());
                    info.setLastName(trainee.getUser().getLastName());
                    return info;
                }).toList();

        dto.setTrainees(trainees);
        return dto;
    }


}
