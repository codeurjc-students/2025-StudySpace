package com.urjcservice.backend.service;

import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.Arrays;

@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    private static final String SUPER_ADMIN_EMAIL = "studyspacetfg@gmail.com";
    private static final String ROLE_ADMIN = "ADMIN";

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
    }

    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> deleteById(Long id) {
        Optional<User> existing = userRepository.findById(id);

        existing.ifPresent(user -> {
            if (SUPER_ADMIN_EMAIL.equals(user.getEmail())) {
                throw new IllegalStateException("Cannot delete the Super Administrator.");
            }
            // if picture, first delete picture
            if (user.getImageName() != null && !user.getImageName().isEmpty()) {
                fileStorageService.delete(user.getImageName());
            }
            userRepository.delete(user);
        });

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

    // to upgrade or downgrade permisions
    public Optional<User> changeRole(Long id, String role) {
        return userRepository.findById(id).map(user -> {
            if (SUPER_ADMIN_EMAIL.equals(user.getEmail()) && !ROLE_ADMIN.equals(role)) {
                throw new IllegalStateException("Cannot remove the ADMIN role from the Super Administrator.");
            }
            // to avoid duplicities and conflicts
            user.getRoles().clear();

            if (ROLE_ADMIN.equals(role)) {
                user.getRoles().addAll(Arrays.asList("USER", ROLE_ADMIN));
                user.setType(User.UserType.ADMIN);
            } else {
                user.getRoles().add("USER");
                user.setType(User.UserType.USER_REGISTERED);
            }
            return userRepository.save(user);
        });
    }

    // to block or unblock a user
    public Optional<User> toggleBlock(Long id) {
        return userRepository.findById(id).map(user -> {
            if (SUPER_ADMIN_EMAIL.equals(user.getEmail())) {
                throw new IllegalStateException("Cannot block the Super Administrator.");
            }
            user.setBlocked(!user.isBlocked()); // inverts the value
            return userRepository.save(user);
        });
    }

    public boolean changePassword(String email, String oldPassword, String newPassword) {
        return userRepository.findByEmail(email).map(user -> {
            // passed password match with the actual one
            if (!passwordEncoder.matches(oldPassword, user.getEncodedPassword())) {
                return false;
            }
            user.setEncodedPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }).orElse(false);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public Optional<User> findByVerificationToken(String token) {
        return userRepository.findByVerificationToken(token);
    }

    @Scheduled(fixedRate = 3600000) // Every hour
    @Transactional
    public void deleteUnverifiedUsers() {
        userRepository.findAll().forEach(user -> {
            if (!user.isEmailVerified()
                    && user.getVerificationTokenExpiry() != null
                    && user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {

                userRepository.delete(user);
                System.out.println("Deleted unverified user: " + user.getEmail());
            }
        });
    }

}
