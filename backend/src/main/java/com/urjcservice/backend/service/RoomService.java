package com.urjcservice.backend.service;

import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.Software;
import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.repositories.ReservationRepository;
import com.urjcservice.backend.repositories.RoomRepository;
import com.urjcservice.backend.repositories.SoftwareRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;

@Service
public class RoomService {

    
    private final RoomRepository roomRepository;
    private final SoftwareRepository softwareRepository;
    private final ReservationRepository reservationRepository;
    
    public RoomService(RoomRepository roomRepository,
                       SoftwareRepository softwareRepository,
                        ReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.softwareRepository = softwareRepository;
        this.reservationRepository = reservationRepository;
    }

    public Page<Room> findAll(Pageable pageable) {
        return roomRepository.findAll(pageable);
    }

    public Room save(Room room) {
        // resolve or create softwares
        if (roomRepository.existsByName(room.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The room name already exists.");
        }
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
            if (!existingRoom.getName().equals(updatedRoom.getName()) && 
                roomRepository.existsByName(updatedRoom.getName())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "The room name already exists.");
            }
            //true if was active and now is disable
            boolean isBeingDisabled = existingRoom.isActive() && !updatedRoom.isActive();

            updateRoomBasicInfo(existingRoom, updatedRoom);
            updateRoomSoftware(existingRoom, updatedRoom.getSoftware());
            
            //if is disable future reservations are deleted
            if (isBeingDisabled) {
                //from now to the future all reservations deleted
                reservationRepository.cancelByRoomIdAndEndDateAfter(id, new Date());    //Now cancel not delete
            }   //deleteByRoom_IdAndEndDateAfter and also fix the problem


            return roomRepository.save(existingRoom);
        });
    }

    //auxiliar methods 
    private void updateRoomBasicInfo(Room existing, Room updated) {
        existing.setName(updated.getName());
        existing.setCapacity(updated.getCapacity());
        existing.setCamp(updated.getCamp());
        existing.setPlace(updated.getPlace());
        existing.setCoordenades(updated.getCoordenades());
        existing.setActive(updated.isActive());
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
        if (partial.getCamp() != null) existing.setCamp(partial.getCamp());
        if (partial.getPlace() != null) existing.setPlace(partial.getPlace());
        if (partial.getCoordenades() != null) existing.setCoordenades(partial.getCoordenades());
    }

    private void patchRoomSoftware(Room existing, List<Software> newList) {
        //if we dont update the list is null
        if (newList != null) {
            removeOldSoftware(existing, newList);
            addNewSoftware(existing, newList);
        }
    }




    public Map<String, Object> getRoomDailyStats(Long roomId, LocalDate date) {
        List<Reservation> reservations = reservationRepository.findByRoomIdAndDate(roomId, date,Pageable.unpaged()).getContent();//check itttttttttttt
        
        // Map shorted by hours (8:00 to 21:00) -> true/false (occupied/free)
        Map<Integer, Boolean> hourlyStatus = new TreeMap<>();
        int startHour = 8;
        int endHour = 21; 
        // False=free
        for (int i = startHour; i <= endHour; i++) {
            hourlyStatus.put(i, false);
        }

        ZoneId zone = ZoneId.of("Europe/Madrid");

        for (Reservation r : reservations) {
            int hStart = r.getStartDate().toInstant().atZone(zone).getHour();
            int hEnd = r.getEndDate().toInstant().atZone(zone).getHour();
            
            //fill the hours of the reservations
            for (int h = hStart; h < hEnd; h++) {
                if (h >= startHour && h <= endHour) {
                    hourlyStatus.put(h, true);
                }
            }
        }

        //% porcentages
        long occupiedCount = hourlyStatus.values().stream().filter(v -> v).count();
        long totalHours = hourlyStatus.size();
        
        double occupiedPct = ((double) occupiedCount / totalHours) * 100;
        double freePct = 100 - occupiedPct;

        Map<String, Object> result = new HashMap<>();
        result.put("hourlyStatus", hourlyStatus);
        result.put("occupiedPercentage", Math.round(occupiedPct * 100.0) / 100.0);
        result.put("freePercentage", Math.round(freePct * 100.0) / 100.0);
        
        return result;
    }


}