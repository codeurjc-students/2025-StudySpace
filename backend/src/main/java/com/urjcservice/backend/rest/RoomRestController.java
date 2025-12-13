package com.urjcservice.backend.rest; 

import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.Software;
import com.urjcservice.backend.repositories.RoomRepository;
import com.urjcservice.backend.repositories.SoftwareRepository;
import com.urjcservice.backend.service.RoomService;
import com.urjcservice.backend.service.SoftwareService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rooms") 
public class RoomRestController {

    
    private final RoomService roomService;
    private final SoftwareService softwareService;

    public RoomRestController(RoomService roomService, SoftwareService softwareService) {
        this.roomService = roomService;
        this.softwareService = softwareService;
    }

    //internal DTO for room requests on frontend
    public static class RoomRequest {
        private String name;
        private Integer capacity;
        private Room.CampusType camp;
        private String place;
        private String coordenades;
        private List<Long> softwareIds;
        private Boolean active;


        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Integer getCapacity() { return capacity; }
        public void setCapacity(Integer capacity) { this.capacity = capacity; }

        public Room.CampusType getCamp() { return camp; }
        public void setCamp(Room.CampusType camp) { this.camp = camp; }

        public String getPlace() { return place; }
        public void setPlace(String place) { this.place = place; }

        public String getCoordenades() { return coordenades; }
        public void setCoordenades(String coordenades) { this.coordenades = coordenades; }

        public List<Long> getSoftwareIds() { return softwareIds; }
        public void setSoftwareIds(List<Long> softwareIds) { this.softwareIds = softwareIds; }

        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
    }

    @GetMapping
    public List<Room> getAllRooms() {
        return roomService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        return roomService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }



    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody RoomRequest request) {
        //COnvert to DTO to entity
        Room room = mapRequestToEntity(new Room(), request);
        
        Room savedRoom = roomService.save(room);
        
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(savedRoom.getId()).toUri();
        return ResponseEntity.created(location).body(savedRoom);
    }
    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable Long id, @RequestBody RoomRequest request) {
        //temporary to entity
        Room roomData = mapRequestToEntity(new Room(), request);

        return roomService.updateRoom(id, roomData)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        Optional<Room> deleted = roomService.deleteById(id);
        
        if (deleted.isPresent()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    //auxiliar method to dto to entity
    private Room mapRequestToEntity(Room room, RoomRequest request) {
        if (request.name != null) room.setName(request.name);
        if (request.capacity != null) room.setCapacity(request.capacity);
        if (request.camp != null) room.setCamp(request.camp);
        if (request.place != null) room.setPlace(request.place);
        if (request.coordenades != null) room.setCoordenades(request.coordenades);
        if (request.active != null) room.setActive(request.active);

        //List of softwares
        if (request.softwareIds != null) {
            List<Software> softwares = new ArrayList<>();
            for (Long softId : request.softwareIds) {
                
                softwareService.findById(softId).ifPresent(softwares::add);
            }
            room.setSoftware(softwares);
        } else {
            room.setSoftware(new ArrayList<>());
        }
        return room;
    }
}