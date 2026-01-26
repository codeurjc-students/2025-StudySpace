package com.urjcservice.backend.service;

import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordResetService, "frontendUrl", "https://localhost:4200");
        
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
    }

    @Test
    @DisplayName("Process Forgot Password - User Found")
    void testProcessForgotPassword_Success() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        passwordResetService.processForgotPassword("test@example.com");

        // Assert
        assertNotNull(mockUser.getResetPasswordToken());
        assertNotNull(mockUser.getResetPasswordTokenExpiry());
        verify(userRepository).save(mockUser);
        verify(emailService).sendResetPasswordEmail(eq("test@example.com"), contains("token="));
    }

    @Test
    @DisplayName("Process Forgot Password - User Not Found")
    void testProcessForgotPassword_UserNotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            passwordResetService.processForgotPassword("nonexistent@example.com");
        });
        
        assertTrue(exception.getMessage().contains("User not found"));
        verify(emailService, never()).sendResetPasswordEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Process Reset Password - Success")
    void testProcessResetPassword_Success() {
        // Arrange
        String token = "valid-token";
        String newPassword = "NewStrongPassword1!";
        
        mockUser.setResetPasswordToken(token);
        mockUser.setResetPasswordTokenExpiry(LocalDateTime.now().plusMinutes(10)); 
        
        when(userRepository.findByResetPasswordToken(token)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedPassword");

        // Act
        passwordResetService.processResetPassword(token, newPassword);

        // Assert
        assertNull(mockUser.getResetPasswordToken()); 
        assertNull(mockUser.getResetPasswordTokenExpiry());
        assertEquals("encodedPassword", mockUser.getEncodedPassword());
        verify(userRepository).save(mockUser);
    }

    @Test
    @DisplayName("Process Reset Password - Token Expired")
    void testProcessResetPassword_ExpiredToken() {
        // Arrange
        String token = "expired-token";
        mockUser.setResetPasswordToken(token);
        mockUser.setResetPasswordTokenExpiry(LocalDateTime.now().minusMinutes(1));

        when(userRepository.findByResetPasswordToken(token)).thenReturn(Optional.of(mockUser));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            passwordResetService.processResetPassword(token, "anyPass");
        });

        assertEquals("The reset link has expired", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}