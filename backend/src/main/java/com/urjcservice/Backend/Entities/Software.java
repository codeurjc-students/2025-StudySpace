package com.urjcservice.Backend.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "softwares")
public class Software {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name; //Primary key
    private Float version;
    private String description;

    @ManyToMany(mappedBy = "software")
    @JsonIgnore
    private List<Room> rooms = new ArrayList<>();

    public Software() {
    }

    public Software(Long id, String name, Float version, String description) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getVersion() {
        return version;
    }

    public void setVersion(Float version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonIgnore
    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    public void addRoom(Room room) {
        if (room != null && !this.rooms.contains(room)) {
            this.rooms.add(room);
            if (room.getSoftware() == null || !room.getSoftware().contains(this)) {
                room.getSoftware().add(this);
            }
        }
    }

    public void removeRoom(Room room) {
        if (room != null && this.rooms.remove(room)) {
            if (room.getSoftware() != null) {
                room.getSoftware().remove(this);
            }
        }
    }

}
