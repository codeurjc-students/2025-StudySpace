package com.urjcservice.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@urjc.es");
    }

    @Test
    @DisplayName("Should send reset password email with correct format")
    void testSendResetPasswordEmail() {
        String to = "user@example.com";
        String resetUrl = "https://localhost:4200/reset-password?token=12345";

        // Act
        emailService.sendResetPasswordEmail(to, resetUrl);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        assertEquals("test@urjc.es", sentMessage.getFrom());
        assertEquals(to, sentMessage.getTo()[0]);
        assertEquals("Password reset", sentMessage.getSubject());
        assertTrue(sentMessage.getText().contains(resetUrl));
    }


    @Test
    @DisplayName("Should send reservation reminder email with correct content")
    void testSendReservationReminder() {
        // Arrange
        String to = "student@urjc.es";
        String userName = "Alex";
        String roomName = "Lab 1";
        String startTime = "10:30";

        // Act
        emailService.sendReservationReminder(to, userName, roomName, startTime);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertEquals("test@urjc.es", sentMessage.getFrom());
        assertEquals(to, sentMessage.getTo()[0]);
        assertEquals("Reminder: Your reservation starts in 15 minutes", sentMessage.getSubject());
        
        //verify
        String body = sentMessage.getText();
        assertTrue(body.contains("Alex"));
        assertTrue(body.contains("Lab 1"));
        assertTrue(body.contains("10:30"));
    }

}