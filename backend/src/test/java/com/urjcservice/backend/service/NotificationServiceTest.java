package com.urjcservice.backend.service;

import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.repositories.ReservationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("Should send reminders for upcoming reservations and mark them as sent")
    void testSendReservationReminders_Success() {
        // Arrange
        long fifteenMinutesLater = System.currentTimeMillis() + (15 * 60 * 1000) + 1000; //plus 1 sec of margin
        Date startDate = new Date(fifteenMinutesLater);

        User mockUser = new User();
        mockUser.setEmail("user@test.com");
        mockUser.setName("John Doe");

        Room mockRoom = new Room();
        mockRoom.setName("Sala 101");

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStartDate(startDate);
        reservation.setUser(mockUser);
        reservation.setRoom(mockRoom);
        reservation.setReminderSent(false);

        when(reservationRepository.findPendingReminders(any(Date.class), any(Date.class)))
                .thenReturn(List.of(reservation));

        // Act
        notificationService.sendReservationReminders();

        // Assert
        verify(emailService, times(1)).sendReservationReminder(
                eq("user@test.com"), 
                eq("John Doe"), 
                eq("Sala 101"), 
                anyString() 
        );
        verify(reservationRepository, times(1)).save(argThat(r -> r.isReminderSent() == true));
    }

    @Test
    @DisplayName("Should not send email if start date is in the past (diff <= 0)")
    void testSendReservationReminders_SkippedIfLate() {
        // Arrange
        Date pastDate = new Date(System.currentTimeMillis() - 1000); 

        Reservation reservation = new Reservation();
        reservation.setStartDate(pastDate);
        reservation.setUser(new User());
        reservation.setRoom(new Room());

        when(reservationRepository.findPendingReminders(any(Date.class), any(Date.class)))
                .thenReturn(List.of(reservation));

        // Act
        notificationService.sendReservationReminders();

        // Assert
        verify(emailService, never()).sendReservationReminder(anyString(), anyString(), anyString(), anyString());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Should do nothing if no reservations are found")
    void testSendReservationReminders_NoReservations() {
        // Arrange
        when(reservationRepository.findPendingReminders(any(Date.class), any(Date.class)))
                .thenReturn(Collections.emptyList());

        // Act
        notificationService.sendReservationReminders();

        // Assert
        verify(emailService, never()).sendReservationReminder(anyString(), anyString(), anyString(), anyString());
    }
}