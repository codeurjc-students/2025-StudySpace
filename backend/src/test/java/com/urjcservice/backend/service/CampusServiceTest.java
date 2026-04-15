package com.urjcservice.backend.service;

import com.urjcservice.backend.entities.Campus;
import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.repositories.CampusRepository;
import com.urjcservice.backend.repositories.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampusServiceTest {

    @Mock
    private CampusRepository campusRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private CampusService campusService;

    private Campus mockCampus;

    @BeforeEach
    void setUp() {
        mockCampus = new Campus();
        mockCampus.setId(1L);
        mockCampus.setName("Móstoles");
        mockCampus.setCoordinates("40.33, -3.87");
    }

    @Test
    void findAll_ShouldReturnCampusList() {
        when(campusRepository.findAll()).thenReturn(List.of(mockCampus));

        List<Campus> result = campusService.findAll();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(campusRepository, times(1)).findAll();
    }

    @Test
    void findById_ShouldReturnCampus() {
        when(campusRepository.findById(1L)).thenReturn(Optional.of(mockCampus));

        Optional<Campus> result = campusService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Móstoles", result.get().getName());
    }

    @Test
    void save_WhenNameDoesNotExist_ShouldSaveCampus() {
        when(campusRepository.findByName("Móstoles")).thenReturn(Optional.empty());
        when(campusRepository.save(mockCampus)).thenReturn(mockCampus);

        Campus result = campusService.save(mockCampus);

        assertNotNull(result);
        verify(campusRepository).save(mockCampus);
    }

    @Test
    void save_WhenNameExists_ShouldThrowConflictException() {
        when(campusRepository.findByName("Móstoles")).thenReturn(Optional.of(mockCampus));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            campusService.save(mockCampus);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getReason().contains("already exists"));
        verify(campusRepository, never()).save(any());
    }

    @Test
    void update_WhenCampusExistsAndNameNotDuplicate_ShouldUpdate() {
        Campus updatedInfo = new Campus();
        updatedInfo.setName("Nuevo Móstoles");
        updatedInfo.setCoordinates("0,0");

        when(campusRepository.findById(1L)).thenReturn(Optional.of(mockCampus));
        when(campusRepository.findByName("Nuevo Móstoles")).thenReturn(Optional.empty());
        when(campusRepository.save(any(Campus.class))).thenReturn(mockCampus);

        Optional<Campus> result = campusService.update(1L, updatedInfo);

        assertTrue(result.isPresent());
        assertEquals("Nuevo Móstoles", mockCampus.getName());
        verify(campusRepository).save(mockCampus);
    }

    @Test
    void update_WhenNameIsDuplicateFromOtherCampus_ShouldThrowConflict() {
        Campus updatedInfo = new Campus();
        updatedInfo.setName("Alcorcón"); 

        Campus otherCampus = new Campus();
        otherCampus.setId(2L);
        otherCampus.setName("Alcorcón");

        when(campusRepository.findById(1L)).thenReturn(Optional.of(mockCampus));
        when(campusRepository.findByName("Alcorcón")).thenReturn(Optional.of(otherCampus)); // Existe y tiene ID 2

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            campusService.update(1L, updatedInfo);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(campusRepository, never()).save(any());
    }

    @Test
    void delete_WhenCampusExists_ShouldDeleteAndNotifyUsers() throws Exception {
        Room mockRoom = new Room();
        mockRoom.setId(10L);
        mockRoom.setName("Lab 1");
        mockCampus.setRooms(List.of(mockRoom));

        Reservation mockReservation = new Reservation();
        User mockUser = new User();
        mockUser.setEmail("test@urjc.es");
        mockUser.setName("Juan");
        mockReservation.setUser(mockUser);
        mockReservation.setRoom(mockRoom);
        mockReservation.setStartDate(new Date());
        mockReservation.setEndDate(new Date());

        when(campusRepository.findById(1L)).thenReturn(Optional.of(mockCampus));
        when(reservationRepository.findActiveReservationsByRoomIdAndEndDateAfter(eq(10L), any(Date.class)))
                .thenReturn(List.of(mockReservation));

        Optional<Campus> result = campusService.delete(1L);

        assertTrue(result.isPresent());

        verify(emailService, times(1)).sendReservationCancellationEmail(
                eq("test@urjc.es"), eq("Juan"), eq("Lab 1"), anyString(), anyString(), anyString(), anyString()
        );
        verify(reservationRepository, times(1)).deleteByRoomIdAndEndDateAfter(eq(10L), any(Date.class));
        verify(campusRepository, times(1)).delete(mockCampus);
    }

    @Test
    void delete_WhenCampusDoesNotExist_ShouldReturnEmpty() {
        when(campusRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Campus> result = campusService.delete(99L);

        assertFalse(result.isPresent());
        verify(reservationRepository, never()).deleteByRoomIdAndEndDateAfter(anyLong(), any());
        verify(campusRepository, never()).delete(any());
    }
}