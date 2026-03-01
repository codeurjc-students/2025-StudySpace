package com.urjcservice.backend.rest;

import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.Software;
import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.service.AdvancedSearchService;
import com.urjcservice.backend.service.UserService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchRestController {

    private final AdvancedSearchService searchService;

    private final UserService userService;

    public SearchRestController(AdvancedSearchService searchService, UserService userService) {
        this.searchService = searchService;
        this.userService = userService;
    }

    @GetMapping("/rooms")
    public ResponseEntity<Page<Room>> searchRooms(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) Room.CampusType campus,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(searchService.searchRooms(text, minCapacity, campus, active, page, size));
    }

    @GetMapping("/users")
    public ResponseEntity<Page<User>> searchUsers(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) Boolean blocked,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String roomName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(searchService.searchUsers(text, blocked, role, roomName, date, page, size));
    }

    @GetMapping("/softwares")
    public ResponseEntity<Page<Software>> searchSoftwares(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) Float minVersion,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(searchService.searchSoftwares(text, minVersion, page, size));
    }

    @GetMapping("/reservations/user/{userId}")
    public ResponseEntity<Page<Reservation>> searchUserReservations(
            @PathVariable Long userId,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(searchService.searchReservations(userId, text, date, page, size));
    }

    @GetMapping("/reservations/me")
    public ResponseEntity<Page<Reservation>> searchMyReservations(
            Authentication authentication,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        User user = userService.findByEmail(authentication.getName()).orElseThrow();
        return ResponseEntity.ok(searchService.searchReservations(user.getId(), text, date, page, size));
    }
}