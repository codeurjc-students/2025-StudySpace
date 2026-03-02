package com.urjcservice.backend.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.urjcservice.backend.TestRepositoryMocks;
import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.entities.Software;
import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.service.AdvancedSearchService;
import com.urjcservice.backend.service.UserService;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestRepositoryMocks.class)
@AutoConfigureMockMvc
public class SearchRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdvancedSearchService searchService;

    @MockBean
    private UserService userService;

    @Test
    public void searchRooms_ShouldReturnPage() throws Exception {
        Page<Room> page = new PageImpl<>(Collections.singletonList(new Room()));
        when(searchService.searchRooms(any(), any(), any(), any(), any(Integer.class), any(Integer.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/search/rooms")
                .param("text", "foo")
                .param("page", "0")
                .param("size", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void searchUsers_ShouldReturnPage() throws Exception {
        Page<User> page = new PageImpl<>(Collections.emptyList());
        when(searchService.searchUsers(any(), any(), any(), any(), any(LocalDate.class), any(Integer.class),
                any(Integer.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/search/users")
                .param("page", "0")
                .param("size", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void searchSoftwares_ShouldReturnPage() throws Exception {
        Page<Software> page = new PageImpl<>(Collections.emptyList());
        when(searchService.searchSoftwares(any(), any(), any(Integer.class), any(Integer.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/search/softwares")
                .param("text", "bar")
                .param("page", "1")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void searchUserReservations_ShouldReturnPage() throws Exception {
        Page<Reservation> page = new PageImpl<>(Collections.emptyList());
        when(searchService.searchReservations(eq(42L), any(), any(LocalDate.class), any(Integer.class),
                any(Integer.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/search/reservations/user/42")
                .param("page", "0")
                .param("size", "2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "someone@example.com", roles = "USER")
    public void searchMyReservations_ShouldReturnPage() throws Exception {
        Page<Reservation> page = new PageImpl<>(Collections.emptyList());
        // userService stub
        User u = new User();
        u.setId(99L);
        when(userService.findByEmail(any())).thenReturn(java.util.Optional.of(u));
        when(searchService.searchReservations(eq(99L), any(), any(LocalDate.class), any(Integer.class),
                any(Integer.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/search/reservations/me")
                .principal(() -> "someone@example.com")
                .param("page", "0")
                .param("size", "3")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
