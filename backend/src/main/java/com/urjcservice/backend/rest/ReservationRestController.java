package com.urjcservice.backend.rest;

import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.security.Principal;

//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

@RestController
@RequestMapping("/api/reservations")
public class ReservationRestController {

    
    private final ReservationService reservationService;
    
    public ReservationRestController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

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

    @PatchMapping("/{id}/cancel")//try to integrate with the upper one
    public ResponseEntity<Reservation> cancelReservation(@PathVariable Long id,Principal principal,HttpServletRequest request) {//CHECK ittttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userEmail = principal.getName();
        //We verify if his role is admin
        boolean isAdmin = request.isUserInRole("ROLE_ADMIN");

        try {
            return reservationService.cancelByIdSecure(id, userEmail, isAdmin)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Reservation> deleteReservation(@PathVariable Long id) {
        Optional<Reservation> deleted = reservationService.deleteById(id);
        return deleted.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
