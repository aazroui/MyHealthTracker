package com.example.myhealthtrackerapp.models;

import java.util.Date;

public class Workout {
    private String id;
    private String workoutType;
    private int duration; // Duration in minutes
    private long timestamp;
    private String userId;

    // Empty constructor needed for Firebase
    public Workout() {
    }

    public Workout(String id, String workoutType, int duration, String userId) {
        this.id = id;
        this.workoutType = workoutType;
        this.duration = duration;
        this.timestamp = new Date().getTime();
        this.userId = userId;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWorkoutType() {
        return workoutType;
    }

    public void setWorkoutType(String workoutType) {
        this.workoutType = workoutType;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
} 