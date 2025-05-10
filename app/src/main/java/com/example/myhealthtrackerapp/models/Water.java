package com.example.myhealthtrackerapp.models;

import java.util.Date;

public class Water {
    private String id;
    private double amount; // Amount in ml
    private long timestamp;
    private String userId;

    // Empty constructor needed for Firebase
    public Water() {
    }

    public Water(String id, double amount, String userId) {
        this.id = id;
        this.amount = amount;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
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
    
    // Convenience methods
    public double getAmountInCups() {
        // 1 cup = 236.59 ml
        return amount / 236.59;
    }
} 