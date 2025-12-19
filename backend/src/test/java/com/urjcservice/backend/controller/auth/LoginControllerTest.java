package com.urjcservice.backend.controller.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.urjcservice.backend.security.jwt.AuthResponse;
import com.urjcservice.backend.security.jwt.LoginRequest;
import com.urjcservice.backend.security.jwt.UserLoginService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
public class LoginControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserLoginService userLoginService;

    @InjectMocks
    private LoginController loginController;

    @BeforeEach
    public void setup() {
        // standaloneSetup for ignoring security filters
        this.mockMvc = MockMvcBuilders.standaloneSetup(loginController).build();
    }

    @Test
    public void testLogin_Success() throws Exception {
        AuthResponse successResponse = new AuthResponse(AuthResponse.Status.SUCCESS, "Logged in");
        
        when(userLoginService.login(any(HttpServletResponse.class), any(LoginRequest.class)))
            .thenReturn(ResponseEntity.ok(successResponse));

        String jsonRequest = "{\"username\": \"test@urjc.es\", \"password\": \"1234\"}";

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Logged in"));
    }

    @Test
    public void testRefresh_WithCookie_Success() throws Exception {
        AuthResponse refreshResponse = new AuthResponse(AuthResponse.Status.SUCCESS, "Token refreshed");
        
        when(userLoginService.refresh(any(HttpServletResponse.class), eq("some-refresh-token")))
            .thenReturn(ResponseEntity.ok(refreshResponse));

        mockMvc.perform(post("/api/auth/refresh")
                .cookie(new Cookie("RefreshToken", "some-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    public void testRefresh_WithoutCookie_CallsServiceWithNull() throws Exception {
        AuthResponse failureResponse = new AuthResponse(AuthResponse.Status.FAILURE, "Missing token");
        
        when(userLoginService.refresh(any(HttpServletResponse.class), eq(null)))
            .thenReturn(ResponseEntity.ok(failureResponse));

        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILURE"));
    }

    @Test
    public void testLogout_Success() throws Exception {
        when(userLoginService.logout(any(HttpServletResponse.class))).thenReturn("logout successfully");

        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("logout successfully"));
    }
}