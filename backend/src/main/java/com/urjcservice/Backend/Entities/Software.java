package com.urjcservice.Backend.Entities;

public class Software {

    private Long id;
    private String name; //Primary key
    private Float version;
    private String description;

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



}
