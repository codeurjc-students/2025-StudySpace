package com.urjcservice.backend.service;

import com.urjcservice.backend.service.EmailService;



import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.repositories.ReservationRepository;
import com.urjcservice.backend.repositories.UserRepository;
import com.urjcservice.backend.repositories.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import com.urjcservice.backend.rest.ReservationRestController.ReservationRequest;

@Service
public class ReservationService {

    private static final String USER_NOT_FOUND_MSG = "User not found";
    
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final EmailService emailService;
    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    
    public ReservationService(ReservationRepository reservationRepository,
                              UserRepository userRepository,
                              RoomRepository roomRepository,
                            EmailService emailService) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.emailService = emailService;
    }

    public Page<Reservation> findAll(Pageable pageable) {
        return reservationRepository.findAll(pageable);
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

    
    public Optional<Reservation> cancelById(Long id) { 
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
            Date newStart = updatedReservation.getStartDate() != null ? updatedReservation.getStartDate() : existingReservation.getStartDate();
            Date newEnd = updatedReservation.getEndDate() != null ? updatedReservation.getEndDate() : existingReservation.getEndDate();
            LocalDateTime startLDT = newStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime endLDT = newEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            long newDuration = java.time.Duration.between(startLDT, endLDT).toMinutes();

            if (newDuration > 180) {
                throw new IllegalArgumentException("A single reservation cannot exceed 3 hours.");
            }
            validateUserDailyQuota(existingReservation.getUser(), startLDT.toLocalDate(), newDuration, id);
            
            validateReservationRules(newStart, newEnd);

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


    public Optional<Reservation> adminUpdateReservation(Long id, Long newRoomId, LocalDate newDate, 
                                                        LocalTime newStart, LocalTime newEnd, String adminReason) {
        
        log.info("--- ADMIN UPDATE: Iniciando modificación para Reserva ID: {} ---", id);

        return reservationRepository.findById(id).map(reservation -> {
            Room newRoom = roomRepository.findById(newRoomId)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + newRoomId));

            LocalDateTime startDateTime = LocalDateTime.of(newDate, newStart);
            LocalDateTime endDateTime = LocalDateTime.of(newDate, newEnd);
            
            Date finalStartDate = java.sql.Timestamp.valueOf(startDateTime);
            Date finalEndDate = java.sql.Timestamp.valueOf(endDateTime);

            reservation.setRoom(newRoom);
            reservation.setStartDate(finalStartDate);
            reservation.setEndDate(finalEndDate);
            reservation.setAdminModificationReason(adminReason); 

            Reservation savedReservation = reservationRepository.saveAndFlush(reservation);
            log.info("Reservation successfully saved in database.");

            //send email
            User user = savedReservation.getUser();
            if (user != null) {
                String email = user.getEmail();
                String userName = user.getName();
                String roomName = newRoom.getName(); 

                log.info("Preparing email for: {}", email);

                try {
                    emailService.sendReservationModificationEmail(
                        email, 
                        userName, 
                        roomName, 
                        newDate.toString(), 
                        newStart.toString(), 
                        newEnd.toString(),
                        adminReason
                    );
                    log.info("Email sent successfully.");
                } catch (Exception e) {
                    log.error("Email failed to send (but reservation was saved): ", e);
                }
            } else {
                log.warn("The reservation has no associated user. No email is sent.");
            }

            return savedReservation;
        });
    }

    public Optional<Reservation> adminCancelReservation(Long id, String reason) {
        
        log.info("--- ADMIN CANCEL: Initiating cancellation for Reservation ID: {} ---", id);

        return reservationRepository.findById(id).map(reservation -> {
            reservation.setCancelled(true);
            reservation.setAdminModificationReason(reason); 
            
            Reservation savedReservation = reservationRepository.saveAndFlush(reservation);
            log.info("Reservation ID {} ​​cancelled in BD.", id);

            //send email
            User user = savedReservation.getUser();
            if (user != null) {
                String email = user.getEmail();
                String userName = user.getName();
                String roomName = (savedReservation.getRoom() != null) ? savedReservation.getRoom().getName() : "Sin Sala";
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

                String dateStr = dateFormat.format(savedReservation.getStartDate());
                String startStr = timeFormat.format(savedReservation.getStartDate());
                String endStr = timeFormat.format(savedReservation.getEndDate());

                log.info("Preparing cancellation email for: {}", email);

                try {
                    emailService.sendReservationCancellationEmail(
                        email, 
                        userName, 
                        roomName, 
                        dateStr, 
                        startStr, 
                        endStr,
                        reason
                    );
                    log.info("Cancellation email sent.");
                } catch (Exception e) {
                    log.error("The cancellation email failed to send: ", e);
                }
            }

            return savedReservation;
        });
    }

   
    public Optional<Reservation> patchReservation(Long id, Reservation partialReservation) {
        return updateReservation(id, partialReservation);
    }


    public Reservation createReservation(ReservationRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND_MSG));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.isActive()) {
        throw new RuntimeException("Reservations are not possible: The classroom is temporarily unavailable.");
        }




        List<Reservation> userConflicts = reservationRepository.findUserOverlappingReservations(
                user.getId(), 
                request.getStartDate(), 
                request.getEndDate()
        );

        if (!userConflicts.isEmpty()) {
            throw new IllegalArgumentException("You already have an active reservation for this time slot. You cannot be in two classrooms at the same time.");
        }
        


        validateReservationRules(request.getStartDate(), request.getEndDate()); 

        
        LocalDateTime start = request.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime end = request.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        long newDuration = java.time.Duration.between(start, end).toMinutes();

        if (newDuration > 180) {
            throw new IllegalArgumentException("A single reservation cannot exceed 3 hours.");
        }


        Page<Reservation> overlaps = reservationRepository.findOverlappingReservations(
            request.getRoomId(), request.getStartDate(), request.getEndDate(),PageRequest.of(0, 1));

        if (overlaps.hasContent()) {
            throw new RuntimeException("The room is already reserved for this time.");
        }





        
        //all day hours reserved
        validateUserDailyQuota(user, start.toLocalDate(), newDuration, null);



        


        Reservation reservation = new Reservation();
        reservation.setStartDate(request.getStartDate());
        reservation.setEndDate(request.getEndDate());
        reservation.setReason(request.getReason());
        reservation.setUser(user);
        reservation.setRoom(room);
        reservation.setCancelled(false);

        reservation.setVerified(false); 
        reservation.setVerificationToken(java.util.UUID.randomUUID().toString()); 

        Date now = new Date();
        Date expiration = new Date(now.getTime() + 3600000); 
        reservation.setTokenExpirationDate(expiration);

        //first save reservation
        Reservation savedReservation = reservationRepository.save(reservation);

        // try confirmation email
        try {
            emailService.sendVerificationEmail(
                user.getEmail(),
                user.getName(),
                savedReservation.getVerificationToken() 
            );
        } catch (Exception e) {
            System.err.println("Error sending verification email: " + e.getMessage());
        }

        return savedReservation;
    }


    public void verifyReservation(String token) {
        Reservation reservation = reservationRepository.findByVerificationToken(token) 
                .orElseThrow(() -> new RuntimeException("Invalid verification token."));

        //is verified?
        if (reservation.isVerified()) {
            throw new RuntimeException("Reservation is already verified.");
        }

        //token expired?
        if (reservation.getTokenExpirationDate() != null && 
            reservation.getTokenExpirationDate().before(new Date())) {
            
            //if expired delete the reservation
            reservationRepository.delete(reservation);
            
            throw new RuntimeException("Verification link has expired. The reservation has been cancelled. Please book again.");
        }

        reservation.setVerified(true);
        reservation.setVerificationToken(null); //delete token
        reservation.setTokenExpirationDate(null); //delete token date
        
        Reservation savedReservation = reservationRepository.save(reservation);

        //send email with calendar event
        try {
            User user = savedReservation.getUser();
            Room room = savedReservation.getRoom();
            
            emailService.sendReservationConfirmationEmail(
                user.getEmail(),
                user.getName(),
                room.getName(),
                room.getPlace(),        
                room.getCoordenades(),  
                savedReservation.getStartDate(), 
                savedReservation.getEndDate()    
            );
        } catch (Exception e) {
            System.err.println("Error sending confirmation email: " + e.getMessage());
        }
    }


    @Scheduled(fixedRate = 60000) //every minute we search for new reservations that are not verified and have expired token
    @Transactional
    public void deleteExpiredUnverifiedReservations() {
        Date now = new Date();
        
        reservationRepository.deleteExpiredReservations(now);
    }


    public Page<Reservation> getReservationsByUserEmail(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND_MSG));
        
        return reservationRepository.findByUser(user, pageable);
    }

    public Page<Reservation> getReservationsByUserId(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND_MSG));
        
        return reservationRepository.findByUser(user, pageable);
    }


    public List<Reservation> getActiveReservationsForRoomAndDate(Long roomId, LocalDate date) {
        return reservationRepository.findActiveReservationsByRoomAndDate(roomId, date);
    }

    private void validateReservationRules(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }

        LocalDateTime start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        if (start.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot create reservations in the past.");
        }

        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End date must be after start date.");
        }
        
        DayOfWeek day = start.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
             throw new IllegalArgumentException("Reservations are not allowed on weekends.");
        }

        //validate (08:00 - 21:00)
        LocalTime openTime = LocalTime.of(8, 0);
        LocalTime closeTime = LocalTime.of(21, 0);

        if (start.toLocalTime().isBefore(openTime) || 
            end.toLocalTime().isAfter(closeTime) || 
            (end.toLocalTime().equals(LocalTime.MIDNIGHT))) { 
             throw new IllegalArgumentException("Reservations must be between 08:00 and 21:00.");
        }

        if (start.getMinute() % 30 != 0 || end.getMinute() % 30 != 0) {
            throw new IllegalArgumentException("Reservations must start and end in 30-minute intervals (e.g., 10:00, 10:30).");
        }
        
        long minutes = java.time.Duration.between(start, end).toMinutes();
        if (minutes < 30) {
             throw new IllegalArgumentException("Minimum reservation duration is 30 minutes.");
        }
    }



    private void validateUserDailyQuota(User user, LocalDate date, long newDurationMinutes, Long excludeReservationId) {
        List<Reservation> existingReservations = reservationRepository.findActiveByUserIdAndDate(user.getId(), date);

        long usedMinutes = existingReservations.stream()
                .filter(r -> !r.getId().equals(excludeReservationId)) 
                .mapToLong(r -> {
                    LocalDateTime start = r.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    LocalDateTime end = r.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    return java.time.Duration.between(start, end).toMinutes();
                })
                .sum();

        //limit (3 hours = 180 minutes)
        if (usedMinutes + newDurationMinutes > 180) {
            throw new IllegalArgumentException("Daily limit exceeded. You can only book up to 3 hours per day. Already used: " + usedMinutes + "minutes.");
        }
    }



}