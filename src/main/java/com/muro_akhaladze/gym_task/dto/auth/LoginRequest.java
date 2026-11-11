package com.muro_akhaladze.gym_task.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @NotBlank(message = "username must not be blank")
    private String username;
    @NotBlank(message = "Password must not be blank")
    private String password;
}
