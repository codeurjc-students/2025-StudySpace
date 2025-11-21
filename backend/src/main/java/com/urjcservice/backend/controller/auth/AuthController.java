package com.urjcservice.backend.controller.auth;

import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Clase auxiliar para recibir los datos del JSON
    public static class RegisterRequest {
        public String name;
        public String email;
        public String password;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        
        // 1. Comprobar si el email ya existe
        if (userRepository.findByEmail(request.email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Error: El email ya está en uso.");
        }

        // 2. Crear el nuevo usuario
        User newUser = new User();
        newUser.setName(request.name);
        newUser.setEmail(request.email);
        // IMPORTANTE: Encriptar la contraseña antes de guardarla
        newUser.setEncodedPassword(passwordEncoder.encode(request.password));
        
        // Asignar roles y tipo por defecto
        newUser.setType(User.UserType.USER_REGISTERED);
        newUser.setRoles(Arrays.asList("USER"));

        // 3. Guardar en la base de datos
        userRepository.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }
}