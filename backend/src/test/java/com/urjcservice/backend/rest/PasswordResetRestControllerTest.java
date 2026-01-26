package com.urjcservice.backend.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urjcservice.backend.service.PasswordResetService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PasswordResetRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PasswordResetService passwordResetService;

    // --- FORGOT PASSWORD TESTS ---

    @Test
    @DisplayName("POST /forgot-password - Success")
    public void testForgotPassword_Success() throws Exception {
        String jsonRequest = "{\"email\": \"test@example.com\"}";

        doNothing().when(passwordResetService).processForgotPassword(anyString());

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /forgot-password - Invalid Email Format")
    public void testForgotPassword_InvalidEmail() throws Exception {
        String jsonRequest = "{\"email\": \"\"}";//empty json

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest()); 
    }

    @Test
    @DisplayName("POST /forgot-password - Service Error (User not found)")
    public void testForgotPassword_ServiceError() throws Exception {
        String jsonRequest = "{\"email\": \"unknown@example.com\"}";

        doThrow(new RuntimeException("User not found"))
                .when(passwordResetService).processForgotPassword(anyString());

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILURE"))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    // --- RESET PASSWORD TESTS ---

    @Test
    @DisplayName("POST /reset-password - Success")
    public void testResetPassword_Success() throws Exception {
        String jsonRequest = """
            {
                "token": "valid-token-123",
                "newPassword": "StrongPassword1!"
            }
        """;

        doNothing().when(passwordResetService).processResetPassword(anyString(), anyString());

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("POST /reset-password - Weak Password Validation")
    public void testResetPassword_WeakPassword() throws Exception {
        String jsonRequest = """
            {
                "token": "valid-token",
                "newPassword": "weak"
            }
        """;

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /reset-password - Expired/Invalid Token")
    public void testResetPassword_InvalidToken() throws Exception {
        String jsonRequest = """
            {
                "token": "invalid-token",
                "newPassword": "StrongPassword1!"
            }
        """;

        doThrow(new RuntimeException("Invalid token"))
                .when(passwordResetService).processResetPassword(anyString(), anyString());

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILURE"));
    }
}