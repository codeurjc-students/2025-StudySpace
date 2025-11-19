package com.urjcservice.backend.service;

import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

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
}
