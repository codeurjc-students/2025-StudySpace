package com.urjcservice.backend.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.annotation.DirtiesContext;
import java.util.UUID;

import com.urjcservice.backend.repositories.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)  //to reset DB after each test
public class UserApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        
        userRepository.findByEmail("testuser@example.com").ifPresent(userRepository::delete);
    }

    @Test
    public void testRegisterUserWithUUID() throws Exception {
        String randomEmail = "testuser_" + UUID.randomUUID().toString() + "@example.com";//random email to avoid conflicts
        
        String newUser = """
            {
                "name": "TestUser",
                "email": "%s",
                "password": "password123"
            }
        """.formatted(randomEmail);

        mockMvc.perform(post("/api/auth/register")
                .content(newUser)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()); // 201
    }

    @Test
    public void testListUsersUnauthenticated() throws Exception {
       //try to see user list WITHOUT login
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()); // 401 
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testListUsersAsAdmin() throws Exception {
        //lis of users AS ADMIN
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) //200 
                .andExpect(jsonPath("$").isArray());
    }
}