package com.example.myhealthtrackerapp.models;

/**
 * A simple data holder for today's sleep summary.
 */
public class SleepSummary {
    private final double totalHours;

    /**
     * @param totalHours sum of hoursSlept for today’s entries
     */
    public SleepSummary(double totalHours) {
        this.totalHours = totalHours;
    }

    /** @return total hours slept today */
    public double getTotalHours() {
        return totalHours;
    }
}
