package com.example.myhealthtrackerapp.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateUtil {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("h:mm a", Locale.getDefault());
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault());
    
    // Format timestamp to date string
    public static String formatDate(long timestamp) {
        return DATE_FORMAT.format(new Date(timestamp));
    }
    
    // Format timestamp to time string
    public static String formatTime(long timestamp) {
        return TIME_FORMAT.format(new Date(timestamp));
    }
    
    // Format timestamp to date and time string
    public static String formatDateTime(long timestamp) {
        return DATE_TIME_FORMAT.format(new Date(timestamp));
    }
    
    // Get start of day in milliseconds
    public static long getStartOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    
    // Get start of week in milliseconds
    public static long getStartOfWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    
    // Check if the timestamp is from today
    public static boolean isToday(long timestamp) {
        return timestamp >= getStartOfDay();
    }
    
    // Check if the timestamp is from this week
    public static boolean isThisWeek(long timestamp) {
        return timestamp >= getStartOfWeek();
    }
    
    // Get day of week from timestamp (0-6, where 0 is Sunday)
    public static int getDayOfWeek(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return calendar.get(Calendar.DAY_OF_WEEK) - 1;
    }
    
    // Get day name from timestamp
    public static String getDayName(long timestamp) {
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        return dayFormat.format(new Date(timestamp));
    }
    
    // Get short day name from timestamp
    public static String getShortDayName(long timestamp) {
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        return dayFormat.format(new Date(timestamp));
    }
} 