package com.example.myhealthtrackerapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myhealthtrackerapp.R;
import com.example.myhealthtrackerapp.models.WorkoutSummary;
import com.example.myhealthtrackerapp.models.SleepSummary;
import com.example.myhealthtrackerapp.utils.DateUtil;
import com.example.myhealthtrackerapp.utils.FirebaseUtil;
import com.example.myhealthtrackerapp.utils.Prefs;
import com.example.myhealthtrackerapp.utils.DashboardWorkoutManager;
import com.example.myhealthtrackerapp.utils.DashboardSleepManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class DashboardFragment extends Fragment {

    // Meals UI
    private TextView tvDate, tvCalories, tvProtein, tvCarbs, tvFats, tvSugars;

    // Water UI
    private TextView   tvWaterCups, tvWaterMl, tvWaterGoal;
    private ProgressBar progressWater;

    // Workout UI
    private TextView tvWorkoutMinutes, tvWorkoutDesc;

    // Sleep UI
    private TextView tvSleepHours, tvSleepGoal;

    private SwipeRefreshLayout swipe;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override public void onViewCreated(@NonNull View view,
                                        @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // bind meals views
        tvDate      = view.findViewById(R.id.tv_today_date);
        tvCalories  = view.findViewById(R.id.tvDashboardCalories);
        tvProtein   = view.findViewById(R.id.tvDashboardProtein);
        tvCarbs     = view.findViewById(R.id.tvDashboardCarbs);
        tvFats      = view.findViewById(R.id.tvDashboardFats);
        tvSugars    = view.findViewById(R.id.tvDashboardSugars);

        // bind water views
        tvWaterCups   = view.findViewById(R.id.tv_water_cups);
        tvWaterMl     = view.findViewById(R.id.tv_water_ml);
        progressWater = view.findViewById(R.id.progress_water);
        tvWaterGoal   = view.findViewById(R.id.tv_water_goal);

        // bind workout views
        tvWorkoutMinutes = view.findViewById(R.id.tv_workout_minutes);
        tvWorkoutDesc    = view.findViewById(R.id.tv_workout_type);

        // bind sleep views
        tvSleepHours = view.findViewById(R.id.tv_sleep_hours);
        tvSleepGoal  = view.findViewById(R.id.tv_sleep_goal);

        // swipe-to-refresh container
        swipe = view.findViewById(R.id.swipe_refresh);
        swipe.setOnRefreshListener(this::fetchAndDisplay);

        // show today's date
        tvDate.setText(DateUtil.formatDate(System.currentTimeMillis()));

        // initial load
        fetchAndDisplay();
    }

    private void fetchAndDisplay() {
        swipe.setRefreshing(true);

        // ---------------------------------------------------------------------
        // 1) Meals
        // ---------------------------------------------------------------------
        DatabaseReference mealsRef = FirebaseUtil.getMealsRef();
        if (mealsRef == null) {
            swipe.setRefreshing(false);
            Toast.makeText(getContext(),
                    "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // compute start/end of today
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE,      0);
        cal.set(Calendar.SECOND,      0);
        cal.set(Calendar.MILLISECOND, 0);
        final long startOfDay = cal.getTimeInMillis();
        final long now        = System.currentTimeMillis();

        mealsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                int totalCal = 0;
                long totalProt = 0, totalCarb = 0, totalFats = 0, totalSug = 0;

                for (DataSnapshot e : snap.getChildren()) {
                    long ts = safeGetLong(e, "timestamp");
                    if (ts < startOfDay || ts > now) continue;

                    totalCal  += safeGetInt(e, "calories");
                    totalProt += safeGetLong(e, "protein");
                    totalCarb += safeGetLong(e, "carbs");
                    totalFats += safeGetLong(e, "fats");
                    totalSug  += safeGetLong(e, "sugars");
                }

                tvCalories.setText(totalCal + " calories");
                tvProtein .setText("Protein: " + totalProt + "g");
                tvCarbs   .setText("Carbs: "   + totalCarb + "g");
                tvFats    .setText("Fats: "    + totalFats + "g");
                tvSugars  .setText("Sugars: "  + totalSug + "g");

                swipe.setRefreshing(false);
            }

            @Override public void onCancelled(@NonNull DatabaseError err) {
                swipe.setRefreshing(false);
                Toast.makeText(getContext(),
                        "Error loading meals: " + err.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        // ---------------------------------------------------------------------
        // 2) Water (direct, today-only fetch)
        // ---------------------------------------------------------------------
        DatabaseReference waterRef = FirebaseUtil.getWaterRef();
        if (waterRef != null) {
            waterRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snap) {
                    float totalCups = 0f;
                    float totalMl   = 0f;

                    for (DataSnapshot e : snap.getChildren()) {
                        long ts = safeGetLong(e, "timestamp");
                        if (ts < startOfDay || ts > now) continue;

                        // parse "amount" as Number or String
                        Object raw = e.child("amount").getValue();
                        float val = 0f;
                        if (raw instanceof Number) {
                            val = ((Number) raw).floatValue();
                        } else if (raw instanceof String) {
                            try {
                                val = Float.parseFloat((String) raw);
                            } catch (NumberFormatException ignored) {}
                        }

                        String unit = e.child("unit")
                                .getValue(String.class);
                        if ("cups".equals(unit)) {
                            totalCups += val;
                        } else {
                            totalMl   += val;
                        }
                    }

                    // update the UI
                    tvWaterCups.setText(Math.round(totalCups) + " cups");

                    // show pure ml count or cups→ml + ml, your choice:
                    float combinedMl = totalCups * 236.59f + totalMl;
                    tvWaterMl.setText(Math.round(combinedMl) + " ml");

                    // load the goal from prefs (stored in mL)
                    SharedPreferences prefs = requireContext()
                            .getSharedPreferences(Prefs.NAME, Context.MODE_PRIVATE);
                    int goalMl   = prefs.getInt(Prefs.KEY_WATER_ML,
                            Prefs.defaultWater);
                    int goalCups = Math.round(goalMl / 236.59f);

                    progressWater.setMax(goalCups);
                    progressWater.setProgress(Math.round(totalCups));

                    tvWaterGoal.setText("Goal: " + goalCups + " cups");
                }

                @Override public void onCancelled(@NonNull DatabaseError err) {
                    Toast.makeText(getContext(),
                            "Error loading water: " + err.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        // ---------------------------------------------------------------------
        // 3) Workout (unchanged)
        // ---------------------------------------------------------------------
        DashboardWorkoutManager.fetchTodayWorkout(new DashboardWorkoutManager.WorkoutFetchListener() {
            @Override public void onFetched(WorkoutSummary summary) {
                long totalMins = summary.getTotalMinutes();
                SharedPreferences prefs = requireContext()
                        .getSharedPreferences(Prefs.NAME, Context.MODE_PRIVATE);
                int goalMins = prefs.getInt(Prefs.KEY_WORKOUT_MINS,
                        Prefs.defaultWorkout);

                tvWorkoutMinutes.setText(totalMins + " minutes");
                tvWorkoutDesc   .setText(
                        totalMins > 0 ? "" : "No workout today"
                );
            }
            @Override public void onError(String errorMessage) {
                Toast.makeText(getContext(),
                        "Error loading workout: " + errorMessage,
                        Toast.LENGTH_SHORT).show();
            }
        });

        // ---------------------------------------------------------------------
        // 4) Sleep (unchanged)
        // ---------------------------------------------------------------------
        DashboardSleepManager.fetchTodaySleep(new DashboardSleepManager.SleepFetchListener() {
            @Override public void onFetched(SleepSummary summary) {
                double slept = summary.getTotalHours();
                SharedPreferences prefs = requireContext()
                        .getSharedPreferences(Prefs.NAME, Context.MODE_PRIVATE);
                int goal = prefs.getInt(Prefs.KEY_SLEEP_HOURS,
                        Prefs.defaultSleep);

                tvSleepHours.setText((int) slept + " hours");
                tvSleepGoal .setText("Goal: " + goal + " hours");
            }
            @Override public void onError(String errorMessage) {
                Toast.makeText(getContext(),
                        "Error loading sleep: " + errorMessage,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Safely parse a node as long (handles Number or String) */
    private long safeGetLong(DataSnapshot snap, String key) {
        Object v = snap.child(key).getValue();
        if (v instanceof Number)      return ((Number) v).longValue();
        if (v instanceof String) {
            try { return Long.parseLong((String) v); }
            catch (NumberFormatException ignored) {}
        }
        return 0L;
    }

    /** Safely parse a node as int (handles Number or String) */
    private int safeGetInt(DataSnapshot snap, String key) {
        Object v = snap.child(key).getValue();
        if (v instanceof Number)      return ((Number) v).intValue();
        if (v instanceof String) {
            try { return Integer.parseInt((String) v); }
            catch (NumberFormatException ignored) {}
        }
        return 0;
    }
}
