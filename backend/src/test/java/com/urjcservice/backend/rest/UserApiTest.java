package com.urjcservice.backend.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID; 

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserApiTest { 

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testRegisterUserUnique() throws Exception {
        String uniqueEmail = "test_" + UUID.randomUUID().toString() + "@example.com";
        
        String newUser = """
            {
                "name": "TestUser",
                "email": "%s",
                "password": "password123"
            }
        """.formatted(uniqueEmail);

        mockMvc.perform(post("/api/auth/register")
                .content(newUser)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()); 
    }

    @Test
    public void testListUsersUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testListUsersAsAdmin() throws Exception {
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}