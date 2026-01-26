package com.urjcservice.backend.rest;

import com.urjcservice.backend.security.jwt.AuthResponse;
import com.urjcservice.backend.service.PasswordResetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetRestController {

    private final PasswordResetService passwordResetService;

    public PasswordResetRestController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    // DTOs internos para mantener tu estilo
    public static class ForgotPasswordRequest {
        @NotBlank(message = "Email cannot be empty")
        private String email;
        
        // Getters y Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class ResetPasswordRequest {
        @NotBlank(message = "Token is required")
        private String token;

        @NotBlank(message = "Password cannot be empty")
        @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@$!%*?&.])(?=\\S+$).{8,}$",
            message = "Password must have at least 8 chars: 1 uppercase, 1 lowercase, 1 number, 1 special char"
        )
        private String newPassword;

        // Getters y Setters
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            passwordResetService.processForgotPassword(request.getEmail());
            return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, 
                "If the email exists, a reset link has been sent."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthResponse(AuthResponse.Status.FAILURE, e.getMessage()));//400
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            passwordResetService.processResetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, 
                "Password successfully updated."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthResponse(AuthResponse.Status.FAILURE, e.getMessage()));
        }
    }
}