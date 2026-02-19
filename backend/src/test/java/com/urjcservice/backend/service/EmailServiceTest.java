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
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Session;
import jakarta.mail.Message;
import java.util.Date;
import static org.mockito.Mockito.when;

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




    @Test
    @DisplayName("Should send modification email with end time and reason")
    void testSendReservationModificationEmail() {
        // Arrange
        String to = "student@test.com";
        String userName = "Alex";
        String roomName = "Lab 3";
        String date = "2026-03-15";
        String startTime = "10:00";
        String endTime = "12:00"; 
        String reason = "Maintenance works";

        // Act
        emailService.sendReservationModificationEmail(to, userName, roomName, date, startTime, endTime, reason);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        String body = sentMessage.getText();

        assertEquals(to, sentMessage.getTo()[0]);
        assertTrue(sentMessage.getSubject().contains("modified")); 
        assertTrue(body.contains(reason));
        assertTrue(body.contains(endTime)); 
        assertTrue(body.contains(roomName));
    }

    @Test
    @DisplayName("Should send cancellation email with reason and details")
    void testSendReservationCancellationEmail() {
        // Arrange
        String to = "student@test.com";
        String reason = "Classroom closure";
        
        // Act
        emailService.sendReservationCancellationEmail(
            to, "Alex", "Lab 1", "2026-03-20", "09:00", "11:00", reason
        );

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage msg = messageCaptor.getValue();
        
        assertTrue(msg.getSubject().contains("CANCELLED"));
        assertTrue(msg.getText().contains(reason));
        assertTrue(msg.getText().contains("11:00")); 
    }


    @Test
    @DisplayName("Should send reservation confirmation email with ICS attachment")
    void testSendReservationConfirmationEmail() throws Exception {
        // Arrange
        String to = "user@test.com";
        String userName = "John Doe";
        String roomName = "Lab A";
        String place = "Building 1";      
        String coords = "40.5,-3.5";      
        Date start = new Date();
        Date end = new Date(System.currentTimeMillis() + 3600000); 

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        emailService.sendReservationConfirmationEmail(to, userName, roomName, place, coords, start, end);

        // Assert
        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        MimeMessage sentMessage = messageCaptor.getValue();
        assertEquals(to, sentMessage.getRecipients(Message.RecipientType.TO)[0].toString());
        assertEquals("Booking confirmation - " + roomName, sentMessage.getSubject());
    }


    @Test
    @DisplayName("Should send verification email with link")
    void testSendVerificationEmail() {
        // Arrange
        String to = "user@test.com";
        String userName = "Alice";
        String token = "abcdef-123456";

        // Act
        emailService.sendVerificationEmail(to, userName, token);

        // Assert
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sentMsg = captor.getValue();
        assertEquals(to, sentMsg.getTo()[0]);
        assertTrue(sentMsg.getSubject().contains("Action Required"));
        
        String body = sentMsg.getText();
        // Verify token
        assertTrue(body.contains(token), "The email body must contain the token");
        // Verify route 
        assertTrue(body.contains("verify-reservation"), "The body must contain the path verify-reservation");
    }
    

}