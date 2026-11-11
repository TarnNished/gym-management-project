package com.muro_akhaladze.gym_task.controller;

import com.muro_akhaladze.gym_task.dto.auth.RegistrationResponse;
import com.muro_akhaladze.gym_task.dto.trainee.TraineeProfileResponse;
import com.muro_akhaladze.gym_task.dto.trainee.TraineeRegistrationRequest;
import com.muro_akhaladze.gym_task.dto.trainee.TraineeUpdateRequest;
import com.muro_akhaladze.gym_task.dto.trainee.UpdateTrainersRequest;
import com.muro_akhaladze.gym_task.dto.trainer.TrainerShortInfo;
import com.muro_akhaladze.gym_task.dto.training.TraineeTrainingsFilterRequest;
import com.muro_akhaladze.gym_task.dto.training.TrainingResponse;
import com.muro_akhaladze.gym_task.security.AccessGuard;
import com.muro_akhaladze.gym_task.service.TraineeService;
import com.muro_akhaladze.gym_task.service.TrainingService;
import com.muro_akhaladze.gym_task.service.UserService;
import com.muro_akhaladze.gym_task.dto.user.ActivateUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trainees")
@Tag(name = "Trainee Operations", description = "API endpoints for handling trainee-related actions")
public class TraineeController {
    private final TraineeService traineeService;
    private final UserService userService;
    private final TrainingService trainingService;
    private final AccessGuard accessGuard;

    // Constructor injection
    public TraineeController(TraineeService traineeService, UserService userService, TrainingService trainingService,
                             AccessGuard accessGuard) {
        this.traineeService = traineeService;
        this.userService = userService;
        this.trainingService = trainingService;
        this.accessGuard = accessGuard;
    }

    @PostMapping
    @Operation(summary = "Register a new trainee (public). Returns the generated username and password.")
    public ResponseEntity<RegistrationResponse> registerTrainee(@Valid @RequestBody TraineeRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(traineeService.register(request));
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get trainee profile (incl. assigned trainers)")
    public ResponseEntity<TraineeProfileResponse> getTraineeProfile(@PathVariable String username,
                                                                    Authentication authentication) {
        accessGuard.requireSelf(authentication, username);
        return ResponseEntity.ok(traineeService.getProfile(username));
    }

    @PutMapping("/{username}")
    @Operation(summary = "Update trainee profile")
    public ResponseEntity<TraineeProfileResponse> updateTraineeProfile(@PathVariable String username,
                                                                       @Valid @RequestBody TraineeUpdateRequest request,
                                                                       Authentication authentication) {
        accessGuard.requireSelf(authentication, username);
        return ResponseEntity.ok(traineeService.update(username, request));
    }

    @DeleteMapping("/{username}")
    @Operation(summary = "Delete trainee profile (also deletes trainings, trainer links and photo)")
    public ResponseEntity<Void> deleteTrainee(@PathVariable String username, Authentication authentication) {
        accessGuard.requireSelf(authentication, username);
        traineeService.delete(username);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{username}")
    @Operation(summary = "Activate or deactivate trainee")
    public ResponseEntity<Void> toggleActivationTrainee(@PathVariable String username,
                                                        @Valid @RequestBody ActivateUserRequest request,
                                                        Authentication authentication) {
        accessGuard.requireSelf(authentication, username);
        userService.updateUserActivationStatus(username, request.getIsActive());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{username}/trainers")
    @Operation(summary = "Replace the trainee's assigned trainers (many-to-many)")
    public ResponseEntity<List<TrainerShortInfo>> updateTrainers(@PathVariable String username,
                                                                 @Valid @RequestBody UpdateTrainersRequest request,
                                                                 Authentication authentication) {
        accessGuard.requireSelf(authentication, username);
        return ResponseEntity.ok(traineeService.updateTrainers(username, request.getTrainerUsernames()));
    }

    @GetMapping("/{username}/trainings")
    @Operation(summary = "Trainee trainings with filters, pagination and sorting",
            description = "Filters: fromDate, toDate (yyyy-MM-dd), trainerName, trainingTypeName. "
                    + "Paging: page, size, sort=trainingDate,desc")
    public ResponseEntity<Page<TrainingResponse>> getTraineeTrainings(
            @PathVariable String username,
            @Valid @ParameterObject @ModelAttribute TraineeTrainingsFilterRequest filter,
            @ParameterObject @PageableDefault(size = 10, sort = "trainingDate", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        accessGuard.requireSelf(authentication, username);
        return ResponseEntity.ok(trainingService.getTraineeTrainings(username, filter, pageable));
    }

    @GetMapping("/{username}/unassigned-trainers")
    @Operation(summary = "Active trainers not yet assigned to the trainee")
    public ResponseEntity<List<TrainerShortInfo>> getNotAssignedTrainers(@PathVariable String username,
                                                                         Authentication authentication) {
        accessGuard.requireSelf(authentication, username);
        return ResponseEntity.ok(traineeService.getUnassignedTrainers(username));
    }
}
