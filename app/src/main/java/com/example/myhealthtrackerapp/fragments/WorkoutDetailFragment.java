package com.example.myhealthtrackerapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myhealthtrackerapp.R;
import com.example.myhealthtrackerapp.models.Workout;
import com.example.myhealthtrackerapp.utils.FirebaseUtil;
import com.example.myhealthtrackerapp.utils.Prefs;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkoutDetailFragment extends Fragment {

    private TextView tvTotalMinutes;
    private ProgressBar progressWorkout;
    private TextView tvProgressPercent;
    private BarChart chartWorkoutDetail;
    private TextView tvMostActiveDay;
    private RecyclerView rvSessions;

    public WorkoutDetailFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout_detail, container, false);

        // Wire up views
        tvTotalMinutes     = view.findViewById(R.id.tv_total_minutes);
        progressWorkout    = view.findViewById(R.id.progress_workout);
        tvProgressPercent  = view.findViewById(R.id.tv_progress_percent);
        chartWorkoutDetail = view.findViewById(R.id.chart_workout_detail);
        tvMostActiveDay    = view.findViewById(R.id.tv_most_active_day);
        rvSessions         = view.findViewById(R.id.rv_workout_sessions);

        // Prepare RecyclerView
        rvSessions.setLayoutManager(new LinearLayoutManager(getContext()));

        loadWorkoutDetails();
        return view;
    }

    private void loadWorkoutDetails() {
        DatabaseReference ref = FirebaseUtil.getWorkoutRef();
        if (ref == null) return;

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                // For chart & totals
                Map<Integer, Float> byDay = new HashMap<>();
                float totalMinutes = 0f;
                long weekAgo = System.currentTimeMillis() - 7L*24*60*60*1000;

                // For session list
                List<Workout> sessions = new ArrayList<>();

                for (DataSnapshot e : snap.getChildren()) {
                    Long ts  = e.child("timestamp").getValue(Long.class);
                    Long dur = e.child("duration").getValue(Long.class);
                    if (ts == null || dur == null || ts < weekAgo) continue;

                    // accumulate chart data
                    totalMinutes += dur;
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(ts);
                    int day = c.get(Calendar.DAY_OF_WEEK);
                    byDay.put(day, byDay.getOrDefault(day, 0f) + dur);

                    // collect session for RecyclerView
                    Workout w = e.getValue(Workout.class);
                    if (w != null) {
                        sessions.add(w);
                    }
                }

                // 1) Update total & progress bar
                tvTotalMinutes.setText(String.format("%.0f min this week", totalMinutes));
                SharedPreferences prefs = requireContext()
                        .getSharedPreferences(Prefs.NAME, Context.MODE_PRIVATE);
                int goal = prefs.getInt(Prefs.KEY_WORKOUT_MINS, Prefs.defaultWorkout);
                progressWorkout.setMax(goal);
                int prog = Math.min((int) totalMinutes, goal);
                progressWorkout.setProgress(prog);
                int percent = Math.round((totalMinutes / goal) * 100);
                tvProgressPercent.setText(String.format("%d%% of goal", percent));

                // 2) Populate bar chart
                ArrayList<BarEntry> entries = new ArrayList<>();
                for (int d = 1; d <= 7; d++) {
                    entries.add(new BarEntry(d, byDay.getOrDefault(d, 0f)));
                }
                BarDataSet ds = new BarDataSet(entries, "Minutes");
                ds.setColor(Color.GREEN);
                ds.setValueTextColor(Color.WHITE);

                chartWorkoutDetail.setData(new BarData(ds));
                chartWorkoutDetail.setFitBars(true);
                chartWorkoutDetail.getDescription().setEnabled(false);

                // Style axes & legend white
                chartWorkoutDetail.getLegend().setTextColor(Color.WHITE);
                XAxis x = chartWorkoutDetail.getXAxis();
                x.setPosition(XAxis.XAxisPosition.BOTTOM);
                x.setGranularity(1f);
                x.setTextColor(Color.WHITE);
                x.setValueFormatter(new ValueFormatter() {
                    private final String[] labels = {"","Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
                    @Override public String getFormattedValue(float value) {
                        int i = Math.round(value);
                        return (i >= 1 && i <= 7) ? labels[i] : "";
                    }
                });
                chartWorkoutDetail.getAxisLeft().setTextColor(Color.WHITE);
                chartWorkoutDetail.getAxisRight().setTextColor(Color.WHITE);
                chartWorkoutDetail.invalidate();

                // 3) Find most active day
                int bestDay = 1;
                float max = byDay.getOrDefault(1, 0f);
                for (int d = 2; d <= 7; d++) {
                    float v = byDay.getOrDefault(d, 0f);
                    if (v > max) { max = v; bestDay = d; }
                }
                String[] days = {"","Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
                tvMostActiveDay.setText(
                        String.format("Most Active Day: %s (%.0f min)",
                                days[bestDay], max)
                );

                // 4) Attach session list to RecyclerView
                rvSessions.setAdapter(new WorkoutSessionAdapter(sessions));
            }

            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
