package com.urjcservice.backend.rest; 

import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.Software;
import com.urjcservice.backend.repositories.RoomRepository;
import com.urjcservice.backend.repositories.SoftwareRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rooms") 
public class RoomRestController {

    
    private final  RoomRepository roomRepository;
    private final SoftwareRepository softwareRepository;
    public RoomRestController(RoomRepository roomRepository,
                              SoftwareRepository softwareRepository) {
        this.roomRepository = roomRepository;
        this.softwareRepository = softwareRepository;
    }

    //internal DTO for room requests on frontend
    public static class RoomRequest {
        public String name;
        public Integer capacity;
        public Room.CampusType camp;
        public String place;
        public String coordenades;
        public List<Long> softwareIds;
    }

    @GetMapping
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        return roomRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody RoomRequest request) {
        Room room = new Room();
        return saveRoomData(room, request, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable Long id, @RequestBody RoomRequest request) {
        Optional<Room> roomOpt = roomRepository.findById(id);
        if (roomOpt.isPresent()) {
            return saveRoomData(roomOpt.get(), request, HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        if (roomRepository.existsById(id)) {
            roomRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    
    private ResponseEntity<Room> saveRoomData(Room room, RoomRequest request, HttpStatus status) {
        if (request.name != null) room.setName(request.name);
        if (request.capacity != null) room.setCapacity(request.capacity);
        if (request.camp != null) room.setCamp(request.camp);
        if (request.place != null) room.setPlace(request.place);
        if (request.coordenades != null) room.setCoordenades(request.coordenades);

       
        if (request.softwareIds != null) {
            List<Software> softwares = softwareRepository.findAllById(request.softwareIds);
            room.setSoftware(softwares);
        } else {
            // If no software IDs provided, clear the association, if not comment the line bellow
             room.setSoftware(new ArrayList<>());
        }

        Room savedRoom = roomRepository.save(room);
        return ResponseEntity.status(status).body(savedRoom);
    }
}