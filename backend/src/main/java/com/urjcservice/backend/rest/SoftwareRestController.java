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

    private final SoftwareService softwareService;

    public SoftwareRestController(SoftwareService softwareService) {
        this.softwareService = softwareService;
    }

    // DTO 
    public static class SoftwareRequest {
        private String name;
        private Float version;
        private String description;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Float getVersion() { return version; }
        public void setVersion(Float version) { this.version = version; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    @GetMapping
    public List<Software> getAllSoftware() {
        return softwareService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Software> getSoftwareById(@PathVariable Long id) {
        return softwareService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Software> createSoftware(@RequestBody SoftwareRequest request) {
        //to dto the entity
        Software software = new Software();
        software.setName(request.name);
        software.setVersion(request.version);
        software.setDescription(request.description);

        Software saved = softwareService.save(software);
        
        URI location = fromCurrentRequest().path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Software> updateSoftware(@PathVariable Long id, @RequestBody SoftwareRequest request) {
        Software softwareData = new Software();
        softwareData.setName(request.name);
        softwareData.setVersion(request.version);
        softwareData.setDescription(request.description);

        return softwareService.updateSoftware(id, softwareData)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSoftware(@PathVariable Long id) {
        Optional<Software> deleted = softwareService.deleteById(id);
        if (deleted.isPresent()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }


}
