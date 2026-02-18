package com.urjcservice.backend.controller.auth;

import jakarta.servlet.http.Cookie;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType; 
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.urjcservice.backend.repositories.UserRepository;
import com.urjcservice.backend.service.UserService;
import com.urjcservice.backend.controller.auth.AuthController;
import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.security.jwt.JwtTokenProvider;

import java.util.Optional;


@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;


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
        when(userService.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    public void testRegister_Success() throws Exception {
        when(userService.existsByEmail(anyString())).thenReturn(false);
        
        String json = "{\"name\":\"Carlos\",\"email\":\"c@gmail.com\",\"password\":\"StrongPass1!\"}";
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    public void testRegister_Conflict() throws Exception {
        when(userService.existsByEmail("c@gmail.com")).thenReturn(true);
        
        String json = "{\"name\":\"Carlos\",\"email\":\"c@gmail.com\",\"password\":\"StrongPass1!\"}";
        
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
        User mockUser = new User();
        mockUser.setName("Test User");
        mockUser.setEmail("test@test.com");

        // GIVEN
        given(userService.findByEmail("test@test.com")).willReturn(Optional.of(mockUser));

        mockMvc.perform(get("/api/auth/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    public void testMe_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "missing@test.com")
    public void testMe_UserNotFound_Returns404() throws Exception {
        when(userService.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isNotFound());
    }



    @Test
    public void testRegister_Conflict_EmailExists() throws Exception {
        when(userService.existsByEmail("c@gmail.com")).thenReturn(true);
        
        String json = "{\"name\":\"Carlos\",\"email\":\"c@gmail.com\",\"password\":\"StrongPass1!\"}";
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict());
    }


   @Test
    @WithMockUser(username = "test@test.com")
    public void testUpdateMe_Success() throws Exception {
        User existingUser = new User();
        existingUser.setName("Old Name");
        existingUser.setEmail("test@test.com");

        given(userService.findByEmail("test@test.com")).willReturn(Optional.of(existingUser));

        String updateJson = "{\"name\": \"Updated Name\"}";

        mockMvc.perform(put("/api/auth/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));

        verify(userService).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    public void testUpdateMe_EmptyName_NoUpdate() throws Exception {
        User user = new User();
        user.setName("Original Name");
        when(userService.findByEmail("user@test.com")).thenReturn(Optional.of(user));

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
    @DisplayName("Change Password - Unauthenticated (Should return 401)")
    public void testChangePassword_Unauthenticated_Returns401() throws Exception {
        String requestBody = """
            {
                "oldPassword": "oldPass",
                "newPassword": "newPass"
            }
        """;

        when(jwtTokenProvider.validateToken(any(), anyBoolean()))
            .thenThrow(new IllegalArgumentException("No access token cookie found in request"));

        mockMvc.perform(post("/api/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf())) 
                .andExpect(status().isUnauthorized());
    }







    @Test
    @WithMockUser(username = "test@test.com")
    public void testUpdateMe_NullName_DoesNotUpdate() throws Exception {
        User user = new User();
        user.setName("Original Name");
        when(userService.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        
        String json = "{\"name\": null, \"email\": \"new@email.com\"}";

        mockMvc.perform(put("/api/auth/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Original Name")); 
        
        verify(userService).save(user);
    }

    @Test
    @WithMockUser(username = "test@test.com")
    public void testUpdateMe_EmptyName_DoesNotUpdate() throws Exception {
        User user = new User();
        user.setName("Original Name");
        when(userService.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        String json = "{\"name\": \"\", \"email\": \"new@email.com\"}";

        mockMvc.perform(put("/api/auth/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Original Name")); 
    }

       






   
    
    @Test
    public void testLogin_Failure_WrongPassword() throws Exception {
        
        String loginJson = """
            { "username": "admin@studyspace.com", "password": "WRONG_PASSWORD" }
        """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isUnauthorized()); // O 401/403
    }











    

    @Test
    public void testRegister_DuplicateEmail_ShouldFail() throws Exception {
        String duplicateJson = """
            {
                "name": "Dupe",
                "email": "exists@test.com",
                "password": "StrongPass1!"
            }
        """;

        // GIVEN
        given(userService.existsByEmail("exists@test.com")).willReturn(true);

        // WHEN & THEN: 400 Bad Request
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(duplicateJson))
                .andExpect(status().isConflict()); 
    }

    

    
    
    @Test
    public void testMe_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }




    @Test
    @DisplayName("Register should fail if email is missing domain extension (e.g. .com)")
    void testRegister_EmailMissingExtension() throws Exception {
        // GIVEN: with @ and without extension (.)
        String jsonInvalidEmail = """
            {
                "name": "Test User",
                "email": "usuario@gmail", 
                "password": "password123."
            }
        """;

        // WHEN & THEN
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonInvalidEmail))
                .andExpect(status().isBadRequest()) //400
                .andExpect(jsonPath("$.message.email").value("Email must be valid domain and contain an extension (e.g., exampleemail@gmail.com)"));
    }



    @Test
    @DisplayName("Register should fail if password is weak (missing special char or uppercase)")
    void testRegister_WeakPassword() throws Exception {
        String weakPasswordJson = """
            {
                "name": "Weak Pass User",
                "email": "valid@email.com",
                "password": "weakpassword123" 
            }
        """;

        // WHEN & THEN
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(weakPasswordJson))
                .andExpect(status().isBadRequest()) //400
                .andExpect(jsonPath("$.message.password").exists());
    }

}

