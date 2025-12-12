package com.urjcservice.backend.repositories;

import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Date;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUser(User user);

    @Query("SELECT COUNT(DISTINCT r.room.id) FROM Reservation r WHERE DATE(r.startDate) = CURRENT_DATE")
    long countOccupiedRoomsToday();

    
    @Query("SELECT r.startDate FROM Reservation r WHERE DATE(r.startDate) = CURRENT_DATE")
    List<Date> findStartDatesToday();
}
