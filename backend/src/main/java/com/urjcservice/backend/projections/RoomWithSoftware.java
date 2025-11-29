package com.urjcservice.backend.projections;

import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.Software;
import org.springframework.data.rest.core.config.Projection;

import java.util.List;


@Projection(name = "withSoftware", types = { Room.class })
public interface RoomWithSoftware {
    Long getId();
    String getName();
    Integer getCapacity();
    Room.CampusType getCamp();
    String getPlace();
    String getCoordenades();
    
    
    List<Software> getSoftware(); 
}