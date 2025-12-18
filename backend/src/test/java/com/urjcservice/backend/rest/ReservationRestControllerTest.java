package com.urjcservice.backend.rest;

import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.repositories.ReservationRepository;
import com.urjcservice.backend.repositories.RoomRepository;
import com.urjcservice.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ReservationRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long testRoomId;
    private Long testReservationId;
    private final String testUserEmail = "test.reservas@example.com";
    private final String otherUserEmail = "other@example.com";

    @BeforeEach
    void setUp() {
        // Limpiamos para evitar conflictos entre tests
        reservationRepository.deleteAll();
        userRepository.deleteAll();
        roomRepository.deleteAll();

        // Crear usuario principal
        User user = new User();
        user.setName("Tester");
        user.setEmail(testUserEmail);
        user.setEncodedPassword("pass");
        user.setRoles(Arrays.asList("USER"));
        userRepository.save(user);

        // Crear otro usuario para pruebas de permisos
        User otherUser = new User();
        otherUser.setName("Other");
        otherUser.setEmail(otherUserEmail);
        otherUser.setEncodedPassword("pass");
        otherUser.setRoles(Arrays.asList("USER"));
        userRepository.save(otherUser);

        // Crear sala
        Room room = new Room();
        room.setName("Aula Test");
        room.setCapacity(20);
        room.setActive(true);
        room.setCamp(Room.CampusType.MOSTOLES);
        room = roomRepository.save(room);
        this.testRoomId = room.getId();

        // Crear una reserva inicial para el usuario principal
        Reservation res = new Reservation();
        res.setStartDate(new Date());
        res.setEndDate(new Date(System.currentTimeMillis() + 3600000));
        res.setUser(user);
        res.setRoom(room);
        res.setCancelled(false);
        res = reservationRepository.save(res);
        this.testReservationId = res.getId();
    }

    // --- TESTS RESERVATION CONTROLLER (Parte "Web/User") ---

    @Test
    @WithMockUser(username = testUserEmail)
    public void testGetMyReservations() throws Exception {
        mockMvc.perform(get("/api/reservations/my-reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(testReservationId));
    }

    // --- TESTS RESERVATION REST CONTROLLER (Parte REST pura) ---

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAllReservationsAsAdmin() throws Exception {
        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser
    public void testGetReservationById_Found() throws Exception {
        mockMvc.perform(get("/api/reservations/" + testReservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testReservationId));
    }

    @Test
    @WithMockUser
    public void testGetReservationById_NotFound() throws Exception {
        mockMvc.perform(get("/api/reservations/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCancelReservation_Unauthorized() throws Exception {
        mockMvc.perform(patch("/api/reservations/" + testReservationId + "/cancel"))
                .andExpect(status().isUnauthorized()); 
    }

    @Test
    @WithMockUser(username = testUserEmail)
    public void testCancelOwnReservation_Success() throws Exception {
        mockMvc.perform(patch("/api/reservations/" + testReservationId + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cancelled").value(true));
    }

    @Test
    @WithMockUser(username = otherUserEmail) // Usuario diferente al dueño
    public void testCancelOtherUserReservation_Forbidden() throws Exception {
        mockMvc.perform(patch("/api/reservations/" + testReservationId + "/cancel"))
                .andExpect(status().isForbidden()); // Cubre el catch(AccessDeniedException)
    }

    @Test
    @WithMockUser(username = otherUserEmail, roles = "ADMIN") // Admin sí puede cancelar cualquiera
    public void testCancelOtherUserReservation_AsAdmin_Success() throws Exception {
        mockMvc.perform(patch("/api/reservations/" + testReservationId + "/cancel"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateReservation_Success() throws Exception {
        String updateJson = """
            {
                "reason": "Updated reason via REST"
            }
        """;
        mockMvc.perform(put("/api/reservations/" + testReservationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reason").value("Updated reason via REST"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteReservation_Success() throws Exception {
        mockMvc.perform(delete("/api/reservations/" + testReservationId))
                .andExpect(status().isOk());
        
        // Verificar que ya no existe
        mockMvc.perform(get("/api/reservations/" + testReservationId))
                .andExpect(status().isNotFound());
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteReservation_NotFound() throws Exception {
        mockMvc.perform(delete("/api/reservations/9999"))
                .andExpect(status().isNotFound());
    }








    @Test
    @WithMockUser
    public void testCreateReservation_Success() throws Exception {
        // Creamos un objeto Reservation para enviar en el cuerpo
        Reservation newRes = new Reservation();
        newRes.setReason("Reserva vía POST REST");
        // No asignamos ID, lo genera la DB

        String json = objectMapper.writeValueAsString(newRes);

        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated()) // Verifica el 201 Created
                .andExpect(header().exists("Location")) // Verifica la cabecera Location
                .andExpect(jsonPath("$.reason").value("Reserva vía POST REST"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateReservation_NotFound() throws Exception {
        // Intentamos actualizar una reserva con un ID inexistente
        String json = "{\"reason\":\"No existe\"}";
        mockMvc.perform(put("/api/reservations/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound()); // Cubre el orElseGet 404
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testPatchReservation_Success() throws Exception {
        // Actualización parcial de la reserva creada en el setUp
        String partialJson = "{\"reason\":\"Razón parcheada\"}";
        
        mockMvc.perform(patch("/api/reservations/" + testReservationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(partialJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reason").value("Razón parcheada"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testPatchReservation_NotFound() throws Exception {
        mockMvc.perform(patch("/api/reservations/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"error\"}"))
                .andExpect(status().isNotFound()); // Cubre el orElseGet 404 de patch
    }

    @Test
    @WithMockUser(username = testUserEmail)
    public void testCancelReservation_NotFound() throws Exception {
        // Intentamos cancelar una reserva que no existe en la base de datos
        mockMvc.perform(patch("/api/reservations/99999/cancel"))
                .andExpect(status().isNotFound()); // Cubre el orElseGet 404 de cancel
    }
}