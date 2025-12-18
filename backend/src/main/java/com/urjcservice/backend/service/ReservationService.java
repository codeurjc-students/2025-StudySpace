package com.urjcservice.backend.service;

import com.urjcservice.backend.controller.ReservationController.ReservationRequest;
import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.repositories.ReservationRepository;
import com.urjcservice.backend.repositories.UserRepository;
import com.urjcservice.backend.repositories.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    
    public ReservationService(ReservationRepository reservationRepository,
                              UserRepository userRepository,
                              RoomRepository roomRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public Reservation save(Reservation reservation) {
        
        Long uid = reservation.getUserId();
        if (uid != null) {
            userRepository.findById(uid).ifPresent(reservation::setUser);
        } 
        
        Long rid = reservation.getRoomId();
        if (rid != null) {
            roomRepository.findById(rid).ifPresent(reservation::setRoom);
        }

        
        return reservationRepository.save(reservation);
    }

    public Optional<Reservation> findById(Long id) {
        return reservationRepository.findById(id);
    }

    public Optional<Reservation> deleteById(Long id) {
        Optional<Reservation> existing = reservationRepository.findById(id);
        existing.ifPresent(res -> reservationRepository.delete(res));
        return existing;
    }

    
    public Optional<Reservation> cancelById(Long id) { //maybe not used in future CHECK ITTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT
        return reservationRepository.findById(id).map(res -> {
            res.setCancelled(true); 
            return reservationRepository.save(res);
        });
    }



    public Optional<Reservation> cancelByIdSecure(Long id, String userEmail,boolean isAdmin) {
        return reservationRepository.findById(id).map(res -> {
        //only for admins or the own user
        if (isAdmin || res.getUser().getEmail().equals(userEmail)) {
            res.setCancelled(true);
            return reservationRepository.save(res);
        }

        throw new AccessDeniedException("You do not have permission to cancel this reservation.");
    });
    }
    
    public Optional<Reservation> updateReservation(Long id, Reservation updatedReservation) {
        return reservationRepository.findById(id).map(existingReservation -> {
            updateReservationUser(existingReservation, updatedReservation.getUserId());
            updateReservationRoom(existingReservation, updatedReservation.getRoomId());
            updateReservationDetails(existingReservation, updatedReservation);
            return reservationRepository.save(existingReservation);
        });
    }

    //auxiliar methods
    private void updateReservationUser(Reservation reservation, Long newUserId) {
        if (newUserId != null) {
            userRepository.findById(newUserId).ifPresent(reservation::setUser);
        }
    }

    private void updateReservationRoom(Reservation reservation, Long newRoomId) {
        if (newRoomId != null) {
            roomRepository.findById(newRoomId).ifPresent(reservation::setRoom);
        }
    }

    private void updateReservationDetails(Reservation existing, Reservation updated) {
        if (updated.getStartDate() != null) {
            existing.setStartDate(updated.getStartDate());
        }
        if (updated.getEndDate() != null) {
            existing.setEndDate(updated.getEndDate());
        }
        if (updated.getReason() != null) {
            existing.setReason(updated.getReason());
        }
    }

   
    public Optional<Reservation> patchReservation(Long id, Reservation partialReservation) {
        return updateReservation(id, partialReservation);
    }


    public Reservation createReservation(ReservationRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.isActive()) {
        throw new RuntimeException("Reservations are not possible: The classroom is temporarily unavailable.");
        }
        List<Reservation> overlaps = reservationRepository.findOverlappingReservations(
            request.getRoomId(), request.getStartDate(), request.getEndDate());

        if (!overlaps.isEmpty()) {
            throw new RuntimeException("The room is already reserved for this time.");
        }
        Reservation reservation = new Reservation();
        reservation.setStartDate(request.getStartDate());
        reservation.setEndDate(request.getEndDate());
        reservation.setReason(request.getReason());
        reservation.setUser(user);
        reservation.setRoom(room);
        reservation.setCancelled(false);

        return reservationRepository.save(reservation);
    }


    public List<Reservation> getReservationsByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        //this is on reposity check ittttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt
        return reservationRepository.findByUser(user);
    }





}