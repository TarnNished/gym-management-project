package com.muro_akhaladze.gym_task.controller;

import com.muro_akhaladze.gym_task.dto.auth.ChangePasswordRequest;
import com.muro_akhaladze.gym_task.dto.auth.LoginRequest;
import com.muro_akhaladze.gym_task.dto.auth.LoginResponse;
import com.muro_akhaladze.gym_task.security.CustomUserDetailsService;
import com.muro_akhaladze.gym_task.security.JwtService;
import com.muro_akhaladze.gym_task.security.LoginAttemptService;
import com.muro_akhaladze.gym_task.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User login and password management")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserService userService;
    private final LoginAttemptService loginAttemptService;

    @PostMapping("/login")
    @Operation(summary = "Login request", description = "Authenticates the user and returns a JWT token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String username = request.getUsername();

        if (loginAttemptService.isBlocked(username)) {
            log.warn("User '{}' is temporarily blocked due to failed login attempts", username);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new LoginResponse("User is temporarily blocked. Try again later."));
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            loginAttemptService.loginFailed(username);
            log.warn("Login failed for user: {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("Invalid username or password"));
        }

        loginAttemptService.loginSucceeded(username);

        UserDetails user = userDetailsService.loadUserByUsername(username);
        String jwt = jwtService.generateToken(user);

        log.info("Login successful for user: {}", username);
        return ResponseEntity.ok(new LoginResponse(jwt));
    }

    @PutMapping("/change-password")
    @Operation(
            summary = "Change password",
            description = "Allows user to change password"
    )
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("Password change requested for user: {}", request.getUsername());

        try {
            userService.updatePassword(
                    request.getUsername(),
                    request.getOldPassword(),
                    request.getNewPassword()
            );
            log.info("Password changed successfully for user: {}", request.getUsername());
            return ResponseEntity.ok("Password changed successfully");

        } catch (IllegalArgumentException ex) {
            log.warn("Password change failed for user: {} - Reason: {}", request.getUsername(), ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }


}
