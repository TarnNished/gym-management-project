package com.muro_akhaladze.gym_task.controller;

import com.muro_akhaladze.gym_task.dto.auth.ChangePasswordRequest;
import com.muro_akhaladze.gym_task.dto.auth.LoginRequest;
import com.muro_akhaladze.gym_task.dto.auth.LoginResponse;
import com.muro_akhaladze.gym_task.security.AccessGuard;
import com.muro_akhaladze.gym_task.service.AuthService;
import com.muro_akhaladze.gym_task.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User login and password management")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final AccessGuard accessGuard;

    // Constructor injection
    public AuthController(AuthService authService, UserService userService, AccessGuard accessGuard) {
        this.authService = authService;
        this.userService = userService;
        this.accessGuard = accessGuard;
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates the user and returns a JWT token (use it as 'Authorization: Bearer <token>')")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        String token = authService.login(request.getUsername(), request.getPassword(), http.getRemoteAddr());
        return ResponseEntity.ok(new LoginResponse(token, "Login successful"));
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
