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

    // Clase auxiliar para recibir los datos del JSON
    public static class ReservationRequest {
        public Long roomId;
        public Date startDate;
        public Date endDate;
        public String reason;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createReservation(@RequestBody ReservationRequest request) {
        
        // 1. Obtener el usuario logueado de la sesión de seguridad
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); // En tu caso configuramos que el "name" sea el email
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no encontrado.");
        }

        // 2. Obtener el aula
        Optional<Room> roomOpt = roomRepository.findById(request.roomId);
        if (roomOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aula no encontrada.");
        }

        // 3. Crear la reserva
        Reservation reservation = new Reservation();
        reservation.setStartDate(request.startDate);
        reservation.setEndDate(request.endDate);
        reservation.setReason(request.reason);
        reservation.setUser(userOpt.get()); // Asignamos el usuario logueado
        reservation.setRoom(roomOpt.get()); // Asignamos el aula seleccionada

        reservationRepository.save(reservation);

        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }
}