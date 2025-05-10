package com.example.myhealthtrackerapp.models;

public class WaterSummary {
    private final double totalMl;
    private final double totalCups;

    /**
     * @param totalMl   total volume of today’s water entries in milliliters
     * @param totalCups total volume in cups (for UI display / progress)
     */
    public WaterSummary(double totalMl, double totalCups) {
        this.totalMl    = totalMl;
        this.totalCups  = totalCups;
    }

    /** @return total water consumed today, in milliliters */
    public double getTotalMl() {
        return totalMl;
    }

    /** @return total water consumed today, converted to cups */
    public double getTotalCups() {
        return totalCups;
    }
}
