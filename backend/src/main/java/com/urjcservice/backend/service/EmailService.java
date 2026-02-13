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
                                                 String place, String coordinates, // <--- NUEVOS PARÁMETROS
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

            // Texto del correo
            String locationText = (coordinates != null && !coordinates.isEmpty()) ? "See map (Coordinates)" : (place != null ? place : "Campus");

            String body = "Dear " + userName + ",\n\n" +
                          "Your reservation has been confirmed.\n" +
                          "Room: " + roomName + "\n" +
                          "Location: " + locationText + "\n" +
                          "Start: " + startStr + "\n" +
                          "End: " + endStr + "\n\n" +
                          "Attached you will find an .ics file to add it to your personal calendar.";
            
            helper.setText(body);

            // Generamos el ICS con la nueva lógica
            String icsContent = generateIcsContent(roomName, place, coordinates, startRaw, endRaw);
            
            helper.addAttachment("reserva.ics", new ByteArrayResource(icsContent.getBytes(StandardCharsets.UTF_8)));

            mailSender.send(message);

        } catch (MessagingException e) {
            System.err.println("Error sending email with attachment: " + e.getMessage());
        }
    }

    private String generateIcsContent(String roomName, String place, String coordinates, Date start, Date end) {
        // 1. Fechas en UTC estricto
        SimpleDateFormat icsFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        icsFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        String startIcs = icsFormat.format(start);
        String endIcs = icsFormat.format(end);
        String nowIcs = icsFormat.format(new Date());
        String uid = UUID.randomUUID().toString();

        // 2. Preparación de textos
        String summary = escapeIcs("Reservation: " + roomName);
        String description = escapeIcs("Confirmed reservation at " + roomName + " via StudySpace.");
        
        // 3. Ubicación y GEO
        String locationVal = "";
        String geoVal = null;

        if (coordinates != null && !coordinates.isEmpty()) {
            // LIMPIEZA CRÍTICA: Quitamos espacios para cumplir el estándar GEO (lat;long)
            // Ejemplo entrada: "40.3, -3.8" -> Limpio: "40.3,-3.8" -> Replace: "40.3;-3.8"
            String cleanCoords = coordinates.replaceAll("\\s+", ""); 
            geoVal = cleanCoords.replace(",", ";");
            
            // En LOCATION dejamos las coordenadas visuales
            locationVal = escapeIcs(coordinates);
        } else if (place != null && !place.isEmpty()) {
            locationVal = escapeIcs(place);
        }

        // 4. Construcción del archivo
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
        
        // Solo añadimos GEO si hemos logrado limpiarlo correctamente
        if (geoVal != null) {
            sb.append("GEO:").append(geoVal).append("\r\n");
        }

        sb.append("STATUS:CONFIRMED\r\n");
        sb.append("SEQUENCE:0\r\n");
        sb.append("END:VEVENT\r\n");
        sb.append("END:VCALENDAR\r\n");

        return sb.toString();
    }

    /**
     * Función auxiliar para escapar caracteres prohibidos en iCalendar.
     * ,  -> \,
     * ;  -> \;
     * \  -> \\
     * \n -> \n
     */
    private String escapeIcs(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace(";", "\\;")
                    .replace(",", "\\,")
                    .replace("\n", "\\n");
    }


}