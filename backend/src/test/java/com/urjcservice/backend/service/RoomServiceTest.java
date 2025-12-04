package com.urjcservice.backend.service;

import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.Software;
import com.urjcservice.backend.repositories.RoomRepository;
import com.urjcservice.backend.repositories.SoftwareRepository;
import com.urjcservice.backend.rest.RoomRestController.RoomRequest; // Internal DTO 
import com.urjcservice.backend.rest.RoomRestController;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

//mock to avoid using the real database
@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private SoftwareRepository softwareRepository;


    @InjectMocks
    private RoomRestController roomController; 

    @Test
    public void testCreateRoomWithSoftware() {
        
        RoomRequest request = new RoomRequest();
        request.name = "Aula Nueva";
        request.capacity = 30;
        request.camp = Room.CampusType.MOSTOLES;
        request.softwareIds = Arrays.asList(1L, 2L);

        Software soft1 = new Software(); soft1.setId(1L);
        Software soft2 = new Software(); soft2.setId(2L);

        
        when(softwareRepository.findAllById(request.softwareIds)).thenReturn(Arrays.asList(soft1, soft2));
        when(roomRepository.save(any(Room.class))).thenAnswer(i -> i.getArguments()[0]);


        ResponseEntity<Room> response = roomController.createRoom(request);

        //verify
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Aula Nueva", response.getBody().getName());
        assertEquals(2, response.getBody().getSoftware().size()); 
    }

    @Test
    public void testUpdateRoomNotFound() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        RoomRequest request = new RoomRequest();
        ResponseEntity<Room> response = roomController.updateRoom(99L, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}