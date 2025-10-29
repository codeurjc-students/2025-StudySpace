package com.urjcservice.Backend.Rest;

import com.urjcservice.Backend.Entities.Software;
import com.urjcservice.Backend.Service.SoftwareService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/")
    public List<Software> getAllSoftware() {
        return softwareService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Software> getSoftwareById(@PathVariable Long id) {
        Optional<Software> software = softwareService.findById(id);
        return software.map(ResponseEntity::ok) // Devuelve 200 OK si se encuentra
                       .orElseGet(() -> ResponseEntity.notFound().build()); // Devuelve 404 Not Found si no se encuentra
    }

    @PostMapping("/")
    public ResponseEntity<Software> createSoftware(@RequestBody Software software) {
        Software savedSoftware = softwareService.save(software);

        URI location = fromCurrentRequest().path("/{id}").buildAndExpand(savedSoftware.getId()).toUri();

        return ResponseEntity.created(location).body(savedSoftware);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Software> updateSoftware(@PathVariable Long id, @RequestBody Software updatedSoftware) {
        Optional<Software> software = softwareService.updateSoftware(id, updatedSoftware);
        return software.map(ResponseEntity::ok) // Devuelve 200 OK si se actualiza correctamente
                       .orElseGet(() -> ResponseEntity.notFound().build()); // Devuelve 404 Not Found si no se encuentra
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Software> patchSoftware(@PathVariable Long id, @RequestBody Software partialSoftware) {
        Optional<Software> software = softwareService.patchSoftware(id, partialSoftware);
        return software.map(ResponseEntity::ok) // Devuelve 200 OK si se actualiza parcialmente
                       .orElseGet(() -> ResponseEntity.notFound().build()); // Devuelve 404 Not Found si no se encuentra
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Software> deleteSoftware(@PathVariable Long id) {
        Optional<Software> deleted = softwareService.deleteById(id);
        return deleted.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
