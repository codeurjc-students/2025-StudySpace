package com.urjcservice.backend.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urjcservice.backend.entities.Room;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RoomApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    //to read rooms
    @Test
    public void testGetAllRooms() throws Exception {
        
        mockMvc.perform(get("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON))
                //we wait till server responds
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$").isArray()); 
    }

    //to create room as admin
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"}) 
    public void testCreateRoomAsAdmin() throws Exception {
        
        
        // a new room JSON
        String newRoomJson = """
            {
                "name": "Aula de Test",
                "capacity": 50,
                "camp": "MOSTOLES",
                "place": "Edificio de Pruebas",
                "coordenades": "0,0",
                "softwareIds": []
            }
        """;

        
        mockMvc.perform(post("/api/rooms")
                .content(newRoomJson)
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isCreated())
                // we verify is our new room
                .andExpect(jsonPath("$.name", is("Aula de Test")));
    }

    //try to create room without authentication, it should fail
    @Test
    public void testCreateRoomUnauthenticated() throws Exception {
        String newRoomJson = """
            { "name": "Aula Incorrecta", "capacity": 100 }
        """;

        mockMvc.perform(post("/api/rooms")
                .content(newRoomJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()); //401
    }
}