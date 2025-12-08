package com.urjcservice.backend.security.jwt;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

class JwtDtosTest {

    @Test
    void testLoginRequest() {
        LoginRequest req = new LoginRequest("user", "pass");
        assertEquals("user", req.getUsername());
        assertEquals("pass", req.getPassword());
        
        req.setUsername("newUser");
        req.setPassword("newPass");
        assertEquals("newUser", req.getUsername());
        
        assertNotNull(req.toString());
        
        // empty constructor
        LoginRequest empty = new LoginRequest();
        assertNull(empty.getUsername());
    }

    @Test
    void testAuthResponse() {
        AuthResponse res = new AuthResponse(AuthResponse.Status.SUCCESS, "OK");
        assertEquals(AuthResponse.Status.SUCCESS, res.getStatus());
        assertEquals("OK", res.getMessage());

        res.setStatus(AuthResponse.Status.FAILURE);
        res.setMessage("Error");
        res.setError("Details");
        
        assertEquals(AuthResponse.Status.FAILURE, res.getStatus());
        assertEquals("Details", res.getError());
        
        assertNotNull(res.toString());
        
        // fill all fields of the constructor
        AuthResponse full = new AuthResponse(AuthResponse.Status.FAILURE, "Msg", "Err");
        assertEquals("Err", full.getError());
    }

    @Test
    void testTokenType() {
        assertEquals("AuthToken", TokenType.ACCESS.cookieName);
        assertEquals(Duration.ofMinutes(5), TokenType.ACCESS.duration);
        
        assertEquals("RefreshToken", TokenType.REFRESH.cookieName);
        assertEquals(Duration.ofDays(7), TokenType.REFRESH.duration);
        
        // Verify enum valueOf works
        assertEquals(TokenType.ACCESS, TokenType.valueOf("ACCESS"));
    }
}