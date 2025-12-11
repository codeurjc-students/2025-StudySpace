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

    
    private final RoomRepository roomRepository;
    private final SoftwareRepository softwareRepository;
    
    public RoomService(RoomRepository roomRepository,
                       SoftwareRepository softwareRepository) {
        this.roomRepository = roomRepository;
        this.softwareRepository = softwareRepository;
    }

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
            updateRoomBasicInfo(existingRoom, updatedRoom);
            updateRoomSoftware(existingRoom, updatedRoom.getSoftware());
            
            return roomRepository.save(existingRoom);
        });
    }

    //auxiliar methods 
    private void updateRoomBasicInfo(Room existing, Room updated) {
        existing.setName(updated.getName());
        existing.setCapacity(updated.getCapacity());
    }

    private void updateRoomSoftware(Room existing, List<Software> newSoftwareList) {
        List<Software> newList = newSoftwareList != null ? newSoftwareList : new ArrayList<>();
        
        removeOldSoftware(existing, newList);
        addNewSoftware(existing, newList);
    }

    private void removeOldSoftware(Room existing, List<Software> newList) {
        List<Software> toRemove = new ArrayList<>();
        for (Software s : existing.getSoftware()) {
            boolean stillExists = newList.stream()
                .anyMatch(n -> n.getId() != null && n.getId().equals(s.getId()));
            if (!stillExists) {
                toRemove.add(s);
            }
        }
        for (Software s : toRemove) {
            existing.removeSoftware(s);
            softwareRepository.save(s);
        }
    }

    private void addNewSoftware(Room existing, List<Software> newList) {
        for (Software s : newList) {
            if (s == null) continue;
            
            if (s.getId() != null) {
                softwareRepository.findById(s.getId()).ifPresent(found -> {
                    if (!existing.getSoftware().contains(found)) {
                        existing.addSoftware(found);
                    }
                });
            } else {
                Software created = softwareRepository.save(s);
                existing.addSoftware(created);
            }
        }
    }


    
    public Optional<Room> patchRoom(Long id, Room partialRoom) {
        return roomRepository.findById(id).map(existingRoom -> {
            patchRoomBasicInfo(existingRoom, partialRoom);
            patchRoomSoftware(existingRoom, partialRoom.getSoftware());
            
            return roomRepository.save(existingRoom);
        });
    }

    private void patchRoomBasicInfo(Room existing, Room partial) {
        if (partial.getName() != null) {
            existing.setName(partial.getName());
        }
        if (partial.getCapacity() != null) {
            existing.setCapacity(partial.getCapacity());
        }
    }

    private void patchRoomSoftware(Room existing, List<Software> newList) {
        //if we dont update the list is null
        if (newList != null) {
            removeOldSoftware(existing, newList);
            addNewSoftware(existing, newList);
        }
    }
}