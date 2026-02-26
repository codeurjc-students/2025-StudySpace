package com.urjcservice.backend.projections;

import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.entities.Room;
import org.springframework.data.rest.core.config.Projection;

import java.util.Date;

//reservation and room data at same time
@Projection(name = "withRoom", types = { Reservation.class })
public interface ReservationWithRoom {
    Long getId();

    Date getStartDate();

    Date getEndDate();

    String getReason();

    Room getRoom();
}