package com.urjcservice.backend.service;

import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.Software;
import com.urjcservice.backend.repositories.RoomRepository;
import com.urjcservice.backend.repositories.SoftwareRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.List;

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
    @DisplayName("Test Find All Paginated")
    public void testFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Software s1 = new Software(); s1.setName("Java");
        Page<Software> page = new PageImpl<>(List.of(s1));

        when(softwareRepository.findAll(pageable)).thenReturn(page);

        Page<Software> result = softwareService.findAll(pageable);

        assertEquals(1, result.getContent().size());
        assertEquals("Java", result.getContent().get(0).getName());
        verify(softwareRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Test Find By ID")
    public void testFindById() {
        Software s = new Software();
        s.setId(1L);
        when(softwareRepository.findById(1L)).thenReturn(Optional.of(s));

        Optional<Software> result = softwareService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    @DisplayName("Test Save Software")
    public void testSave() {
        Software s = new Software();
        s.setName("Python");
        when(softwareRepository.save(s)).thenReturn(s);

        Software result = softwareService.save(s);

        assertNotNull(result);
        verify(softwareRepository).save(s);
    }

    // --- TEST DELETE  ---

    @Test
    public void testDeleteSoftwareRemovesAssociations() {
        
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
    @DisplayName("Test Delete: Software with no rooms (Null check coverage)")
    public void testDeleteSoftware_NoRooms() {
        Long softId = 1L;
        Software software = new Software();
        software.setId(softId);
        software.setRooms(null); 

        when(softwareRepository.findById(softId)).thenReturn(Optional.of(software));

        softwareService.deleteById(softId);

        verify(roomRepository, never()).save(any());
        verify(softwareRepository).delete(software);
    }

    @Test
    @DisplayName("Test Delete: Software not found")
    public void testDeleteSoftware_NotFound() {
        when(softwareRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Software> result = softwareService.deleteById(99L);

        assertTrue(result.isEmpty());
        verify(softwareRepository, never()).delete(any());
    }

    // --- TEST UPDATE ---

    @Test
    @DisplayName("Test Update Software: Success")
    public void testUpdateSoftware() {
        Long id = 1L;
        Software existing = new Software();
        existing.setId(id);
        existing.setName("Old Name");

        Software updatedData = new Software();
        updatedData.setName("New Name");
        updatedData.setVersion(2.0f); 
        updatedData.setDescription("New Desc");

        when(softwareRepository.findById(id)).thenReturn(Optional.of(existing));
        when(softwareRepository.save(any(Software.class))).thenAnswer(i -> i.getArguments()[0]);

        Optional<Software> result = softwareService.updateSoftware(id, updatedData);

        assertTrue(result.isPresent());
        assertEquals("New Name", result.get().getName());
        assertEquals(2.0f, result.get().getVersion()); 
        assertEquals("New Desc", result.get().getDescription());
    }

    @Test
    @DisplayName("Test Update Software: Not Found")
    public void testUpdateSoftware_NotFound() {
        when(softwareRepository.findById(99L)).thenReturn(Optional.empty());
        Optional<Software> result = softwareService.updateSoftware(99L, new Software());
        assertTrue(result.isEmpty());
    }

    // --- TEST PATCH  ---

    @Test
    @DisplayName("Test Patch: Updates only non-null fields")
    public void testPatchSoftware() {
        Long id = 1L;
        Software existing = new Software();
        existing.setName("Old Name");
        existing.setVersion(1.0f);       
        existing.setDescription("Old Desc"); 

        Software patch = new Software();
        patch.setName("New Name"); 
        patch.setVersion(null); 
        patch.setDescription(null); 

        when(softwareRepository.findById(id)).thenReturn(Optional.of(existing));
        when(softwareRepository.save(any(Software.class))).thenAnswer(i -> i.getArguments()[0]);

        Optional<Software> result = softwareService.patchSoftware(id, patch);

        assertTrue(result.isPresent());
        assertEquals("New Name", result.get().getName());
        assertEquals(1.0f, result.get().getVersion()); 
        assertEquals("Old Desc", result.get().getDescription());
    }
    
    @Test
    @DisplayName("Test Patch: Not Found")
    public void testPatchSoftware_NotFound() {
        when(softwareRepository.findById(99L)).thenReturn(Optional.empty());
        Optional<Software> result = softwareService.patchSoftware(99L, new Software());
        assertTrue(result.isEmpty());
    }


}
