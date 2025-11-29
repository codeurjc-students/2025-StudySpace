package com.urjcservice.backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms")
public class Room {

    public enum CampusType {
        ALCORCON,
        MOSTOLES,
        VICALVARO,
        FUENLABRADA,
        QUINTANA
    }

    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; //primary key
    private Integer capacity;
    private CampusType Camp; 
    private String place;
    private String coordenades;
    @ManyToMany
    @JoinTable(
        name = "room_software",
        joinColumns = @JoinColumn(name = "room_id"),
        inverseJoinColumns = @JoinColumn(name = "software_id")
    )
    private List<Software> software = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Reservation> reservations = new ArrayList<>();

    

    public Room() {
    }
    public Room(Long id, String name, Integer capacity, CampusType camp, String place, String coordenades,
            List<Software> software) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        Camp = camp;
        this.place = place;
        this.coordenades = coordenades;
        this.software = software;
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
    public Integer getCapacity() {
        return capacity;
    }
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
    public CampusType getCamp() {
        return Camp;
    }
    public void setCamp(CampusType camp) {
        Camp = camp;
    }
    public String getPlace() {
        return place;
    }
    public void setPlace(String place) {
        this.place = place;
    }
    public String getCoordenades() {
        return coordenades;
    }

    public void setCoordenades(String coordenades) {
        this.coordenades = coordenades;
    }
    public List<Software> getSoftware() {
        return software;
    }

    public void setSoftware(List<Software> software) {
        this.software = software;
    }

    public void addSoftware(Software s) {
        if (s != null && !this.software.contains(s)) {
            this.software.add(s);
            if (s.getRooms() == null || !s.getRooms().contains(this)) {
                s.getRooms().add(this);
            }
        }
    }

    public void removeSoftware(Software s) {
        if (s != null && this.software.remove(s)) {
            if (s.getRooms() != null) {
                s.getRooms().remove(this);
            }
        }
    }
    @JsonIgnore
    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public void addReservation(Reservation reservation) {
        if (reservation != null && !this.reservations.contains(reservation)) {
            this.reservations.add(reservation);
            reservation.setRoom(this);
        }
    }

    public void removeReservation(Reservation reservation) {
        if (reservation != null && this.reservations.remove(reservation)) {
            if (reservation.getRoom() == this) {
                reservation.setRoom(null);
            }
        }
    }
    

}
