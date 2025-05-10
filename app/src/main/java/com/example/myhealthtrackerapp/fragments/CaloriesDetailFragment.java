package com.example.myhealthtrackerapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myhealthtrackerapp.R;
import com.example.myhealthtrackerapp.utils.FirebaseUtil;
import com.example.myhealthtrackerapp.utils.Prefs;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class CaloriesDetailFragment extends Fragment {

    private TableLayout macroTable;
    private ProgressBar pbCalories;

    public CaloriesDetailFragment() {}

    @Nullable @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_calories_detail,
                container, false
        );
        macroTable = view.findViewById(R.id.table_macros);
        pbCalories = view.findViewById(R.id.pb_calories);

        loadMacroData();
        return view;
    }

    // Safely read any numeric field stored as Number or String
    private long getLong(DataSnapshot snap, String key) {
        Object v = snap.child(key).getValue();
        if (v instanceof Number) return ((Number)v).longValue();
        if (v instanceof String) {
            try { return Long.parseLong((String)v); }
            catch (NumberFormatException ignored) {}
        }
        return 0L;
    }

    private float getFloat(DataSnapshot snap, String key) {
        Object v = snap.child(key).getValue();
        if (v instanceof Number) return ((Number)v).floatValue();
        if (v instanceof String) {
            try { return Float.parseFloat((String)v); }
            catch (NumberFormatException ignored) {}
        }
        return 0f;
    }

    private void loadMacroData() {
        // Read from the same /Users/{uid}/Meals node
        DatabaseReference mealsRef = FirebaseUtil.getMealsRef();
        if (mealsRef == null) return;

        mealsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                String[] days = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
                float[][] macros = new float[7][4];
                long weekAgo = System.currentTimeMillis() - 7L*24*60*60*1000;

                // 1) Accumulate by day
                for (DataSnapshot entry : snap.getChildren()) {
                    long ts = getLong(entry, "timestamp");
                    if (ts < weekAgo) continue;

                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(ts);
                    int idx = (cal.get(Calendar.DAY_OF_WEEK) + 6) % 7;

                    macros[idx][0] += getFloat(entry, "protein");
                    macros[idx][1] += getFloat(entry, "carbs");
                    macros[idx][2] += getFloat(entry, "fats");
                    macros[idx][3] += getFloat(entry, "sugars");
                }

                // 2) Build table and track totals & max days
                float totalP=0, totalC=0, totalF=0, totalS=0;
                float maxP=0, maxC=0, maxF=0, maxS=0;
                int dayP=0, dayC=0, dayF=0, dayS=0;

                for (int i = 0; i < 7; i++) {
                    float p = macros[i][0], c = macros[i][1];
                    float f = macros[i][2], s = macros[i][3];
                    totalP += p; if (p>maxP){maxP=p;dayP=i;}
                    totalC += c; if (c>maxC){maxC=c;dayC=i;}
                    totalF += f; if (f>maxF){maxF=f;dayF=i;}
                    totalS += s; if (s>maxS){maxS=s;dayS=i;}

                    TableRow row = new TableRow(getContext());
                    row.setPadding(4,8,4,8);
                    row.addView(makeCell(days[i]));
                    row.addView(makeCell((int)p + "g"));
                    row.addView(makeCell((int)c + "g"));
                    row.addView(makeCell((int)f + "g"));
                    row.addView(makeCell((int)s + "g"));
                    macroTable.addView(row);
                }

                // Summary row
                TableRow sumRow = new TableRow(getContext());
                sumRow.setPadding(4,8,4,8);
                sumRow.setBackgroundColor(
                        ContextCompat.getColor(getContext(), R.color.medium_gray)
                );
                sumRow.addView(makeCell("Total"));
                sumRow.addView(makeCell((int)totalP + "g"));
                sumRow.addView(makeCell((int)totalC + "g"));
                sumRow.addView(makeCell((int)totalF + "g"));
                sumRow.addView(makeCell((int)totalS + "g"));
                macroTable.addView(sumRow);

                // Highest-day row
                TableRow highRow = new TableRow(getContext());
                highRow.setPadding(4,8,4,8);
                highRow.setBackgroundColor(
                        ContextCompat.getColor(getContext(), R.color.dark_gray)
                );
                highRow.addView(makeCell("Highest Day"));
                highRow.addView(makeCell(days[dayP]));
                highRow.addView(makeCell(days[dayC]));
                highRow.addView(makeCell(days[dayF]));
                highRow.addView(makeCell(days[dayS]));
                macroTable.addView(highRow);

                // 3) ProgressBar toward weekly calorie goal
                SharedPreferences prefs = requireContext()
                        .getSharedPreferences(Prefs.NAME, Context.MODE_PRIVATE);
                int goal = prefs.getInt(Prefs.KEY_CALORIES, Prefs.defaultCalories);
                float weekTotal = totalP + totalC + totalF + totalS;
                if (goal > 0) {
                    int pct = Math.min(100,
                            Math.round((weekTotal/goal) * 100));
                    pbCalories.setProgress(pct);
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    // Helper to make a styled TextView cell
    private TextView makeCell(String txt) {
        TextView tv = new TextView(getContext());
        tv.setText(txt);
        tv.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        tv.setPadding(16, 0, 16, 0);
        return tv;
    }
}
