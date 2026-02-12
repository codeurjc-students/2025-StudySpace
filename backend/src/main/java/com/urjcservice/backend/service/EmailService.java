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

    public void sendReservationModificationEmail(String to, String userName, String newRoomName, 
                                                 String newDate, String newStartTime,String newEndTime, String reason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Notice: Your reservation has been modified by an administrator");
        message.setText("Dear " + userName + ",\n\n" +
                        "We inform you that an administrator has modified your reservation.\n\n" +
                        "REASON: " + reason + "\n\n" +
                        "--- NEW DETAILS ---\n" +
                        "Room: " + newRoomName + "\n" +
                        "Date: " + newDate + "\n" +
                        "Start hour: " + newStartTime + "\n" +
                        "End hour: " + newEndTime + "\n\n" +
                        "If you have any questions, please contact us: studyspacetfg@gmail.com");
        
        mailSender.send(message);
    }

    public void sendReservationCancellationEmail(String to, String userName, String roomName, 
                                                 String date, String startTime,String endTime, String reason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Notice: Your reservation has been CANCELLED by an administrator");
        message.setText("Dear " + userName + ",\n\n" +
                        "We regret to inform you that an administrator has cancelled your reservation.\n\n" +
                        "REASON FOR CANCELLATION: " + reason + "\n\n" +
                        "--- DETAILS OF THE CANCELLED RESERVATION ---\n" +
                        "Room: " + roomName + "\n" +
                        "Date: " + date + "\n" +
                        "Start hour: " + startTime + "\n" +
                        "End hour: " + endTime + "\n\n" +
                        "We apologize for any inconvenience this may cause");
        
        mailSender.send(message);
    }

    public void sendReservationConfirmationEmail(String to, String userName, String roomName, 
                                                 String date, String startTime, String endTime) {
        
                        //complete later                            
        System.out.println("Mock sending email to: " + to);
    }


}