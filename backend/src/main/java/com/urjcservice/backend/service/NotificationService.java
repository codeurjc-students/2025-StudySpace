package com.urjcservice.backend.service;

import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.repositories.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private EmailService emailService;

    private static final long ONE_MINUTE_IN_MILLIS = 60000L;

    @Scheduled(fixedRate = 60000) // 60000 ms= 1 minute
    @Transactional
    public void sendReservationReminders() {
        Date now = new Date();
        // start on the next 15/20 min (5 of margin)

        long twentyMinutesInMillis = 20 * ONE_MINUTE_IN_MILLIS;

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

                // if send once, not necesary a second one
                reservation.setReminderSent(true);
                reservationRepository.save(reservation);

                logger.info("Reminder sent to: {}", to);
            }
        }
    }
}