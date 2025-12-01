package com.urjcservice.backend.config;

import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.repositories.UserRepository;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import org.slf4j.Logger; 
import org.slf4j.LoggerFactory;

import java.util.Arrays;

@Component
public class DatabaseInitializer implements CommandLineRunner {


    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        
        // we create a default admin user if it does not exist, if it exist we dont created it again
        //search by email(primary key for user)
        String adminEmail = "admin@studyspace.com"; 
        //if it is already on the database, do nothing
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            
            User admin = new User();
            admin.setName("SuperAdmin");
            admin.setEmail(adminEmail);
            admin.setEncodedPassword(passwordEncoder.encode("admin")); // Password: admin
            
            // we give him the ADMIN role and USER role
            admin.setRoles(Arrays.asList("USER", "ADMIN"));
            admin.setType(User.UserType.ADMIN);

            userRepository.save(admin);
            
            logger.info("--------------------------------------");
            logger.info(" ADMIN USER AUTOMATICALLY CREATED ");
            logger.info(" Email: {}", adminEmail);
            logger.info(" Pass:  admin");
            logger.info("--------------------------------------");
        }
    }
}