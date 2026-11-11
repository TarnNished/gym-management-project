package com.muro_akhaladze.gym_task.security;

import com.muro_akhaladze.gym_task.exceptions.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/** A user may only read or change their own resources. */
@Component
public class AccessGuard {

    public void requireSelf(Authentication authentication, String username) {
        if (authentication == null || !authentication.getName().equals(username)) {
            throw new ApiException("FORBIDDEN", "You can only access your own profile", HttpStatus.FORBIDDEN);
        }
    }
}
