package com.urjcservice.backend.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserLoginServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private UserLoginService userLoginService;

    @Test
    void login_Successful_ReturnsAuthResponseAndSetsCookies() {
        // GIVEN
        LoginRequest loginRequest = new LoginRequest("user", "pass");
        UserDetails userDetails = new User("user", "pass", Collections.emptyList());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(userDetailsService.loadUserByUsername("user")).thenReturn(userDetails);
        when(jwtTokenProvider.generateAccessToken(userDetails)).thenReturn("accessToken123");
        when(jwtTokenProvider.generateRefreshToken(userDetails)).thenReturn("refreshToken123");

        // WHEN
        ResponseEntity<AuthResponse> result = userLoginService.login(response, loginRequest);

        // THEN
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(AuthResponse.Status.SUCCESS, result.getBody().getStatus());
        
        //Verify
        // Capture the cookies set in the response
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response, times(2)).addCookie(cookieCaptor.capture());

        var cookies = cookieCaptor.getAllValues();
        assertTrue(cookies.stream().anyMatch(c -> c.getName().equals("AuthToken") && c.getValue().equals("accessToken123")));
        assertTrue(cookies.stream().anyMatch(c -> c.getName().equals("RefreshToken") && c.getValue().equals("refreshToken123")));
    }

    @Test
    void refresh_Successful_ReturnsNewAccessToken() {
        // GIVEN
        String refreshToken = "validRefresh";
        String subject = "user";
        Claims claims = new DefaultClaims(Map.of("sub", subject));
        UserDetails userDetails = new User(subject, "pass", Collections.emptyList());

        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(claims);
        when(userDetailsService.loadUserByUsername(subject)).thenReturn(userDetails);
        when(jwtTokenProvider.generateAccessToken(userDetails)).thenReturn("newAccess123");

        // WHEN
        ResponseEntity<AuthResponse> result = userLoginService.refresh(response, refreshToken);

        // THEN
        assertEquals(AuthResponse.Status.SUCCESS, result.getBody().getStatus());
        
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());
        
        Cookie cookie = cookieCaptor.getValue();
        assertEquals("AuthToken", cookie.getName());
        assertEquals("newAccess123", cookie.getValue());
    }

    @Test
    void refresh_Failure_ReturnsErrorResponse() {
        // GIVEN
        String invalidToken = "invalid";
        when(jwtTokenProvider.validateToken(invalidToken)).thenThrow(new RuntimeException("Token invalid"));

        // WHEN
        ResponseEntity<AuthResponse> result = userLoginService.refresh(response, invalidToken);

        // THEN
        assertEquals(AuthResponse.Status.FAILURE, result.getBody().getStatus());
        verify(response, never()).addCookie(any());
    }

    @Test
    void logout_ClearsCookies() {
        // WHEN
        String msg = userLoginService.logout(response);

        // THEN
        assertEquals("logout successfully", msg);
        
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response, times(2)).addCookie(cookieCaptor.capture());
        
        // Verify cookies not valid (maxAge 0)
        assertTrue(cookieCaptor.getAllValues().stream().allMatch(c -> c.getMaxAge() == 0));
    }
}