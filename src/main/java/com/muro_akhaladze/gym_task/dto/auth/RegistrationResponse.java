package com.muro_akhaladze.gym_task.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Returned once after registration: the generated username and the plain password. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationResponse {
    private String username;
    private String password;
}
