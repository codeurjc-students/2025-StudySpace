package com.urjcservice.Backend.Entities;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //Primary key
    private Date startDate;
    private Date endDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; //one user can have many bookings

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;  //one room can have many bookings

    private String reason;

    public Reservation() {
    }

    public Reservation(Long id, Date startDate, Date endDate, User user, Room room, String reason) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.user = user;
        this.room = room;
        this.reason = reason;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getRoomId() {
        return room != null ? room.getId() : null;
    }

    public void setRoomId(Long roomId) {
        if (this.room == null) this.room = new Room();
        this.room.setId(roomId);
    }

    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    public void setUserId(Long userId) {
        if (this.user == null) this.user = new User();
        this.user.setId(userId);
    }

}
