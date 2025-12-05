package com.urjcservice.backend.service;

import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.Software;
import com.urjcservice.backend.repositories.RoomRepository;
import com.urjcservice.backend.repositories.SoftwareRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SoftwareServiceTest {
  @Mock
    private SoftwareRepository softwareRepository;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private SoftwareService softwareService;

    @Test
    public void testDeleteSoftwareRemovesAssociations() {
        //delete software from a room
        
        Long softId = 1L;
        Software software = new Software();
        software.setId(softId);
        
        Room room = new Room();
        room.setId(10L);
        room.setSoftware(new ArrayList<>(Arrays.asList(software)));
        software.setRooms(new ArrayList<>(Arrays.asList(room)));

        when(softwareRepository.findById(softId)).thenReturn(Optional.of(software));
        
        softwareService.deleteById(softId);

        // Verify
        verify(roomRepository).save(room);
        verify(softwareRepository).delete(software);
        assertFalse(room.getSoftware().contains(software));
    }

    @Test
    public void testPatchSoftware() {
        Long id = 1L;
        Software existing = new Software();
        existing.setName("Old Name");
        existing.setVersion(1.0f);

        Software patch = new Software();
        patch.setName("New Name");
        

        when(softwareRepository.findById(id)).thenReturn(Optional.of(existing));
        when(softwareRepository.save(any(Software.class))).thenAnswer(i -> i.getArguments()[0]);

        Optional<Software> result = softwareService.patchSoftware(id, patch);

        // Verify
        assertTrue(result.isPresent());
        assertEquals("New Name", result.get().getName()); //updated
        assertEquals(1.0f, result.get().getVersion());    //old
    }
}
