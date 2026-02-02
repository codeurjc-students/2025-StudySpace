package com.urjcservice.backend.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.Software;
import com.urjcservice.backend.service.FileStorageService;
import com.urjcservice.backend.service.RoomService;
import com.urjcservice.backend.service.SoftwareService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.*;


import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
public class RoomRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    
    @MockBean
    private RoomService roomService;

    @MockBean
    private SoftwareService softwareService;

    @MockBean
    private FileStorageService fileStorageService;


    private Room mockRoom;
    private Software mockSoftware;

    @BeforeEach
    void setUp() {
        mockSoftware = new Software();
        mockSoftware.setId(10L);
        mockSoftware.setName("Java JDK");
        mockSoftware.setVersion(17.0f);

        mockRoom = new Room();
        mockRoom.setId(1L);
        mockRoom.setName("Aula Magna");
        mockRoom.setCapacity(50);
        mockRoom.setCamp(Room.CampusType.ALCORCON);
        mockRoom.setActive(true);
        mockRoom.setSoftware(new ArrayList<>(List.of(mockSoftware)));
    }

    // --- 1. GET ALL ROOMS ---

    @Test
    @WithMockUser(roles = "USER")
    public void testGetAllRooms() throws Exception {
        // GIVEN
        Page<Room> page = new PageImpl<>(Collections.singletonList(mockRoom));
        given(roomService.findAll(any(Pageable.class))).willReturn(page);

        // WHEN & THEN
        mockMvc.perform(get("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Aula Magna")));
    }

    // --- 2. GET BY ID ---

    @Test
    @WithMockUser(roles = "USER")
    public void testGetRoomById_Success() throws Exception {
        given(roomService.findById(1L)).willReturn(Optional.of(mockRoom));

        mockMvc.perform(get("/api/rooms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Aula Magna")));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetRoomById_NotFound() throws Exception {
        given(roomService.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/rooms/999"))
                .andExpect(status().isNotFound());
    }

    // --- 3. CREATE ROOM (POST) ---

    @Test
    @DisplayName("Create Room mocks SoftwareService to link software correctly")
    @WithMockUser(roles = "ADMIN")
    public void testCreateRoom_FullCoverage() throws Exception {
        // GIVEN
        RoomRestController.RoomRequest request = new RoomRestController.RoomRequest();
        request.setName("Aula Nueva");
        request.setCapacity(40);
        request.setCamp(Room.CampusType.MOSTOLES);
        request.setSoftwareIds(List.of(10L)); 
        request.setActive(true);


        given(softwareService.findById(10L)).willReturn(Optional.of(mockSoftware));
        

        Room savedRoom = new Room();
        savedRoom.setId(2L);
        savedRoom.setName("Aula Nueva");
        savedRoom.setSoftware(List.of(mockSoftware));
        
        given(roomService.save(any(Room.class))).willReturn(savedRoom);

        // WHEN & THEN
        mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Aula Nueva")))
                .andExpect(jsonPath("$.software", hasSize(1)))
                .andExpect(jsonPath("$.software[0].name", is("Java JDK")));
    }

    // --- 4. UPDATE ROOM (PUT) ---

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateRoom_Success() throws Exception {
        RoomRestController.RoomRequest updateReq = new RoomRestController.RoomRequest();
        updateReq.setName("Nombre Editado");
        updateReq.setActive(false);

        Room updatedRoom = new Room();
        updatedRoom.setId(1L);
        updatedRoom.setName("Nombre Editado");
        updatedRoom.setActive(false);

        given(roomService.updateRoom(eq(1L), any(Room.class))).willReturn(Optional.of(updatedRoom));

        mockMvc.perform(put("/api/rooms/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Nombre Editado")))
                .andExpect(jsonPath("$.active", is(false)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateRoom_NotFound() throws Exception {
        RoomRestController.RoomRequest updateReq = new RoomRestController.RoomRequest();
        updateReq.setName("Inexistente");

        given(roomService.updateRoom(eq(999L), any(Room.class))).willReturn(Optional.empty());

        mockMvc.perform(put("/api/rooms/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound());
    }

    // --- 5. DELETE ROOM ---

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteRoom_Success() throws Exception {
        given(roomService.deleteById(1L)).willReturn(Optional.of(mockRoom));

        mockMvc.perform(delete("/api/rooms/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteRoom_NotFound() throws Exception {
        given(roomService.deleteById(999L)).willReturn(Optional.empty());

        mockMvc.perform(delete("/api/rooms/999"))
                .andExpect(status().isNotFound());
    }

    // --- 6. STATS (getRoomDailyStats) ---

    @Test
    @WithMockUser(roles = "USER")
    public void testGetRoomStats_Success() throws Exception {
        given(roomService.findById(1L)).willReturn(Optional.of(mockRoom));

        Map<String, Object> mockStats = new HashMap<>();
        mockStats.put("occupiedPercentage", 75.5);
        mockStats.put("totalHours", 10);
        
        given(roomService.getRoomDailyStats(eq(1L), any(LocalDate.class))).willReturn(mockStats);

        mockMvc.perform(get("/api/rooms/1/stats")
                .param("date", "2025-05-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.occupiedPercentage", is(75.5)));
    }

    // --- 7. IMAGE UPLOAD & DOWNLOAD ---

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUploadRoomImage_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "room.jpg", "image/jpeg", "fake-bytes".getBytes());
        
        //room exists
        given(roomService.findById(1L)).willReturn(Optional.of(mockRoom));
        
        //save picture
        given(fileStorageService.store(any())).willReturn("generated_uuid.jpg");
        
        //updated room
        given(roomService.updateRoom(eq(1L), any(Room.class))).willReturn(Optional.of(mockRoom));
        mockMvc.perform(multipart("/api/rooms/1/image")
            .file(file)
            .with(csrf())) // Asegúrate de tener csrf si usas seguridad web
            .andExpect(status().isOk());
        /*mockMvc.perform(multipart("/api/rooms/1/image")
                .file(file))
                .andExpect(status().isOk());*/
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetRoomImage_Success() throws Exception {
        mockRoom.setImageName("my-photo.jpg");
        given(roomService.findById(1L)).willReturn(Optional.of(mockRoom));
        
        //simulate file loading
        given(fileStorageService.loadAsResource("my-photo.jpg"))
                .willReturn(new ByteArrayResource("fake-image-content".getBytes()));

        mockMvc.perform(get("/api/rooms/1/image"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes("fake-image-content".getBytes()));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetRoomImage_NoImageSet() throws Exception {
        mockRoom.setImageName(null);
        given(roomService.findById(1L)).willReturn(Optional.of(mockRoom));

        mockMvc.perform(get("/api/rooms/1/image"))
                .andExpect(status().isNotFound());
    }
}