package com.urjcservice.Backend.Service;

import com.urjcservice.Backend.Entities.Room;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RoomService {

    private final List<Room> rooms = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public List<Room> findAll() {
        return new ArrayList<>(rooms); // Devuelve una copia para evitar modificaciones externas
    }

    public Room save(Room room) {
        if (room.getId() == null) {
            room.setId(idCounter.getAndIncrement()); // Asigna un ID único
        }
        rooms.add(room);
        return room;
    }

    public Optional<Room> findById(Long id) {
        return rooms.stream().filter(room -> room.getId().equals(id)).findFirst(); // Busca la Room por ID
    }

    public Optional<Room> deleteById(Long id) {
        Optional<Room> existing = findById(id);
        existing.ifPresent(rooms::remove);
        return existing; // Elimina la Room por ID y devuelve la entidad eliminada si existía
    }

    public Optional<Room> updateRoom(Long id, Room updatedRoom) {
        return findById(id).map(existingRoom -> {
            existingRoom.setName(updatedRoom.getName());
            existingRoom.setCapacity(updatedRoom.getCapacity());
            return existingRoom;
        });
    }

    public Optional<Room> patchRoom(Long id, Room partialRoom) {
        return findById(id).map(existingRoom -> {
            if (partialRoom.getName() != null) {
                existingRoom.setName(partialRoom.getName());
            }
            if (partialRoom.getCapacity() != 0) {
                existingRoom.setCapacity(partialRoom.getCapacity());
            }
            return existingRoom;
        });
    }
}