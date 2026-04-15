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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AdvancedSearchServiceTest {

    @Autowired
    private AdvancedSearchService advancedSearchService;
    @Autowired
    private EntityManager entityManager;

    @Test
    public void testSearchRooms() {
        Page<Room> resultFull = advancedSearchService.searchRooms("Lab", 20, 1L, true, 0, 10);
        assertNotNull(resultFull, "The page must not be null");

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

    @Test
    public void testSearchUsers_StrictRoomAndDate_Coverage() {
        User user = new User();
        user.setEmail("memoria@test.com");

        Room room = new Room();
        room.setName("Aula 55");

        Reservation res = new Reservation();
        res.setStartDate(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        res.setEndDate(Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));

        user.addReservation(res);
        room.addReservation(res);

        // room and date match
        Boolean perfectMatch = ReflectionTestUtils.invokeMethod(
                advancedSearchService,
                "matchesStrictRoomAndDate",
                user, "Aula 55", LocalDate.now());
        Assertions.assertTrue(perfectMatch, "The classroom and date must match");

        // diferent room
        Boolean falseRoomMatch = ReflectionTestUtils.invokeMethod(
                advancedSearchService,
                "matchesStrictRoomAndDate",
                user, "Aula Equivocada", LocalDate.now());
        Assertions.assertFalse(falseRoomMatch, "It should not match if the classroom is different.");

        // diferent date
        Boolean falseDateMatch = ReflectionTestUtils.invokeMethod(
                advancedSearchService,
                "matchesStrictRoomAndDate",
                user, "Aula 55", LocalDate.now().plusDays(2));
        Assertions.assertFalse(falseDateMatch, "It should not match if the date is different.");

        // null room name
        Boolean nullMatch = ReflectionTestUtils.invokeMethod(
                advancedSearchService,
                "matchesStrictRoomAndDate",
                user, null, LocalDate.now());
        Assertions.assertTrue(nullMatch, "If roomName is null, it should return true by default");

        // null pointer no room asigned
        Reservation emptyRes = new Reservation();
        user.addReservation(emptyRes);
        Boolean emptyResMatch = ReflectionTestUtils.invokeMethod(
                advancedSearchService,
                "matchesStrictRoomAndDate",
                user, "Aula Fantasma", LocalDate.now());
        Assertions.assertFalse(emptyResMatch, "Should safely handle reservations without rooms/dates");
    }
}