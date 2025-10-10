package com.urjcservice.Backend.Entities;

import java.util.Date;

public class Book {

    private Long id; //Primary key
    private Date startDate;
    private Date endDate;
    private User user; //one user can have many bookings
    private Room room;  //one room can have many bookings
    private String reason;

    public Book() {
    }
    public Book(Long id, Date startDate, Date endDate, User user, Room room, String reason) {
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



}
