package com.example.myhealthtrackerapp.utils;

import androidx.annotation.NonNull;

import com.example.myhealthtrackerapp.models.Meal;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DashboardMealManager {

    public interface MealTotalsCallback {
        void onTotalsCalculated(long totalCalories,
                                long totalProtein,
                                long totalCarbs,
                                long totalFats,
                                long totalSugars);
        void onError(DatabaseError error);
    }

    private final DatabaseReference mealsRef;

    public DashboardMealManager() {
        // assume meals are stored under "meals/{uid}/<pushId>"
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mealsRef = FirebaseDatabase
                .getInstance()
                .getReference("meals")
                .child(uid);
    }

    /**
     * Fetch all meals from today, sum their calories & macros, and
     * return via the callback.
     */
    public void fetchTodayMealTotals(@NonNull final MealTotalsCallback callback) {
        long startOfDay = DateUtil.getStartOfDay();

        // query only meals with timestamp >= startOfDay
        Query todayQuery = mealsRef.orderByChild("timestamp")
                .startAt(startOfDay);

        todayQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long calories = 0, protein = 0, carbs = 0, fats = 0, sugars = 0;

                for (DataSnapshot child : snapshot.getChildren()) {
                    Meal meal = child.getValue(Meal.class);
                    if (meal != null) {
                        calories += meal.getCalories();
                        protein  += meal.getProtein();
                        carbs    += meal.getCarbs();
                        fats     += meal.getFats();
                        sugars   += meal.getSugars();
                    }
                }
                callback.onTotalsCalculated(calories, protein, carbs, fats, sugars);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error);
            }
        });
    }
}
