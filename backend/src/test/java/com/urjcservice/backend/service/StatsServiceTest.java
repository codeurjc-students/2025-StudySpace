package com.urjcservice.backend.service;

import com.urjcservice.backend.dtos.DashboardStatsDTO;
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
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StatsServiceTest {

    @Mock
    private RoomRepository roomRepo;

    @Mock
    private ReservationRepository resRepo;

    @InjectMocks
    private StatsService statsService;

    @Test
    public void testGetStatsWithData() {
        // GIVEN: Test data
        LocalDate today = LocalDate.now();
        
        // Mock 10 total rooms
        when(roomRepo.countTotalRooms()).thenReturn(10L);

        when(resRepo.countOccupiedRoomsByDate(today)).thenReturn(5L);
        when(resRepo.countOccupiedWithSoftwareByDate(today)).thenReturn(4L);

        // Mock dates for hourly grouping, simulate a DB timestamp (e.g. 10:00 UTC)
        Date dateMock = Date.from(Instant.parse("2025-12-12T10:00:00Z")); 
        when(resRepo.findStartDatesByDate(today,any(Pageable.class))).thenReturn(new PageImpl<>(Arrays.asList(dateMock)));

        // WHEN
        DashboardStatsDTO result = statsService.getStats(today);

        // THEN
        
        //Occupancy: 5 out of 10 = 50%
        assertEquals(50.0, result.getOccupiedPercentage());
        assertEquals(50.0, result.getFreePercentage());

        //Software: 4 out of 5 occupied = 80% (Calculated over occupied, not total)
        assertEquals(80.0, result.getRoomsWithSoftwarePercentage());
        assertEquals(20.0, result.getRoomsWithoutSoftwarePercentage());

        //Hourly Grouping
        // Verify map is not empty and repositories were called
        assertFalse(result.getHourlyOccupancy().isEmpty());
        verify(resRepo).findStartDatesByDate(today, any(Pageable.class));
    }

    @Test
    public void testGetStatsNoRooms() {
        // Edge case: 0 Rooms (Prevent Division by Zero)
        LocalDate today = LocalDate.now();
        when(roomRepo.countTotalRooms()).thenReturn(0L);

        DashboardStatsDTO result = statsService.getStats(today);
        //Verify
        assertEquals(0, result.getTotalRooms());
        // Percentages should be 0.0, not NaN or Infinity
        assertEquals(0.0, result.getOccupiedPercentage());
    }

    @Test
    public void testGetStatsNoReservations() {
        // Edge case: Rooms exist, but 0 reservations
        LocalDate today = LocalDate.now();
        
        when(roomRepo.countTotalRooms()).thenReturn(10L);
        when(resRepo.countOccupiedRoomsByDate(today)).thenReturn(0L);
        when(resRepo.findStartDatesByDate(today, any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        DashboardStatsDTO result = statsService.getStats(today);
        //Verify
        assertEquals(0.0, result.getOccupiedPercentage());
        assertEquals(100.0, result.getFreePercentage());
        assertEquals(0.0, result.getRoomsWithSoftwarePercentage());
    }
}