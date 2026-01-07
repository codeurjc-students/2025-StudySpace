package com.urjcservice.backend.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urjcservice.backend.entities.Software;
import com.urjcservice.backend.rest.SoftwareRestController.SoftwareRequest;
import com.urjcservice.backend.service.SoftwareService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@AutoConfigureMockMvc
public class SoftwareRestControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private SoftwareService softwareService;

    @Autowired
    private ObjectMapper objectMapper;

    private Software mockSoftware;


    @BeforeEach
    void setUp() {
        mockSoftware = new Software();
        mockSoftware.setId(1L);
        mockSoftware.setName("IntelliJ IDEA");
        mockSoftware.setVersion(2024.1f);
        mockSoftware.setDescription("Java IDE");
    }


    @Test
    @WithMockUser(roles = "USER")
    public void testGetSoftwares_Pagination() throws Exception {
        List<Software> softwareList = List.of(mockSoftware, new Software());
        Pageable pageable = PageRequest.of(0, 2);
        Page<Software> page = new PageImpl<>(softwareList, pageable, 2);

        given(softwareService.findAll(any(Pageable.class))).willReturn(page);

        // WHEN & THEN
        mockMvc.perform(get("/api/softwares")
                .param("page", "0")
                .param("size", "2") 
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(2)); 
    }



    // --- GET ALL ---

    

    @Test
    @WithMockUser
    public void testGetAllSoftwares() throws Exception {
        Page<Software> page = new PageImpl<>(Collections.singletonList(mockSoftware));
        given(softwareService.findAll(any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/api/softwares")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("IntelliJ IDEA"));
    }

    // --- GET ONE ---
    @Test
    @WithMockUser
    public void testGetSoftwareById_Success() throws Exception {
        given(softwareService.findById(1L)).willReturn(Optional.of(mockSoftware));

        mockMvc.perform(get("/api/softwares/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("IntelliJ IDEA"));
    }

    @Test
    @WithMockUser
    public void testGetSoftwareById_NotFound() throws Exception {
        given(softwareService.findById(99L)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/softwares/99"))
                .andExpect(status().isNotFound());
    }

    // --- CREATE (POST) ---
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateSoftware_Success() throws Exception {
        //dto for the request
        SoftwareRequest request = new SoftwareRequest();
        request.setName("New Software");
        request.setVersion(1.0f);
        request.setDescription("Test Desc");

        given(softwareService.save(any(Software.class))).willReturn(mockSoftware);

        mockMvc.perform(post("/api/softwares")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    // --- UPDATE (PUT) ---
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateSoftware_Success() throws Exception {
        SoftwareRequest request = new SoftwareRequest();
        request.setName("Updated Name");

        Software updatedSoftware = new Software();
        updatedSoftware.setId(1L);
        updatedSoftware.setName("Updated Name");
        
        given(softwareService.updateSoftware(eq(1L), any(Software.class)))
                .willReturn(Optional.of(updatedSoftware));

        mockMvc.perform(put("/api/softwares/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateSoftware_NotFound() throws Exception {
        SoftwareRequest request = new SoftwareRequest();
        
        given(softwareService.updateSoftware(eq(99L), any(Software.class)))
                .willReturn(Optional.empty());

        mockMvc.perform(put("/api/softwares/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // --- DELETE ---
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteSoftware_Success() throws Exception {
        given(softwareService.deleteById(1L)).willReturn(Optional.of(mockSoftware));

        mockMvc.perform(delete("/api/softwares/1"))
                .andExpect(status().isNoContent()); // 204
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteSoftware_NotFound() throws Exception {
        given(softwareService.deleteById(99L)).willReturn(Optional.empty());

        mockMvc.perform(delete("/api/softwares/99"))
                .andExpect(status().isNotFound()); // 404
    }
    
    @Test
    public void testGetSoftwareUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/softwares")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    
}