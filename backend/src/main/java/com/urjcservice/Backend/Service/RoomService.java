package com.urjcservice.Backend.Service;

import com.urjcservice.Backend.Entities.Room;
import com.urjcservice.Backend.Entities.Software;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RoomService {

    private final List<Room> rooms = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @Autowired
    private SoftwareService softwareService;

    public List<Room> findAll() {
        return new ArrayList<>(rooms); // Devuelve una copia para evitar modificaciones externas
    }

    public Room save(Room room) {
        if (room.getId() == null) {
            room.setId(idCounter.getAndIncrement()); // Asigna un ID único
        }

        // link softwares if provided
        if (room.getSoftware() != null) {
            List<Software> toLink = new ArrayList<>(room.getSoftware());
            room.setSoftware(new ArrayList<>());
            for (Software s : toLink) {
                if (s == null) continue;
                if (s.getId() != null) {
                    softwareService.findById(s.getId()).ifPresent(found -> room.addSoftware(found));
                } else {
                    // create new software if no id
                    Software created = softwareService.save(s);
                    room.addSoftware(created);
                }
            }
        }

        rooms.add(room);
        return room;
    }

    public Optional<Room> findById(Long id) {
        return rooms.stream().filter(room -> room.getId().equals(id)).findFirst(); // Busca la Room por ID
    }

    public Optional<Room> deleteById(Long id) {
        Optional<Room> existing = findById(id);
        existing.ifPresent(r -> {
            // remove associations from softwares
            if (r.getSoftware() != null) {
                List<Software> copy = new ArrayList<>(r.getSoftware());
                for (Software s : copy) {
                    r.removeSoftware(s);
                }
            }
            rooms.remove(r);
        });
        return existing; // Elimina la Room por ID y devuelve la entidad eliminada si existía
    }

    public Optional<Room> updateRoom(Long id, Room updatedRoom) {
        return findById(id).map(existingRoom -> {
            existingRoom.setName(updatedRoom.getName());
            existingRoom.setCapacity(updatedRoom.getCapacity());

            // synchronize software associations: remove those not present and add new ones
            List<Software> newList = updatedRoom.getSoftware() != null ? updatedRoom.getSoftware() : new ArrayList<>();

            // remove software not in newList
            List<Software> toRemove = new ArrayList<>();
            for (Software s : existingRoom.getSoftware()) {
                boolean still = newList.stream().anyMatch(n -> n.getId() != null && n.getId().equals(s.getId()));
                if (!still) toRemove.add(s);
            }
            for (Software s : toRemove) existingRoom.removeSoftware(s);

            // add new ones
            for (Software s : newList) {
                if (s == null) continue;
                if (s.getId() != null) {
                    softwareService.findById(s.getId()).ifPresent(found -> {
                        if (!existingRoom.getSoftware().contains(found)) existingRoom.addSoftware(found);
                    });
                } else {
                    Software created = softwareService.save(s);
                    existingRoom.addSoftware(created);
                }
            }

            return existingRoom;
        });
    }

    public Optional<Room> patchRoom(Long id, Room partialRoom) {
        return findById(id).map(existingRoom -> {
            if (partialRoom.getName() != null) {
                existingRoom.setName(partialRoom.getName());
            }
            if (partialRoom.getCapacity() != null) {
                existingRoom.setCapacity(partialRoom.getCapacity());
            }

            if (partialRoom.getSoftware() != null) {
                // treat provided list as the desired list: sync
                List<Software> newList = partialRoom.getSoftware();

                List<Software> toRemove = new ArrayList<>();
                for (Software s : existingRoom.getSoftware()) {
                    boolean still = newList.stream().anyMatch(n -> n.getId() != null && n.getId().equals(s.getId()));
                    if (!still) toRemove.add(s);
                }
                for (Software s : toRemove) existingRoom.removeSoftware(s);

                for (Software s : newList) {
                    if (s == null) continue;
                    if (s.getId() != null) {
                        softwareService.findById(s.getId()).ifPresent(found -> {
                            if (!existingRoom.getSoftware().contains(found)) existingRoom.addSoftware(found);
                        });
                    } else {
                        Software created = softwareService.save(s);
                        existingRoom.addSoftware(created);
                    }
                }
            }

            return existingRoom;
        });
    }
}