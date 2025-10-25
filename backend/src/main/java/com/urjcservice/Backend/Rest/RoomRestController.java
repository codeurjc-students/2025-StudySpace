package com.urjcservice.Backend.Rest;

import com.urjcservice.Backend.Entities.Room;
import com.urjcservice.Backend.Service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

@RestController
@RequestMapping("/api/rooms")
public class RoomRestController {

    @Autowired
    private RoomService roomService;

    @GetMapping("/")
    public List<Room> getAllRooms() {
        return roomService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        Optional<Room> room = roomService.findById(id);
        return room.map(ResponseEntity::ok) // Devuelve 200 OK si se encuentra la Room
                   .orElseGet(() -> ResponseEntity.notFound().build()); // Devuelve 404 Not Found si no se encuentra
    }

    @PostMapping("/")
    public ResponseEntity<Room> createRoom(@RequestBody Room room) {
        Room savedRoom = roomService.save(room);

        URI location = fromCurrentRequest().path("/{id}").buildAndExpand(savedRoom.getId()).toUri();

        return ResponseEntity.created(location).body(savedRoom);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable Long id, @RequestBody Room updatedRoom) {
        Optional<Room> room = roomService.updateRoom(id, updatedRoom);
        return room.map(ResponseEntity::ok) // Devuelve 200 OK si se actualiza correctamente
                   .orElseGet(() -> ResponseEntity.notFound().build()); // Devuelve 404 Not Found si no se encuentra
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Room> patchRoom(@PathVariable Long id, @RequestBody Room partialRoom) {
        Optional<Room> room = roomService.patchRoom(id, partialRoom);
        return room.map(ResponseEntity::ok) // Devuelve 200 OK si se actualiza parcialmente
                   .orElseGet(() -> ResponseEntity.notFound().build()); // Devuelve 404 Not Found si no se encuentra
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        boolean deleted = roomService.deleteById(id);
        if (deleted) {
            return ResponseEntity.noContent().build(); // Devuelve 204 No Content si se elimina correctamente
        } else {
            return ResponseEntity.notFound().build(); // Devuelve 404 Not Found si no se encuentra la Room
        }
    }
}
