package com.urjcservice.backend.rest;

import com.urjcservice.backend.rest.ReservationRestController.ReservationRequest;
import com.urjcservice.backend.service.EmailService;      
import com.urjcservice.backend.service.FileStorageService;
import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.service.ReservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

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

    @MockBean 
    private EmailService emailService;
    
    @MockBean
    private FileStorageService fileStorageService;

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
        mockMvc.perform(post("/api/reservations")
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
        mockMvc.perform(post("/api/reservations")
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






    @Test
    @WithMockUser(roles = "USER")
    public void testCheckAvailability_Success() throws Exception {
        // GIVEN
        LocalDate date = LocalDate.of(2026, 1, 31);
        given(reservationService.getActiveReservationsForRoomAndDate(eq(1L), eq(date)))
                .willReturn(List.of(mockReservation));

        // WHEN & THEN
        mockMvc.perform(get("/api/reservations/check-availability")
                .param("roomId", "1")
                .param("date", "2026-01-31")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(jsonPath("$.length()").value(1));
    }

    // --- Limit hour test ---

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    public void testCreateReservation_DailyLimitExceeded_Returns400() throws Exception {
        // GIVEN
        given(reservationService.createReservation(any(ReservationRequest.class), eq("user@test.com")))
                .willThrow(new IllegalArgumentException("Daily limit exceeded. Used: 180m."));

        ReservationRequest request = new ReservationRequest();
        request.setRoomId(1L);
        request.setStartDate(new Date()); 
        request.setEndDate(new Date());

        // WHEN & THEN
        mockMvc.perform(post("/api/reservations") 
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // 400 Bad Request
                .andExpect(content().string("Daily limit exceeded. Used: 180m."));
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    public void testCreateReservation_RoomOccupied_Returns400() throws Exception {
        // GIVEN overlaping
        given(reservationService.createReservation(any(ReservationRequest.class), eq("user@test.com")))
                .willThrow(new RuntimeException("The room is already reserved for this time."));

        ReservationRequest request = new ReservationRequest();
        request.setRoomId(1L);
        request.setStartDate(new Date());
        request.setEndDate(new Date());

        // WHEN & THEN
        mockMvc.perform(post("/api/reservations") 
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // 400 Bad Request
                .andExpect(content().string("The room is already reserved for this time."));
    }



    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /admin/{id}/cancel - Should call service and return 200")
    public void testCancelReservationAsAdmin_Success() throws Exception {
        // GIVEN
        Long reservationId = 1L;
        String reason = "Violation of rules";
        
        Reservation mockRes = new Reservation();
        mockRes.setId(reservationId);
        mockRes.setCancelled(true);
        mockRes.setAdminModificationReason(reason);

        // Mockeamos la llamada al servicio
        given(reservationService.adminCancelReservation(eq(reservationId), eq(reason)))
                .willReturn(Optional.of(mockRes));

        // Objeto JSON para el body
        String jsonBody = "{\"reason\": \"" + reason + "\"}";

        // WHEN & THEN
        mockMvc.perform(patch("/api/reservations/admin/{id}/cancel", reservationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .with(csrf())) // Importante si tienes seguridad habilitada
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cancelled").value(true))
                .andExpect(jsonPath("$.adminModificationReason").value(reason));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /admin/{id}/cancel - Should use default reason if empty")
    public void testCancelReservationAsAdmin_EmptyReason_DefaultApplied() throws Exception {
        // GIVEN
        Long reservationId = 1L;
        String defaultReason = "Administrative cancellation without specified reason.";
        
        Reservation mockRes = new Reservation();
        mockRes.setAdminModificationReason(defaultReason);

        given(reservationService.adminCancelReservation(eq(reservationId), anyString()))
                .willReturn(Optional.of(mockRes));

        String jsonBody = "{}"; 

        // WHEN
        mockMvc.perform(patch("/api/reservations/admin/{id}/cancel", reservationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .with(csrf()))
                .andExpect(status().isOk());
        
        verify(reservationService).adminCancelReservation(eq(reservationId), eq(defaultReason));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /admin/{id} - Should update reservation details")
    public void testUpdateReservationAsAdmin_Success() throws Exception {
        // GIVEN
        Long reservationId = 10L;
        
        String jsonBody = """
            {
                "roomId": 5,
                "date": "2026-12-25",
                "startTime": "10:00",
                "endTime": "12:00",
                "adminReason": "Room maintenance"
            }
        """;

        Reservation updatedRes = new Reservation();
        updatedRes.setId(reservationId);
        updatedRes.setAdminModificationReason("Room maintenance");

        given(reservationService.adminUpdateReservation(
                eq(reservationId), 
                eq(5L), 
                any(LocalDate.class), 
                any(LocalTime.class), 
                any(LocalTime.class), 
                eq("Room maintenance")
        )).willReturn(Optional.of(updatedRes));

        // WHEN & THEN
        mockMvc.perform(put("/api/reservations/admin/{id}", reservationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.adminModificationReason").value("Room maintenance"));
    }



    @Test
    @DisplayName("GET /verify - Success")
    @WithMockUser(username = "user", roles = "USER") //for not 401 unautorized
    public void testVerifyReservation_Success() throws Exception {
        String token = "valid-token";
        doNothing().when(reservationService).verifyReservation(token);

        mockMvc.perform(get("/api/reservations/verify").param("token", token))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("confirmed successfully")));
    }

    @Test
    @DisplayName("GET /verify - Failure")
    @WithMockUser(username = "user", roles = "USER") //for not 401 unautorized
    public void testVerifyReservation_Failure() throws Exception {
        String token = "invalid";
        doThrow(new RuntimeException("Invalid token")).when(reservationService).verifyReservation(token);

        mockMvc.perform(get("/api/reservations/verify").param("token", token))
                .andExpect(status().isBadRequest());
    }

}