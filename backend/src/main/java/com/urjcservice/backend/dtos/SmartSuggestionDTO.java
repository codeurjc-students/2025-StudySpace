package com.urjcservice.backend.dtos;

import com.urjcservice.backend.entities.Room;
import java.util.Date;

public class SmartSuggestionDTO {
    private Room room;
    private Date suggestedStart;
    private Date suggestedEnd;
    private String matchType; // "EXACT_MATCH", "ALTERNATIVE_TIME", "ALTERNATIVE_ROOM"
    private int score; // from 0 to 100 puntuation

    public SmartSuggestionDTO(Room room, Date start, Date end, String matchType, int score) {
        this.room = room;
        this.suggestedStart = start;
        this.suggestedEnd = end;
        this.matchType = matchType;
        this.score = score;
    }

    // Getters and Setters
    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public Date getSuggestedStart() {
        return suggestedStart;
    }

    public void setSuggestedStart(Date suggestedStart) {
        this.suggestedStart = suggestedStart;
    }

    public Date getSuggestedEnd() {
        return suggestedEnd;
    }

    public void setSuggestedEnd(Date suggestedEnd) {
        this.suggestedEnd = suggestedEnd;
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}