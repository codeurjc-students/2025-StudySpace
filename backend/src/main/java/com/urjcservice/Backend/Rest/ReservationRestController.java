package com.urjcservice.Backend.Rest;

import com.urjcservice.Backend.Entities.Reservation;
import com.urjcservice.Backend.Service.ReservationService;
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

    @GetMapping("/")
    public List<Reservation> getAllReservations() {
        return reservationService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        Optional<Reservation> reservation = reservationService.findById(id);
        return reservation.map(ResponseEntity::ok) // Devuelve 200 OK si se encuentra
                          .orElseGet(() -> ResponseEntity.notFound().build()); // Devuelve 404 Not Found si no se encuentra
    }

    @PostMapping("/")
    public ResponseEntity<Reservation> createReservation(@RequestBody Reservation reservation) {
        Reservation savedReservation = reservationService.save(reservation);

        URI location = fromCurrentRequest().path("/{id}").buildAndExpand(savedReservation.getId()).toUri();

        return ResponseEntity.created(location).body(savedReservation);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(@PathVariable Long id, @RequestBody Reservation updatedReservation) {
        Optional<Reservation> reservation = reservationService.updateReservation(id, updatedReservation);
        return reservation.map(ResponseEntity::ok) // Devuelve 200 OK si se actualiza correctamente
                          .orElseGet(() -> ResponseEntity.notFound().build()); // Devuelve 404 Not Found si no se encuentra
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Reservation> patchReservation(@PathVariable Long id, @RequestBody Reservation partialReservation) {
        Optional<Reservation> reservation = reservationService.patchReservation(id, partialReservation);
        return reservation.map(ResponseEntity::ok) // Devuelve 200 OK si se actualiza parcialmente
                          .orElseGet(() -> ResponseEntity.notFound().build()); // Devuelve 404 Not Found si no se encuentra
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        boolean deleted = reservationService.deleteById(id);
        if (deleted) {
            return ResponseEntity.noContent().build(); // Devuelve 204 No Content si se elimina correctamente
        } else {
            return ResponseEntity.notFound().build(); // Devuelve 404 Not Found si no se encuentra
        }
    }
}
