package com.urjcservice.backend.rest;

import com.urjcservice.backend.entities.Campus;
import com.urjcservice.backend.service.CampusService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campus")
public class CampusRestController {

    private final CampusService campusService;

    public CampusRestController(CampusService campusService) {
        this.campusService = campusService;
    }

    @GetMapping
    public ResponseEntity<List<Campus>> getAllCampuses() {
        return ResponseEntity.ok(campusService.findAll());
    }

    @PostMapping
    public ResponseEntity<Campus> createCampus(@RequestBody Campus campus) {
        return ResponseEntity.ok(campusService.save(campus));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Campus> updateCampus(@PathVariable Long id, @RequestBody Campus campus) {
        return campusService.update(id, campus)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Campus> deleteCampus(@PathVariable Long id) {
        return campusService.delete(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}