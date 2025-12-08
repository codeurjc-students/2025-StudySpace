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

import java.util.Date; 
import java.util.Optional;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    
    public ReservationController(ReservationRepository reservationRepository,
                                 UserRepository userRepository,
                                 RoomRepository roomRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    //auxiliar class to receive reservation data from frontend(JSON)
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

    @PostMapping("/create")
    public ResponseEntity<Object> createReservation(@RequestBody ReservationRequest request) {
        
       
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); 
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found.");
        }

        
        Optional<Room> roomOpt = roomRepository.findById(request.getRoomId());
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