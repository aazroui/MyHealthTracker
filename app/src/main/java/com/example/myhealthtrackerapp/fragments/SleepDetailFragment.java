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
import com.example.myhealthtrackerapp.models.Sleep;
import com.example.myhealthtrackerapp.utils.FirebaseUtil;
import com.example.myhealthtrackerapp.utils.Prefs;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class SleepDetailFragment extends Fragment {

    private TextView tvTotalSleep, tvAverageSleep, tvSleepPercent, tvBestNight;
    private ProgressBar progressSleep;
    private BarChart chartSleepDetail;
    private RecyclerView rvSleepSessions;

    public SleepDetailFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sleep_detail, container, false);

        // bind views
        tvTotalSleep     = view.findViewById(R.id.tv_total_sleep);
        tvAverageSleep   = view.findViewById(R.id.tv_average_sleep);
        progressSleep    = view.findViewById(R.id.progress_sleep);
        tvSleepPercent   = view.findViewById(R.id.tv_sleep_percent);
        chartSleepDetail = view.findViewById(R.id.chart_sleep_detail);
        tvBestNight      = view.findViewById(R.id.tv_best_night);
        rvSleepSessions  = view.findViewById(R.id.rv_sleep_sessions);

        // setup RecyclerView
        rvSleepSessions.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadSleepDetails();
        return view;
    }

    private void loadSleepDetails() {
        DatabaseReference ref = FirebaseUtil.getSleepRef();
        if (ref == null) return;

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                Map<Integer, Float> byDay = new HashMap<>();
                float totalHours = 0f;
                long weekAgo = System.currentTimeMillis() - 7L*24*60*60*1000;

                List<Sleep> sessions = new ArrayList<>();

                for (DataSnapshot e : snap.getChildren()) {
                    Long ts = e.child("timestamp").getValue(Long.class);
                    String hrsStr = e.child("hours").getValue(String.class);
                    String quality = e.child("quality").getValue(String.class);
                    if (ts == null || hrsStr == null) continue;
                    if (ts < weekAgo) continue;

                    float hrs;
                    try { hrs = Float.parseFloat(hrsStr); }
                    catch (NumberFormatException x) { continue; }

                    // accumulate totals
                    totalHours += hrs;
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(ts);
                    int day = c.get(Calendar.DAY_OF_WEEK);
                    byDay.put(day, byDay.getOrDefault(day, 0f) + hrs);

                    // build session object including quality
                    Sleep s = new Sleep();
                    s.setTimestamp(ts);
                    s.setHoursSlept(hrs);
                    s.setQuality(quality);             // ← set quality here
                    sessions.add(s);
                }

                // 1) Totals & average
                tvTotalSleep.setText(String.format(Locale.getDefault(),
                        "%.0f hr this week", totalHours));
                float avg = totalHours / 7f;
                tvAverageSleep.setText(String.format(Locale.getDefault(),
                        "Avg. %.1f hr/night", avg));

                SharedPreferences prefs = requireContext()
                        .getSharedPreferences(Prefs.NAME, Context.MODE_PRIVATE);
                int nightlyGoal = prefs.getInt(Prefs.KEY_SLEEP_HRS, Prefs.defaultSleep);
                int weeklyGoal = nightlyGoal * 7;

                progressSleep.setMax(weeklyGoal);
                int prog = Math.min(Math.round(totalHours), weeklyGoal);
                progressSleep.setProgress(prog);

                int percent = Math.round((totalHours / weeklyGoal) * 100);
                tvSleepPercent.setText(String.format(Locale.getDefault(),
                        "%d%% of goal", percent));

                // 2) Bar chart
                ArrayList<BarEntry> entries = new ArrayList<>();
                for (int d = 1; d <= 7; d++) {
                    entries.add(new BarEntry(d, byDay.getOrDefault(d, 0f)));
                }
                BarDataSet ds = new BarDataSet(entries, "Sleep (hrs)");
                ds.setColor(Color.YELLOW);
                ds.setValueTextColor(Color.WHITE);

                chartSleepDetail.setData(new BarData(ds));
                chartSleepDetail.setFitBars(true);
                chartSleepDetail.getDescription().setEnabled(false);

                chartSleepDetail.getLegend().setTextColor(Color.WHITE);
                XAxis x = chartSleepDetail.getXAxis();
                x.setPosition(XAxis.XAxisPosition.BOTTOM);
                x.setGranularity(1f);
                x.setTextColor(Color.WHITE);
                x.setValueFormatter(new ValueFormatter() {
                    private final String[] labels = {"","Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
                    @Override public String getFormattedValue(float v) {
                        int i = Math.round(v);
                        return (i >= 1 && i <= 7) ? labels[i] : "";
                    }
                });
                chartSleepDetail.getAxisLeft().setTextColor(Color.WHITE);
                chartSleepDetail.getAxisRight().setTextColor(Color.WHITE);
                chartSleepDetail.invalidate();

                // 3) Best night
                int bestDay = 1;
                float max = byDay.getOrDefault(1, 0f);
                for (int d = 2; d <= 7; d++) {
                    float v = byDay.getOrDefault(d, 0f);
                    if (v > max) { max = v; bestDay = d; }
                }
                String[] days = {"","Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
                tvBestNight.setText(String.format(Locale.getDefault(),
                        "Best Night: %s (%.1f hr)", days[bestDay], max));

                // 4) Session list
                rvSleepSessions.setAdapter(new SleepSessionAdapter(sessions));
            }

            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
