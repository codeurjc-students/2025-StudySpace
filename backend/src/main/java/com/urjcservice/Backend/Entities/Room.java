package com.urjcservice.Backend.Entities;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Room {

    public enum CampusType {
        ALCORCON,
        MOSTOLES,
        VICALVARO,
        FUENLABRADA,
        QUINTANA
    }

    
    private Long id;

    private String name; //primary key
    private Integer capacity;
    private CampusType Camp; 
    private String place;
    private String coordenades;
    private Software[] equipment;
    private List<Reservation> reservations = new ArrayList<>();

    

    public Room() {
    }
    public Room(Long id, String name, Integer capacity, CampusType camp, String place, String coordenades,
            Software[] equipment) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        Camp = camp;
        this.place = place;
        this.coordenades = coordenades;
        this.equipment = equipment;
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
    public Software[] getEquipment() {
        return equipment;
    }
    public void setEquipment(Software[] equipment) {
        this.equipment = equipment;
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
