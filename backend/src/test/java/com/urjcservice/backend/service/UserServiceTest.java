package com.urjcservice.backend.service;

import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
   @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void testToggleBlock() {
        User user = new User();
        user.setId(1L);
        user.setBlocked(false); //not blocked initially

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        Optional<User> result = userService.toggleBlock(1L);

        // Verify
        assertTrue(result.isPresent());
        assertTrue(result.get().isBlocked());
    }

    @Test
    public void testChangeRoleToAdmin() {
        User user = new User();
        user.setId(1L);
        user.setRoles(new ArrayList<>()); //empty roles

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.changeRole(1L, "ADMIN");

        //Verify roles
        assertTrue(user.getRoles().contains("ADMIN"));
        assertEquals(User.UserType.ADMIN, user.getType());
    }
}
