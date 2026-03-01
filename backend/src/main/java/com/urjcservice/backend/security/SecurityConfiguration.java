package com.urjcservice.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.urjcservice.backend.security.jwt.JwtRequestFilter;
import com.urjcservice.backend.security.jwt.UnauthorizedHandlerJwt;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(RepositoryUserDetailsService userDetailService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
            UnauthorizedHandlerJwt unauthorizedHandler,
            RepositoryUserDetailsService userDetailService) throws Exception {
        http.authenticationProvider(authenticationProvider(userDetailService));
        http
                // activate cors here
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authorize -> authorize
                        // Public
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // CORS pre-flight

                        // Auth
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh").permitAll()
                        .requestMatchers("/api/auth/forgot-password", "/api/auth/reset-password").permitAll()

                        // Public GET endpoints
                        .requestMatchers(new AntPathRequestMatcher("/api/rooms/**", "GET")).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/search/rooms").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/softwares/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/stats/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/*/image").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reservations/check-availability").permitAll()
                        .requestMatchers("/api/reservations/verify").permitAll()

                        // User
                        .requestMatchers("/api/auth/me", "/api/auth/change-password", "/api/auth/logout")
                        .authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/users/*/image").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/users/*/image").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/reservations").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/reservations/my-reservations").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/reservations/*").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/reservations/*/cancel").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/search/reservations/me").authenticated()

                        // Admin
                        .anyRequest().hasRole("ADMIN"))

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedHandler) // for general exceptions
                );

        // Disable Form login Authentication
        http.formLogin(formLogin -> formLogin.disable());
        // Disable Basic Authentication for jwt
        http.httpBasic(httpBasic -> httpBasic.disable());
        // Stateless session
        http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        // Add JWT filter
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        // Disable CSRF at the moment
        http.csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // allows Angular (port 4200)
        configuration.setAllowedOrigins(Arrays.asList("https://localhost", "https://localhost:4200"));
        // allows HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // allows headers(Authorization for login)
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept",
                "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        // allows credentials(cookies and basic auth)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}