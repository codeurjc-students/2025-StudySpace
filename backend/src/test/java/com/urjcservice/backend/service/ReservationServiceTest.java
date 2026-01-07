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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;



import java.util.Optional;
import java.util.Date;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
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

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reservationService.createReservation(request, email);
        });

        assertEquals("Reservations are not possible: The classroom is temporarily unavailable.", exception.getMessage());
        
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
        // No overlaps expected
        when(reservationRepository.findOverlappingReservations(any(Long.class), nullable(Date.class), nullable(Date.class), any(Pageable.class))).thenReturn(Page.empty());
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
        //simulate overlaping
        when(reservationRepository.findOverlappingReservations(any(Long.class), any(Date.class), any(Date.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(new Reservation())));

        assertThrows(RuntimeException.class, () -> {
            reservationService.createReservation(req, email);
        }, "The room is already reserved");
    }









    @Test
    void testGetReservationsByUserEmail_Success() {
        // GIVEN
        String email = "test@user.com";
        User user = new User();
        user.setEmail(email);
        
        Pageable pageable = PageRequest.of(0, 10);
        List<Reservation> resList = Arrays.asList(new Reservation(), new Reservation());
        Page<Reservation> page = new PageImpl<>(resList);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(reservationRepository.findByUser(user, pageable)).thenReturn(page);

        // WHEN
        Page<Reservation> result = reservationService.getReservationsByUserEmail(email, pageable);

        // THEN
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(reservationRepository).findByUser(user, pageable);
    }

    @Test
    void testCreateReservation_InvalidDates_StartAfterEnd() {
        // GIVEN
        ReservationRequest req = new ReservationRequest();
        req.setRoomId(1L);
        // start date after the end date
        req.setStartDate(new Date(System.currentTimeMillis() + 10000)); 
        req.setEndDate(new Date(System.currentTimeMillis()));     

       assertThrows(RuntimeException.class, () -> {
            reservationService.createReservation(req, "test@user.com");
        });
    }
    
    @Test
    void testFindAll_Paginated() {
        // GIVEN
        Pageable pageable = PageRequest.of(1, 5);
        when(reservationRepository.findAll(pageable)).thenReturn(Page.empty());

        // WHEN
        Page<Reservation> result = reservationService.findAll(pageable);

        // THEN
        assertNotNull(result);
        verify(reservationRepository).findAll(pageable);
    }





    @Test
    void testCreateReservation_RoomInactive() {
        // GIVEN
        ReservationRequest req = new ReservationRequest();
        req.setRoomId(1L);
        req.setStartDate(new Date(System.currentTimeMillis() + 10000));
        req.setEndDate(new Date(System.currentTimeMillis() + 20000));

        User user = new User();
        user.setEmail("test@user.com");

        Room room = new Room();
        room.setId(1L);
        room.setActive(false); // <--- LA CLAVE: Habitación inactiva

        when(userRepository.findByEmail("test@user.com")).thenReturn(Optional.of(user));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reservationService.createReservation(req, "test@user.com");
        });
        
        assertEquals("Reservations are not possible: The classroom is temporarily unavailable.", exception.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void testCreateReservation_Overlapping() {
        // GIVEN
        ReservationRequest req = new ReservationRequest();
        req.setRoomId(1L);
        Date start = new Date(System.currentTimeMillis() + 10000);
        Date end = new Date(System.currentTimeMillis() + 20000);
        req.setStartDate(start);
        req.setEndDate(end);

        User user = new User();
        Room room = new Room();
        room.setId(1L);
        room.setActive(true);

        Reservation existingRes = new Reservation();
        Page<Reservation> overlapPage = new PageImpl<>(List.of(existingRes));

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        // We simulate that the repository returns content when searching for overlaps
        when(reservationRepository.findOverlappingReservations(eq(1L), eq(start), eq(end), any(PageRequest.class)))
                .thenReturn(overlapPage);

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reservationService.createReservation(req, "test@user.com");
        });

        assertEquals("The room is already reserved for this time.", exception.getMessage());
    }

    // --- TESTS DE DELETE ---

    @Test
    void testDeleteById_Success() {
        Long id = 1L;
        Reservation res = new Reservation();
        when(reservationRepository.findById(id)).thenReturn(Optional.of(res));

        Optional<Reservation> result = reservationService.deleteById(id);

        assertTrue(result.isPresent());
        verify(reservationRepository).delete(res);
    }

    @Test
    void testDeleteById_NotFound() {
        Long id = 99L;
        when(reservationRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Reservation> result = reservationService.deleteById(id);

        assertTrue(result.isEmpty());
        verify(reservationRepository, never()).delete(any());
    }

    // --- TESTS DE UPDATE (PUT) ---

    @Test
    void testUpdateReservation_Success() {
        Long id = 1L;
        Reservation existing = new Reservation();
        existing.setReason("Old Reason");

        Reservation updates = new Reservation();
        updates.setReason("New Reason");
        updates.setStartDate(new Date());
        updates.setEndDate(new Date());

        when(reservationRepository.findById(id)).thenReturn(Optional.of(existing));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArguments()[0]);

        Optional<Reservation> result = reservationService.updateReservation(id, updates);

        assertTrue(result.isPresent());
        assertEquals("New Reason", result.get().getReason());
    }

    @Test
    void testUpdateReservation_NotFound() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());
        Optional<Reservation> result = reservationService.updateReservation(99L, new Reservation());
        assertTrue(result.isEmpty());
    }

    // --- TESTS DE PATCH (Actualización Parcial) ---

    @Test
    void testPatchReservation_PartialUpdate() {
        Long id = 1L;
        Reservation existing = new Reservation();
        existing.setReason("Old Reason");
        Date oldDate = new Date(1000);
        existing.setStartDate(oldDate);

        Reservation patch = new Reservation();
        patch.setReason("Patched Reason");
        patch.setStartDate(null); 
        patch.setEndDate(null);

        when(reservationRepository.findById(id)).thenReturn(Optional.of(existing));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArguments()[0]);

        Optional<Reservation> result = reservationService.patchReservation(id, patch);

        assertTrue(result.isPresent());
        assertEquals("Patched Reason", result.get().getReason());
        assertEquals(oldDate, result.get().getStartDate());
    }

    // --- TEST DE BUSQUEDA POR USER ID (Faltaba) ---

    @Test
    void testGetReservationsByUserId_Success() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        Pageable pageable = PageRequest.of(0, 10);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reservationRepository.findByUser(user, pageable)).thenReturn(Page.empty());

        Page<Reservation> result = reservationService.getReservationsByUserId(userId, pageable);

        assertNotNull(result);
        verify(reservationRepository).findByUser(user, pageable);
    }

    @Test
    void testGetReservationsByUserId_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> {
            reservationService.getReservationsByUserId(99L, PageRequest.of(0, 10));
        });
    }
    
    // --- SECURE CANCELLATION TEST (Not Found ) ---
    
    @Test
    void testCancelByIdSecure_NotFound() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());
        
        Optional<Reservation> result = reservationService.cancelByIdSecure(99L, "any@email.com", true);
        
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testSave() {
        Reservation res = new Reservation();
        when(reservationRepository.save(res)).thenReturn(res);
        
        Reservation result = reservationService.save(res);
        
        assertNotNull(result);
        verify(reservationRepository).save(res);
    }


} 

