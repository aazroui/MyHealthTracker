package com.example.myhealthtrackerapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SummaryFragment extends Fragment {

    private BarChart chartWeeklyCalories, chartWater, chartWorkout, chartSleep;
    private PieChart chartMacros;
    private TextView tvWeeklyFeedback, tvWeekRange;

    // Feedback accumulation
    private float sumWeeklyCalories = 0f;
    private float sumWeeklyWater    = 0f;
    private float sumWeeklyWorkout  = 0f;
    private float sumWeeklySleep    = 0f;
    private int   feedbackLoadedCount = 0;

    public SummaryFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_weekly_summary, container, false);

        // Chart widgets
        chartWeeklyCalories = view.findViewById(R.id.chart_calories);
        chartWater          = view.findViewById(R.id.chart_water);
        chartWorkout        = view.findViewById(R.id.chart_workout);
        chartSleep          = view.findViewById(R.id.chart_sleep);
        chartMacros         = view.findViewById(R.id.chart_macros);

        // Feedback and date‐range TextViews
        tvWeeklyFeedback    = view.findViewById(R.id.tv_weekly_feedback);
        tvWeekRange         = view.findViewById(R.id.tv_weekly_date_range);

        // Set current week range (e.g. “Apr 27 - May 3”)
        tvWeekRange.setText(getCurrentWeekRange());

        // Navigate to detail fragments on click
        chartWater.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new WaterDetailFragment())
                        .addToBackStack(null)
                        .commit()
        );
        chartWeeklyCalories.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new CaloriesDetailFragment())
                        .addToBackStack(null)
                        .commit()
        );
        chartWorkout.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new WorkoutDetailFragment())
                        .addToBackStack(null)
                        .commit()
        );
        chartSleep.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new SleepDetailFragment())
                        .addToBackStack(null)
                        .commit()
        );

        // Load all charts
        loadWeeklyCaloriesChart();
        loadWeeklyWaterChart();
        loadWeeklyWorkoutChart();
        loadWeeklySleepChart();
        loadMacroBreakdownChart();

        // Style X‐axes on bar charts
        setDayAxis(chartWeeklyCalories);
        setDayAxis(chartWater);
        setDayAxis(chartWorkout);
        setDayAxis(chartSleep);

        return view;
    }

    /**
     * Returns this week’s date range as “MMM d – MMM d”, e.g. “Apr 27 – May 3”.
     */
    private String getCurrentWeekRange() {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        // Move to first day of week
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        SimpleDateFormat fmt = new SimpleDateFormat("MMM d", Locale.getDefault());
        String start = fmt.format(cal.getTime());
        // Advance six days to last day of week
        cal.add(Calendar.DAY_OF_WEEK, 6);
        String end = fmt.format(cal.getTime());
        return start + " - " + end;
    }

    // Safely read values from DataSnapshot
    private long getLongValue(DataSnapshot snap, String key) {
        Object v = snap.child(key).getValue();
        if (v instanceof Number) return ((Number)v).longValue();
        if (v instanceof String) {
            try { return Long.parseLong((String)v); }
            catch (NumberFormatException ignored) {}
        }
        return 0L;
    }

    private float getFloatValue(DataSnapshot snap, String key) {
        Object v = snap.child(key).getValue();
        if (v instanceof Number) return ((Number)v).floatValue();
        if (v instanceof String) {
            try { return Float.parseFloat((String)v); }
            catch (NumberFormatException ignored) {}
        }
        return 0f;
    }

    private void loadWeeklyCaloriesChart() {
        DatabaseReference mealsRef = FirebaseUtil.getMealsRef();
        if (mealsRef == null) return;

        mealsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                Map<Integer, Float> byDay = new HashMap<>();
                long weekAgo = System.currentTimeMillis() - 7L*24*60*60*1000;

                sumWeeklyCalories = 0f;
                for (DataSnapshot e : snap.getChildren()) {
                    long ts = getLongValue(e, "timestamp");
                    float cal = getFloatValue(e, "calories");
                    if (ts >= weekAgo) {
                        sumWeeklyCalories += cal;
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(ts);
                        int day = c.get(Calendar.DAY_OF_WEEK);
                        byDay.put(day, byDay.getOrDefault(day,0f) + cal);
                    }
                }

                ArrayList<BarEntry> entries = new ArrayList<>();
                for (int d=1; d<=7; d++) {
                    entries.add(new BarEntry(d, byDay.getOrDefault(d,0f)));
                }
                BarDataSet ds = new BarDataSet(entries, "Calories");
                ds.setColor(Color.MAGENTA);
                ds.setValueTextColor(Color.WHITE);
                chartWeeklyCalories.setData(new BarData(ds));
                chartWeeklyCalories.setFitBars(true);
                chartWeeklyCalories.getDescription().setEnabled(false);
                styleBarChart(chartWeeklyCalories);
                chartWeeklyCalories.invalidate();

                onChartDataLoaded();
            }
            @Override public void onCancelled(@NonNull DatabaseError err) {}
        });
    }

    private void loadWeeklyWaterChart() {
        DatabaseReference ref = FirebaseUtil.getWaterRef();
        if (ref == null) return;

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                Map<Integer, Float> byDay = new HashMap<>();
                long weekAgo = System.currentTimeMillis() - 7L*24*60*60*1000;

                sumWeeklyWater = 0f;
                for (DataSnapshot e: snap.getChildren()) {
                    long ts = getLongValue(e, "timestamp");
                    float val = getFloatValue(e, "amount");
                    String unit = e.child("unit").getValue(String.class);
                    if ("cups".equals(unit)) val *= 240f;
                    if (ts >= weekAgo) {
                        sumWeeklyWater += val;
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(ts);
                        int day = c.get(Calendar.DAY_OF_WEEK);
                        byDay.put(day, byDay.getOrDefault(day,0f) + val);
                    }
                }

                ArrayList<BarEntry> entries = new ArrayList<>();
                for (int d=1; d<=7; d++) {
                    entries.add(new BarEntry(d, byDay.getOrDefault(d,0f)));
                }
                BarDataSet ds = new BarDataSet(entries, "Water (ml)");
                ds.setColor(Color.CYAN);
                ds.setValueTextColor(Color.WHITE);
                chartWater.setData(new BarData(ds));
                chartWater.setFitBars(true);
                chartWater.getDescription().setEnabled(false);
                styleBarChart(chartWater);
                chartWater.invalidate();

                onChartDataLoaded();
            }
            @Override public void onCancelled(@NonNull DatabaseError err) {}
        });
    }

    private void loadWeeklyWorkoutChart() {
        DatabaseReference ref = FirebaseUtil.getWorkoutRef();
        if (ref == null) return;

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                Map<Integer, Float> byDay = new HashMap<>();
                long weekAgo = System.currentTimeMillis() - 7L*24*60*60*1000;

                sumWeeklyWorkout = 0f;
                for (DataSnapshot e: snap.getChildren()) {
                    long ts = getLongValue(e, "timestamp");
                    float dur = getFloatValue(e, "duration");
                    if (ts >= weekAgo) {
                        sumWeeklyWorkout += dur;
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(ts);
                        int day = c.get(Calendar.DAY_OF_WEEK);
                        byDay.put(day, byDay.getOrDefault(day,0f) + dur);
                    }
                }

                ArrayList<BarEntry> entries = new ArrayList<>();
                for (int d=1; d<=7; d++) {
                    entries.add(new BarEntry(d, byDay.getOrDefault(d,0f)));
                }
                BarDataSet ds = new BarDataSet(entries, "Workout (min)");
                ds.setColor(Color.GREEN);
                ds.setValueTextColor(Color.WHITE);
                chartWorkout.setData(new BarData(ds));
                chartWorkout.setFitBars(true);
                chartWorkout.getDescription().setEnabled(false);
                styleBarChart(chartWorkout);
                chartWorkout.invalidate();

                onChartDataLoaded();
            }
            @Override public void onCancelled(@NonNull DatabaseError err) {}
        });
    }

    private void loadWeeklySleepChart() {
        DatabaseReference ref = FirebaseUtil.getSleepRef();
        if (ref == null) return;

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                Map<Integer, Float> byDay = new HashMap<>();
                long weekAgo = System.currentTimeMillis() - 7L*24*60*60*1000;

                sumWeeklySleep = 0f;
                for (DataSnapshot e: snap.getChildren()) {
                    long ts = getLongValue(e, "timestamp");
                    float hrs = getFloatValue(e, "hours");
                    if (ts >= weekAgo) {
                        sumWeeklySleep += hrs;
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(ts);
                        int day = c.get(Calendar.DAY_OF_WEEK);
                        byDay.put(day, byDay.getOrDefault(day,0f) + hrs);
                    }
                }

                ArrayList<BarEntry> entries = new ArrayList<>();
                for (int d=1; d<=7; d++) {
                    entries.add(new BarEntry(d, byDay.getOrDefault(d,0f)));
                }
                BarDataSet ds = new BarDataSet(entries, "Sleep (hrs)");
                ds.setColor(Color.YELLOW);
                ds.setValueTextColor(Color.WHITE);
                chartSleep.setData(new BarData(ds));
                chartSleep.setFitBars(true);
                chartSleep.getDescription().setEnabled(false);
                styleBarChart(chartSleep);
                chartSleep.invalidate();

                onChartDataLoaded();
            }
            @Override public void onCancelled(@NonNull DatabaseError err) {}
        });
    }

    private void loadMacroBreakdownChart() {
        DatabaseReference ref = FirebaseUtil.getMealsRef();
        if (ref == null) return;

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                float protein=0f, carbs=0f, fats=0f, sugars=0f;
                long weekAgo = System.currentTimeMillis() - 7L*24*60*60*1000;

                for (DataSnapshot e: snap.getChildren()) {
                    long ts = getLongValue(e,"timestamp");
                    if (ts < weekAgo) continue;
                    protein += getFloatValue(e,"protein");
                    carbs   += getFloatValue(e,"carbs");
                    fats    += getFloatValue(e,"fats");
                    sugars  += getFloatValue(e,"sugars");
                }

                ArrayList<PieEntry> entries = new ArrayList<>();
                if (protein>0) entries.add(new PieEntry(protein,"Protein"));
                if (carbs>0)   entries.add(new PieEntry(carbs,"Carbs"));
                if (fats>0)    entries.add(new PieEntry(fats,"Fats"));
                if (sugars>0)  entries.add(new PieEntry(sugars,"Sugars"));

                PieDataSet ds = new PieDataSet(entries,"Macros");
                ds.setColors(Color.RED,Color.BLUE,Color.GREEN,Color.MAGENTA);
                ds.setValueTextColor(Color.WHITE);
                ds.setValueTextSize(14f);

                PieData data = new PieData(ds);
                data.setValueFormatter(new PercentFormatter(chartMacros));

                chartMacros.setData(data);
                chartMacros.setUsePercentValues(true);
                chartMacros.setDrawEntryLabels(true);
                chartMacros.getDescription().setEnabled(false);
                stylePieChart(chartMacros);
                chartMacros.invalidate();
            }
            @Override public void onCancelled(@NonNull DatabaseError err) {}
        });
    }

    private void onChartDataLoaded() {
        if (++feedbackLoadedCount < 4) return;
        SharedPreferences prefs = requireContext()
                .getSharedPreferences(Prefs.NAME, Context.MODE_PRIVATE);

        int goalWater    = prefs.getInt(Prefs.KEY_WATER_ML,2000)*7;
        int goalSleep    = prefs.getInt(Prefs.KEY_SLEEP_HOURS,8)*7;
        int goalWorkout  = prefs.getInt(Prefs.KEY_WORKOUT_MINS,150);
        int goalCalories = prefs.getInt(Prefs.KEY_CALORIES,2000)*7;

        int pctWater    = (int)(100*sumWeeklyWater/goalWater);
        int pctSleep    = (int)(100*sumWeeklySleep/goalSleep);
        int pctWorkout  = (int)(100*sumWeeklyWorkout/goalWorkout);
        int pctCalories = (int)(100*sumWeeklyCalories/goalCalories);

        StringBuilder fb = new StringBuilder();
        fb.append(pctWater>=100? "💧 Great hydration!\n": pctWater>=60? "💧 Almost met water goal.\n":"💧 Drink more water.\n");
        fb.append(pctSleep>=100? "😴 Sleep on point!\n": pctSleep>=60? "😴 Almost there with sleep.\n":"😴 Aim for more rest.\n");
        fb.append(pctWorkout>=100? "💪 Crushed workouts!\n": pctWorkout>=60? "💪 Almost hit workout goal.\n":"💪 Let's move more next week.\n");
        fb.append(pctCalories>=100? "🍽️ Eating well!\n": pctCalories>=60? "🍽️ Slightly low on calories.\n":"🍽️ Log meals consistently.\n");

        tvWeeklyFeedback.setText(fb.toString().trim());
    }

    private void setDayAxis(BarChart chart) {
        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1f);
        x.setLabelCount(7);
        final String[] days = {"","Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
        x.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float v) {
                int i = Math.round(v);
                return (i>=1 && i<=7)? days[i] : "";
            }
        });
        chart.getAxisRight().setEnabled(false);
    }

    private void styleBarChart(BarChart chart) {
        chart.getLegend().setTextColor(Color.WHITE);
        chart.getXAxis().setTextColor(Color.WHITE);
        chart.getAxisLeft().setTextColor(Color.WHITE);
        chart.getAxisRight().setTextColor(Color.WHITE);
    }

    private void stylePieChart(PieChart chart) {
        chart.getLegend().setTextColor(Color.WHITE);
        chart.setEntryLabelColor(Color.WHITE);
        chart.setCenterTextColor(Color.WHITE);
    }
}
