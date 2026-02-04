package com.urjcservice.backend.service;

import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.repositories.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private EmailService emailService;
    
    private static final long ONE_MINUTE_IN_MILLIS = 60000L;

    @Scheduled(fixedRate = 60000) //60000 ms= 1 minute
    @Transactional
    public void sendReservationReminders() {
        Date now = new Date();
        // start on the next 15/20 min (5 of margin)
        
        long fifteenMinutesInMillis = 15 * ONE_MINUTE_IN_MILLIS;
        long twentyMinutesInMillis = 20 * ONE_MINUTE_IN_MILLIS;
        
        Date limitStart = new Date(now.getTime() + fifteenMinutesInMillis);
        Date limitEnd = new Date(now.getTime() + twentyMinutesInMillis);
        
        List<Reservation> upcomingReservations = reservationRepository.findPendingReminders(now, limitEnd);

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        for (Reservation reservation : upcomingReservations) {
            long diff = reservation.getStartDate().getTime() - now.getTime();
            
            if (diff > 0) {
                String to = reservation.getUser().getEmail();
                String userName = reservation.getUser().getName();
                String roomName = reservation.getRoom().getName();
                String time = timeFormat.format(reservation.getStartDate());

                emailService.sendReservationReminder(to, userName, roomName, time);

                //if send once, not necesary a second one
                reservation.setReminderSent(true);
                reservationRepository.save(reservation);
                
                System.out.println("Reminder sent to: " + to);
            }
        }
    }
}