package com.urjcservice.backend.controller.auth;

import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.security.jwt.AuthResponse;
import com.urjcservice.backend.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;

import java.util.Arrays;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;     

    
    private final PasswordEncoder passwordEncoder;

    public AuthController(PasswordEncoder passwordEncoder,UserService userService) {
        
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    // for JSON data
    public static class RegisterRequest {

        @NotBlank(message = "Name cannot be empty")
        private String name;

        @NotBlank(message = "Email cannot be empty")
        @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", 
            message = "Email must be valid domain and contain an extension (e.g., exampleemail@gmail.com)"
        )
        private String email;

        @NotBlank(message = "Password cannot be empty")
        @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@$!%*?&.])(?=\\S+$).{8,}$",
            message = "Password must have at least 8 chars: 1 or more uppercase, lowercase, number and special characters."
        )
        private String password;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }



    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;

        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    
   @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<User> userOpt = userService.findByEmail(auth.getName());
        
        return userOpt.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@Valid@RequestBody RegisterRequest request) {
        
        // Check if the email already exists
        if (userService.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new AuthResponse(AuthResponse.Status.FAILURE,"Error: The email is already in use."));
        }

        // Create new user account
        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        // Encrypt the password before saving
        newUser.setEncodedPassword(passwordEncoder.encode(request.getPassword()));
        
        // Set default user type and roles
        newUser.setType(User.UserType.USER_REGISTERED);
        newUser.setRoles(Arrays.asList("USER"));

        //Save on the repository
        userService.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateMe(@RequestBody User request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        //it search by the email of the logged user
        Optional<User> userOpt = userService.findByEmail(auth.getName());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            //only if data is provided
            if (request.getName() != null && !request.getName().isEmpty()) user.setName(request.getName());
            
            
            userService.save(user);
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }




    @PostMapping("/change-password")
    public ResponseEntity<AuthResponse> changePassword(@RequestBody ChangePasswordRequest request) {
        // obtain user email
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = auth.getName();
        
        boolean success = userService.changePassword(email, request.getOldPassword(), request.getNewPassword());

        if (success) {
            return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, "Password successfully updated"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(AuthResponse.Status.FAILURE, "The current password is incorrect"));
        }
    }


}