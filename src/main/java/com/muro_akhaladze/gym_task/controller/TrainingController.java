package com.muro_akhaladze.gym_task.controller;

import com.muro_akhaladze.gym_task.dto.training.AddTrainingRequest;
import com.muro_akhaladze.gym_task.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/training")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Training Operations", description = "API endpoints for handling training-related actions")
public class TrainingController {

    private final TrainingService trainingService;

    @PostMapping
    @Operation(summary = "Add a new training")
    public ResponseEntity<Void> addTraining(
            @Valid @RequestBody AddTrainingRequest request,
            Authentication authentication) {

        String authUsername = authentication.getName();

        if (!authUsername.equals(request.getTrainerUsername()) &&
                !authUsername.equals(request.getTraineeUsername())) {
            return ResponseEntity.status(403).build();
        }

        trainingService.createTraining(request);
        return ResponseEntity.ok().build();
    }
}
