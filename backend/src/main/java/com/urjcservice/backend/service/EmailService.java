package com.urjcservice.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

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
                                                 String place, String coordinates, 
                                                 Date startRaw, Date endRaw) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Booking confirmation - " + roomName);

            SimpleDateFormat printFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
            String startStr = printFormat.format(startRaw);
            String endStr = printFormat.format(endRaw);

            String locationText = (coordinates != null && !coordinates.isEmpty()) ? "See map (Coordinates)" : (place != null ? place : "Campus");

            String body = "Dear " + userName + ",\n\n" +
                          "Your reservation has been confirmed.\n" +
                          "Room: " + roomName + "\n" +
                          "Location: " + locationText + "\n" +
                          "Start: " + startStr + "\n" +
                          "End: " + endStr + "\n\n" +
                          "Attached you will find an .ics file to add it to your personal calendar.";
            
            helper.setText(body);

            String icsContent = generateIcsContent(roomName, place, coordinates, startRaw, endRaw);
            
            helper.addAttachment("reserva.ics", new ByteArrayResource(icsContent.getBytes(StandardCharsets.UTF_8)));

            mailSender.send(message);

        } catch (MessagingException e) {
            System.err.println("Error sending email with attachment: " + e.getMessage());
        }
    }



    public void sendVerificationEmail(String to, String userName, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Action Required: Confirm your StudySpace Reservation");
        //local npm start/ng serve
        //String verificationLink = "https://localhost:4200/verify-reservation?token=" + token;
        //docker
        String verificationLink = "https://localhost/verify-reservation?token=" + token;

        message.setText("Dear " + userName + ",\n\n" +
                        "You have requested a reservation on StudySpace.\n" +
                        "Please click the link below to confirm your booking and receive the calendar event:\n\n" +
                        verificationLink + "\n\n" +
                        "If you did not request this, please ignore this email.");
        
        mailSender.send(message);
    }

    private String generateIcsContent(String roomName, String place, String coordinates, Date start, Date end) {
        SimpleDateFormat icsFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        icsFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        String startIcs = icsFormat.format(start);
        String endIcs = icsFormat.format(end);
        String nowIcs = icsFormat.format(new Date());
        String uid = UUID.randomUUID().toString();

        String summary = escapeIcs("Reservation: " + roomName);
        String description = escapeIcs("Confirmed reservation at " + roomName + " via StudySpace.");
        
        //ubication
        String locationVal = "";
        String geoVal = null;

        if (coordinates != null && !coordinates.isEmpty()) {
            String cleanCoords = coordinates.replaceAll("\\s+", ""); 
            geoVal = cleanCoords.replace(",", ";");
            
            //visual coordenades
            locationVal = escapeIcs(coordinates);
        } else if (place != null && !place.isEmpty()) {
            locationVal = escapeIcs(place);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\r\n");
        sb.append("VERSION:2.0\r\n");
        sb.append("PRODID:-//StudySpace//Reserva//EN\r\n");
        sb.append("CALSCALE:GREGORIAN\r\n");
        sb.append("METHOD:PUBLISH\r\n");
        
        sb.append("BEGIN:VEVENT\r\n");
        sb.append("UID:").append(uid).append("\r\n");
        sb.append("DTSTAMP:").append(nowIcs).append("\r\n");
        sb.append("DTSTART:").append(startIcs).append("\r\n");
        sb.append("DTEND:").append(endIcs).append("\r\n");
        
        sb.append("SUMMARY:").append(summary).append("\r\n");
        sb.append("DESCRIPTION:").append(description).append("\r\n");
        
        if (!locationVal.isEmpty()) {
            sb.append("LOCATION:").append(locationVal).append("\r\n");
        }
        
        if (geoVal != null) {
            sb.append("GEO:").append(geoVal).append("\r\n");
        }

        sb.append("STATUS:CONFIRMED\r\n");
        sb.append("SEQUENCE:0\r\n");
        sb.append("END:VEVENT\r\n");
        sb.append("END:VCALENDAR\r\n");

        return sb.toString();
    }

    //to avoid incorrect caracters in the ICS breaking the format
    private String escapeIcs(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace(";", "\\;")
                    .replace(",", "\\,")
                    .replace("\n", "\\n");
    }


}