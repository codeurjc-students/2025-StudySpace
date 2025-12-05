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
        public String name;
        public Integer capacity;
        public Room.CampusType camp;
        public String place;
        public String coordenades;
        public List<Long> softwareIds;
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
        // Convertimos DTO a Entidad
        Room room = mapRequestToEntity(new Room(), request);
        
        // Delegamos la lógica de guardado (y asignación de software) al servicio
        Room savedRoom = roomService.save(room);
        
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(savedRoom.getId()).toUri();
        return ResponseEntity.created(location).body(savedRoom);
    }
    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable Long id, @RequestBody RoomRequest request) {
        // Creamos una entidad temporal con los datos nuevos
        Room roomData = mapRequestToEntity(new Room(), request);

        // El servicio se encarga de buscar la sala por ID y actualizarla
        return roomService.updateRoom(id, roomData)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        // Usamos el servicio para borrar (esto asegura que se borren las relaciones en room_software)
        Optional<Room> deleted = roomService.deleteById(id);
        
        if (deleted.isPresent()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    /*
    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody RoomRequest request) {
        Room room = mapRequestToEntity(new Room(), request);
        Room savedRoom = roomService.save(room);
        
        URI location = fromCurrentRequest().path("/{id}").buildAndExpand(savedRoom.getId()).toUri();
        return ResponseEntity.created(location).body(savedRoom);
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

*/
    //auxiliar method to dto to entity
    private Room mapRequestToEntity(Room room, RoomRequest request) {
        if (request.name != null) room.setName(request.name);
        if (request.capacity != null) room.setCapacity(request.capacity);
        if (request.camp != null) room.setCamp(request.camp);
        if (request.place != null) room.setPlace(request.place);
        if (request.coordenades != null) room.setCoordenades(request.coordenades);

        // Preparamos la lista de software basada en los IDs
        if (request.softwareIds != null) {
            List<Software> softwares = new ArrayList<>();
            for (Long softId : request.softwareIds) {
                // Buscamos el software real para asignarlo
                softwareService.findById(softId).ifPresent(softwares::add);
            }
            room.setSoftware(softwares);
        } else {
            room.setSoftware(new ArrayList<>());
        }
        return room;
    }
}