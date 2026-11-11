package com.muro_akhaladze.gym_task.service;

import com.muro_akhaladze.gym_task.dto.trainingType.TrainingTypeDto;
import com.muro_akhaladze.gym_task.entity.TrainingType;
import com.muro_akhaladze.gym_task.repository.TrainingTypeRepo;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainingTypeService {
    private final TrainingTypeRepo trainingTypeRepo;

    // Constructor injection
    public TrainingTypeService(TrainingTypeRepo trainingTypeRepo) {
        this.trainingTypeRepo = trainingTypeRepo;
    }

    /** Creates the training type unless one with that name (case-insensitive) already exists. */
    @Transactional
    public void createIfMissing(String name) {
        if (trainingTypeRepo.findByTrainingTypeNameIgnoreCase(name).isEmpty()) {
            trainingTypeRepo.save(new TrainingType(name));
        }
    }

    @Transactional(readOnly = true)
    public List<TrainingTypeDto> getAll() {
        return trainingTypeRepo.findAll(Sort.by("trainingTypeName")).stream()
                .map(type -> new TrainingTypeDto(type.getTrainingTypeId(), type.getTrainingTypeName()))
                .toList();
    }
}
