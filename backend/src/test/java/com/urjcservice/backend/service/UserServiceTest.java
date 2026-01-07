package com.urjcservice.backend.service;

import com.urjcservice.backend.controller.NoSuchElementExceptionCA;
import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.repositories.UserRepository;
import com.urjcservice.backend.controller.NoSuchElementExceptionCA; 

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;


import java.util.ArrayList;
import java.util.Optional;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock 
    private PasswordEncoder passwordEncoder;

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




    @Test
    public void testChangePassword_Success() {
        String email = "test@urjc.es";
        String oldPass = "1234";
        String newPass = "5678";
        User user = new User();
        user.setEmail(email);
        user.setEncodedPassword("encoded_1234");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPass, "encoded_1234")).thenReturn(true);
        when(passwordEncoder.encode(newPass)).thenReturn("encoded_5678");


        boolean result = userService.changePassword(email, oldPass, newPass);

        // Verify
        assertTrue(result);
        assertEquals("encoded_5678", user.getEncodedPassword());
        verify(userRepository).save(user);
    }

    @Test
    public void testChangePassword_WrongOldPassword() {
        String email = "test@urjc.es";
        User user = new User();
        user.setEncodedPassword("encoded_real");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong_pass", "encoded_real")).thenReturn(false);

        boolean result = userService.changePassword(email, "wrong_pass", "new_pass");

        assertFalse(result);
        verify(userRepository, never()).save(any());
    }





    @Test
    public void testFindAll() {
        when(userRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(Arrays.asList(new User(), new User())));
        Page<User> result = userService.findAll(PageRequest.of(0, 10));
        assertEquals(2, result.getContent().size());
    }

    @Test
    public void testFindById_Found() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Optional<User> result = userService.findById(1L);
        assertTrue(result.isPresent());
    }

    @Test
    public void testDeleteById_Success() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Optional<User> result = userService.deleteById(1L);
        verify(userRepository).delete(user);
        assertTrue(result.isPresent());
    }

    @Test
    public void testUpdateUser_Success() {
        User existing = new User();
        existing.setName("Old");
        User updated = new User();
        updated.setName("New");
        updated.setEmail("new@test.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Optional<User> result = userService.updateUser(1L, updated);
        assertEquals("New", result.get().getName());
    }

    @Test
    public void testPatchUser_OnlyName() {
        User existing = new User();
        existing.setName("Old Name");
        existing.setEmail("keep@test.com");
        User partial = new User();
        partial.setName("New Name");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Optional<User> result = userService.patchUser(1L, partial);
        assertEquals("New Name", result.get().getName());
        assertEquals("keep@test.com", result.get().getEmail());
    }





    @Test
    public void testGetUsers_Paginated() {
        // GIVEN
        Pageable pageable = PageRequest.of(0, 5);
        List<User> users = Arrays.asList(new User(), new User(), new User());
        Page<User> page = new PageImpl<>(users);

        when(userRepository.findAll(pageable)).thenReturn(page);

        // WHEN
        Page<User> result = userService.findAll(pageable);

        // THEN
        assertEquals(3, result.getContent().size());
        verify(userRepository).findAll(pageable);
    }

    @Test
    public void testChangeRole_UserToAdmin() {
        // GIVEN
        User user = new User();
        user.setId(1L);
        user.setRoles(new ArrayList<>(List.of("USER")));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        Optional<User> result = userService.changeRole(1L, "ADMIN");

        // THEN
        assertTrue(result.isPresent());
        assertTrue(result.get().getRoles().contains("ADMIN"));
    }


    @Test
    @DisplayName("Test Find By Id - Not Found")
    void testFindById_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(99L);

        assertTrue(result.isEmpty(), "It should return Optional.empty() if it doesn't exist");
    }

    @Test
    @DisplayName("Test Find By Email - Not Found")
    void testFindByEmail_NotFound() {
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        Optional<User> result = userService.findByEmail("nonexistent@test.com");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test Save User - Simple Save")
    void testSaveUser_Simple() {
        // Arrange
        User inputUser = new User();
        inputUser.setId(1L);
        inputUser.setEmail("test@test.com");
        inputUser.setEncodedPassword("somePassword"); 


        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // Act
        User result = userService.save(inputUser);
        
        // Assert
        assertNotNull(result);
        verify(userRepository).save(inputUser);
    }

    @Test
    @DisplayName("Test Update User - No Roles Update")
    void testUpdateUser_NoRoles() {
        // Arrange
        Long userId = 1L;
        
        User updateData = new User(); 
        updateData.setName("New Name");
        updateData.setEmail("new@test.com");
        updateData.setRoles(null); 

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Old Name");
        existingUser.setEmail("old@test.com");
        existingUser.setRoles(new ArrayList<>(List.of("USER")));

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Optional<User> result = userService.updateUser(userId, updateData);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("New Name", result.get().getName());
        assertEquals(1, result.get().getRoles().size()); 
        assertEquals("USER", result.get().getRoles().get(0));
    }

    @Test
    @DisplayName("Test Toggle Block - User Not Found")
    void testToggleBlock_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        
        Optional<User> result = userService.toggleBlock(99L);
        
        assertTrue(result.isEmpty());
    }
    

    
}
