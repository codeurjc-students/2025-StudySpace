package com.urjcservice.backend.repositories;

import com.urjcservice.backend.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}


