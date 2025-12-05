package com.urjcservice.backend.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@AutoConfigureMockMvc
public class SoftwareApiTest {

    @Autowired
    private MockMvc mockMvc;

    
    @Test
    public void testGetSoftwareUnauthenticated() throws Exception {
        //create software without login
        mockMvc.perform(get("/api/softwares")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());//401
    }

    
    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void testGetSoftwareAuthenticated() throws Exception {
        //see software beeing logged
        mockMvc.perform(get("/api/softwares")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // 200 
                .andExpect(jsonPath("$").isArray());
    }

    
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testCreateSoftwareAsAdmin() throws Exception {
        //create software
        String newSoftware = """
            {
                "name": "Eclipse IDE",
                "version": 2024.03,
                "description": "Entorno de desarrollo Java"
            }
        """;

        mockMvc.perform(post("/api/softwares")
                .content(newSoftware)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()) // 201 Created
                .andExpect(jsonPath("$.name", is("Eclipse IDE")));
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDeleteSoftwareAsAdmin() throws Exception {
        //software for larter delete
        String softwareJson = """
            { "name": "To Delete", "version": 1.0, "description": "Temp" }
        """;
        
        // to obtain id
        String response = mockMvc.perform(post("/api/softwares")
                .content(softwareJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        
        //should parser id?
        
        //simulate 1 id
        mockMvc.perform(delete("/api/softwares/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent()); // 204
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void testDeleteSoftwareAsUserForbidden() throws Exception {
        mockMvc.perform(delete("/api/softwares/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()); // 403
    }
    
    @Test
    public void testGetSingleSoftwareNotFound() throws Exception {
        mockMvc.perform(get("/api/softwares/9999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // 404
    }
}