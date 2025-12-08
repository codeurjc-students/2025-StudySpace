package com.urjcservice.backend.security.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.urjcservice.backend.security.jwt.JwtTokenProvider;
import com.urjcservice.backend.security.jwt.TokenType;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        //mock of a user
        userDetails = new User("testuser@example.com", "password", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void testGenerateAccessToken() {
        String token = jwtTokenProvider.generateAccessToken(userDetails);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        //can we read the tocken?
        Claims claims = jwtTokenProvider.validateToken(token);
        assertEquals("testuser@example.com", claims.getSubject());
        assertEquals("ACCESS", claims.get("type"));
    }

    @Test
    void testGenerateRefreshToken() {
        String token = jwtTokenProvider.generateRefreshToken(userDetails);
        
        assertNotNull(token);
        Claims claims = jwtTokenProvider.validateToken(token);
        assertEquals("testuser@example.com", claims.getSubject());
        assertEquals("REFRESH", claims.get("type"));
    }

    @Test
    void testTokenStringFromHeaders_Success() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        String token = jwtTokenProvider.generateAccessToken(userDetails);
        
        //simulate the Authorization header
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        //we call the method that extracts the token from headers, it should fail
        Claims claims = jwtTokenProvider.validateToken(request, false);
        
        assertEquals("testuser@example.com", claims.getSubject());
    }

    @Test
    void testTokenStringFromHeaders_MissingHeader() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            jwtTokenProvider.tokenStringFromHeaders(request);
        });

        assertEquals("Missing Authorization header", exception.getMessage());
    }

    @Test
    void testTokenStringFromHeaders_InvalidPrefix() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic 12345");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            jwtTokenProvider.tokenStringFromHeaders(request);
        });

        assertTrue(exception.getMessage().contains("Authorization header does not start with Bearer"));
    }

    @Test
    void testValidateTokenFromCookies_Success() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        String token = jwtTokenProvider.generateAccessToken(userDetails);
        
        Cookie cookie = new Cookie(TokenType.ACCESS.cookieName, token);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        Claims claims = jwtTokenProvider.validateToken(request, true); // true = desde cookie
        
        assertEquals("testuser@example.com", claims.getSubject());
    }

    @Test
    void testValidateTokenFromCookies_NoCookies() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            jwtTokenProvider.validateToken(request, true);
        });
        
        assertEquals("No cookies found in request", exception.getMessage());
    }
    
    @Test
    void testValidateTokenFromCookies_TokenNotFound() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        //incorrect cookie name
        Cookie cookie = new Cookie("otherCookie", "value");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            jwtTokenProvider.validateToken(request, true);
        });
        
        assertEquals("No access token cookie found in request", exception.getMessage());
    }
}