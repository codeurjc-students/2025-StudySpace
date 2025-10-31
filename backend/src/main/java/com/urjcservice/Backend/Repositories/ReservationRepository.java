package com.urjcservice.Backend.Repositories;

import com.urjcservice.Backend.Entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
