package com.muro_akhaladze.gym_task.heathIndicators;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class CustomMetrics {

    private final Counter loginCounter;
    private final Counter trainingCreationCounter;

    public CustomMetrics(MeterRegistry registry) {
        this.loginCounter = Counter.builder("login_success_counter")
                .description("Counts successful logins")
                .register(registry);

        this.trainingCreationCounter = Counter.builder("training_created_counter")
                .description("Counts new trainings")
                .register(registry);
    }

    public void countLoginSuccess() {
        loginCounter.increment();
    }

    public void countTrainingCreated() {
        trainingCreationCounter.increment();
    }
}
