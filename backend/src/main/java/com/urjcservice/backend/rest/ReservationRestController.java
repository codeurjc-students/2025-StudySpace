package com.urjcservice.backend.rest;

import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.service.ReservationService;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.security.Principal;
import java.time.LocalDate;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

@RestController
@RequestMapping("/api/reservations")
public class ReservationRestController {

    
    private final ReservationService reservationService;
    
    public ReservationRestController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    public static class ReservationRequest {
        private Long roomId;

        private Date startDate;
        private Date endDate;
        private String reason;
        
        public Long getRoomId() { return roomId; }
        public void setRoomId(Long roomId) { this.roomId = roomId; }
        public Date getStartDate() { return startDate; }
        public void setStartDate(Date startDate) { this.startDate = startDate; }
        public Date getEndDate() { return endDate; }
        public void setEndDate(Date endDate) { this.endDate = endDate; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    @GetMapping
    public Page<Reservation> getAllReservations(@PageableDefault(size = 10) Pageable pageable) {//if frontend dont send size, default 10
        return reservationService.findAll(pageable);
    }

    @GetMapping("/my-reservations")
    public ResponseEntity<Page<Reservation>> getMyReservations(
        @PageableDefault(size = 10) 
        @SortDefault(sort = "startDate", direction = Sort.Direction.DESC)
        Pageable pageable) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Page<Reservation> reservations = reservationService.getReservationsByUserEmail(email, pageable);
        
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        Optional<Reservation> reservation = reservationService.findById(id);
    return reservation.map(ResponseEntity::ok) // Returns 200 OK if found
              .orElseGet(() -> ResponseEntity.notFound().build()); // Returns 404 Not Found if not found
    }


    @PostMapping
    public ResponseEntity<Object> createReservation(@RequestBody ReservationRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = auth.getName();

            Reservation newReservation = reservationService.createReservation(request, currentUserEmail);
            
            URI location = fromCurrentRequest().path("/{id}").buildAndExpand(newReservation.getId()).toUri();
            return ResponseEntity.created(location).body(newReservation);

        } catch (IllegalArgumentException e) {
             return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
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

    @GetMapping("/check-availability")
    public ResponseEntity<List<Reservation>> checkAvailability(
            @RequestParam Long roomId, 
            @RequestParam String date) { 
        
        try {
            LocalDate localDate = LocalDate.parse(date);
            List<Reservation> reservations = reservationService.getActiveReservationsForRoomAndDate(roomId, localDate);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    




}
