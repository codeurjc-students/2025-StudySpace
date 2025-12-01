package com.urjcservice.backend.controller;

import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.repositories.ReservationRepository;
import com.urjcservice.backend.repositories.RoomRepository;
import com.urjcservice.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date; // O java.time.LocalDate si prefieres
import java.util.Optional;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoomRepository roomRepository;

    //auxiliar class to receive reservation data from frontend(JSON)
    public static class ReservationRequest {
        public Long roomId;
        public Date startDate;
        public Date endDate;
        public String reason;
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createReservation(@RequestBody ReservationRequest request) {
        
       
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); 
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found.");
        }

        
        Optional<Room> roomOpt = roomRepository.findById(request.roomId);
        if (roomOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Room not found.");
        }

        // Create the reservation
        Reservation reservation = new Reservation();
        reservation.setStartDate(request.startDate);
        reservation.setEndDate(request.endDate);
        reservation.setReason(request.reason);
        reservation.setUser(userOpt.get()); 
        reservation.setRoom(roomOpt.get()); 
        reservationRepository.save(reservation);

        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }
}