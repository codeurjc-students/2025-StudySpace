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

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

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
            
            //update only if changed
            Long newUserId = updatedReservation.getUserId();
            if (newUserId != null) {
                //only asigned the user not saving it, hibernate will manage the rest
                User newUser = userRepository.findById(newUserId).orElse(null);
                if (newUser != null) {
                    existingReservation.setUser(newUser);
                }
            }

            //update only if changed
            Long newRoomId = updatedReservation.getRoomId();
            if (newRoomId != null) {
                Room newRoom = roomRepository.findById(newRoomId).orElse(null);
                if (newRoom != null) {
                    existingReservation.setRoom(newRoom);
                }
            }

            //update only if changed
            if (updatedReservation.getStartDate() != null) 
                existingReservation.setStartDate(updatedReservation.getStartDate());
            if (updatedReservation.getEndDate() != null) 
                existingReservation.setEndDate(updatedReservation.getEndDate());
            if (updatedReservation.getReason() != null) 
                existingReservation.setReason(updatedReservation.getReason());

            
            return reservationRepository.save(existingReservation);
        });
    }

   
    public Optional<Reservation> patchReservation(Long id, Reservation partialReservation) {
        return updateReservation(id, partialReservation);
    }
}