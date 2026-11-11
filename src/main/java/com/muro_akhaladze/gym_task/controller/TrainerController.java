package com.muro_akhaladze.gym_task.controller;

import com.muro_akhaladze.gym_task.dto.auth.RegistrationResponse;
import com.muro_akhaladze.gym_task.dto.user.ActivateUserRequest;
import com.muro_akhaladze.gym_task.dto.trainer.TrainerProfileResponse;
import com.muro_akhaladze.gym_task.dto.trainer.TrainerRegistrationRequest;
import com.muro_akhaladze.gym_task.dto.trainer.TrainerShortInfo;
import com.muro_akhaladze.gym_task.dto.trainer.TrainerUpdateRequest;
import com.muro_akhaladze.gym_task.dto.training.TrainerTrainingsFilterRequest;
import com.muro_akhaladze.gym_task.dto.training.TrainingResponse;
import com.muro_akhaladze.gym_task.security.AccessGuard;
import com.muro_akhaladze.gym_task.service.TrainerService;
import com.muro_akhaladze.gym_task.service.TrainingService;
import com.muro_akhaladze.gym_task.service.UserService;
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

@RestController
@RequestMapping("/api/v1/trainers")
@Tag(name = "Trainer Operations", description = "API endpoints for handling trainer-related actions")
public class TrainerController {
    private final TrainerService trainerService;
    private final UserService userService;
    private final TrainingService trainingService;
    private final AccessGuard accessGuard;

    // Constructor injection
    public TrainerController(TrainerService trainerService, UserService userService, TrainingService trainingService,
                             AccessGuard accessGuard) {
        this.trainerService = trainerService;
        this.userService = userService;
        this.trainingService = trainingService;
        this.accessGuard = accessGuard;
    }

    @PostMapping
    @Operation(summary = "Register a new trainer (public). Returns the generated username and password.")
    public ResponseEntity<RegistrationResponse> registerTrainer(@Valid @RequestBody TrainerRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainerService.register(request));
    }

    @GetMapping
    @Operation(summary = "List active trainers with server-side pagination and sorting",
            description = "Filter: specialization. Paging: page, size, sort=field,asc or sort=field,desc "
                    + "(fields: firstName, lastName, username, specialization)")
    public ResponseEntity<Page<TrainerShortInfo>> listTrainers(
            @RequestParam(required = false) String specialization,
            @ParameterObject @PageableDefault(size = 10, sort = "lastName", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(trainerService.listTrainers(specialization, pageable));
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get trainer profile (incl. assigned trainees)")
    public ResponseEntity<TrainerProfileResponse> getTrainerProfile(@PathVariable String username,
                                                                    Authentication authentication) {
        accessGuard.requireSelf(authentication, username);
        return ResponseEntity.ok(trainerService.getProfile(username));
    }

    @PutMapping("/{username}")
    @Operation(summary = "Update trainer profile")
    public ResponseEntity<TrainerProfileResponse> updateTrainerProfile(@PathVariable String username,
                                                                       @Valid @RequestBody TrainerUpdateRequest request,
                                                                       Authentication authentication) {
        accessGuard.requireSelf(authentication, username);
        return ResponseEntity.ok(trainerService.update(username, request));
    }

    @DeleteMapping("/{username}")
    @Operation(summary = "Delete trainer profile (also deletes its trainings, trainee links and photo)")
    public ResponseEntity<Void> deleteTrainer(@PathVariable String username, Authentication authentication) {
        accessGuard.requireSelf(authentication, username);
        trainerService.delete(username);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{username}")
    @Operation(summary = "Activate or deactivate trainer")
    public ResponseEntity<Void> toggleActivationTrainer(@PathVariable String username,
                                                        @Valid @RequestBody ActivateUserRequest request,
                                                        Authentication authentication) {
        accessGuard.requireSelf(authentication, username);
        userService.updateUserActivationStatus(username, request.getIsActive());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/trainings")
    @Operation(summary = "Trainer trainings with filters, pagination and sorting",
            description = "Filters: fromDate, toDate (yyyy-MM-dd), traineeName. Paging: page, size, sort=trainingDate,desc")
    public ResponseEntity<Page<TrainingResponse>> getTrainerTrainings(
            @PathVariable String username,
            @Valid @ParameterObject @ModelAttribute TrainerTrainingsFilterRequest filter,
            @ParameterObject @PageableDefault(size = 10, sort = "trainingDate", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        accessGuard.requireSelf(authentication, username);
        return ResponseEntity.ok(trainingService.getTrainerTrainings(username, filter, pageable));
    }
}
