package com.urjcservice.backend.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.entities.Software;
import com.urjcservice.backend.repositories.RoomRepository;
import com.urjcservice.backend.repositories.UserRepository;
import com.urjcservice.backend.repositories.SoftwareRepository;
import com.urjcservice.backend.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional 
public class RoomApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SoftwareRepository softwareRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private Long existingRoomId;
    private Long existingSoftwareId;
    private Cookie authCookie;

    @BeforeEach
    void setUp() {
        //real user for tocken
        User admin = new User();
        admin.setName("Admin Test");
        admin.setEmail("admin@urjc.es");
        admin.setEncodedPassword("password");
        admin.setRoles(List.of("ADMIN", "USER"));
        userRepository.save(admin);

        //generate real tocken and insert it inside a cookie
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin@urjc.es");
        String token = jwtTokenProvider.generateAccessToken(userDetails);
        authCookie = new Cookie("AuthToken", token); 


        Software soft = new Software();
        soft.setName("Java JDK");
        soft = softwareRepository.save(soft);
        existingSoftwareId = soft.getId();


        Room room = new Room();
        room.setName("Aula Pre-existente");
        room.setCapacity(20);
        room.setCamp(Room.CampusType.ALCORCON);
        room.setActive(true);
        room.setSoftware(new ArrayList<>());
        
        room = roomRepository.save(room);
        existingRoomId = room.getId();
    }

    @Test
    public void testGetAllRooms() throws Exception {
        mockMvc.perform(get("/api/rooms")
                .cookie(authCookie)) //we send the cookie because if not the filter blocks us
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray()); 
    }

    @Test
    public void testCreateRoom_FullCoverage() throws Exception {
        RoomRestController.RoomRequest request = new RoomRestController.RoomRequest();
        request.setName("Aula Nueva Sonar");
        request.setCapacity(40);
        request.setCamp(Room.CampusType.MOSTOLES);
        request.setPlace("Lab 1");
        request.setCoordenades("1,1");
        request.setActive(true);
        request.setSoftwareIds(List.of(existingSoftwareId)); 

        mockMvc.perform(post("/api/rooms")
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Aula Nueva Sonar")))
                .andExpect(jsonPath("$.software", hasSize(1)));
    }

    @Test
    public void testUpdateRoom_Success() throws Exception {
        RoomRestController.RoomRequest updateReq = new RoomRestController.RoomRequest();
        updateReq.setName("Nombre Actualizado");
        updateReq.setActive(false);

        mockMvc.perform(put("/api/rooms/" + existingRoomId)
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Nombre Actualizado")))
                .andExpect(jsonPath("$.active", is(false)));
    }

    @Test
    public void testGetRoomStats_Now() throws Exception {//if date null
        mockMvc.perform(get("/api/rooms/" + existingRoomId + "/stats")
                .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.occupiedPercentage").exists());
    }

    @Test
    public void testDeleteRoom_Success() throws Exception {
        mockMvc.perform(delete("/api/rooms/" + existingRoomId)
                .cookie(authCookie))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/rooms/" + existingRoomId).cookie(authCookie))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetRoomById_NotFound() throws Exception {
        mockMvc.perform(get("/api/rooms/999999")
                .cookie(authCookie))
                .andExpect(status().isNotFound());
    }


    @Test
    public void testCreateRoom_NullSoftwareIds() throws Exception {
        RoomRestController.RoomRequest request = new RoomRestController.RoomRequest();
        request.setName("Aula Sin Software");
        request.setSoftwareIds(null); //execute room.setSoftware(new ArrayList<>())

        mockMvc.perform(post("/api/rooms")
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.software", hasSize(0)));
    }

    //404 error on update
    @Test
    public void testUpdateRoom_NotFound() throws Exception {
        RoomRestController.RoomRequest updateReq = new RoomRestController.RoomRequest();
        updateReq.setName("Inexistente");

        mockMvc.perform(put("/api/rooms/999999")
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetRoomStats_WithSpecificDate() throws Exception {
        String testDate = "2025-05-20";
        
        mockMvc.perform(get("/api/rooms/" + existingRoomId + "/stats")
                .param("date", testDate)
                .cookie(authCookie))
                .andExpect(status().isOk());
    }

    //404 on delete
    @Test
    public void testDeleteRoom_NotFound() throws Exception {
        mockMvc.perform(delete("/api/rooms/999999")
                .cookie(authCookie))
                .andExpect(status().isNotFound());
    }
}