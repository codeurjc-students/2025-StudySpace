package com.urjcservice.backend.repositories;

import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDate;
import java.util.Date;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUser(User user);
    
    @Query("SELECT COUNT(DISTINCT r.room.id) FROM Reservation r WHERE DATE(r.startDate) = :date")
    long countOccupiedRoomsByDate(@Param("date") LocalDate date);

    
    @Query("SELECT COUNT(DISTINCT r.room.id) FROM Reservation r WHERE DATE(r.startDate) = :date AND SIZE(r.room.software) > 0")
    long countOccupiedWithSoftwareByDate(@Param("date") LocalDate date);

    @Query("SELECT r.startDate FROM Reservation r WHERE DATE(r.startDate) = :date")
    List<Date> findStartDatesByDate(@Param("date") LocalDate date);
}
