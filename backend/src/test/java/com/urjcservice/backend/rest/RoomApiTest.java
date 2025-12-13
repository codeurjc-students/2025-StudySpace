package com.urjcservice.backend.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.repositories.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional 
public class RoomApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoomRepository roomRepository; 

    @Autowired
    private ObjectMapper objectMapper;

    private Long existingRoomId;

    @BeforeEach
    void setUp() {
        Room room = new Room();
        room.setName("Aula Pre-existente");
        room.setCapacity(20);
        room.setCamp(Room.CampusType.ALCORCON);
        room.setPlace("Bajo A");
        room.setCoordenades("0,0");
        room.setActive(true);
        room.setSoftware(new ArrayList<>());
        
        room = roomRepository.save(room);
        existingRoomId = room.getId();
    }

    //GET ALL
    @Test
    public void testGetAllRooms() throws Exception {
        mockMvc.perform(get("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray()); 
    }

    // GET BY ID (Happy Path)
    @Test
    public void testGetRoomById() throws Exception {
        mockMvc.perform(get("/api/rooms/" + existingRoomId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Aula Pre-existente")));
    }

    //GET BY ID (Not Found)
    @Test
    public void testGetRoomById_NotFound() throws Exception {
        mockMvc.perform(get("/api/rooms/999999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    //CREATE (Admin)
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"}) 
    public void testCreateRoomAsAdmin() throws Exception {
        String newRoomJson = """
            {
                "name": "Aula Nueva Test",
                "capacity": 50,
                "camp": "MOSTOLES",
                "place": "Edificio de Pruebas",
                "coordenades": "10,20",
                "active": true,
                "softwareIds": []
            }
        """;
        
        mockMvc.perform(post("/api/rooms")
                .content(newRoomJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Aula Nueva Test")));
    }

    //UPDATE (Admin)
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"}) 
    public void testUpdateRoom() throws Exception {
        String updateJson = """
            {
                "name": "Aula Modificada",
                "capacity": 100,
                "camp": "VICALVARO",
                "place": "Ático",
                "coordenades": "5,5",
                "active": false,
                "softwareIds": []
            }
        """;

        mockMvc.perform(put("/api/rooms/" + existingRoomId)
                .content(updateJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Aula Modificada")))
                .andExpect(jsonPath("$.active", is(false)));
    }

    //UPDATE (Not Found)
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testUpdateRoom_NotFound() throws Exception {
        String updateJson = "{ \"name\": \"Ghost\" }";
        
        mockMvc.perform(put("/api/rooms/999999")
                .content(updateJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    //DELETE (Admin)
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"}) 
    public void testDeleteRoom() throws Exception {
        mockMvc.perform(delete("/api/rooms/" + existingRoomId))
                .andExpect(status().isNoContent()); // 204 No Content

        // Verify
        mockMvc.perform(get("/api/rooms/" + existingRoomId))
                .andExpect(status().isNotFound());
    }

    //DELETE (Not Found)
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDeleteRoom_NotFound() throws Exception {
        mockMvc.perform(delete("/api/rooms/999999"))
                .andExpect(status().isNotFound());
    }

    //SECURITY CHECK (Unauthenticated)
    @Test
    public void testCreateRoomUnauthenticated() throws Exception {
        String newRoomJson = "{ \"name\": \"Aula Incorrecta\", \"capacity\": 100 }";

        mockMvc.perform(post("/api/rooms")
                .content(newRoomJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()); // 401
    }
}