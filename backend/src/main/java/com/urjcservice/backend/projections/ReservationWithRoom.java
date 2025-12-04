package com.urjcservice.backend.projections;

import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.entities.Room;
import org.springframework.data.rest.core.config.Projection;

import java.util.Date;

// Esta proyección sirve para ver la reserva Y los datos del aula a la vez
@Projection(name = "withRoom", types = { Reservation.class })
public interface ReservationWithRoom {
    Long getId();
    Date getStartDate();
    Date getEndDate();
    String getReason();
    
    // Esto obliga a Spring a incrustar los datos del aula en lugar de un link
    Room getRoom(); 
}