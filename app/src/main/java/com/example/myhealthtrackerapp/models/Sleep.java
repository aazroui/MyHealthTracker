package com.example.myhealthtrackerapp.models;

import java.util.Date;

public class Sleep {
    private String id;
    private double hoursSlept;
    private long timestamp;
    private String userId;

    private String quality;

    // Empty constructor needed for Firebase
    public Sleep() {
    }

    public Sleep(String id, double hoursSlept, String userId) {
        this.id = id;
        this.hoursSlept = hoursSlept;
        this.timestamp = new Date().getTime();
        this.userId = userId;
    }

    public String getQuality() {
        return quality;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getHoursSlept() {
        return hoursSlept;
    }

    public void setHoursSlept(double hoursSlept) {
        this.hoursSlept = hoursSlept;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
} 