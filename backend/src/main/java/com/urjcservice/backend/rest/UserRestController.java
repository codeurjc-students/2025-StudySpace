package com.urjcservice.backend.rest;

import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.service.UserService;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.service.ReservationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;

import java.net.URI;
import java.util.List;
import java.util.Optional;


import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

   
    private final UserService userService;
    private final ReservationService reservationService;
    
    public UserRestController(UserService userService, ReservationService reservationService) {
        this.userService = userService;
        this.reservationService = reservationService;
    }

    @GetMapping
    public Page<User> getAllUsers(@PageableDefault(size = 10) Pageable pageable) {//if frontend dont send size, default 10
        return userService.findAll(pageable);
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<User> changeRole(@PathVariable Long id, @RequestParam String role) {
        return userService.changeRole(id, role)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    
    @PutMapping("/{id}/block")
    public ResponseEntity<User> toggleBlock(@PathVariable Long id) {
        return userService.toggleBlock(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);
    return user.map(ResponseEntity::ok) // Returns 200 OK if found
           .orElseGet(() -> ResponseEntity.notFound().build()); // Returns 404 Not Found if not found
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User savedUser = userService.save(user);

        URI location = fromCurrentRequest().path("/{id}").buildAndExpand(savedUser.getId()).toUri();

        return ResponseEntity.created(location).body(savedUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        Optional<User> user = userService.updateUser(id, updatedUser);
    return user.map(ResponseEntity::ok) // Returns 200 OK if updated successfully
           .orElseGet(() -> ResponseEntity.notFound().build()); // Returns 404 Not Found if not found
    }

    @PatchMapping("/{id}")
    public ResponseEntity<User> patchUser(@PathVariable Long id, @RequestBody User partialUser) {
        Optional<User> user = userService.patchUser(id, partialUser);
    return user.map(ResponseEntity::ok) // Returns 200 OK if partially updated
           .orElseGet(() -> ResponseEntity.notFound().build()); // Returns 404 Not Found if not found
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<User> deleteUser(@PathVariable Long id) {
        Optional<User> deleted = userService.deleteById(id);
        return deleted.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }

    //for getting reservations of a user
    @GetMapping("/{id}/reservations")
    public ResponseEntity<Page<Reservation>> getUserReservations(@PathVariable Long id,
            @PageableDefault(size = 10) 
            @SortDefault(sort = "startDate", direction = Sort.Direction.DESC) 
            Pageable pageable
    ) {
        return ResponseEntity.ok(reservationService.getReservationsByUserId(id, pageable));
    }
}
