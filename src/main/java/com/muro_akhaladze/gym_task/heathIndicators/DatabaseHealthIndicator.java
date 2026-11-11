package com.muro_akhaladze.gym_task.heathIndicators;

import com.muro_akhaladze.gym_task.repository.TraineeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {

    private final TraineeRepo traineeRepo;

    @Override
    public Health health() {
        try {
            long count = traineeRepo.count();
            return Health.up()
                    .withDetail("custom-db-check", "Database is reachable")
                    .withDetail("trainee-count", count)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("custom-db-check", "Database connection failed")
                    .withException(e)
                    .build();
        }
    }
}
