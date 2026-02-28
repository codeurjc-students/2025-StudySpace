package com.urjcservice.backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;

@Entity
@Indexed
@Table(name = "users")
public class User {

    public enum UserType {
        ADMIN,
        USER_NOT_REGISTERED,
        USER_REGISTERED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @FullTextField(analyzer = "standard")
    private String email; // Primary key
    private String encodedPassword;
    @FullTextField(analyzer = "standard")
    private String name;
    @GenericField
    private UserType type; // admin and registered
    @GenericField
    private boolean blocked = false;// in order to block users

    private String resetPasswordToken;// to recover password
    private LocalDateTime resetPasswordTokenExpiry;

    @GenericField
    @ElementCollection(fetch = FetchType.EAGER) // for later autentication
    private List<String> roles;

    @Column(name = "image_name")
    private String imageName;

    @IndexedEmbedded(includePaths = { "startDate", "endDate", "room.name" })
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Reservation> reservations = new ArrayList<>();

    public User() {
    }

    public User(Long id, String email, String password, String name, UserType type) {
        this.id = id;
        this.email = email;
        this.encodedPassword = password;
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

    public String getEncodedPassword() {
        return encodedPassword;
    }

    public void setEncodedPassword(String password) {
        this.encodedPassword = password;
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

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
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
            reservation.setUser(this);
        }
    }

    public void removeReservation(Reservation reservation) {
        if (reservation != null && this.reservations.remove(reservation) && reservation.getUser() == this) {
            reservation.setUser(null);

        }
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public String getResetPasswordToken() {
        return resetPasswordToken;
    }

    public void setResetPasswordToken(String resetPasswordToken) {
        this.resetPasswordToken = resetPasswordToken;
    }

    public LocalDateTime getResetPasswordTokenExpiry() {
        return resetPasswordTokenExpiry;
    }

    public void setResetPasswordTokenExpiry(LocalDateTime resetPasswordTokenExpiry) {
        this.resetPasswordTokenExpiry = resetPasswordTokenExpiry;
    }
}
