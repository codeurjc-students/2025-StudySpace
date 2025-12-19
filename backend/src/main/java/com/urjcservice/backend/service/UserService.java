package com.urjcservice.backend.service;

import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Arrays;


@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> deleteById(Long id) {
        Optional<User> existing = userRepository.findById(id);
        existing.ifPresent(userRepository::delete);
        return existing;
    }

    public Optional<User> updateUser(Long id, User updatedUser) {
        return userRepository.findById(id).map(existingUser -> {
            existingUser.setName(updatedUser.getName());
            existingUser.setEmail(updatedUser.getEmail());
            return userRepository.save(existingUser);
        });
    }

    public Optional<User> patchUser(Long id, User partialUser) {
        return userRepository.findById(id).map(existingUser -> {
            if (partialUser.getName() != null) {
                existingUser.setName(partialUser.getName());
            }
            if (partialUser.getEmail() != null) {
                existingUser.setEmail(partialUser.getEmail());
            }
            return userRepository.save(existingUser);
        });
    }

    //to upgrade or downgrade permisions
    public Optional<User> changeRole(Long id, String role) {
        return userRepository.findById(id).map(user -> {
            //to avoid duplicities and conflicts
            user.getRoles().clear(); 
            
            if ("ADMIN".equals(role)) {
                user.getRoles().addAll(Arrays.asList("USER", "ADMIN"));
                user.setType(User.UserType.ADMIN);
            } else {
                user.getRoles().add("USER");
                user.setType(User.UserType.USER_REGISTERED);
            }
            return userRepository.save(user);
        });
    }

    //to block or unblock a user
    public Optional<User> toggleBlock(Long id) {
        return userRepository.findById(id).map(user -> {
            user.setBlocked(!user.isBlocked()); //inverts the value
            return userRepository.save(user);
        });
    }




    public boolean changePassword(String email, String oldPassword, String newPassword) {
    return userRepository.findByEmail(email).map(user -> {
        //passed password match with the actual one
        if (!passwordEncoder.matches(oldPassword, user.getEncodedPassword())) {
            return false;
        }
        user.setEncodedPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }).orElse(false);
}


}
