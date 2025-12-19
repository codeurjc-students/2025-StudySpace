package com.urjcservice.backend.controller.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.urjcservice.backend.controller.auth.AuthController;
import com.urjcservice.backend.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType; // Importación para MediaType
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.urjcservice.backend.repositories.UserRepository;
import com.urjcservice.backend.service.UserService;

import java.util.Optional;


@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @Test
    @WithMockUser(username = "carlos@urjc.es")
    public void changePasswordEndpoint_Success() throws Exception {
        when(userService.changePassword(eq("carlos@urjc.es"), anyString(), anyString())).thenReturn(true);

        String jsonRequest = "{\"oldPassword\": \"1234\", \"newPassword\": \"5678\"}";

        mockMvc.perform(post("/api/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }





    @Test
    @WithMockUser(username = "test@test.com")
    public void testMe_Authenticated() throws Exception {
        User user = new User();
        user.setEmail("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    public void testRegister_Success() throws Exception {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        
        String json = "{\"name\":\"Carlos\",\"email\":\"c@c.com\",\"password\":\"123\"}";
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    public void testRegister_Conflict() throws Exception {
        when(userRepository.findByEmail("c@c.com")).thenReturn(Optional.of(new User()));
        
        String json = "{\"name\":\"Carlos\",\"email\":\"c@c.com\",\"password\":\"123\"}";
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "test@test.com")
    public void testChangePassword_Failure() throws Exception {
        //wrong password
        when(userService.changePassword(anyString(), anyString(), anyString())).thenReturn(false);

        String json = "{\"oldPassword\":\"wrong\",\"newPassword\":\"123\"}";

        mockMvc.perform(post("/api/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILURE"));
    }















    @Test
    @WithMockUser(username = "test@test.com")
    public void testMe_Authenticated_Success() throws Exception {
        User user = new User();
        user.setEmail("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    public void testMe_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "missing@test.com")
    public void testMe_UserNotFound_Returns404() throws Exception {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isNotFound());
    }



    @Test
    public void testRegister_Conflict_EmailExists() throws Exception {
        when(userRepository.findByEmail("c@c.com")).thenReturn(Optional.of(new User()));
        
        String json = "{\"name\":\"Carlos\",\"email\":\"c@c.com\",\"password\":\"123\"}";
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict());
    }


    @Test
    @WithMockUser(username = "user@test.com")
    public void testUpdateMe_Success() throws Exception {
        User user = new User();
        user.setName("Old Name");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        String json = "{\"name\":\"New Name\"}";

        mockMvc.perform(put("/api/auth/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));
        
        verify(userRepository).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    public void testUpdateMe_EmptyName_NoUpdate() throws Exception {
        User user = new User();
        user.setName("Original Name");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        String json = "{\"name\":\"\"}"; 

        mockMvc.perform(put("/api/auth/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Original Name"));
    }

    @Test
    public void testUpdateMe_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(put("/api/auth/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(username = "carlos@urjc.es")
    public void testChangePassword_Success() throws Exception {
        when(userService.changePassword(eq("carlos@urjc.es"), anyString(), anyString())).thenReturn(true);

        String jsonRequest = "{\"oldPassword\": \"1234\", \"newPassword\": \"5678\"}";

        mockMvc.perform(post("/api/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "carlos@urjc.es")
    public void testChangePassword_IncorrectOldPassword_ReturnsBadRequest() throws Exception {
        when(userService.changePassword(eq("carlos@urjc.es"), anyString(), anyString())).thenReturn(false);

        String jsonRequest = "{\"oldPassword\": \"wrong\", \"newPassword\": \"5678\"}";

        mockMvc.perform(post("/api/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILURE"));
    }

    @Test
    public void testChangePassword_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(post("/api/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }








    @Test
    @WithMockUser(username = "test@test.com")
    public void testUpdateMe_NullName_DoesNotUpdate() throws Exception {
        User user = new User();
        user.setName("Original Name");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        
        String json = "{\"name\": null, \"email\": \"new@email.com\"}";

        mockMvc.perform(put("/api/auth/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Original Name")); 
        
        verify(userRepository).save(user);
    }

    @Test
    @WithMockUser(username = "test@test.com")
    public void testUpdateMe_EmptyName_DoesNotUpdate() throws Exception {
        User user = new User();
        user.setName("Original Name");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        String json = "{\"name\": \"\", \"email\": \"new@email.com\"}";

        mockMvc.perform(put("/api/auth/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Original Name")); 
    }

    


}

