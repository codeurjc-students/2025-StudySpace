package com.urjcservice.backend.service;

import com.urjcservice.backend.rest.ReservationRestController.ReservationRequest;
import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.repositories.ReservationRepository;
import com.urjcservice.backend.repositories.RoomRepository;
import com.urjcservice.backend.repositories.UserRepository;
import com.urjcservice.backend.service.EmailService;      
import com.urjcservice.backend.service.FileStorageService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.LocalDate;
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
    @Mock private EmailService emailService;
    
    @MockBean
    private FileStorageService fileStorageService;

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
        User user = new User(); 
        user.setId(99L);

        Reservation existing = new Reservation();
        existing.setId(id);
        existing.setUser(user);
        existing.setStartDate(toDate(LocalDateTime.of(2026, 1, 31, 10, 0))); 
        existing.setEndDate(toDate(LocalDateTime.of(2026, 1, 31, 11, 0)));   

        Reservation updates = new Reservation();
        updates.setReason("Nueva razón");

        //Mocks
        when(reservationRepository.findById(id)).thenReturn(Optional.of(existing));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(existing);
        when(reservationRepository.findActiveByUserIdAndDate(eq(99L), any(LocalDate.class)))
                .thenReturn(List.of());

        // WHEN
        reservationService.updateReservation(id, updates);

        // THEN
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
        User user = new User(); user.setId(1L);
        
        Room activeRoom = new Room();
        activeRoom.setId(roomId);
        activeRoom.setActive(true);

        ReservationRequest request = new ReservationRequest();
        request.setRoomId(roomId);
        request.setStartDate(toDate(LocalDateTime.of(2026, 5, 20, 10, 0))); 
        request.setEndDate(toDate(LocalDateTime.of(2026, 5, 20, 11, 0)));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(activeRoom));
        
        // Mocks
        when(reservationRepository.findOverlappingReservations(anyLong(), any(), any(), any()))
                .thenReturn(Page.empty()); 
        when(reservationRepository.findActiveByUserIdAndDate(anyLong(), any()))
                .thenReturn(List.of());    
        
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
        ReservationRequest req = new ReservationRequest();
        req.setRoomId(1L);
        req.setStartDate(toDate(LocalDateTime.of(2026, 1, 31, 10, 00))); 
        req.setEndDate(toDate(LocalDateTime.of(2026, 1, 31, 11, 00)));

        User user = new User(); user.setEmail("test@urjc.es");
        Room room = new Room(); room.setId(1L); room.setActive(true);


        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        //simulate overlaping
        when(reservationRepository.findOverlappingReservations(anyLong(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(new Reservation())));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            reservationService.createReservation(req, "test@urjc.es");
        });
        assertTrue(ex.getMessage().contains("already reserved"));
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
        ReservationRequest req = new ReservationRequest();
        req.setRoomId(1L);
        req.setStartDate(toDate(LocalDateTime.of(2026, 1, 31, 10, 0))); 
        req.setEndDate(toDate(LocalDateTime.of(2026, 1, 31, 10, 30)));

        User user = new User(); user.setEmail("test@urjc.es");
        Room room = new Room(); room.setId(1L); room.setActive(true);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        
        when(reservationRepository.findOverlappingReservations(anyLong(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(new Reservation())));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> 
            reservationService.createReservation(req, "test@urjc.es")
        );

        assertEquals("The room is already reserved for this time.", ex.getMessage());
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
        User user = new User(); 
        user.setId(99L); 
        
        Reservation existing = new Reservation();
        existing.setId(1L);
        existing.setUser(user);
        existing.setStartDate(toDate(LocalDateTime.of(2026, 1, 31, 10, 0)));
        existing.setEndDate(toDate(LocalDateTime.of(2026, 1, 31, 11, 0)));

        Reservation updateInfo = new Reservation();
        updateInfo.setReason("New Reason");
        
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArguments()[0]);

        when(reservationRepository.findActiveByUserIdAndDate(eq(99L), any())).thenReturn(List.of());

        // WHEN
        Optional<Reservation> result = reservationService.updateReservation(1L, updateInfo);

        // THEN
        assertTrue(result.isPresent());
        assertEquals("New Reason", result.get().getReason());
    }

    @Test
    void testUpdateReservation_NotFound() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());
        Optional<Reservation> result = reservationService.updateReservation(99L, new Reservation());
        assertTrue(result.isEmpty());
    }

    // --- TESTS DE PATCH ---

    @Test
    void testPatchReservation_PartialUpdate() {
        Long id = 1L;
        User user = new User(); user.setId(50L);

        Reservation existing = new Reservation();
        existing.setId(id);
        existing.setUser(user);
        existing.setReason("Old Reason");
        existing.setStartDate(toDate(LocalDateTime.of(2026, 2, 1, 10, 0))); 
        existing.setEndDate(toDate(LocalDateTime.of(2026, 2, 1, 11, 0)));  

        //only change the reason
        Reservation patch = new Reservation();
        patch.setReason("Patched Reason");


        //Mocks
        when(reservationRepository.findById(id)).thenReturn(Optional.of(existing));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArguments()[0]);
        when(reservationRepository.findActiveByUserIdAndDate(eq(50L), any(LocalDate.class)))
                .thenReturn(List.of());

        // WHEN
        Optional<Reservation> result = reservationService.patchReservation(id, patch);

        // THEN
        assertTrue(result.isPresent());
        assertEquals("Patched Reason", result.get().getReason());
        assertNotNull(result.get().getStartDate());
    }

    // --- search user by ID  ---

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



    @Test
    void testCreateReservation_SingleReservationExceeds3Hours() {
        // GIVEN
        ReservationRequest req = new ReservationRequest();
        req.setRoomId(1L);
        // 10:00 a 14:00 (4 hours)
        Date start = toDate(LocalDateTime.of(2026, 1, 31, 10, 0));
        Date end = toDate(LocalDateTime.of(2026, 1, 31, 14, 0)); 
        req.setStartDate(start);
        req.setEndDate(end);

        User user = new User(); user.setEmail("test@urjc.es"); user.setId(1L);
        Room room = new Room(); room.setId(1L); room.setActive(true);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        // WHEN & THEN
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            reservationService.createReservation(req, "test@urjc.es");
        });

        assertEquals("A single reservation cannot exceed 3 hours.", ex.getMessage());
    }

    @Test
    void testCreateReservation_DailyQuotaExceeded() {
        // GIVEN
        User user = new User(); user.setId(1L); user.setEmail("test@urjc.es");
        Room room = new Room(); room.setId(1L); room.setActive(true);
        
        ReservationRequest req = new ReservationRequest();
        req.setRoomId(1L);
        req.setStartDate(toDate(LocalDateTime.of(2026, 1, 31, 12, 0)));
        req.setEndDate(toDate(LocalDateTime.of(2026, 1, 31, 14, 0)));   

        // MOCKS
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        
        when(reservationRepository.findOverlappingReservations(anyLong(), any(), any(), any()))
            .thenReturn(Page.empty());
        //already used 180 min
        Reservation existing = new Reservation();
        existing.setId(100L);
        existing.setStartDate(toDate(LocalDateTime.of(2026, 1, 31, 8, 0)));
        existing.setEndDate(toDate(LocalDateTime.of(2026, 1, 31, 10, 0))); 
        
        when(reservationRepository.findActiveByUserIdAndDate(eq(1L), any()))
            .thenReturn(List.of(existing));

        // WHEN & THEN
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            reservationService.createReservation(req, "test@urjc.es");
        });

        assertTrue(ex.getMessage().contains("Daily limit exceeded"));
    }

    @Test
    void testCreateReservation_DailyQuotaIgnoreCanceled() {
        User user = new User(); user.setId(1L); user.setEmail("test@urjc.es");
        Room room = new Room(); room.setId(1L); room.setActive(true);

        when(reservationRepository.findOverlappingReservations(anyLong(), any(), any(), any()))
            .thenReturn(Page.empty());
        when(reservationRepository.findActiveByUserIdAndDate(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of()); //empty list =0 min

        //2 hours
        ReservationRequest req = new ReservationRequest();
        req.setRoomId(1L);
        req.setStartDate(toDate(LocalDateTime.of(2026, 1, 31, 12, 0)));
        req.setEndDate(toDate(LocalDateTime.of(2026, 1, 31, 14, 0)));

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(new Reservation());

        // WHEN
        reservationService.createReservation(req, "test@urjc.es");

        // THEN
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void testValidateRules_BadIntervals() {
        // GIVEN only end the hour on 30 or on 00
        ReservationRequest req = new ReservationRequest();
        req.setRoomId(1L);
        req.setStartDate(toDate(LocalDateTime.of(2026, 1, 31, 10, 15))); 
        req.setEndDate(toDate(LocalDateTime.of(2026, 1, 31, 11, 15)));

        User user = new User(); user.setEmail("a@a.com");
        Room room = new Room(); room.setActive(true);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(roomRepository.findById(anyLong())).thenReturn(Optional.of(room));

        // WHEN & THEN
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            reservationService.createReservation(req, "a@a.com");
        });
        assertTrue(ex.getMessage().contains("30-minute intervals"));
    }

    private Date toDate(LocalDateTime ldt) {
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }




    @Test
    void testCreateReservation_NullDates() {
        ReservationRequest req = new ReservationRequest();
        req.setRoomId(1L);
        req.setStartDate(null); // Nulo
        req.setEndDate(null);

        User user = new User(); user.setEmail("test@urjc.es");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(new Room()));

        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.createReservation(req, "test@urjc.es");
        }, "Dates cannot be null");
    }

    @Test
    void testCreateReservation_BeforeOpeningHours() {
        ReservationRequest req = new ReservationRequest();
        req.setRoomId(1L);
        req.setStartDate(toDate(LocalDateTime.of(2026, 2, 1, 7, 0)));
        req.setEndDate(toDate(LocalDateTime.of(2026, 2, 1, 8, 0)));

        User user = new User(); user.setEmail("test@urjc.es");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(new Room()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            reservationService.createReservation(req, "test@urjc.es");
        });
        assertTrue(ex.getMessage().contains("between 08:00 and 21:00"));
    }

    @Test
    void testCreateReservation_AfterClosingHours() {
        ReservationRequest req = new ReservationRequest();
        req.setRoomId(1L);
        req.setStartDate(toDate(LocalDateTime.of(2026, 2, 1, 21, 0)));
        req.setEndDate(toDate(LocalDateTime.of(2026, 2, 1, 22, 0)));

        User user = new User(); user.setEmail("test@urjc.es");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(new Room()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            reservationService.createReservation(req, "test@urjc.es");
        });
        assertTrue(ex.getMessage().contains("between 08:00 and 21:00"));
    }

    @Test
    void testCreateReservation_TooShortDuration() {
        
        ReservationRequest req = new ReservationRequest();
        req.setRoomId(1L);
        req.setStartDate(toDate(LocalDateTime.of(2026, 2, 1, 10, 0)));
        req.setEndDate(toDate(LocalDateTime.of(2026, 2, 1, 10, 0))); // 0 minutos

        User user = new User(); user.setEmail("test@urjc.es");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(new Room()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            reservationService.createReservation(req, "test@urjc.es");
        });
        assertNotNull(ex); 
    }

    @Test
    void testCreateReservation_PastDate() {
        ReservationRequest req = new ReservationRequest();
        req.setRoomId(1L);
        req.setStartDate(toDate(LocalDateTime.now().minusDays(1).withHour(10).withMinute(0))); 
        req.setEndDate(toDate(LocalDateTime.now().minusDays(1).withHour(11).withMinute(0)));

        User user = new User(); user.setEmail("test@urjc.es");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(new Room()));

        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.createReservation(req, "test@urjc.es");
        }, "Cannot create reservations in the past.");
    }


    @Test
    void testFindById() {
        Long id = 1L;
        Reservation res = new Reservation();
        when(reservationRepository.findById(id)).thenReturn(Optional.of(res));

        Optional<Reservation> result = reservationService.findById(id);
        assertTrue(result.isPresent());
    }

    @Test
    void testGetActiveReservationsForRoomAndDate() {
        Long roomId = 1L;
        LocalDate date = LocalDate.now();
        when(reservationRepository.findActiveReservationsByRoomAndDate(roomId, date))
            .thenReturn(List.of(new Reservation()));

        List<Reservation> result = reservationService.getActiveReservationsForRoomAndDate(roomId, date);
        assertEquals(1, result.size());
    }

    @Test
    void testCancelById_Internal() {
        Long id = 1L;
        Reservation res = new Reservation();
        res.setCancelled(false);
        
        when(reservationRepository.findById(id)).thenReturn(Optional.of(res));
        when(reservationRepository.save(res)).thenReturn(res);

        Optional<Reservation> result = reservationService.cancelById(id);
        
        assertTrue(result.isPresent());
        assertTrue(result.get().isCancelled());
    }






    @Test
    void testCancelByIdSecure_AsAdmin_Success() {
        Long id = 1L;
        Reservation res = new Reservation();
        User owner = new User(); owner.setEmail("pepe@user.com");
        res.setUser(owner);
        res.setCancelled(false);

        when(reservationRepository.findById(id)).thenReturn(Optional.of(res));
        when(reservationRepository.save(res)).thenReturn(res);

        Optional<Reservation> result = reservationService.cancelByIdSecure(id, "admin@urjc.es", true);

        assertTrue(result.isPresent());
        assertTrue(result.get().isCancelled());
    }

    

    @Test
    void testUpdateReservation_ChangeUserAndRoom() {
        Long id = 1L;
        //already existing
        Reservation existing = new Reservation();
        existing.setId(id);
        existing.setUser(new User()); existing.getUser().setId(1L);
        existing.setRoom(new Room());
        existing.setStartDate(toDate(LocalDateTime.of(2026, 2, 1, 10, 0)));
        existing.setEndDate(toDate(LocalDateTime.of(2026, 2, 1, 11, 0)));

        Reservation updates = new Reservation();
        updates.setUserId(2L); 
        updates.setRoomId(3L); 

        User newUser = new User(); newUser.setId(2L);
        Room newRoom = new Room(); newRoom.setId(3L);

        when(reservationRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.findById(2L)).thenReturn(Optional.of(newUser));
        when(roomRepository.findById(3L)).thenReturn(Optional.of(newRoom));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArguments()[0]);
        when(reservationRepository.findActiveByUserIdAndDate(anyLong(), any())).thenReturn(List.of());

        Optional<Reservation> result = reservationService.updateReservation(id, updates);

        assertTrue(result.isPresent());
        verify(userRepository).findById(2L);
        verify(roomRepository).findById(3L);
        assertEquals(newUser, result.get().getUser());
        assertEquals(newRoom, result.get().getRoom());
    }






    @Test
    @DisplayName("Admin Cancel - Should mark as cancelled, save reason and send email")
    void testAdminCancelReservation_Success() {
        // Arrange
        Long reservationId = 1L;
        String reason = "Maintenance required";
        
        User user = new User();
        user.setEmail("student@test.com");
        user.setName("Student Name");

        Room room = new Room();
        room.setName("Lab 1");

        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        reservation.setUser(user);
        reservation.setRoom(room);

        Date now = new Date();
        Date oneHourLater = new Date(now.getTime() + 3600000);
        
        reservation.setStartDate(now);
        reservation.setEndDate(oneHourLater);

        reservation.setCancelled(false);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepository.saveAndFlush(any(Reservation.class))).thenReturn(reservation);

        // Act
        Optional<Reservation> result = reservationService.adminCancelReservation(reservationId, reason);

        // Assert
        assertTrue(result.isPresent());
        Reservation savedRes = result.get();
        
        assertTrue(savedRes.isCancelled());
        assertEquals(reason, savedRes.getAdminModificationReason());

        verify(reservationRepository).saveAndFlush(reservation);

        verify(emailService, times(1)).sendReservationCancellationEmail(
            eq("student@test.com"),
            eq("Student Name"),
            eq("Lab 1"),
            anyString(), 
            anyString(), 
            anyString(), 
            eq(reason)
        );
    }

    @Test
    @DisplayName("Admin Update - Should update fields and send notification email")
    void testAdminUpdateReservation_Success() {
        // Arrange
        Long reservationId = 1L;
        Long newRoomId = 2L;
        LocalDate newDate = LocalDate.of(2026, 5, 20);
        LocalTime newStart = LocalTime.of(10, 0);
        LocalTime newEnd = LocalTime.of(12, 0);
        String adminReason = "Moved by admin";

        Reservation existing = new Reservation();
        existing.setId(reservationId);
        User user = new User(); 
        user.setEmail("user@test.com"); 
        user.setName("User");
        existing.setUser(user);
        
        Room oldRoom = new Room(); 
        oldRoom.setName("Old Room");
        existing.setRoom(oldRoom);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(existing));
        
        Room newRoom = new Room();
        newRoom.setId(newRoomId);
        newRoom.setName("New Room");
        when(roomRepository.findById(newRoomId)).thenReturn(Optional.of(newRoom));

        when(reservationRepository.saveAndFlush(any(Reservation.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Optional<Reservation> result = reservationService.adminUpdateReservation(
                reservationId, newRoomId, newDate, newStart, newEnd, adminReason
        );

        //Verify
        assertTrue(result.isPresent());
        assertEquals(adminReason, result.get().getAdminModificationReason());
        assertEquals(newRoomId, result.get().getRoom().getId()); 
        
        verify(emailService, times(1)).sendReservationModificationEmail(
            eq("user@test.com"),
            eq("User"),
            eq("New Room"), 
            anyString(), 
            anyString(),
            anyString(),
            eq(adminReason)
        );
    }

} 

