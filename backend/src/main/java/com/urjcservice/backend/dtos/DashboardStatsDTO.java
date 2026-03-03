package com.urjcservice.backend.dtos;

import java.util.Map;

public class DashboardStatsDTO {
    // Chart 1: Occupancy (% occupied vs % free today)
    private long totalRooms;
    private double occupiedPercentage;
    private double freePercentage;

    // Chart 2: Traffic (hour map -> reservation count)
    private Map<String, Long> hourlyOccupancy;

    // Chart 3: Software (% with software vs % without software)
    private double roomsWithSoftwarePercentage;
    private double roomsWithoutSoftwarePercentage;

    public DashboardStatsDTO() {
    }

    public long getTotalRooms() {
        return totalRooms;
    }

    public void setTotalRooms(long totalRooms) {
        this.totalRooms = totalRooms;
    }

    public double getOccupiedPercentage() {
        return occupiedPercentage;
    }

    public void setOccupiedPercentage(double occupiedPercentage) {
        this.occupiedPercentage = occupiedPercentage;
    }

    public double getFreePercentage() {
        return freePercentage;
    }

    public void setFreePercentage(double freePercentage) {
        this.freePercentage = freePercentage;
    }

    public Map<String, Long> getHourlyOccupancy() {
        return hourlyOccupancy;
    }

    public void setHourlyOccupancy(Map<String, Long> hourlyOccupancy) {
        this.hourlyOccupancy = hourlyOccupancy;
    }

    public double getRoomsWithSoftwarePercentage() {
        return roomsWithSoftwarePercentage;
    }

    public void setRoomsWithSoftwarePercentage(double roomsWithSoftwarePercentage) {
        this.roomsWithSoftwarePercentage = roomsWithSoftwarePercentage;
    }

    public double getRoomsWithoutSoftwarePercentage() {
        return roomsWithoutSoftwarePercentage;
    }

    public void setRoomsWithoutSoftwarePercentage(double roomsWithoutSoftwarePercentage) {
        this.roomsWithoutSoftwarePercentage = roomsWithoutSoftwarePercentage;
    }
}