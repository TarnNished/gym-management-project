package com.muro_akhaladze.gym_task.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Brute-force protection: after 3 failed logins for the same username FROM THE SAME IP,
 * that username is blocked for 5 minutes for that IP only. Keying on the IP as well means
 * an attacker cannot lock a real user out of their own account from another machine.
 */
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 3;
    private static final long BLOCK_DURATION_MILLIS = 5 * 60 * 1000;

    private record Attempt(int count, long lastFailedTime) {}

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    private static String key(String username, String clientIp) {
        return username.toLowerCase() + "|" + clientIp;
    }

    public void loginSucceeded(String username, String clientIp) {
        attempts.remove(key(username, clientIp));
    }

    public void loginFailed(String username, String clientIp) {
        attempts.merge(key(username, clientIp), new Attempt(1, Instant.now().toEpochMilli()),
                (old, next) -> new Attempt(old.count() + 1, next.lastFailedTime()));
    }

    public boolean isBlocked(String username, String clientIp) {
        Attempt attempt = attempts.get(key(username, clientIp));
        if (attempt == null) {
            return false;
        }
        boolean stillBlocked = Instant.now().toEpochMilli() - attempt.lastFailedTime() < BLOCK_DURATION_MILLIS;
        if (!stillBlocked) {
            attempts.remove(key(username, clientIp));
            return false;
        }
        return attempt.count() >= MAX_ATTEMPTS;
    }
}
