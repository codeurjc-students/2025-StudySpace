package com.urjcservice.Backend.Service;

import com.urjcservice.Backend.Entities.Reservation;
import com.urjcservice.Backend.Entities.User;
import com.urjcservice.Backend.Entities.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReservationService {

    private final List<Reservation> reservations = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @Autowired
    private UserService userService;

    @Autowired
    private RoomService roomService;

    public List<Reservation> findAll() {
        return new ArrayList<>(reservations); // Devuelve una copia para evitar modificaciones externas
    }

    public Reservation save(Reservation reservation) {
        if (reservation.getId() == null) {
            reservation.setId(idCounter.getAndIncrement()); // Asigna un ID único
        }

        // Si la reserva tiene userId o user, enlazar con el usuario y añadir la reserva a su lista
        Long uid = reservation.getUserId();
        if (uid != null) {
            Optional<User> userOpt = userService.findById(uid);
            userOpt.ifPresent(user -> {
                reservation.setUser(user);
                user.addReservation(reservation);
            });
        } else if (reservation.getUser() != null && reservation.getUser().getId() != null) {
            Optional<User> userOpt = userService.findById(reservation.getUser().getId());
            userOpt.ifPresent(user -> {
                reservation.setUser(user);
                user.addReservation(reservation);
            });
        }

        // Si la reserva tiene roomId o room, enlazar con la room y añadir la reserva a su lista
        Long rid = reservation.getRoomId();
        if (rid != null) {
            Optional<Room> roomOpt = roomService.findById(rid);
            roomOpt.ifPresent(room -> {
                reservation.setRoom(room);
                room.addReservation(reservation);
            });
        } else if (reservation.getRoom() != null && reservation.getRoom().getId() != null) {
            Optional<Room> roomOpt = roomService.findById(reservation.getRoom().getId());
            roomOpt.ifPresent(room -> {
                reservation.setRoom(room);
                room.addReservation(reservation);
            });
        }

        reservations.add(reservation);
        return reservation;
    }

    public Optional<Reservation> findById(Long id) {
        return reservations.stream().filter(reservation -> reservation.getId().equals(id)).findFirst(); // Busca por ID
    }

    public Optional<Reservation> deleteById(Long id) {
        Optional<Reservation> existing = findById(id);
        existing.ifPresent(res -> {
            // quitar de la lista global
            reservations.remove(res);
            // quitar de la lista del usuario si está asociado
            if (res.getUser() != null) {
                res.getUser().removeReservation(res);
            }
            // quitar de la lista de la room si está asociada
            if (res.getRoom() != null) {
                res.getRoom().removeReservation(res);
            }
        });
        return existing; // Elimina por ID y devuelve la entidad eliminada si existía
    }

    public Optional<Reservation> updateReservation(Long id, Reservation updatedReservation) {
        return findById(id).map(existingReservation -> {
            // Manage user association changes
            User previousUser = existingReservation.getUser();

            Long newUserId = updatedReservation.getUserId();
            User newUser = null;
            if (newUserId != null) {
                newUser = userService.findById(newUserId).orElse(null);
            } else if (updatedReservation.getUser() != null && updatedReservation.getUser().getId() != null) {
                newUser = userService.findById(updatedReservation.getUser().getId()).orElse(null);
            }

            if (previousUser != null && (newUser == null || !previousUser.getId().equals(newUser.getId()))) {
                previousUser.removeReservation(existingReservation);
            }
            if (newUser != null && (previousUser == null || !newUser.getId().equals(previousUser.getId()))) {
                newUser.addReservation(existingReservation);
            }

            // Manage room association changes
            Room previousRoom = existingReservation.getRoom();

            Long newRoomId = updatedReservation.getRoomId();
            Room newRoom = null;
            if (newRoomId != null) {
                newRoom = roomService.findById(newRoomId).orElse(null);
            } else if (updatedReservation.getRoom() != null && updatedReservation.getRoom().getId() != null) {
                newRoom = roomService.findById(updatedReservation.getRoom().getId()).orElse(null);
            }

            if (previousRoom != null && (newRoom == null || !previousRoom.getId().equals(newRoom.getId()))) {
                previousRoom.removeReservation(existingReservation);
            }
            if (newRoom != null && (previousRoom == null || !newRoom.getId().equals(previousRoom.getId()))) {
                newRoom.addReservation(existingReservation);
            }

            existingReservation.setUser(newUser);
            existingReservation.setStartDate(updatedReservation.getStartDate());
            existingReservation.setEndDate(updatedReservation.getEndDate());
            existingReservation.setReason(updatedReservation.getReason());
            return existingReservation;
        });
    }

    public Optional<Reservation> patchReservation(Long id, Reservation partialReservation) {
        return findById(id).map(existingReservation -> {
            if (partialReservation.getRoomId() != null || partialReservation.getRoom() != null) {
                Room previousRoom = existingReservation.getRoom();
                Long newRoomId = partialReservation.getRoomId();
                Room newRoom = null;
                if (newRoomId != null) {
                    newRoom = roomService.findById(newRoomId).orElse(null);
                } else if (partialReservation.getRoom() != null && partialReservation.getRoom().getId() != null) {
                    newRoom = roomService.findById(partialReservation.getRoom().getId()).orElse(null);
                }

                if (previousRoom != null && (newRoom == null || !previousRoom.getId().equals(newRoom.getId()))) {
                    previousRoom.removeReservation(existingReservation);
                }
                if (newRoom != null && (previousRoom == null || !newRoom.getId().equals(previousRoom.getId()))) {
                    newRoom.addReservation(existingReservation);
                }
                existingReservation.setRoom(newRoom);
            }

            if (partialReservation.getUserId() != null || partialReservation.getUser() != null) {
                User previousUser = existingReservation.getUser();
                Long newUserId = partialReservation.getUserId();
                User newUser = null;
                if (newUserId != null) {
                    newUser = userService.findById(newUserId).orElse(null);
                } else if (partialReservation.getUser() != null && partialReservation.getUser().getId() != null) {
                    newUser = userService.findById(partialReservation.getUser().getId()).orElse(null);
                }

                if (previousUser != null && (newUser == null || !previousUser.getId().equals(newUser.getId()))) {
                    previousUser.removeReservation(existingReservation);
                }
                if (newUser != null && (previousUser == null || !newUser.getId().equals(previousUser.getId()))) {
                    newUser.addReservation(existingReservation);
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
            return existingReservation;
        });
    }
}
