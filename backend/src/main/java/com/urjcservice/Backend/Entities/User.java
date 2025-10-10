package com.urjcservice.Backend.Entities;

import java.sql.Blob;

public class User {

    public enum UserType {
        ADMIN,
        USER_NOT_REGISTERED,
        USER_REGISTERED
    }

    private Long id;
    private String email; //Primary key
    private String password;
    private String name;
    private UserType type; //admin, user not-registered and registered
    private Blob picture;

    public User() {
    }

    public User(Long id, String email, String password, String name, UserType type) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.type = type;
    }
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public UserType getType() {
        return type;
    }
    public void setType(UserType type) {
        this.type = type;
    }
    public Blob getPicture() {
        return picture;
    }
    public void setPicture(Blob picture) {
        this.picture = picture;
    }
}
