package com.urjcservice.backend.service;

import com.urjcservice.backend.controller.ReservationController.ReservationRequest;
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
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.Date;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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





    @Test
    void testCreateReservation_WhenRoomIsDisabled_ShouldThrowException() {
        // GIVEN
        String email = "test@urjc.es";
        Long roomId = 1L;

        User user = new User();
        user.setEmail(email);

        Room disabledRoom = new Room();
        disabledRoom.setId(roomId);
        disabledRoom.setActive(false); //disable

        ReservationRequest request = new ReservationRequest();
        request.setRoomId(roomId);
        request.setStartDate(new Date());
        request.setEndDate(new Date());

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(disabledRoom));

        // WHEN & THEN: Esperamos que lance RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reservationService.createReservation(request, email);
        });

        assertEquals("Reservations are not possible: The classroom is temporarily unavailable.", exception.getMessage());
        
        // Aseguramos que NUNCA se guardó la reserva
        verify(reservationRepository, never()).save(any(Reservation.class));
    }
    @Test
    void testCreateReservation_WhenRoomIsActive_ShouldSuccess() {
        // GIVEN
        String email = "test@urjc.es";
        Long roomId = 1L;
        User user = new User();
        Room activeRoom = new Room();
        activeRoom.setId(roomId);
        activeRoom.setActive(true); //active

        ReservationRequest request = new ReservationRequest();
        request.setRoomId(roomId);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(activeRoom));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(new Reservation());

        // WHEN
        reservationService.createReservation(request, email);

        // THEN
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }




    @Test
    void testCancelByIdSecure_Forbidden() {
        Reservation res = new Reservation();
        User owner = new User();
        owner.setEmail("owner@test.com");
        res.setUser(owner);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(res));

        // Usuario que no es dueño ni admin
        assertThrows(AccessDeniedException.class, () -> {
            reservationService.cancelByIdSecure(1L, "other@test.com", false);
        });
    }

    @Test
    void testCreateReservation_UserNotFound() {
        ReservationRequest req = new ReservationRequest();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            reservationService.createReservation(req, "missing@test.com");
        }, "User not found");
    }

    @Test
    void testCreateReservation_OverlapError() {
        String email = "user@test.com";
        ReservationRequest req = new ReservationRequest();
        req.setRoomId(1L);
        req.setStartDate(new Date());
        req.setEndDate(new Date());

        Room room = new Room(); room.setActive(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        // Simulamos que hay un solapamiento
        when(reservationRepository.findOverlappingReservations(any(), any(), any()))
            .thenReturn(Arrays.asList(new Reservation()));

        assertThrows(RuntimeException.class, () -> {
            reservationService.createReservation(req, email);
        }, "The room is already reserved");
    }

} 

