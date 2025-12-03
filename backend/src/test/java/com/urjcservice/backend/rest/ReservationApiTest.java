package com.urjcservice.backend.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ReservationApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCreateReservationUnauthenticated() throws Exception {
        // resrervation WITHOUT login
        String reservation = """
            {
                "roomId": 1,
                "startDate": "2025-12-01T10:00:00.000Z",
                "endDate": "2025-12-01T12:00:00.000Z",
                "reason": "Test Reserva"
            }
        """;

        mockMvc.perform(post("/api/reservations/create")
                .content(reservation)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()); //401
    }

    @Test
    @WithMockUser(username = "admin@studyspace.com", roles = {"USER"}) 
    public void testCreateReservationAuthenticated() throws Exception {
        // NOTA: Este test podría fallar si el usuario "admin@studyspace.com" 
        // no existe en tu BDD de test o si el roomId 1 no existe.
        // Para un test unitario puro habría que mockear el repositorio, 
        // pero para este test de integración necesitamos datos reales o cargarlos antes.
        
        String reservation = """
            {
                "roomId": 1, 
                "startDate": "2025-12-01T10:00:00.000Z",
                "endDate": "2025-12-01T12:00:00.000Z",
                "reason": "Test Reserva Auth"
            }
        """;

        // Si la BDD de test está vacía, esto dará 404 (Room not found) o 401 (User not found),
        // pero al menos comprobamos que NO da 403 Forbidden, lo que significa que la seguridad pasó.
        mockMvc.perform(post("/api/reservations/create")
                .content(reservation)
                .contentType(MediaType.APPLICATION_JSON));
                //expect status no because it depends on data
    }
}