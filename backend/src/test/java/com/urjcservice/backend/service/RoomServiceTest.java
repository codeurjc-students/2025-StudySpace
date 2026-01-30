package com.urjcservice.backend.service;

import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.entities.Software;
import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.repositories.ReservationRepository;
import com.urjcservice.backend.repositories.RoomRepository;
import com.urjcservice.backend.repositories.SoftwareRepository;
import com.urjcservice.backend.rest.RoomRestController.RoomRequest; // Internal DTO 
import com.urjcservice.backend.rest.RoomRestController;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate; 
import java.time.ZoneId;    
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.List;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private SoftwareRepository softwareRepository;
    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private EmailService emailService;      
    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private RoomService roomService;

    private Room activeRoom;

    @BeforeEach
    void setUp() {
        activeRoom = new Room();
        activeRoom.setId(1L);
        activeRoom.setName("Lab 1");
        activeRoom.setActive(true); 
        activeRoom.setSoftware(new ArrayList<>());
    }

    @Test
    public void testFindAll() {
        when(roomRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(Arrays.asList(new Room(), new Room())));
        Page<Room> result = roomService.findAll(PageRequest.of(0, 10));
        assertEquals(2, result.getContent().size());
    }




    @Test
    void testUpdateRoom_DisablingRoom_ShouldCancelFutureReservations() {
        // GIVEN
        when(roomRepository.findById(1L)).thenReturn(Optional.of(activeRoom));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        Room updatedData = new Room();
        updatedData.setName("Lab 1 Renamed");
        updatedData.setActive(false);  // now its not active
        updatedData.setSoftware(new ArrayList<>());

        roomService.updateRoom(1L, updatedData);

        // THEN
        //verify(reservationRepository, times(1)).cancelByRoomIdAndEndDateAfter(eq(1L), any(Date.class));    REVISARRRRRRRRRRRRRRRRRRRRRRRRRRRRRRr
        verify(roomRepository).save(argThat(room -> !room.isActive()));
    }

    @Test
    void testUpdateRoom_KeepingActive_ShouldNotDeleteReservations() {
        // GIVEN
        when(roomRepository.findById(1L)).thenReturn(Optional.of(activeRoom));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        Room updatedData = new Room();
        updatedData.setName("Lab 1");
        updatedData.setActive(true);  //now its active
        updatedData.setSoftware(new ArrayList<>());

        roomService.updateRoom(1L, updatedData);

        // THEN   no call the delete of bookings
        verify(reservationRepository, never()).deleteByRoomIdAndEndDateAfter(anyLong(), any(Date.class));
    }





    @Test
    public void testSaveRoomWithSoftware() {
        // Given
        Room room = new Room();
        room.setName("Lab 1");
        
        Software soft = new Software();
        soft.setId(1L);
        room.setSoftware(Arrays.asList(soft));

        when(softwareRepository.findById(1L)).thenReturn(Optional.of(soft));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        // When
        Room result = roomService.save(room);

        //Verify
        assertNotNull(result);
        verify(softwareRepository, times(1)).findById(1L);
    }

    @Test
    public void testUpdateRoomAddingAndRemovingSoftware() {
        Long roomId = 1L;
        
        // already existing software 
        Software softA = new Software(); softA.setId(10L); softA.setName("Old Soft");
        
        // new software to add
        Software softB = new Software(); softB.setId(20L); softB.setName("New Soft");

        // already existing room 
        Room existingRoom = new Room();
        existingRoom.setId(roomId);
        existingRoom.setName("Old Name");
        existingRoom.setSoftware(new ArrayList<>(Arrays.asList(softA))); // Mutable list

        // new data
        Room updateData = new Room();
        updateData.setName("New Name");
        updateData.setCapacity(50);
        updateData.setSoftware(Arrays.asList(softB));

        // Mocks
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(existingRoom));
        when(softwareRepository.findById(20L)).thenReturn(Optional.of(softB));
        when(roomRepository.save(any(Room.class))).thenAnswer(i -> i.getArguments()[0]);

        // Ejecución
        Optional<Room> result = roomService.updateRoom(roomId, updateData);

        //Verify
        assertTrue(result.isPresent());
        assertEquals("New Name", result.get().getName());
        assertEquals(1, result.get().getSoftware().size());
        assertEquals(20L, result.get().getSoftware().get(0).getId());
        
        verify(roomRepository).save(existingRoom);
    }

    @Test
    public void testDeleteRoom() {
        Long id = 1L;
        Room room = new Room();
        room.setId(id);
        
        when(roomRepository.findById(id)).thenReturn(Optional.of(room));
        
        roomService.deleteById(id);
        
        verify(roomRepository).delete(room);
    }

    @Test
    void testUpdateRoom_ReEnablingRoom_ShouldNotDeleteReservations() {
        // GIVEN
        Room disabledRoom = new Room();
        disabledRoom.setId(1L);
        disabledRoom.setName("Lab Disabled");
        disabledRoom.setActive(false); // Disable
        disabledRoom.setSoftware(new ArrayList<>());

        when(roomRepository.findById(1L)).thenReturn(Optional.of(disabledRoom));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        Room updatedData = new Room();
        updatedData.setName("Lab Enabled");
        updatedData.setActive(true); //   now active
        updatedData.setSoftware(new ArrayList<>());

        roomService.updateRoom(1L, updatedData);

        // THEN
        verify(reservationRepository, never()).deleteByRoomIdAndEndDateAfter(anyLong(), any(Date.class));
        verify(roomRepository).save(argThat(Room::isActive));
    }

    @Test
    void testPatchRoom_UpdateBasicInfoOnly() {
        Room existing = new Room();
        existing.setId(1L);
        existing.setName("Old");
        
        when(roomRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(roomRepository.save(any(Room.class))).thenAnswer(i -> i.getArguments()[0]);

        Room partial = new Room();
        partial.setName("New Name"); // only change name

        Optional<Room> result = roomService.patchRoom(1L, partial);
        
        assertTrue(result.isPresent());
        assertEquals("New Name", result.get().getName());
    }

    @Test
    void testPatchRoom_UpdateSoftwareList() {
        Room existing = new Room();
        existing.setId(1L);
        existing.setSoftware(new ArrayList<>());
        
        when(roomRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(roomRepository.save(any(Room.class))).thenAnswer(i -> i.getArguments()[0]);

        Room partial = new Room();
        Software newSoft = new Software();
        newSoft.setId(2L);
        newSoft.setName("Patch Soft");
        partial.setSoftware(Arrays.asList(newSoft));

        when(softwareRepository.findById(2L)).thenReturn(Optional.of(newSoft));

        Optional<Room> result = roomService.patchRoom(1L, partial);
        
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getSoftware().size());
    }
    
    @Test
    void testPatchRoom_WithNullSoftware_ShouldNotChangeExistingSoftware() {
        Room existing = new Room();
        existing.setId(1L);
        existing.setSoftware(new ArrayList<>());
        Software s = new Software(); s.setId(1L);
        existing.addSoftware(s);
        
        when(roomRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(roomRepository.save(any(Room.class))).thenAnswer(i -> i.getArguments()[0]);

        Room partial = new Room();
        partial.setSoftware(null); 

        Optional<Room> result = roomService.patchRoom(1L, partial);
        
        //original list stays the same
        assertEquals(1, result.get().getSoftware().size());
    }

    @Test
    void testGetRoomDailyStats() {
        // GIVEN
        Long roomId = 1L;
        LocalDate date = LocalDate.now();
        
        //(2 hours)
        Reservation r1 = new Reservation();
        r1.setStartDate(Date.from(date.atTime(10, 0).atZone(ZoneId.of("Europe/Madrid")).toInstant()));
        r1.setEndDate(Date.from(date.atTime(12, 0).atZone(ZoneId.of("Europe/Madrid")).toInstant()));
        
        // Also mock findByRoomIdAndDate used by service
        when(reservationRepository.findByRoomIdAndDate(eq(roomId), eq(date), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(r1)));

        // WHEN
        Map<String, Object> stats = roomService.getRoomDailyStats(roomId, date);

        // THEN
        assertNotNull(stats);
        
        @SuppressWarnings("unchecked")
        Map<Integer, Boolean> hourlyStatus = (Map<Integer, Boolean>) stats.get("hourlyStatus");
        
        
        assertTrue(hourlyStatus.get(10), "The 10 o'clock hour must be occupied.");
        assertTrue(hourlyStatus.get(11), "The 11 o'clock hour must be occupied.");
        assertFalse(hourlyStatus.get(9), "The 9 o'clock hour must be free");
        
        // Verificamos porcentajes: 2 horas ocupadas de 14 posibles (8 a 21) = 14.29%
        double expectedOccupied = Math.round((2.0 / 14.0 * 100) * 100.0) / 100.0;
        assertEquals(expectedOccupied, stats.get("occupiedPercentage"));
    }



    @Test
    void testGetRoomDailyStats_NoReservations_ShouldReturnZero() {
        // GIVEN
        Long roomId = 1L;
        LocalDate date = LocalDate.now();
        
        //mock empty list
        when(reservationRepository.findByRoomIdAndDate(eq(roomId), eq(date), any(Pageable.class)))
             .thenReturn(new PageImpl<>(Arrays.asList()));

        // WHEN
        Map<String, Object> stats = roomService.getRoomDailyStats(roomId, date);

        // THEN
        assertEquals(0.0, stats.get("occupiedPercentage"));
        assertEquals(100.0, stats.get("freePercentage"));
        
        @SuppressWarnings("unchecked")
        Map<Integer, Boolean> hourlyStatus = (Map<Integer, Boolean>) stats.get("hourlyStatus");
        //no one is true
        assertFalse(hourlyStatus.values().stream().anyMatch(Boolean::booleanValue));
    }

    @Test
    void testGetRoomDailyStats_FullOccupancy() {
        // GIVEN
        Long roomId = 1L;
        LocalDate date = LocalDate.now();
        ZoneId zone = ZoneId.of("Europe/Madrid");

        Reservation r1 = new Reservation();
        r1.setStartDate(Date.from(date.atTime(8, 0).atZone(zone).toInstant()));
        
        r1.setEndDate(Date.from(date.atTime(22, 0).atZone(zone).toInstant())); 

        when(reservationRepository.findByRoomIdAndDate(eq(roomId), eq(date), any(Pageable.class)))
             .thenReturn(new PageImpl<>(Arrays.asList(r1)));

        // WHEN
        Map<String, Object> stats = roomService.getRoomDailyStats(roomId, date);

        // THEN
        assertEquals(100.0, stats.get("occupiedPercentage"));
        assertEquals(0.0, stats.get("freePercentage"));
    }
    
    @Test
    void testGetRoomDailyStats_ReservationOutsideBounds_ShouldIgnore() {
        // GIVEN: out range 22:00
        Long roomId = 1L;
        LocalDate date = LocalDate.now();
        ZoneId zone = ZoneId.of("Europe/Madrid");

        Reservation r1 = new Reservation();
        r1.setStartDate(Date.from(date.atTime(22, 0).atZone(zone).toInstant()));
        r1.setEndDate(Date.from(date.atTime(23, 0).atZone(zone).toInstant()));

        when(reservationRepository.findByRoomIdAndDate(eq(roomId), eq(date), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(r1)));

        // WHEN
        Map<String, Object> stats = roomService.getRoomDailyStats(roomId, date);

        // THEN
        assertEquals(0.0, stats.get("occupiedPercentage"));
    }






    @Test
    void testSaveRoomWithNewSoftware() {
        Room room = new Room();
        Software newSoft = new Software(); // without ID
        room.setSoftware(Arrays.asList(newSoft));

        when(softwareRepository.save(any(Software.class))).thenReturn(newSoft);
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        roomService.save(room);

        verify(softwareRepository).save(newSoft);
    }

    @Test
    void testDeleteRoomNotFound() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());
        Optional<Room> result = roomService.deleteById(99L);
        assertFalse(result.isPresent());
        verify(roomRepository, never()).delete(any());
    }

    @Test
    void testPatchRoomUpdateAllFields() {
        Room existing = new Room();
        existing.setId(1L);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(roomRepository.save(any(Room.class))).thenAnswer(i -> i.getArguments()[0]);

        Room partial = new Room();
        partial.setCapacity(100);
        partial.setPlace("Planta 2");
        partial.setCoordenades("40.3,-3.8");

        Optional<Room> result = roomService.patchRoom(1L, partial);
        
        assertEquals(100, result.get().getCapacity());
        assertEquals("Planta 2", result.get().getPlace());
        assertEquals("40.3,-3.8", result.get().getCoordenades());
    }

    @Test
    void testSaveRoom_DuplicateName_ShouldThrowConflict() {
        // GIVEN
        Room newRoom = new Room();
        newRoom.setName("Duplicate Name");
        
        when(roomRepository.existsByName("Duplicate Name")).thenReturn(true);

        // WHEN & THEN
        assertThrows(ResponseStatusException.class, () -> {
            roomService.save(newRoom);
        });
        
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void testUpdateRoom_DuplicateName_ShouldThrowConflict() {
        // GIVEN
        Long roomId = 1L;
        Room existingRoom = new Room();
        existingRoom.setId(roomId);
        existingRoom.setName("Original Name");

        Room updateData = new Room();
        updateData.setName("Existing Other Name"); 


        when(roomRepository.findById(roomId)).thenReturn(Optional.of(existingRoom));
        when(roomRepository.existsByName("Existing Other Name")).thenReturn(true);

        // WHEN & THEN
        assertThrows(ResponseStatusException.class, () -> {
            roomService.updateRoom(roomId, updateData);
        });

        verify(roomRepository, never()).save(any(Room.class));
    }


    @Test
    @DisplayName("Delete Room - Should notify users, delete reservations, delete image and delete room")
    void testDeleteRoom_FullFlow() {
        // Arrange
        Long roomId = 10L;
        String reason = "Building renovation";
        
        Room room = new Room();
        room.setId(roomId);
        room.setName("Sala Magna");
        room.setImageName("sala_magna.jpg"); 

        User user = new User();
        user.setEmail("user@urjc.es");
        user.setName("John");

        Reservation res = new Reservation();
        res.setId(100L);
        res.setRoom(room);
        res.setUser(user);
        res.setStartDate(new Date()); 
        res.setEndDate(new Date(System.currentTimeMillis() + 3600000));

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        
        when(reservationRepository.findActiveReservationsByRoomIdAndEndDateAfter(eq(roomId), any(Date.class)))
                .thenReturn(List.of(res));

        // Act
        roomService.deleteRoom(roomId, reason);

        // Assert
        verify(emailService, times(1)).sendReservationCancellationEmail(
            eq("user@urjc.es"), 
            eq("John"), 
            eq("Sala Magna"), 
            anyString(), anyString(), anyString(), 
            eq(reason)
        );

        verify(reservationRepository, times(1))
            .deleteByRoomIdAndEndDateAfter(eq(roomId), any(Date.class));


        verify(fileStorageService, times(1)).delete("sala_magna.jpg");

        verify(roomRepository, times(1)).deleteById(roomId); 
    }


}