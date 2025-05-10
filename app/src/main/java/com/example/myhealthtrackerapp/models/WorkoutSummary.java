package com.example.myhealthtrackerapp.models;

/**
 * A simple data holder for today's workout summary.
 */
public class WorkoutSummary {
    private final long totalMinutes;

    /**
     * @param totalMinutes sum of durations (in minutes) of today’s workouts
     */
    public WorkoutSummary(long totalMinutes) {
        this.totalMinutes = totalMinutes;
    }

    /** @return total workout minutes for today */
    public long getTotalMinutes() {
        return totalMinutes;
    }
}
