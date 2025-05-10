package com.example.myhealthtrackerapp.utils;

import android.text.TextUtils;
import android.util.Patterns;

public class ValidationUtil {
    
    // Validate email
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    
    // Validate password (minimum 6 characters)
    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= 6;
    }
    
    // Validate required field
    public static boolean isNotEmpty(String text) {
        return !TextUtils.isEmpty(text);
    }
    
    // Validate numeric field
    public static boolean isNumeric(String text) {
        if (TextUtils.isEmpty(text)) {
            return false;
        }
        try {
            Double.parseDouble(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    // Validate positive numeric field
    public static boolean isPositiveNumeric(String text) {
        if (!isNumeric(text)) {
            return false;
        }
        return Double.parseDouble(text) > 0;
    }
} 