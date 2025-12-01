package com.urjcservice.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager; 
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; 
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.urjcservice.backend.security.jwt.UnauthorizedHandlerJwt;
import java.util.Arrays; 
import java.util.List;



@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

   
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    




    @Bean
    public DaoAuthenticationProvider authenticationProvider(RepositoryUserDetailsService userDetailService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /*
    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder().encode("pass"))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }*/

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
        UnauthorizedHandlerJwt unauthorizedHandler,
        RepositoryUserDetailsService userDetailService) throws Exception {
        http.authenticationProvider(authenticationProvider(userDetailService));
        http
        // activate cors here
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authorize -> authorize
                        // PUBLIC PAGES
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/rooms/**").permitAll()
                        .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        // allows pre-flight requests (for CORS)
                    .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        // PRIVATE PAGES
                        .anyRequest().authenticated())
                /*.formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .failureUrl("/loginerror")
                        .defaultSuccessUrl("/private")
                        .permitAll()
                )*/
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) -> response.setStatus(200))
                        .permitAll()
                )
                .httpBasic(basic -> basic
                    .authenticationEntryPoint(unauthorizedHandler)
                )
                .exceptionHandling(exception -> exception
                    .authenticationEntryPoint(unauthorizedHandler) // También para excepciones generales
                );
                //.httpBasic(Customizer.withDefaults());
        // Disable CSRF at the moment
        http.csrf(csrf -> csrf.disable());
        return http.build();
    }
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // allows Angular (port 4200)
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        // allows HTTP methods 
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // allows headers(Authorization for login)
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        // allows credentials(cookies and basic auth)
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}