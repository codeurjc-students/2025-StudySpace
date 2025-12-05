package com.urjcservice.backend.service;

import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.repositories.ReservationRepository;
import com.urjcservice.backend.repositories.RoomRepository;
import com.urjcservice.backend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {
    @Mock private ReservationRepository reservationRepository;
    @Mock private UserRepository userRepository;
    @Mock private RoomRepository roomRepository;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    public void testSaveReservationLinkingUserAndRoom() {
        //reservation with ids
        Reservation res = new Reservation();
        res.setUserId(1L);
        res.setRoomId(2L);

        User mockUser = new User(); mockUser.setId(1L);
        Room mockRoom = new Room(); mockRoom.setId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(roomRepository.findById(2L)).thenReturn(Optional.of(mockRoom));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(res);

        reservationService.save(res);

        // Verify
        verify(userRepository).findById(1L);
        verify(roomRepository).findById(2L);
        verify(reservationRepository).save(res);
    }
    
    @Test
    public void testUpdateReservation() {
        Long id = 1L;
        Reservation existing = new Reservation();
        Reservation updates = new Reservation();
        updates.setReason("Nueva razón");

        when(reservationRepository.findById(id)).thenReturn(Optional.of(existing));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(existing);

        reservationService.updateReservation(id, updates);

        verify(reservationRepository).save(existing);
    }
} 

