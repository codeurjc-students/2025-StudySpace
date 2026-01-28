package com.urjcservice.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendResetPasswordEmail(String to, String resetUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Password reset");
        message.setText("Click the following link to reset your password:\n" + resetUrl + 
                        "\n\nThis link will expire in 15 minutes.");
        mailSender.send(message);
    }

    public void sendReservationReminder(String to, String userName, String roomName, String startTime) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Reminder: Your reservation starts in 15 minutes");
        message.setText("Dear " + userName + ",\n\n" +
                        "We remind you that your reservation in the room " + roomName + 
                        " Is about to begin (Start time: " + startTime + ").\n\n" +
                        "Have a good study session!");
        mailSender.send(message);
    }
}