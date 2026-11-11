package com.muro_akhaladze.gym_task.service;

import com.muro_akhaladze.gym_task.exceptions.ApiException;
import com.muro_akhaladze.gym_task.monitoring.CustomMetrics;
import com.muro_akhaladze.gym_task.security.JwtService;
import com.muro_akhaladze.gym_task.security.LoginAttemptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/** Login business logic: brute-force lockout, credential check and JWT creation. */
@Service
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;
    private final CustomMetrics metrics;

    // Constructor injection
    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       LoginAttemptService loginAttemptService,
                       CustomMetrics metrics) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
        this.metrics = metrics;
    }

    /**
     * Checks the credentials and returns a signed JWT.
     *
     * @throws ApiException 429 when this username is locked for this IP, 403 when the account is
     *                      deactivated, 401 when the username/password is wrong
     */
    public String login(String username, String password, String clientIp) {
        if (loginAttemptService.isBlocked(username, clientIp)) {
            log.warn("User '{}' is temporarily blocked for {} after failed logins", username, clientIp);
            throw new ApiException("TOO_MANY_ATTEMPTS", "Too many failed logins. Try again in 5 minutes.",
                    HttpStatus.TOO_MANY_REQUESTS);
        }

        Authentication auth;
        try {
            auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException ex) {
            throw new ApiException("ACCOUNT_DISABLED", "Account is deactivated", HttpStatus.FORBIDDEN);
        } catch (AuthenticationException ex) {
            loginAttemptService.loginFailed(username, clientIp);
            log.warn("Login failed for user: {}", username);
            throw new ApiException("INVALID_CREDENTIALS", "Invalid username or password", HttpStatus.UNAUTHORIZED);
        }

        loginAttemptService.loginSucceeded(username, clientIp);
        metrics.countLoginSuccess();
        log.info("Login successful for user: {}", username);
        return jwtService.generateToken((UserDetails) auth.getPrincipal());
    }
}
