package com.urjcservice.backend.rest;

import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

@RestController
@RequestMapping("/api/reservations")
public class ReservationRestController {

    @Autowired
    private ReservationService reservationService;

    @GetMapping
    public List<Reservation> getAllReservations() {
        return reservationService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        Optional<Reservation> reservation = reservationService.findById(id);
    return reservation.map(ResponseEntity::ok) // Returns 200 OK if found
              .orElseGet(() -> ResponseEntity.notFound().build()); // Returns 404 Not Found if not found
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation(@RequestBody Reservation reservation) {
        Reservation savedReservation = reservationService.save(reservation);

        URI location = fromCurrentRequest().path("/{id}").buildAndExpand(savedReservation.getId()).toUri();

        return ResponseEntity.created(location).body(savedReservation);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(@PathVariable Long id, @RequestBody Reservation updatedReservation) {
    Optional<Reservation> reservation = reservationService.updateReservation(id, updatedReservation);
    return reservation.map(ResponseEntity::ok) // Returns 200 OK if updated successfully
              .orElseGet(() -> ResponseEntity.notFound().build()); // Returns 404 Not Found if not found
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Reservation> patchReservation(@PathVariable Long id, @RequestBody Reservation partialReservation) {
    Optional<Reservation> reservation = reservationService.patchReservation(id, partialReservation);
    return reservation.map(ResponseEntity::ok) // Returns 200 OK if partially updated
              .orElseGet(() -> ResponseEntity.notFound().build()); // Returns 404 Not Found if not found
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Reservation> deleteReservation(@PathVariable Long id) {
        Optional<Reservation> deleted = reservationService.deleteById(id);
        return deleted.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
