package com.muro_akhaladze.gym_task.controller;

import com.muro_akhaladze.gym_task.dto.training.AddTrainingRequest;
import com.muro_akhaladze.gym_task.dto.training.TrainingResponse;
import com.muro_akhaladze.gym_task.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trainings")
@Tag(name = "Training Operations", description = "API endpoints for handling training-related actions")
public class TrainingController {
    private final TrainingService trainingService;

    // Constructor injection
    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @PostMapping
    @Operation(summary = "Add a new training (caller must be the trainee or the trainer)")
    public ResponseEntity<TrainingResponse> addTraining(@Valid @RequestBody AddTrainingRequest request,
                                                        Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(trainingService.createTraining(request, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a training (caller must be the trainee or the trainer)")
    public ResponseEntity<Void> deleteTraining(@PathVariable int id, Authentication authentication) {
        trainingService.deleteTraining(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
