package com.muro_akhaladze.gym_task.controller;

import com.muro_akhaladze.gym_task.dto.trainee.ActivateUserRequest;
import com.muro_akhaladze.gym_task.dto.trainee.TraineeProfileResponse;
import com.muro_akhaladze.gym_task.dto.trainer.TraineeRegistrationRequest;
import com.muro_akhaladze.gym_task.dto.trainer.TrainerShortInfo;
import com.muro_akhaladze.gym_task.dto.training.TraineeTrainingsFilterRequest;
import com.muro_akhaladze.gym_task.entity.Trainee;
import com.muro_akhaladze.gym_task.entity.Training;
import com.muro_akhaladze.gym_task.entity.User;
import com.muro_akhaladze.gym_task.service.TraineeService;
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
@RequestMapping("/api/v1/trainees")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Trainee Operations", description = "API endpoints for handling trainee-related actions")
public class TraineeController {
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final UserService userService;
    private final TrainingService trainingService;

    @PostMapping()
    @Operation(summary = "Creating trainee")
    public ResponseEntity<?> registerTrainee(@RequestBody TraineeRegistrationRequest request){
        Trainee trainee = new Trainee();
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        trainee.setUser(user);
        trainee.setDateOfBirth(request.getDateOfBirth());
        trainee.setAddress(request.getAddress());

        return ResponseEntity.status(200).body(traineeService.createTrainee(trainee));
    }

    @GetMapping("/{username}")
    @Operation(summary = "Profile getter")
    public ResponseEntity<TraineeProfileResponse> getTraineeProfile(
            @PathVariable("username") String username,
            Authentication authentication) {

        String authUsername = authentication.getName();

        if (!authUsername.equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<TraineeProfileResponse> trainee = traineeService.getTraineeProfile(username);
        return trainee.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{username}")
    @Operation(summary = "Update trainee profile")
    public ResponseEntity<TraineeProfileResponse> updateTraineeProfile(
            @PathVariable("username") String username,
            @Valid @RequestBody TraineeProfileResponse request,
            Authentication authentication) {

        String authUsername = authentication.getName();
        log.info("Trainee update requested for {}", username);

        if (!authUsername.equals(username) || !username.equals(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setActive(request.getIsActive());

        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setDateOfBirth(request.getDateOfBirth());
        trainee.setAddress(request.getAddress());

        Trainee updated = traineeService.updateTrainee(trainee);
        TraineeProfileResponse response = traineeService.getTraineeProfile(updated.getUser().getUsername()).orElseThrow();

        log.info("Trainee {} successfully updated", username);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{username}")
    @Operation(summary = "Delete trainee profile and cascade trainings")
    public ResponseEntity<Void> deleteTrainee(
            @PathVariable("username") String username,
            Authentication authentication) {

        String authUsername = authentication.getName();

        if (!authUsername.equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        traineeService.deleteTrainee(username);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{username}")
    @Operation(summary = "Activate or deactivate trainee")
    public ResponseEntity<Void> toggleActivationTrainee(
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
    @Operation(summary = "Get trainee trainings with filters")
    public ResponseEntity<List<Training>> getTraineeTrainings(
            @PathVariable("username") String username,
            @Valid @ModelAttribute TraineeTrainingsFilterRequest request,
            Authentication authentication) {

        String authUsername = authentication.getName();

        if (!authUsername.equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Training> trainings = trainingService.getTraineeTrainings(
                username,
                request.getFromDate(),
                request.getToDate(),
                request.getTrainerName(),
                request.getTrainingTypeName());
        return ResponseEntity.ok(trainings);
    }

    @GetMapping("/{username}/unassigned-trainers")
    @Operation(summary = "Get non-assigned active trainers for a trainee")
    public ResponseEntity<List<TrainerShortInfo>> getNotAssignedTrainers(
            @PathVariable("username") String username,
            Authentication authentication) {

        String authUsername = authentication.getName();

        if (!authUsername.equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<TrainerShortInfo> trainers = trainerService.getNotAssigned(username)
                .stream()
                .map(trainer -> {
                    TrainerShortInfo info = new TrainerShortInfo();
                    info.setUsername(trainer.getUser().getUsername());
                    info.setFirstName(trainer.getUser().getFirstName());
                    info.setLastName(trainer.getUser().getLastName());
                    info.setSpecialization(trainer.getSpecialization().toString());
                    return info;
                })
                .toList();

        return ResponseEntity.ok(trainers);
    }
}
