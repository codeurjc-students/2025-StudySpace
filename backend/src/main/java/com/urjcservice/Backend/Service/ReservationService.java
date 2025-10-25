package com.urjcservice.Backend.Service;

import com.urjcservice.Backend.Entities.Reservation;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReservationService {

    private final List<Reservation> reservations = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public List<Reservation> findAll() {
        return new ArrayList<>(reservations); // Devuelve una copia para evitar modificaciones externas
    }

    public Reservation save(Reservation reservation) {
        if (reservation.getId() == null) {
            reservation.setId(idCounter.getAndIncrement()); // Asigna un ID único
        }
        reservations.add(reservation);
        return reservation;
    }

    public Optional<Reservation> findById(Long id) {
        return reservations.stream().filter(reservation -> reservation.getId().equals(id)).findFirst(); // Busca por ID
    }

    public boolean deleteById(Long id) {
        return reservations.removeIf(reservation -> reservation.getId().equals(id)); // Elimina por ID
    }

    public Optional<Reservation> updateReservation(Long id, Reservation updatedReservation) {
        return findById(id).map(existingReservation -> {
            existingReservation.setRoomId(updatedReservation.getRoomId());
            existingReservation.setUserId(updatedReservation.getUserId());
            existingReservation.setStartTime(updatedReservation.getStartTime());
            existingReservation.setEndTime(updatedReservation.getEndTime());
            return existingReservation;
        });
    }

    public Optional<Reservation> patchReservation(Long id, Reservation partialReservation) {
        return findById(id).map(existingReservation -> {
            if (partialReservation.getRoomId() != null) {
                existingReservation.setRoomId(partialReservation.getRoomId());
            }
            if (partialReservation.getUserId() != null) {
                existingReservation.setUserId(partialReservation.getUserId());
            }
            if (partialReservation.getStartTime() != null) {
                existingReservation.setStartTime(partialReservation.getStartTime());
            }
            if (partialReservation.getEndTime() != null) {
                existingReservation.setEndTime(partialReservation.getEndTime());
            }
            return existingReservation;
        });
    }
}
