package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muro_akhaladze.gym_task.controller.AuthController;
import com.muro_akhaladze.gym_task.dto.auth.ChangePasswordRequest;
import com.muro_akhaladze.gym_task.dto.auth.LoginRequest;
import com.muro_akhaladze.gym_task.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ContextConfiguration(classes = com.muro_akhaladze.gym_task.Main.class)
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Login success returns 200")
    void loginSuccess() throws Exception {
        LoginRequest request = new LoginRequest("john", "password123");

        Mockito.when(userService.isInvalidPassword("john", "password123"))
                .thenReturn(false);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Login successful"));
    }

    @Test
    @DisplayName("Login failure returns 401")
    void loginFailure() throws Exception {
        LoginRequest request = new LoginRequest("john", "wrongpass");

        Mockito.when(userService.isInvalidPassword("john", "wrongpass"))
                .thenReturn(true);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid username or password"));
    }

    @Test
    @DisplayName("Change password success returns 200")
    void changePasswordSuccess() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("john", "oldpass", "newpass");

        Mockito.doNothing().when(userService).updatePassword("john", "oldpass", "newpass");

        mockMvc.perform(put("/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password changed successfully"));
    }

    @Test
    @DisplayName("Change password failure returns 400")
    void changePasswordFailure() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("john", "wrongold", "newpass");

        Mockito.doThrow(new IllegalArgumentException("Old password is incorrect"))
                .when(userService).updatePassword("john", "wrongold", "newpass");

        mockMvc.perform(put("/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Old password is incorrect"));
    }
}
