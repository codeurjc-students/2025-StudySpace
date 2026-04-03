package com.urjcservice.backend.service;

import com.urjcservice.backend.entities.Campus;
import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.repositories.CampusRepository;
import com.urjcservice.backend.repositories.ReservationRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CampusService {

    private static final Logger logger = LoggerFactory.getLogger(CampusService.class);

    private final CampusRepository campusRepository;
    private final ReservationRepository reservationRepository;
    private final EmailService emailService;

    public CampusService(CampusRepository campusRepository, ReservationRepository reservationRepository,
            EmailService emailService) {
        this.campusRepository = campusRepository;
        this.reservationRepository = reservationRepository;
        this.emailService = emailService;
    }

    public List<Campus> findAll() {
        return campusRepository.findAll();
    }

    public Optional<Campus> findById(Long id) {
        return campusRepository.findById(id);
    }

    public Campus save(Campus campus) {
        if (campusRepository.findByName(campus.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A campus with this name already exists.");
        }
        return campusRepository.save(campus);
    }

    public Optional<Campus> update(Long id, Campus updatedCampus) {
        return campusRepository.findById(id).map(existing -> {
            Optional<Campus> duplicate = campusRepository.findByName(updatedCampus.getName());
            if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A campus with this name already exists.");
            }
            existing.setName(updatedCampus.getName());
            existing.setCoordinates(updatedCampus.getCoordinates());
            return campusRepository.save(existing);
        });
    }

    public boolean delete(Long id) {
        Optional<Campus> campusOp = campusRepository.findById(id);

        if (campusOp.isPresent()) {
            Campus campus = campusOp.get();

            String reason = "The campus '" + campus.getName()
                    + "' is no longer part of our entity and all its rooms have been permanently closed. "
                    + "Therefore, your reservation has been cancelled. We apologize for any inconvenience.";

            for (Room room : campus.getRooms()) {
                List<Reservation> affectedReservations = reservationRepository
                        .findActiveReservationsByRoomIdAndEndDateAfter(room.getId(), new Date());

                // sen email to all affected users with the same reason
                for (Reservation res : affectedReservations) {
                    notifyUserCancellation(res, reason);
                }

                reservationRepository.deleteByRoomIdAndEndDateAfter(room.getId(), new Date());
            }

            campusRepository.delete(campus);
            return true;
        }
        return false;
    }

    private void notifyUserCancellation(Reservation res, String reason) {
        if (res.getUser() != null) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

                String dateStr = dateFormat.format(res.getStartDate());
                String startStr = timeFormat.format(res.getStartDate());
                String endStr = timeFormat.format(res.getEndDate());
                String roomName = (res.getRoom() != null) ? res.getRoom().getName() : "Unknown room";

                emailService.sendReservationCancellationEmail(
                        res.getUser().getEmail(),
                        res.getUser().getName(),
                        roomName,
                        dateStr,
                        startStr,
                        endStr,
                        reason);
            } catch (Exception e) {
                logger.error("Error sending email to user {}", res.getUser().getEmail(), e);
            }
        }
    }
}