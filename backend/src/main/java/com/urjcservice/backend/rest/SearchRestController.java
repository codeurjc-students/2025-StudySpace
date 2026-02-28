package com.urjcservice.backend.rest;

import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.Software;
import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.service.AdvancedSearchService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchRestController {

    private final AdvancedSearchService searchService;

    public SearchRestController(AdvancedSearchService searchService) {
        this.searchService = searchService;
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
}