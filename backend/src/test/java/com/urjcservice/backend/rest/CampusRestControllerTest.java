package com.urjcservice.backend.rest;

import com.urjcservice.backend.entities.Campus;
import com.urjcservice.backend.service.CampusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampusRestControllerTest {

    @Mock
    private CampusService campusService;

    @InjectMocks
    private CampusRestController campusRestController;

    private Campus mockCampus;

    @BeforeEach
    void setUp() {
        mockCampus = new Campus();
        mockCampus.setId(1L);
        mockCampus.setName("Fuenlabrada");
        mockCampus.setCoordinates("40.28, -3.82");
    }

    @Test
    void getAllCampuses_ShouldReturnList() {
        when(campusService.findAll()).thenReturn(List.of(mockCampus));

        ResponseEntity<List<Campus>> response = campusRestController.getAllCampuses();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(campusService, times(1)).findAll();
    }

    @Test
    void createCampus_ShouldReturnCreatedCampus() {
        when(campusService.save(any(Campus.class))).thenReturn(mockCampus);

        ResponseEntity<Campus> response = campusRestController.createCampus(mockCampus);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Fuenlabrada", response.getBody().getName());
        verify(campusService, times(1)).save(mockCampus);
    }

    @Test
    void updateCampus_WhenCampusExists_ShouldReturnOk() {
        when(campusService.update(eq(1L), any(Campus.class))).thenReturn(Optional.of(mockCampus));

        ResponseEntity<Campus> response = campusRestController.updateCampus(1L, mockCampus);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockCampus, response.getBody());
    }

    @Test
    void updateCampus_WhenCampusDoesNotExist_ShouldReturnNotFound() {
        when(campusService.update(eq(99L), any(Campus.class))).thenReturn(Optional.empty());

        ResponseEntity<Campus> response = campusRestController.updateCampus(99L, mockCampus);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteCampus_WhenCampusExists_ShouldReturnOk() {
        when(campusService.delete(1L)).thenReturn(Optional.of(mockCampus));

        ResponseEntity<Campus> response = campusRestController.deleteCampus(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockCampus, response.getBody());
    }

    @Test
    void deleteCampus_WhenCampusDoesNotExist_ShouldReturnNotFound() {
        when(campusService.delete(99L)).thenReturn(Optional.empty());

        ResponseEntity<Campus> response = campusRestController.deleteCampus(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}