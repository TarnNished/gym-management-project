package com.muro_akhaladze.gym_task.controller;

import com.muro_akhaladze.gym_task.dto.trainingType.TrainingTypeDto;
import com.muro_akhaladze.gym_task.service.TrainingTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/training-types")
@Tag(name = "Training type Operations", description = "API endpoints for handling trainingType-related actions")
public class TrainingTypeController {
    private final TrainingTypeService trainingTypeService;

    // Constructor injection
    public TrainingTypeController(TrainingTypeService trainingTypeService) {
        this.trainingTypeService = trainingTypeService;
    }

    @GetMapping
    @Operation(summary = "Get all training types (public)")
    public ResponseEntity<List<TrainingTypeDto>> getAllTrainingTypes() {
        return ResponseEntity.ok(trainingTypeService.getAll());
    }
}
