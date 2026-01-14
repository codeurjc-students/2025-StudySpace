package com.urjcservice.backend.service;

import com.urjcservice.backend.dtos.DashboardStatsDTO;
import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.repositories.ReservationRepository;
import com.urjcservice.backend.repositories.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StatsServiceTest {

    @Mock
    private RoomRepository roomRepo;

    @Mock
    private ReservationRepository resRepo;

    @InjectMocks
    private StatsService statsService;

    private Date toDate(LocalDateTime ldt) {
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    @Test
    public void testGetStatsWithData() {
        // GIVEN
        LocalDate today = LocalDate.now();
        
        // Mockl
        when(roomRepo.countTotalRooms()).thenReturn(10L);
        when(resRepo.countOccupiedRoomsByDate(today)).thenReturn(5L);
        when(resRepo.countOccupiedWithSoftwareByDate(today)).thenReturn(4L);
        Reservation res = new Reservation();
        res.setStartDate(toDate(today.atTime(10, 0))); 
        res.setEndDate(toDate(today.atTime(11, 0)));

        when(resRepo.findAllActiveByDate(today)).thenReturn(List.of(res));

        // WHEN
        DashboardStatsDTO result = statsService.getStats(today);



        assertEquals(10, result.getTotalRooms());
        assertEquals(50.0, result.getOccupiedPercentage()); 
        assertEquals(50.0, result.getFreePercentage());
        assertEquals(80.0, result.getRoomsWithSoftwarePercentage());
        assertNotNull(result.getHourlyOccupancy());
        assertEquals(1L, result.getHourlyOccupancy().get("10:00"));
        assertEquals(1L, result.getHourlyOccupancy().get("10:30"));
        assertEquals(0L, result.getHourlyOccupancy().get("09:30")); 
        assertEquals(0L, result.getHourlyOccupancy().get("11:00")); 
        verify(resRepo).findAllActiveByDate(eq(today));
    }

    @Test
    public void testGetStatsNoRooms() {
        LocalDate today = LocalDate.now();
        
        when(roomRepo.countTotalRooms()).thenReturn(0L);
        when(resRepo.countOccupiedRoomsByDate(today)).thenReturn(0L);
        when(resRepo.findAllActiveByDate(today)).thenReturn(Collections.emptyList());

        DashboardStatsDTO result = statsService.getStats(today);
        
        // Verify
        assertEquals(0, result.getTotalRooms());
        assertEquals(0.0, result.getOccupiedPercentage());
    }

    @Test
    public void testGetStatsNoReservations() {
        LocalDate today = LocalDate.now();
        
        when(roomRepo.countTotalRooms()).thenReturn(10L);
        when(resRepo.countOccupiedRoomsByDate(today)).thenReturn(0L);
        when(resRepo.findAllActiveByDate(today)).thenReturn(Collections.emptyList());

        DashboardStatsDTO result = statsService.getStats(today);
        
        // Verify
        assertEquals(0.0, result.getOccupiedPercentage());
        assertEquals(100.0, result.getFreePercentage());
        assertTrue(result.getHourlyOccupancy().values().stream().allMatch(v -> v == 0));
        verify(resRepo).findAllActiveByDate(eq(today));
    }
}