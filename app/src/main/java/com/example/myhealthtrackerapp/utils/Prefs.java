package com.example.myhealthtrackerapp.utils;

/**
 * Keys and defaults for your “Goals” SharedPreferences.
 */
public class Prefs {
    public static final String NAME = "com.example.myhealthtrackerapp.PREFS";

    public static final String KEY_WATER_ML       = "goal_water_ml";
    public static final String KEY_CALORIES       = "goal_calories";
    public static final String KEY_WORKOUT_MINS   = "goal_workout_mins";
    public static final String KEY_SLEEP_HOURS    = "goal_sleep_hours";
    public static final String KEY_SLEEP_HRS = "sleep_goal_hours";

    public static final String  KEY_WATER_UNIT = "water_unit";

    public static final String defaultWaterUnit = "ml";

    // sensible defaults
    public static final int defaultWater    = 2000;  // mL
    public static final int defaultCalories = 2000;  // kcal
    public static final int defaultWorkout  =  30;   // minutes
    public static final int defaultSleep    =   8;   // hours
}
