package com.urjcservice.backend.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.service.EmailService;
import com.urjcservice.backend.service.FileStorageService;
import com.urjcservice.backend.service.ReservationService;
import com.urjcservice.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.servlet.http.Cookie;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.urjcservice.backend.service.ReservationService;
import com.urjcservice.backend.service.UserService;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
public class UserRestControllerTest { 

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private EmailService emailService;

    private User mockUser;


    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("Test User");
        mockUser.setEmail("test@example.com");
        mockUser.setEncodedPassword("Pass123.");
        mockUser.setType(User.UserType.USER_REGISTERED);
    }




    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testListUsersAsAdmin() throws Exception {
        Page<User> page = new PageImpl<>(Collections.singletonList(mockUser));
        given(userService.findAll(any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test User"));
    }

    @Test
    public void testListUsersUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // --- 2. GET USER BY ID ---

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetUserById_Success() throws Exception {
        given(userService.findById(1L)).willReturn(Optional.of(mockUser));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetUserById_NotFound() throws Exception {
        given(userService.findById(99L)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    // --- 3. CREATE USER (POST) ---

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateUser_Success() throws Exception {
        String newUserJson = """
            {
                "name": "Test User",
                "email": "test@example.com",
                "password": "StrongPass1!" 
            }
        """;
        given(userService.save(any(User.class))).willReturn(mockUser);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newUserJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    // --- 4. UPDATE USER (PUT) ---

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateUser_Success() throws Exception {
        given(userService.updateUser(eq(1L), any(User.class))).willReturn(Optional.of(mockUser));

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateUser_NotFound() throws Exception {
        given(userService.updateUser(eq(99L), any(User.class))).willReturn(Optional.empty());

        mockMvc.perform(put("/api/users/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockUser)))
                .andExpect(status().isNotFound());
    }

    // --- 5. PATCH USER ---

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testPatchUser_Success() throws Exception {
        mockUser.setName("Patched Name");
        given(userService.patchUser(eq(1L), any(User.class))).willReturn(Optional.of(mockUser));

        String patchJson = "{\"name\": \"Patched Name\"}";

        mockMvc.perform(patch("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Patched Name"));
    }

    // --- 6. DELETE USER ---

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteUser_Success() throws Exception {
        given(userService.deleteById(1L)).willReturn(Optional.of(mockUser));

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteUser_NotFound() throws Exception {
        given(userService.deleteById(99L)).willReturn(Optional.empty());

        mockMvc.perform(delete("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    // --- 7. ROLES & BLOCKING ---

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testChangeRole_Success() throws Exception {
        User adminUser = new User();
        adminUser.setType(User.UserType.ADMIN);
        
        given(userService.changeRole(1L, "ADMIN")).willReturn(Optional.of(adminUser));

        mockMvc.perform(put("/api/users/1/role")
                .param("role", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testToggleBlock_User() throws Exception {
        mockUser.setBlocked(true);
        given(userService.toggleBlock(1L)).willReturn(Optional.of(mockUser));

        mockMvc.perform(put("/api/users/1/block")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.blocked").value(true));
    }

    // --- 8. GET USER RESERVATIONS ---

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetUserReservations_AsAdmin() throws Exception {
        Page<Reservation> reservationsPage = new PageImpl<>(Collections.emptyList());
        
        given(reservationService.getReservationsByUserId(eq(2L), any(Pageable.class)))
                .willReturn(reservationsPage);

        mockMvc.perform(get("/api/users/2/reservations")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }


    @Test
    @DisplayName("Create User via Admin API should fail with weak password")
    @WithMockUser(roles = "ADMIN")
    void testCreateUser_WeakPassword_ShouldFail() throws Exception {
        String invalidJson = """
            {
                "name": "Admin Created",
                "email": "valid@email.com",
                "password": "weak" 
            }
        """;

        mockMvc.perform(post("/api/users") 
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest()) //400
                .andExpect(jsonPath("$.message.password").exists());
    }





    @Test
    @WithMockUser(roles = "USER")
    public void testUploadUserImage_Success() throws Exception {
        // GIVEN
        MockMultipartFile file = new MockMultipartFile(
                "file", "profile.png", "image/png", "content".getBytes());
        
        String jwtToken = "token-falso-para-el-test";
        given(fileStorageService.store(any())).willReturn("uuid_profile.png");

        given(userService.findById(1L)).willReturn(Optional.of(mockUser));

        // WHEN & THEN
        mockMvc.perform(multipart("/api/users/1/image")
                .file(file)
                .with(csrf())
                .cookie(new Cookie("accessToken", jwtToken)) 
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageName").value("uuid_profile.png"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetUserImage_Success() throws Exception {
        // GIVEN
        mockUser.setImageName("my-photo.jpg");
        given(userService.findById(1L)).willReturn(Optional.of(mockUser));
        
        // Simulamos que el fichero existe y tiene contenido
        given(fileStorageService.loadAsResource("my-photo.jpg"))
                .willReturn(new ByteArrayResource("fake-image-content".getBytes()));

        // WHEN & THEN
        mockMvc.perform(get("/api/users/1/image"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG)) // O lo que detecte tu controlador
                .andExpect(content().bytes("fake-image-content".getBytes()));
    }

}