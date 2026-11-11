package com.muro_akhaladze.gym_task.heathIndicators;

import com.muro_akhaladze.gym_task.repository.TrainingRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrainingLoadHealthIndicator implements HealthIndicator {

    private final TrainingRepo trainingRepo;

    @Override
    public Health health() {
        long trainingCount = trainingRepo.count();

        if (trainingCount < 100) {
            return Health.up()
                    .withDetail("training-load", trainingCount)
                    .build();
        } else {
            return Health.status("WARN")
                    .withDetail("training-load", trainingCount)
                    .withDetail("message", "Training load is getting high")
                    .build();
        }
    }
}
