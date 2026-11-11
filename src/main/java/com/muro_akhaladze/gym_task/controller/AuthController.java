package com.muro_akhaladze.gym_task.controller;

import com.muro_akhaladze.gym_task.dto.auth.ChangePasswordRequest;
import com.muro_akhaladze.gym_task.dto.auth.LoginRequest;
import com.muro_akhaladze.gym_task.dto.auth.LoginResponse;
import com.muro_akhaladze.gym_task.exceptions.ApiException;
import com.muro_akhaladze.gym_task.monitoring.CustomMetrics;
import com.muro_akhaladze.gym_task.security.AccessGuard;
import com.muro_akhaladze.gym_task.security.JwtService;
import com.muro_akhaladze.gym_task.security.LoginAttemptService;
import com.muro_akhaladze.gym_task.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor // generates the constructor used for constructor injection
@Slf4j
@Tag(name = "Authentication", description = "User login and password management")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final LoginAttemptService loginAttemptService;
    private final AccessGuard accessGuard;
    private final CustomMetrics metrics;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates the user and returns a JWT token (use it as 'Authorization: Bearer <token>')")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        String username = request.getUsername();
        String clientIp = http.getRemoteAddr();

        if (loginAttemptService.isBlocked(username, clientIp)) {
            log.warn("User '{}' is temporarily blocked for {} after failed logins", username, clientIp);
            throw new ApiException("TOO_MANY_ATTEMPTS", "Too many failed logins. Try again in 5 minutes.",
                    HttpStatus.TOO_MANY_REQUESTS);
        }

        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword()));
        } catch (DisabledException ex) {
            throw new ApiException("ACCOUNT_DISABLED", "Account is deactivated", HttpStatus.FORBIDDEN);
        } catch (AuthenticationException ex) {
            loginAttemptService.loginFailed(username, clientIp);
            log.warn("Login failed for user: {}", username);
            throw new ApiException("INVALID_CREDENTIALS", "Invalid username or password", HttpStatus.UNAUTHORIZED);
        }

        loginAttemptService.loginSucceeded(username, clientIp);
        metrics.countLoginSuccess();
        String jwt = jwtService.generateToken((UserDetails) auth.getPrincipal());
        log.info("Login successful for user: {}", username);
        return ResponseEntity.ok(new LoginResponse(jwt, "Login successful"));
    }

    @PutMapping("/change-password")
    @Operation(summary = "Change password", description = "Requires a JWT; you can only change your own password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                 Authentication authentication) {
        accessGuard.requireSelf(authentication, request.getUsername());
        userService.updatePassword(request.getUsername(), request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok("Password changed successfully");
    }
}
