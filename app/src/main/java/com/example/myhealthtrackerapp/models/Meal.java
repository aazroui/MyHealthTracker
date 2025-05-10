package com.example.myhealthtrackerapp.models;

import java.util.Date;

public class Meal {
    private String id;
    private String mealName;
    private int calories;
    private long protein;
    private long carbs;
    private long fats;
    private long sugars;
    private long timestamp;
    private String userId;

    // Empty constructor required by Firebase
    public Meal() {
    }

    /**
     * Create a new Meal.
     * Timestamp is set to current time automatically.
     */
    public Meal(String id,
                String mealName,
                int calories,
                long protein,
                long carbs,
                long fats,
                long sugars,
                String userId) {
        this.id = id;
        this.mealName = mealName;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fats = fats;
        this.sugars = sugars;
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

    public String getMealName() {
        return mealName;
    }

    public void setMealName(String mealName) {
        this.mealName = mealName;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public long getProtein() {
        return protein;
    }

    public void setProtein(long protein) {
        this.protein = protein;
    }

    public long getCarbs() {
        return carbs;
    }

    public void setCarbs(long carbs) {
        this.carbs = carbs;
    }

    public long getFats() {
        return fats;
    }

    public void setFats(long fats) {
        this.fats = fats;
    }

    public long getSugars() {
        return sugars;
    }

    public void setSugars(long sugars) {
        this.sugars = sugars;
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
