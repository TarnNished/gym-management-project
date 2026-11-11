package com.muro_akhaladze.gym_task.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 3;
    private static final long BLOCK_DURATION_MILLIS = 5 * 60 * 1000;

    private record Attempt(int count, long lastFailedTime) {}

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public void loginSucceeded(String username) {
        attempts.remove(username);
    }

    public void loginFailed(String username) {
        Attempt current = attempts.getOrDefault(username, new Attempt(0, 0));
        attempts.put(username, new Attempt(current.count + 1, Instant.now().toEpochMilli()));
    }

    public boolean isBlocked(String username) {
        Attempt attempt = attempts.get(username);
        if (attempt == null) return false;

        boolean isMaxed = attempt.count >= MAX_ATTEMPTS;
        boolean stillBlocked = Instant.now().toEpochMilli() - attempt.lastFailedTime < BLOCK_DURATION_MILLIS;

        return isMaxed && stillBlocked;
    }
}
