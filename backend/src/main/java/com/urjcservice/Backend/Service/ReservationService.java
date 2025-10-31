package com.urjcservice.Backend.Service;

import com.urjcservice.Backend.Entities.Reservation;
import com.urjcservice.Backend.Entities.User;
import com.urjcservice.Backend.Entities.Room;
import com.urjcservice.Backend.Repositories.ReservationRepository;
import com.urjcservice.Backend.Repositories.UserRepository;
import com.urjcservice.Backend.Repositories.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    public java.util.List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public Reservation save(Reservation reservation) {
        // link user
        Long uid = reservation.getUserId();
        if (uid != null) {
            userRepository.findById(uid).ifPresent(user -> {
                reservation.setUser(user);
            });
        } else if (reservation.getUser() != null && reservation.getUser().getId() != null) {
            userRepository.findById(reservation.getUser().getId()).ifPresent(reservation::setUser);
        }

        // link room
        Long rid = reservation.getRoomId();
        if (rid != null) {
            roomRepository.findById(rid).ifPresent(room -> {
                reservation.setRoom(room);
            });
        } else if (reservation.getRoom() != null && reservation.getRoom().getId() != null) {
            roomRepository.findById(reservation.getRoom().getId()).ifPresent(reservation::setRoom);
        }

        Reservation saved = reservationRepository.save(reservation);

        // maintain bidirectional relations
        if (saved.getUser() != null) {
            User u = saved.getUser();
            if (!u.getReservations().contains(saved)) {
                u.getReservations().add(saved);
                userRepository.save(u);
            }
        }
        if (saved.getRoom() != null) {
            Room r = saved.getRoom();
            if (!r.getReservations().contains(saved)) {
                r.getReservations().add(saved);
                roomRepository.save(r);
            }
        }

        return saved;
    }

    public Optional<Reservation> findById(Long id) {
        return reservationRepository.findById(id);
    }

    public Optional<Reservation> deleteById(Long id) {
        Optional<Reservation> existing = reservationRepository.findById(id);
        existing.ifPresent(res -> {
            if (res.getUser() != null) {
                User u = res.getUser();
                u.getReservations().remove(res);
                userRepository.save(u);
            }
            if (res.getRoom() != null) {
                Room r = res.getRoom();
                r.getReservations().remove(res);
                roomRepository.save(r);
            }
            reservationRepository.delete(res);
        });
        return existing;
    }

    public Optional<Reservation> updateReservation(Long id, Reservation updatedReservation) {
        return reservationRepository.findById(id).map(existingReservation -> {
            // user
            User previousUser = existingReservation.getUser();
            User newUser = null;
            Long newUserId = updatedReservation.getUserId();
            if (newUserId != null) newUser = userRepository.findById(newUserId).orElse(null);
            else if (updatedReservation.getUser() != null && updatedReservation.getUser().getId() != null)
                newUser = userRepository.findById(updatedReservation.getUser().getId()).orElse(null);

            if (previousUser != null && (newUser == null || !previousUser.getId().equals(newUser.getId()))) {
                previousUser.getReservations().remove(existingReservation);
                userRepository.save(previousUser);
            }
            if (newUser != null && (previousUser == null || !newUser.getId().equals(previousUser.getId()))) {
                newUser.getReservations().add(existingReservation);
                userRepository.save(newUser);
            }

            // room
            Room previousRoom = existingReservation.getRoom();
            Room newRoom = null;
            Long newRoomId = updatedReservation.getRoomId();
            if (newRoomId != null) newRoom = roomRepository.findById(newRoomId).orElse(null);
            else if (updatedReservation.getRoom() != null && updatedReservation.getRoom().getId() != null)
                newRoom = roomRepository.findById(updatedReservation.getRoom().getId()).orElse(null);

            if (previousRoom != null && (newRoom == null || !previousRoom.getId().equals(newRoom.getId()))) {
                previousRoom.getReservations().remove(existingReservation);
                roomRepository.save(previousRoom);
            }
            if (newRoom != null && (previousRoom == null || !newRoom.getId().equals(previousRoom.getId()))) {
                newRoom.getReservations().add(existingReservation);
                roomRepository.save(newRoom);
            }

            existingReservation.setUser(newUser);
            existingReservation.setRoom(newRoom);
            existingReservation.setStartDate(updatedReservation.getStartDate());
            existingReservation.setEndDate(updatedReservation.getEndDate());
            existingReservation.setReason(updatedReservation.getReason());

            return reservationRepository.save(existingReservation);
        });
    }

    public Optional<Reservation> patchReservation(Long id, Reservation partialReservation) {
        return reservationRepository.findById(id).map(existingReservation -> {
            if (partialReservation.getRoomId() != null || partialReservation.getRoom() != null) {
                Room previousRoom = existingReservation.getRoom();
                Room newRoom = null;
                Long newRoomId = partialReservation.getRoomId();
                if (newRoomId != null) newRoom = roomRepository.findById(newRoomId).orElse(null);
                else if (partialReservation.getRoom() != null && partialReservation.getRoom().getId() != null)
                    newRoom = roomRepository.findById(partialReservation.getRoom().getId()).orElse(null);

                if (previousRoom != null && (newRoom == null || !previousRoom.getId().equals(newRoom.getId()))) {
                    previousRoom.getReservations().remove(existingReservation);
                    roomRepository.save(previousRoom);
                }
                if (newRoom != null && (previousRoom == null || !newRoom.getId().equals(previousRoom.getId()))) {
                    newRoom.getReservations().add(existingReservation);
                    roomRepository.save(newRoom);
                }
                existingReservation.setRoom(newRoom);
            }

            if (partialReservation.getUserId() != null || partialReservation.getUser() != null) {
                User previousUser = existingReservation.getUser();
                User newUser = null;
                Long newUserId = partialReservation.getUserId();
                if (newUserId != null) newUser = userRepository.findById(newUserId).orElse(null);
                else if (partialReservation.getUser() != null && partialReservation.getUser().getId() != null)
                    newUser = userRepository.findById(partialReservation.getUser().getId()).orElse(null);

                if (previousUser != null && (newUser == null || !previousUser.getId().equals(newUser.getId()))) {
                    previousUser.getReservations().remove(existingReservation);
                    userRepository.save(previousUser);
                }
                if (newUser != null && (previousUser == null || !newUser.getId().equals(previousUser.getId()))) {
                    newUser.getReservations().add(existingReservation);
                    userRepository.save(newUser);
                }
                existingReservation.setUser(newUser);
            }

            if (partialReservation.getStartDate() != null) {
                existingReservation.setStartDate(partialReservation.getStartDate());
            }
            if (partialReservation.getEndDate() != null) {
                existingReservation.setEndDate(partialReservation.getEndDate());
            }
            if (partialReservation.getReason() != null) {
                existingReservation.setReason(partialReservation.getReason());
            }
            return reservationRepository.save(existingReservation);
        });
    }
}
