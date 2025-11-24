package com.urjcservice.backend.config;

import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
            admin.setEncodedPassword(passwordEncoder.encode("admin")); // Contraseña: admin
            
            // we give him the ADMIN role and USER role
            admin.setRoles(Arrays.asList("USER", "ADMIN"));
            admin.setType(User.UserType.ADMIN);

            userRepository.save(admin);
            
            System.out.println("--------------------------------------");
            System.out.println(" ADMIN USER AUTOMATICALLY CREATED ");
            System.out.println(" Email: " + adminEmail);
            System.out.println(" Pass:  admin");
            System.out.println("--------------------------------------");
        }
    }
}