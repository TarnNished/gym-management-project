package com.muro_akhaladze.gym_task.controller;

import com.muro_akhaladze.gym_task.dto.trainingType.TrainingTypeDto;
import com.muro_akhaladze.gym_task.repository.TrainingTypeRepo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/training-types")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Training type Operations", description = "API endpoints for handling trainingType-related actions")
public class TrainingTypeController {
    private final TrainingTypeRepo trainingTypeRepo;

    @GetMapping
    @Operation(summary = "Get all training types")
    public ResponseEntity<List<TrainingTypeDto>> getAllTrainingTypes() {
        List<TrainingTypeDto> types = trainingTypeRepo.findAll().stream()
                .map(type -> new TrainingTypeDto(type.getTrainingTypeId(), type.getTrainingTypeName()))
                .toList();

        return ResponseEntity.ok(types);
    }
}
