package com.muro_akhaladze.gym_task.controller;

import com.muro_akhaladze.gym_task.dto.trainee.ActivateUserRequest;
import com.muro_akhaladze.gym_task.dto.trainer.TrainerProfileResponse;
import com.muro_akhaladze.gym_task.dto.trainee.TrainerRegistrationRequest;
import com.muro_akhaladze.gym_task.dto.training.TrainerTrainingsFilterRequest;
import com.muro_akhaladze.gym_task.entity.Trainer;
import com.muro_akhaladze.gym_task.entity.Training;
import com.muro_akhaladze.gym_task.entity.User;
import com.muro_akhaladze.gym_task.service.TrainerService;
import com.muro_akhaladze.gym_task.service.TrainingService;
import com.muro_akhaladze.gym_task.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/trainers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Trainer Operations", description = "API endpoints for handling trainer-related actions")
public class TrainerController {
    private final TrainerService trainerService;
    private final UserService userService;
    private final TrainingService trainingService;

    @PostMapping()
    @Operation(summary = "Register a new trainer")
    public ResponseEntity<?> registerTrainer(@RequestBody TrainerRegistrationRequest request){
        Trainer trainer = new Trainer();
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        trainer.setUser(user);
        trainer.setSpecialization(request.getSpecialization());

        return ResponseEntity.status(200).body(trainerService.createTrainer(trainer));
    }

    @GetMapping("/{username}")
    @Operation(summary = "Trainer Profile getter")
    public ResponseEntity<TrainerProfileResponse> getTrainerProfile(
            @PathVariable("username") String username,
            Authentication authentication) {

        String authUsername = authentication.getName();

        if (!authUsername.equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<TrainerProfileResponse> trainer = trainerService.getTrainerProfile(username);
        return trainer.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{username}")
    @Operation(summary = "Update trainer profile")
    public ResponseEntity<TrainerProfileResponse> updateTrainerProfile(
            @PathVariable("username") String username,
            @Valid @RequestBody TrainerProfileResponse request,
            Authentication authentication) {

        log.info("Trainer update requested for {}", username);
        String authUsername = authentication.getName();

        if (!authUsername.equals(username) || !username.equals(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        User user = new User();
        user.setUserName(request.getUsername());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setActive(request.getIsActive());

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(request.getSpecialization());

        Trainer updated = trainerService.updateTrainer(trainer);
        TrainerProfileResponse response = trainerService.getTrainerProfile(updated.getUser().getUsername()).orElseThrow();

        log.info("Trainer {} successfully updated", username);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{username}")
    @Operation(summary = "Activate or deactivate trainer")
    public ResponseEntity<Void> toggleActivationTrainer(
            @PathVariable("username") String username,
            @RequestBody @Valid ActivateUserRequest request,
            Authentication authentication) {

        String authUsername = authentication.getName();

        if (!authUsername.equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        userService.updateUserActivationStatus(username, request.getIsActive());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/trainings")
    @Operation(summary = "Get trainer trainings with filters")
    public ResponseEntity<List<Training>> getTrainerTrainings(
            @PathVariable("username") String username,
            @Valid TrainerTrainingsFilterRequest request,
            Authentication authentication) {

        String authUsername = authentication.getName();

        if (!authUsername.equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Training> trainings = trainingService.getTrainerTrainings(
                username,
                request.getFromDate(),
                request.getToDate(),
                request.getTraineeName());

        return ResponseEntity.ok(trainings);
    }
}
