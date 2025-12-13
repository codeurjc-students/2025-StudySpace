package com.urjcservice.backend.service;

import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.Software;
import com.urjcservice.backend.repositories.ReservationRepository;
import com.urjcservice.backend.repositories.RoomRepository;
import com.urjcservice.backend.repositories.SoftwareRepository;
import com.urjcservice.backend.rest.RoomRestController.RoomRequest; // Internal DTO 
import com.urjcservice.backend.rest.RoomRestController;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.List;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
        when(roomRepository.findAll()).thenReturn(Arrays.asList(new Room(), new Room()));
        List<Room> result = roomService.findAll();
        assertEquals(2, result.size());
    }


    

    @Test
    void testUpdateRoom_DisablingRoom_ShouldDeleteFutureReservations() {
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
        verify(reservationRepository, times(1)).deleteByRoomIdAndEndDateAfter(eq(1L), any(Date.class));
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
}