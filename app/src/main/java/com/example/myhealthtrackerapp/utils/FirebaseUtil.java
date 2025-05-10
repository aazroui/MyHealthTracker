package com.example.myhealthtrackerapp.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUtil {
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();

    // Database references
    private static final String USERS_REF = "Users"; // ✅ changed to uppercase 'U'
    private static final String MEALS_REF = "meals";
    private static final String WATER_REF = "water";
    private static final String WORKOUT_REF = "workout";
    private static final String SLEEP_REF = "sleep";

    // Get current user
    public static FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    // Get current user ID
    public static String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    // Get database reference for users
    public static DatabaseReference getUsersRef() {
        return database.getReference(USERS_REF); // now points to "Users"
    }

    // Get database reference for current user
    public static DatabaseReference getCurrentUserRef() {
        String userId = getCurrentUserId();
        return userId != null ? getUsersRef().child(userId) : null;
    }

    // Get database reference for meals
//    public static DatabaseReference getMealsRef() {
//        String userId = getCurrentUserId();
////        return userId != null ? database.getReference(MEALS_REF).child(userId) : null;
//        return userId != null ? database.getReference("meals").child(userId) : null;
//    }
    public static DatabaseReference getMealsRef() {
        String uid = getCurrentUserId();
        return (uid != null)
                ? getUsersRef().child(uid).child("Meals")
                : null;
    }

    // Get database reference for water
    public static DatabaseReference getWaterRef() {
        String userId = getCurrentUserId();
        return userId != null ? database.getReference(WATER_REF).child(userId) : null;
    }

    // Get database reference for workout
    public static DatabaseReference getWorkoutRef() {
        String userId = getCurrentUserId();
        return userId != null ? database.getReference(WORKOUT_REF).child(userId) : null;
    }

    // Get database reference for sleep
    public static DatabaseReference getSleepRef() {
        String userId = getCurrentUserId();
        return userId != null ? database.getReference(SLEEP_REF).child(userId) : null;
    }

    // Create a new database reference ID
    public static String getNewId(DatabaseReference ref) {
        return ref.push().getKey();
    }
}
