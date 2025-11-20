package com.urjcservice.backend.projections;

import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.Software;
import org.springframework.data.rest.core.config.Projection;

import java.util.List;

// Esto le dice a Spring: "Cuando te pida esta proyección, dame el Aula con su Software inline"
@Projection(name = "withSoftware", types = { Room.class })
public interface RoomWithSoftware {
    Long getId();
    String getName();
    Integer getCapacity();
    Room.CampusType getCamp();
    String getPlace();
    String getCoordenades();
    
    // ¡Aquí está la magia! Esto incluirá la lista completa en el JSON
    List<Software> getSoftware(); 
}