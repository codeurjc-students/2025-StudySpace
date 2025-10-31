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
    return room.map(ResponseEntity::ok) // Returns 200 OK if the Room is found
           .orElseGet(() -> ResponseEntity.notFound().build()); // Returns 404 Not Found if not found
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
    return room.map(ResponseEntity::ok) // Returns 200 OK if updated successfully
           .orElseGet(() -> ResponseEntity.notFound().build()); // Returns 404 Not Found if not found
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Room> patchRoom(@PathVariable Long id, @RequestBody Room partialRoom) {
    Optional<Room> room = roomService.patchRoom(id, partialRoom);
    return room.map(ResponseEntity::ok) // Returns 200 OK if partially updated
           .orElseGet(() -> ResponseEntity.notFound().build()); // Returns 404 Not Found if not found
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Room> deleteRoom(@PathVariable Long id) {
        Optional<Room> deleted = roomService.deleteById(id);
        return deleted.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
