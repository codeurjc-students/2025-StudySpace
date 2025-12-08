package com.urjcservice.backend.rest;

import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.repositories.RoomRepository;
import com.urjcservice.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ReservationApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    private Long testRoomId;
    private String testUserEmail = "test.reservas@example.com";

    @BeforeEach
    void setUp() {
        if (userRepository.findByEmail(testUserEmail).isEmpty()) {
            User user = new User();
            user.setName("Tester Reservas");
            user.setEmail(testUserEmail);
            user.setEncodedPassword("pass");
            user.setRoles(Arrays.asList("USER"));
            user.setType(User.UserType.USER_REGISTERED);
            userRepository.save(user);
        }

        Room room = new Room();
        room.setName("Aula Test Reservas");
        room.setCapacity(20);
        room.setCamp(Room.CampusType.MOSTOLES);
        Room savedRoom = roomRepository.save(room);
        this.testRoomId = savedRoom.getId();
    }

    @Test
    public void testCreateReservationUnauthenticated() throws Exception {
        String reservation = """
            {
                "roomId": %d,
                "startDate": "2025-12-01T10:00:00.000Z",
                "endDate": "2025-12-01T12:00:00.000Z",
                "reason": "Test Reserva"
            }
        """.formatted(testRoomId);

        mockMvc.perform(post("/api/reservations/create")
                .content(reservation)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test.reservas@example.com", roles = {"USER"}) 
    public void testCreateReservationAuthenticated() throws Exception {
        String reservation = """
            {
                "roomId": %d,
                "startDate": "2025-12-01T10:00:00.000Z",
                "endDate": "2025-12-01T12:00:00.000Z",
                "reason": "Test Reserva Auth"
            }
        """.formatted(testRoomId);

        mockMvc.perform(post("/api/reservations/create")
                .content(reservation)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }
}