package com.urjcservice.backend.rest;

import com.urjcservice.backend.controller.ReservationController.ReservationRequest;
import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.service.ReservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ReservationRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationService reservationService; 

    @Autowired
    private ObjectMapper objectMapper;

    private Reservation mockReservation;

    @BeforeEach
    void setUp() {
        mockReservation = new Reservation();
        mockReservation.setId(1L);
        mockReservation.setReason("Test Reservation");
        mockReservation.setStartDate(new Date());
        mockReservation.setEndDate(new Date());
        
        User user = new User();
        user.setEmail("user@test.com");
        mockReservation.setUser(user);
        
        Room room = new Room();
        room.setName("Test Room");
        mockReservation.setRoom(room);
    }

    // --- TESTS RESERVATION CONTROLLER (WEB)---

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    public void testGetMyReservations() throws Exception {
        // GIVEN
        Page<Reservation> page = new PageImpl<>(List.of(mockReservation));
        given(reservationService.getReservationsByUserEmail(eq("user@test.com"), any(Pageable.class)))
                .willReturn(page);

        // WHEN & THEN
        mockMvc.perform(get("/api/reservations/my-reservations")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].reason").value("Test Reservation"));
    }
    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    public void testGetMyReservations_Pagination() throws Exception {
        // GIVEN
        Page<Reservation> page = new PageImpl<>(List.of(mockReservation));
        given(reservationService.getReservationsByUserEmail(eq("user@test.com"), any(Pageable.class)))
                .willReturn(page);

        // WHEN & THEN
        mockMvc.perform(get("/api/reservations/my-reservations")
                .param("page", "0")
                .param("size", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
    

    // --- TESTS RESERVATION REST CONTROLLER (REST) ---

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAllReservationsAsAdmin() throws Exception {
        // GIVEN
        Page<Reservation> page = new PageImpl<>(List.of(mockReservation));
        given(reservationService.findAll(any(Pageable.class))).willReturn(page);

        // WHEN & THEN
        mockMvc.perform(get("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    public void testCreateReservation_Success() throws Exception {
        // GIVEN
        ReservationRequest request = new ReservationRequest();
        request.setRoomId(1L);
        request.setReason("New Meeting");
        // dates dummy
        request.setStartDate(new Date());
        request.setEndDate(new Date());

        given(reservationService.createReservation(any(ReservationRequest.class), eq("user@test.com")))
                .willReturn(mockReservation);

        // WHEN & THEN
        mockMvc.perform(post("/api/reservations/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()) // 201
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    public void testCreateReservation_InvalidData_BadRequest() throws Exception {
        // GIVEN
        given(reservationService.createReservation(any(), anyString()))
                .willThrow(new RuntimeException("Invalid dates"));

        String json = "{\"roomId\": 1, \"reason\": \"Bad Dates\"}";

        // WHEN & THEN
        mockMvc.perform(post("/api/reservations/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest()); // 400
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateReservation_Success() throws Exception {
        // GIVEN
        given(reservationService.updateReservation(eq(1L), any(Reservation.class)))
                .willReturn(Optional.of(mockReservation));

        mockMvc.perform(put("/api/reservations/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockReservation)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testPatchReservation_Success() throws Exception {
        // GIVEN
        given(reservationService.patchReservation(eq(1L), any(Reservation.class)))
                .willReturn(Optional.of(mockReservation));

        mockMvc.perform(patch("/api/reservations/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\": \"New Reason\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteReservation_Success() throws Exception {
        // GIVEN
        given(reservationService.deleteById(1L)).willReturn(Optional.of(mockReservation));

        // WHEN & THEN
        mockMvc.perform(delete("/api/reservations/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "owner@test.com", roles = "USER")
    public void testCancelOwnReservation_Success() throws Exception {
        // GIVEN
        mockReservation.setCancelled(true);
        given(reservationService.cancelByIdSecure(eq(1L), eq("owner@test.com"), eq(false)))
                .willReturn(Optional.of(mockReservation));

        // WHEN & THEN
        mockMvc.perform(patch("/api/reservations/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cancelled").value(true));
    }

    @Test
    @WithMockUser(username = "other@test.com", roles = "USER")
    public void testCancelOtherUserReservation_Forbidden() throws Exception {
        // GIVEN
        given(reservationService.cancelByIdSecure(eq(1L), eq("other@test.com"), eq(false)))
                .willThrow(new AccessDeniedException("Not owner"));

        // WHEN & THEN
        mockMvc.perform(patch("/api/reservations/1/cancel"))
                .andExpect(status().isForbidden()); // 403
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testCancelOtherUserReservation_AsAdmin_Success() throws Exception {
        // GIVEN
        mockReservation.setCancelled(true);
        //isAdmin = true
        given(reservationService.cancelByIdSecure(eq(1L), eq("admin"), eq(true)))
                .willReturn(Optional.of(mockReservation));

        // WHEN & THEN
        mockMvc.perform(patch("/api/reservations/1/cancel"))
                .andExpect(status().isOk());
    }
    
    
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetReservationById_Found() throws Exception {
        given(reservationService.findById(1L)).willReturn(Optional.of(mockReservation));

        mockMvc.perform(get("/api/reservations/1"))
                .andExpect(status().isOk());
    
    
    
    }

    
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAllReservations() throws Exception {
        Page<Reservation> page = new PageImpl<>(List.of(mockReservation));
        given(reservationService.findAll(any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // --- UPDATE (PUT) TESTS ---

    

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateReservation_NotFound() throws Exception {
        given(reservationService.updateReservation(eq(99L), any(Reservation.class)))
                .willReturn(Optional.empty());

        mockMvc.perform(put("/api/reservations/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockReservation)))
                .andExpect(status().isNotFound());
    }

    // --- DELETE TESTS ---

    

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteReservation_NotFound() throws Exception {
        given(reservationService.deleteById(99L)).willReturn(Optional.empty());

        mockMvc.perform(delete("/api/reservations/99"))
                .andExpect(status().isNotFound());
    }

    // --- CANCEL TESTS (Security Logic) ---

    @Test
    @WithMockUser(username = "owner@test.com")
    public void testCancelReservation_Success() throws Exception {
        mockReservation.setCancelled(true);
        // is admin = false
        given(reservationService.cancelByIdSecure(eq(1L), eq("owner@test.com"), eq(false)))
                .willReturn(Optional.of(mockReservation));

        mockMvc.perform(patch("/api/reservations/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cancelled").value(true));
    }

    @Test
    @WithMockUser(username = "hacker@test.com")
    public void testCancelReservation_Forbidden() throws Exception {
        given(reservationService.cancelByIdSecure(eq(1L), eq("hacker@test.com"), eq(false)))
                .willThrow(new AccessDeniedException("Not allowed"));

        mockMvc.perform(patch("/api/reservations/1/cancel"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "owner@test.com")
    public void testCancelReservation_NotFound() throws Exception {
        given(reservationService.cancelByIdSecure(eq(99L), anyString(), anyBoolean()))
                .willReturn(Optional.empty());

        mockMvc.perform(patch("/api/reservations/99/cancel"))
                .andExpect(status().isNotFound());
    }

}