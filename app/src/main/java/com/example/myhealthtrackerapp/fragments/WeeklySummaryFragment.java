package com.example.myhealthtrackerapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myhealthtrackerapp.R;
import com.example.myhealthtrackerapp.utils.FirebaseUtil;
import com.example.myhealthtrackerapp.utils.Prefs;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class WeeklySummaryFragment extends Fragment {

    private TextView tvWeeklyFeedback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weekly_summary, container, false);

        tvWeeklyFeedback = view.findViewById(R.id.tv_weekly_feedback);
        fetchWeeklyData();

        return view;
    }

    private void fetchWeeklyData() {
        SharedPreferences prefs = requireContext().getSharedPreferences(Prefs.NAME, Context.MODE_PRIVATE);

        long[] weekRange = getThisWeekRange();
        long startTime = weekRange[0];
        long endTime = weekRange[1];

        final int[] totalWater = {0};
        final int[] totalWorkout = {0};
        final int[] totalCalories = {0};
        final int[] totalSleep = {0};

        DatabaseReference waterRef = FirebaseUtil.getWaterRef();
        DatabaseReference workoutRef = FirebaseUtil.getWorkoutRef();
        DatabaseReference mealsRef = FirebaseUtil.getMealsRef();
        DatabaseReference sleepRef = FirebaseUtil.getSleepRef();

        if (waterRef != null)
            waterRef.orderByChild("timestamp").startAt(startTime).endAt(endTime)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot s : snapshot.getChildren()) {
                                Object v = s.child("amount").getValue();
                                if (v instanceof Number) {
                                    totalWater[0] += ((Number) v).intValue();
                                }
                            }
                            maybeGenerateFeedback(prefs, totalWater[0], totalWorkout[0], totalSleep[0], totalCalories[0]);
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) { }
                    });

        if (workoutRef != null)
            workoutRef.orderByChild("timestamp").startAt(startTime).endAt(endTime)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot s : snapshot.getChildren()) {
                                Object v = s.child("duration").getValue();
                                if (v instanceof Number) {
                                    totalWorkout[0] += ((Number) v).intValue();
                                }
                            }
                            maybeGenerateFeedback(prefs, totalWater[0], totalWorkout[0], totalSleep[0], totalCalories[0]);
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) { }
                    });

        if (mealsRef != null)
            mealsRef.orderByChild("timestamp").startAt(startTime).endAt(endTime)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot s : snapshot.getChildren()) {
                                Object v = s.child("calories").getValue();
                                if (v instanceof Number) {
                                    totalCalories[0] += ((Number) v).intValue();
                                }
                            }
                            maybeGenerateFeedback(prefs, totalWater[0], totalWorkout[0], totalSleep[0], totalCalories[0]);
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) { }
                    });

        if (sleepRef != null)
            sleepRef.orderByChild("timestamp").startAt(startTime).endAt(endTime)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot s : snapshot.getChildren()) {
                                Object v = s.child("hours").getValue();
                                if (v instanceof Number) {
                                    totalSleep[0] += ((Number) v).intValue();
                                }
                            }
                            maybeGenerateFeedback(prefs, totalWater[0], totalWorkout[0], totalSleep[0], totalCalories[0]);
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) { }
                    });
    }

    private void maybeGenerateFeedback(SharedPreferences prefs,
                                       int totalWaterMl, int totalWorkoutMin, int totalSleepHrs, int totalCalories) {

        int goalWaterPerDay = prefs.getInt(Prefs.KEY_WATER_ML, 2000);
        int goalSleepPerDay = prefs.getInt(Prefs.KEY_SLEEP_HOURS, 8);
        int goalWorkoutPerWeek = prefs.getInt(Prefs.KEY_WORKOUT_MINS, 150);
        int goalCaloriesPerDay = prefs.getInt(Prefs.KEY_CALORIES, 2000);

        generateWeeklyFeedback(
                totalWaterMl, goalWaterPerDay,
                totalSleepHrs, goalSleepPerDay,
                totalWorkoutMin, goalWorkoutPerWeek,
                totalCalories, goalCaloriesPerDay
        );
    }

    private void generateWeeklyFeedback(
            int totalWaterMl, int goalWaterMlPerDay,
            int totalSleepHrs, int goalSleepHrsPerDay,
            int totalWorkoutMin, int goalWorkoutMinPerWeek,
            int totalCalories, int goalCaloriesPerDay) {

        int waterGoal = goalWaterMlPerDay * 7;
        int waterPct = (int) (100.0 * totalWaterMl / waterGoal);

        int sleepGoal = goalSleepHrsPerDay * 7;
        int sleepPct = (int) (100.0 * totalSleepHrs / sleepGoal);

        int workoutPct = (int) (100.0 * totalWorkoutMin / goalWorkoutMinPerWeek);

        int calorieGoal = goalCaloriesPerDay * 7;
        int caloriePct = (int) (100.0 * totalCalories / calorieGoal);

        StringBuilder feedback = new StringBuilder();

        if (waterPct >= 100) feedback.append("💧 Great hydration!\n");
        else if (waterPct >= 60) feedback.append("💧 Almost met your water goal.\n");
        else feedback.append("💧 Try to drink more water.\n");

        if (sleepPct >= 100) feedback.append("😴 Sleep goals on point!\n");
        else if (sleepPct >= 60) feedback.append("😴 Getting there with sleep.\n");
        else feedback.append("😴 Aim for more rest.\n");

        if (workoutPct >= 100) feedback.append("💪 Crushed your workouts!\n");
        else if (workoutPct >= 60) feedback.append("💪 Almost hit your workout goal.\n");
        else feedback.append("💪 Let's get moving next week.\n");

        if (caloriePct >= 100) feedback.append("🍽️ Eating well!\n");
        else if (caloriePct >= 60) feedback.append("🍽️ Slightly low on calories.\n");
        else feedback.append("🍽️ Consider logging meals consistently.\n");

        tvWeeklyFeedback.setText(feedback.toString().trim());
    }

    private long[] getThisWeekRange() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long end = cal.getTimeInMillis() + 24 * 60 * 60 * 1000 - 1;
        cal.add(Calendar.DAY_OF_YEAR, -6);
        long start = cal.getTimeInMillis();
        return new long[]{start, end};
    }
}
