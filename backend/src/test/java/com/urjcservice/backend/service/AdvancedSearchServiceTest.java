package com.urjcservice.backend.service;

import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.Software;
import com.urjcservice.backend.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AdvancedSearchServiceTest {

    @Autowired
    private AdvancedSearchService advancedSearchService;

    @Test
    public void testSearchRooms() {
        Page<Room> resultFull = advancedSearchService.searchRooms("Lab", 20, Room.CampusType.MOSTOLES, true, 0, 10);
        assertNotNull(resultFull, "La página no debe ser nula");

        Page<Room> resultEmpty = advancedSearchService.searchRooms(null, null, null, null, 0, 10);
        assertNotNull(resultEmpty);

        Page<Room> resultBlank = advancedSearchService.searchRooms("   ", null, null, null, 0, 10);
        assertNotNull(resultBlank);
    }

    @Test
    public void testSearchUsers() {
        Page<User> resultFull = advancedSearchService.searchUsers("Juan", true, "ADMIN", "Aula 1", LocalDate.now(), 0,
                10);
        assertNotNull(resultFull);

        Page<User> resultEmpty = advancedSearchService.searchUsers(null, null, null, null, null, 0, 10);
        assertNotNull(resultEmpty);

        Page<User> resultBlank = advancedSearchService.searchUsers("   ", null, "   ", "   ", null, 0, 10);
        assertNotNull(resultBlank);
    }

    @Test
    public void testSearchSoftwares() {
        Page<Software> resultFull = advancedSearchService.searchSoftwares("Eclipse", 10.5f, 0, 10);
        assertNotNull(resultFull);

        Page<Software> resultEmpty = advancedSearchService.searchSoftwares(null, null, 0, 10);
        assertNotNull(resultEmpty);

        Page<Software> resultBlank = advancedSearchService.searchSoftwares("   ", null, 0, 10);
        assertNotNull(resultBlank);
    }

    @Test
    public void testSearchReservations() {
        Page<Reservation> resultFull = advancedSearchService.searchReservations(1L, "Examen", LocalDate.now(), 0, 10);
        assertNotNull(resultFull);

        Page<Reservation> resultEmpty = advancedSearchService.searchReservations(1L, null, null, 0, 10);
        assertNotNull(resultEmpty);

        Page<Reservation> resultBlank = advancedSearchService.searchReservations(1L, "   ", null, 0, 10);
        assertNotNull(resultBlank);
    }
}