package com.urjcservice.backend.dtos;

import java.util.List;

public class RoomCalendarDTO {
    private List<CalendarEvent> events;
    private List<DailyOccupancy> dailyOccupancy;

    public RoomCalendarDTO(List<CalendarEvent> events, List<DailyOccupancy> dailyOccupancy) {
        this.events = events;
        this.dailyOccupancy = dailyOccupancy;
    }

    //getters
    public List<CalendarEvent> getEvents() { return events; }
    public void setEvents(List<CalendarEvent> events) { this.events = events; }

    public List<DailyOccupancy> getDailyOccupancy() { return dailyOccupancy; }
    public void setDailyOccupancy(List<DailyOccupancy> dailyOccupancy) { this.dailyOccupancy = dailyOccupancy; }

    // --- Internal classes ---
    public static class CalendarEvent {
        private Long id;
        private String title;
        private String start;
        private String end;

        public CalendarEvent(Long id, String title, String start, String end) {
            this.id = id;
            this.title = title;
            this.start = start;
            this.end = end;
        }

        // Getters for JSON
        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getStart() { return start; }
        public String getEnd() { return end; }
    }

    public static class DailyOccupancy {
        private String date;
        private String color;
        private String status;

        public DailyOccupancy(String date, String color, String status) {
            this.date = date;
            this.color = color;
            this.status = status;
        }

        public String getDate() { return date; }
        public String getColor() { return color; }
        public String getStatus() { return status; }
    }


}