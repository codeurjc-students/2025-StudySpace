package com.urjcservice.backend.controller.auth;

import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // for JSON data
    public static class RegisterRequest {
        public String name;
        public String email;
        public String password;
    }

    public static class UserUpdateRequest {
        public String name;
        public String email;
    }

    @GetMapping("/me")
    public ResponseEntity<User> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Fetch user details from the repository
        return userRepository.findByEmail(auth.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        
        // Check if the email already exists
        if (userRepository.findByEmail(request.email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Error: El email ya está en uso.");
        }

        // Create new user account
        User newUser = new User();
        newUser.setName(request.name);
        newUser.setEmail(request.email);
        // Encrypt the password before saving
        newUser.setEncodedPassword(passwordEncoder.encode(request.password));
        
        // Set default user type and roles
        newUser.setType(User.UserType.USER_REGISTERED);
        newUser.setRoles(Arrays.asList("USER"));

        //Save on the repository
        userRepository.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateMe(@RequestBody UserUpdateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        //it search by the email of the logged user
        Optional<User> userOpt = userRepository.findByEmail(auth.getName());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            //only if data is provided
            if (request.name != null && !request.name.isEmpty()) user.setName(request.name);
            
            
            userRepository.save(user);
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }
}