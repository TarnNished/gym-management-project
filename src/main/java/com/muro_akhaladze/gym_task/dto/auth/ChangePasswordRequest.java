package com.muro_akhaladze.gym_task.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {
    @NotBlank(message = "username must not be blank")
    private String username;

    @NotBlank(message = "password must not be blank")
    private String oldPassword;

    @NotBlank(message = "new password must not be blank")
    @Size(min = 8, max = 64, message = "new password must be 8-64 characters long")
    private String newPassword;
}
