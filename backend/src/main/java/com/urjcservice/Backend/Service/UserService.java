package com.urjcservice.Backend.Service;

import com.urjcservice.Backend.Entities.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {

    private final List<User> users = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public List<User> findAll() {
        return new ArrayList<>(users); // Devuelve una copia para evitar modificaciones externas
    }

    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idCounter.getAndIncrement()); // Asigna un ID único
        }
        users.add(user);
        return user;
    }

    public Optional<User> findById(Long id) {
        return users.stream().filter(user -> user.getId().equals(id)).findFirst(); // Busca por ID
    }

    public boolean deleteById(Long id) {
        return users.removeIf(user -> user.getId().equals(id)); // Elimina por ID
    }

    public Optional<User> updateUser(Long id, User updatedUser) {
        return findById(id).map(existingUser -> {
            existingUser.setName(updatedUser.getName());
            existingUser.setEmail(updatedUser.getEmail());
            return existingUser;
        });
    }

    public Optional<User> patchUser(Long id, User partialUser) {
        return findById(id).map(existingUser -> {
            if (partialUser.getName() != null) {
                existingUser.setName(partialUser.getName());
            }
            if (partialUser.getEmail() != null) {
                existingUser.setEmail(partialUser.getEmail());
            }
            return existingUser;
        });
    }
}
