package com.urjcservice.backend.rest; 

import com.urjcservice.backend.dtos.RoomCalendarDTO;
import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.Software;
import com.urjcservice.backend.service.FileStorageService;
import com.urjcservice.backend.service.RoomService;
import com.urjcservice.backend.service.SoftwareService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import java.io.IOException;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms") 
public class RoomRestController {

    
    private final RoomService roomService;
    private final SoftwareService softwareService;
    private final FileStorageService fileStorageService;

    public RoomRestController(RoomService roomService, SoftwareService softwareService, FileStorageService fileStorageService) {
        this.roomService = roomService;
        this.softwareService = softwareService;
        this.fileStorageService = fileStorageService;
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
    public Page<Room> getAllRooms(@PageableDefault(size = 10) Pageable pageable) {//if frontend dont send size, default 10
        return roomService.findAll(pageable);
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
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id, 
                                           @RequestParam(required = false) String reason) {
        String finalReason = (reason != null && !reason.isBlank()) 
                             ? reason 
                             : "Permanent closure of the room by administration.";

        roomService.deleteRoom(id, finalReason);
        
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String, Object>> getRoomStats(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        if (date == null) {
            date = LocalDate.now();
        }
        
        // Verify the room exist
        if (roomService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(roomService.getRoomDailyStats(id, date));
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










    @PostMapping("/{id}/image")
    public ResponseEntity<Room> uploadRoomImage(@PathVariable Long id, 
                                                @RequestParam("file") MultipartFile file) throws IOException {
        Optional<Room> roomOp = roomService.findById(id);
        if (roomOp.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Room room = roomOp.get();
        if (room.getImageName() != null) {
            fileStorageService.delete(room.getImageName());
        }

        String filename = fileStorageService.store(file);
        room.setImageName(filename);
        //roomService.save(room);

        return roomService.updateRoom(id, room)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> getRoomImage(@PathVariable Long id) {
        Optional<Room> roomOp = roomService.findById(id);
        if (roomOp.isEmpty() || roomOp.get().getImageName() == null) {
            return ResponseEntity.notFound().build();
        }

        Resource file = fileStorageService.loadAsResource(roomOp.get().getImageName());
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) 
                .body(file);
    }
















    @GetMapping("/{id}/calendar")
    public ResponseEntity<RoomCalendarDTO> getRoomCalendar(
            @PathVariable Long id,
            @RequestParam String start, 
            @RequestParam String end    
    ) {
        // exists room
        if (roomService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        //FullCalendar sends "2026-02-01T00:00:00+01:00".
        // cur first 10 chars to "2026-0.2-01".
        LocalDate startDate = LocalDate.parse(start.substring(0, 10));
        LocalDate endDate = LocalDate.parse(end.substring(0, 10));

        return ResponseEntity.ok(roomService.getRoomCalendarData(id, startDate, endDate));
    }


}