package com.urjcservice.backend.config;

import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
@Profile("e2e") 
public class E2EDataInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (userRepository.findByEmail("test@test.com").isEmpty()) {
                User user = new User();
                user.setName("Test User");
                user.setEmail("test@test.com");
                user.setEncodedPassword(passwordEncoder.encode("password123"));
                user.setRoles(List.of("USER"));
                user.setBlocked(false);
                userRepository.save(user);
                System.out.println("--- USUARIO E2E CREADO: test@test.com / password123 ---");
            }
        };
    }
}