package com.urjcservice.backend.repositories;

import com.urjcservice.backend.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RoomRepository extends JpaRepository<Room, Long> {

    
    @Query("SELECT COUNT(r) FROM Room r")
    long countTotalRooms();

    @Query("SELECT COUNT(r) FROM Room r WHERE SIZE(r.software) > 0")
    long countRoomsWithSoftware();

}


