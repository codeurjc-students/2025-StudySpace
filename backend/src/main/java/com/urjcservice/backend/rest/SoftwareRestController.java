package com.urjcservice.backend.rest;

import com.urjcservice.backend.entities.Software;
import com.urjcservice.backend.repositories.SoftwareRepository;
import com.urjcservice.backend.service.SoftwareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

@RestController
@RequestMapping("/api/softwares")
public class SoftwareRestController {

    @Autowired
    private SoftwareService softwareService;

    @Autowired
    private SoftwareRepository softwareRepository;

    // DTO 
    public static class SoftwareRequest {
        public String name;
        public Float version;
        public String description;
    }

    /*
    @GetMapping
    public List<Software> getAllSoftware() {
        return softwareService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Software> getSoftwareById(@PathVariable Long id) {
        Optional<Software> software = softwareService.findById(id);
    return software.map(ResponseEntity::ok) // Returns 200 OK if found
               .orElseGet(() -> ResponseEntity.notFound().build()); // Returns 404 Not Found if not found
    }

    @PostMapping
    public ResponseEntity<Software> createSoftware(@RequestBody Software software) {
        Software savedSoftware = softwareService.save(software);

        URI location = fromCurrentRequest().path("/{id}").buildAndExpand(savedSoftware.getId()).toUri();

        return ResponseEntity.created(location).body(savedSoftware);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Software> updateSoftware(@PathVariable Long id, @RequestBody Software updatedSoftware) {
    Optional<Software> software = softwareService.updateSoftware(id, updatedSoftware);
    return software.map(ResponseEntity::ok) // Returns 200 OK if updated successfully
               .orElseGet(() -> ResponseEntity.notFound().build()); // Returns 404 Not Found if not found
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Software> patchSoftware(@PathVariable Long id, @RequestBody Software partialSoftware) {
    Optional<Software> software = softwareService.patchSoftware(id, partialSoftware);
    return software.map(ResponseEntity::ok) // Returns 200 OK if partially updated
               .orElseGet(() -> ResponseEntity.notFound().build()); // Returns 404 Not Found if not found
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Software> deleteSoftware(@PathVariable Long id) {
        Optional<Software> deleted = softwareService.deleteById(id);
        return deleted.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }*/

                      
    @GetMapping
    public List<Software> getAllSoftware() {
        return softwareRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Software> getSoftwareById(@PathVariable Long id) {
        return softwareRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Software> createSoftware(@RequestBody SoftwareRequest request) {
        Software software = new Software();
        return saveSoftwareData(software, request, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Software> updateSoftware(@PathVariable Long id, @RequestBody SoftwareRequest request) {
        Optional<Software> softwareOpt = softwareRepository.findById(id);
        if (softwareOpt.isPresent()) {
            return saveSoftwareData(softwareOpt.get(), request, HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSoftware(@PathVariable Long id) {
        if (softwareRepository.existsById(id)) {
            softwareRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private ResponseEntity<Software> saveSoftwareData(Software software, SoftwareRequest request, HttpStatus status) {
        software.setName(request.name);
        software.setVersion(request.version);
        software.setDescription(request.description);
        
        Software saved = softwareRepository.save(software);
        return ResponseEntity.status(status).body(saved);
    }


}
