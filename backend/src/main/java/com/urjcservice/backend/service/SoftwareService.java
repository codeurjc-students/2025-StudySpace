package com.urjcservice.backend.service;

import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.Software;
import com.urjcservice.backend.repositories.RoomRepository;
import com.urjcservice.backend.repositories.SoftwareRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SoftwareService {

    
    private final SoftwareRepository softwareRepository;
    private final RoomRepository roomRepository;
    
    public SoftwareService(SoftwareRepository softwareRepository,
                           RoomRepository roomRepository) {
        this.softwareRepository = softwareRepository;
        this.roomRepository = roomRepository;
    }

    public Page<Software> findAll(Pageable pageable) {
        return softwareRepository.findAll(pageable);
    }

    public Software save(Software software) {
        return softwareRepository.save(software);
    }

    public Optional<Software> findById(Long id) {
        return softwareRepository.findById(id);
    }

    public Optional<Software> deleteById(Long id) {
        Optional<Software> existing = softwareRepository.findById(id);
        existing.ifPresent(s -> {
            // remove associations from rooms
            if (s.getRooms() != null) {
                List<Room> copy = new ArrayList<>(s.getRooms());
                for (Room r : copy) {
                    r.removeSoftware(s);
                    roomRepository.save(r);
                }
            }
            softwareRepository.delete(s);
        });
        return existing;
    }

    public Optional<Software> updateSoftware(Long id, Software updatedSoftware) {
        return softwareRepository.findById(id).map(existingSoftware -> {
            existingSoftware.setName(updatedSoftware.getName());
            existingSoftware.setVersion(updatedSoftware.getVersion());
            existingSoftware.setDescription(updatedSoftware.getDescription());
            return softwareRepository.save(existingSoftware);
        });
    }

    public Optional<Software> patchSoftware(Long id, Software partialSoftware) {
        return softwareRepository.findById(id).map(existingSoftware -> {
            if (partialSoftware.getName() != null) {
                existingSoftware.setName(partialSoftware.getName());
            }
            if (partialSoftware.getVersion() != null) {
                existingSoftware.setVersion(partialSoftware.getVersion());
            }
            if (partialSoftware.getDescription() != null) {
                existingSoftware.setDescription(partialSoftware.getDescription());
            }
            return softwareRepository.save(existingSoftware);
        });
    }

    
}
