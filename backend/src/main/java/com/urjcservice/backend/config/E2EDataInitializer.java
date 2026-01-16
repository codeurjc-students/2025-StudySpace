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
                System.out.println("--- USER E2E CREATED: test@test.com / password123 ---");
            }
            if (userRepository.findByEmail("admin@studyspace.com").isEmpty()) {
                User admin = new User();
                admin.setName("Admin E2E");
                admin.setEmail("admin@studyspace.com");
                admin.setEncodedPassword(passwordEncoder.encode("password")); 
                admin.setRoles(List.of("USER", "ADMIN"));
                admin.setBlocked(false);
                userRepository.save(admin);
                System.out.println("--- E2E ADMIN CREATED: admin@studyspace.com / password ---");
            } else {
                User existingAdmin = userRepository.findByEmail("admin@studyspace.com").get();
                existingAdmin.setEncodedPassword(passwordEncoder.encode("password"));
                userRepository.save(existingAdmin);
                System.out.println("--- ADMIN E2E UPDATED with known password ---");
            }
        };
    }
}