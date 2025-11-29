package com.urjcservice.backend.service;

import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.Software;
import com.urjcservice.backend.repositories.RoomRepository;
import com.urjcservice.backend.repositories.SoftwareRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private SoftwareRepository softwareRepository;

    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    public Room save(Room room) {
        // resolve or create softwares
        List<Software> linked = new ArrayList<>();
        if (room.getSoftware() != null) {
            for (Software s : room.getSoftware()) {
                if (s == null) continue;
                if (s.getId() != null) {
                    softwareRepository.findById(s.getId()).ifPresent(linked::add);
                } else {
                    linked.add(softwareRepository.save(s));
                }
            }
        }
        room.setSoftware(linked);
        return roomRepository.save(room);
    }

    public Optional<Room> findById(Long id) {
        return roomRepository.findById(id);
    }

    public Optional<Room> deleteById(Long id) {
        Optional<Room> existing = roomRepository.findById(id);
        existing.ifPresent(r -> {
            // remove associations from softwares
            if (r.getSoftware() != null) {
                List<Software> copy = new ArrayList<>(r.getSoftware());
                for (Software s : copy) {
                    r.removeSoftware(s);
                    softwareRepository.save(s);
                }
            }
            roomRepository.delete(r);
        });
        return existing;
    }

    public Optional<Room> updateRoom(Long id, Room updatedRoom) {
        return roomRepository.findById(id).map(existingRoom -> {
            existingRoom.setName(updatedRoom.getName());
            existingRoom.setCapacity(updatedRoom.getCapacity());

            List<Software> newList = updatedRoom.getSoftware() != null ? updatedRoom.getSoftware() : new ArrayList<>();

            // remove software not in newList
            List<Software> toRemove = new ArrayList<>();
            for (Software s : existingRoom.getSoftware()) {
                boolean still = newList.stream().anyMatch(n -> n.getId() != null && n.getId().equals(s.getId()));
                if (!still) toRemove.add(s);
            }
            for (Software s : toRemove) {
                existingRoom.removeSoftware(s);
                softwareRepository.save(s);
            }

            // add new ones
            for (Software s : newList) {
                if (s == null) continue;
                if (s.getId() != null) {
                    softwareRepository.findById(s.getId()).ifPresent(found -> {
                        if (!existingRoom.getSoftware().contains(found)) existingRoom.addSoftware(found);
                    });
                } else {
                    Software created = softwareRepository.save(s);
                    existingRoom.addSoftware(created);
                }
            }

            return roomRepository.save(existingRoom);
        });
    }

    public Optional<Room> patchRoom(Long id, Room partialRoom) {
        return roomRepository.findById(id).map(existingRoom -> {
            if (partialRoom.getName() != null) {
                existingRoom.setName(partialRoom.getName());
            }
            if (partialRoom.getCapacity() != null) {
                existingRoom.setCapacity(partialRoom.getCapacity());
            }

            if (partialRoom.getSoftware() != null) {
                List<Software> newList = partialRoom.getSoftware();

                List<Software> toRemove = new ArrayList<>();
                for (Software s : existingRoom.getSoftware()) {
                    boolean still = newList.stream().anyMatch(n -> n.getId() != null && n.getId().equals(s.getId()));
                    if (!still) toRemove.add(s);
                }
                for (Software s : toRemove) {
                    existingRoom.removeSoftware(s);
                    softwareRepository.save(s);
                }

                for (Software s : newList) {
                    if (s == null) continue;
                    if (s.getId() != null) {
                        softwareRepository.findById(s.getId()).ifPresent(found -> {
                            if (!existingRoom.getSoftware().contains(found)) existingRoom.addSoftware(found);
                        });
                    } else {
                        Software created = softwareRepository.save(s);
                        existingRoom.addSoftware(created);
                    }
                }
            }

            return roomRepository.save(existingRoom);
        });
    }
}