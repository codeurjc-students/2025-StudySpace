package com.urjcservice.backend.service;

import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.repositories.ReservationRepository;
import com.urjcservice.backend.repositories.UserRepository;
import com.urjcservice.backend.repositories.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}