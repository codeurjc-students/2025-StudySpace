package com.urjcservice.backend.repositories;

import com.urjcservice.backend.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    boolean existsByName(String name);
    Optional<Room> findByName(String name);

    @Query("SELECT COUNT(r) FROM Room r")
    long countTotalRooms();

    @Query("SELECT COUNT(r) FROM Room r WHERE SIZE(r.software) > 0")
    long countRoomsWithSoftware();

}


