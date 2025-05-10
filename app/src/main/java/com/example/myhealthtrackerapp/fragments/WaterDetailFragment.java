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

import com.example.myhealthtrackerapp.R;
import com.example.myhealthtrackerapp.utils.FirebaseUtil;
import com.example.myhealthtrackerapp.utils.Prefs;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class WaterDetailFragment extends Fragment {

    private TextView tvMostHydratedDay, tvTotalCups, tvTotalMl;
    private BarChart  chartWaterBars;
    private ProgressBar pbWater;

    public WaterDetailFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_water_detail, container, false);

        // bind views
        tvMostHydratedDay = view.findViewById(R.id.tv_most_hydrated_day);
        tvTotalCups       = view.findViewById(R.id.tv_total_cups);
        tvTotalMl         = view.findViewById(R.id.tv_total_ml);
        chartWaterBars    = view.findViewById(R.id.chart_water_bars);
        pbWater           = view.findViewById(R.id.pb_water);

        loadWaterDetails();
        return view;
    }

    private void loadWaterDetails() {
        FirebaseUtil.getWaterRef()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        float totalCups = 0f;
                        float totalMl   = 0f;
                        Map<Integer, Float> cupsPerDay = new HashMap<>();
                        Map<Integer, Float> mlPerDay   = new HashMap<>();

                        long weekAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000;

                        // 1) Tally up your weekly water
                        for (DataSnapshot e : snap.getChildren()) {
                            // timestamp
                            Long ts = e.child("timestamp").getValue(Long.class);

                            // amount: might be stored as Long/Double in Firebase
                            Object rawAmt = e.child("amount").getValue();
                            if (rawAmt == null || ts == null) continue;

                            // unit
                            String unit = e.child("unit").getValue(String.class);
                            if (unit == null || ts < weekAgo) continue;

                            // parse rawAmt into a float
                            float val;
                            if (rawAmt instanceof Number) {
                                val = ((Number) rawAmt).floatValue();
                            } else {
                                try {
                                    val = Float.parseFloat(rawAmt.toString());
                                } catch (NumberFormatException ex) {
                                    // skip malformed entries
                                    continue;
                                }
                            }

                            // figure out which day of week this entry belongs to
                            Calendar cal = Calendar.getInstance();
                            cal.setTimeInMillis(ts);
                            int day = cal.get(Calendar.DAY_OF_WEEK);

                            // accumulate
                            if (unit.equals("cups")) {
                                totalCups += val;
                                cupsPerDay.put(day,
                                        cupsPerDay.getOrDefault(day, 0f) + val);
                            } else {
                                totalMl += val;
                                mlPerDay.put(day,
                                        mlPerDay.getOrDefault(day, 0f) + val);
                            }
                        }

                        // 2) Determine most hydrated day
                        float[] totals = new float[7];
                        for (int d = 1; d <= 7; d++) {
                            totals[d - 1] = cupsPerDay.getOrDefault(d, 0f) * 240f
                                    + mlPerDay.getOrDefault(d, 0f);
                        }
                        int maxIndex = 0;
                        for (int i = 1; i < 7; i++) {
                            if (totals[i] > totals[maxIndex]) {
                                maxIndex = i;
                            }
                        }
                        String[] days = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
                        tvMostHydratedDay.setText("Most Hydrated Day: " + days[maxIndex]);

                        // 3) Update totals text
                        tvTotalCups.setText("Total Cups: " + Math.round(totalCups));
                        tvTotalMl.setText("Total Milliliters: " + Math.round(totalMl));

                        // 4) Build chart entries
                        ArrayList<BarEntry> entries = new ArrayList<>();
                        for (int d = 1; d <= 7; d++) {
                            float sumMl = cupsPerDay.getOrDefault(d, 0f) * 240f
                                    + mlPerDay.getOrDefault(d, 0f);
                            entries.add(new BarEntry(d, sumMl));
                        }
                        BarDataSet ds = new BarDataSet(entries, "Water (ml)");
                        ds.setColor(Color.CYAN);
                        ds.setValueTextColor(Color.WHITE);

                        chartWaterBars.setData(new BarData(ds));
                        chartWaterBars.getLegend().setTextColor(Color.WHITE);
                        chartWaterBars.getXAxis().setTextColor(Color.WHITE);
                        chartWaterBars.getAxisLeft().setTextColor(Color.WHITE);
                        chartWaterBars.getAxisRight().setTextColor(Color.WHITE);
                        chartWaterBars.getDescription().setEnabled(false);
                        chartWaterBars.invalidate();

                        // 5) Update ProgressBar toward user goal
                        SharedPreferences prefs = requireContext()
                                .getSharedPreferences(Prefs.NAME, Context.MODE_PRIVATE);
                        int goalMl = prefs.getInt(Prefs.KEY_WATER_ML, Prefs.defaultWater);
                        if (goalMl > 0) {
                            int percent = Math.min(100,
                                    Math.round((totalMl / goalMl) * 100));
                            pbWater.setProgress(percent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { /* no-op */ }
                });
    }
}
