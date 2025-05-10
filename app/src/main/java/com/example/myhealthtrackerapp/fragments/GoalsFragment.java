package com.example.myhealthtrackerapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myhealthtrackerapp.R;
import com.example.myhealthtrackerapp.utils.Prefs;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class GoalsFragment extends Fragment {
    private RadioGroup        rgWaterUnit;
    private RadioButton       rbWaterMl, rbWaterCups;
    private TextInputLayout   tilWaterGoal;
    private TextInputEditText etWater, etCalories, etWorkout, etSleep;
    private MaterialButton    btnSave;

    public GoalsFragment() { /* Required empty constructor */ }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_goals, container, false);

        // 1) Bind views
        rgWaterUnit   = v.findViewById(R.id.rg_water_unit);
        rbWaterMl     = v.findViewById(R.id.rb_water_ml);
        rbWaterCups   = v.findViewById(R.id.rb_water_cups);
        tilWaterGoal  = v.findViewById(R.id.til_goal_water);
        etWater       = v.findViewById(R.id.et_goal_water);
        etCalories    = v.findViewById(R.id.et_goal_calories);
        etWorkout     = v.findViewById(R.id.et_goal_workout);
        etSleep       = v.findViewById(R.id.et_goal_sleep);
        btnSave       = v.findViewById(R.id.btn_save_goals);

        // 2) Load existing prefs
        Context ctx = requireContext();
        SharedPreferences prefs =
                ctx.getSharedPreferences(Prefs.NAME, Context.MODE_PRIVATE);

        // Water: stored always in ml
        int savedMl = prefs.getInt(Prefs.KEY_WATER_ML, Prefs.defaultWater);
        String savedUnit =
                prefs.getString(Prefs.KEY_WATER_UNIT, Prefs.defaultWaterUnit);

        // 2a) set the radio selection
        if ("cups".equals(savedUnit)) rbWaterCups.setChecked(true);
        else                           rbWaterMl  .setChecked(true);

        // 2b) update hint text
        tilWaterGoal.setHint(
                savedUnit.equals("cups")
                        ? "Daily Water Goal (cups)"
                        : "Daily Water Goal (ml)"
        );

        // 2c) populate water field
        if (savedUnit.equals("cups")) {
            int cups = (int) Math.round(savedMl / 236.59);
            etWater.setText(String.valueOf(cups));
        } else {
            etWater.setText(String.valueOf(savedMl));
        }

        // other goals
        etCalories.setText(
                String.valueOf(
                        prefs.getInt(Prefs.KEY_CALORIES, Prefs.defaultCalories)
                )
        );
        etWorkout .setText(
                String.valueOf(
                        prefs.getInt(Prefs.KEY_WORKOUT_MINS, Prefs.defaultWorkout)
                )
        );
        etSleep   .setText(
                String.valueOf(
                        prefs.getInt(Prefs.KEY_SLEEP_HOURS, Prefs.defaultSleep)
                )
        );

        // 3) Save handler
        btnSave.setOnClickListener(ignored -> {
            try {
                // 3a) read & convert water input
                String chosenUnit = rbWaterCups.isChecked() ? "cups" : "ml";
                int rawValue     = Integer.parseInt(etWater.getText().toString().trim());
                int mlValue      = chosenUnit.equals("cups")
                        ? (int) Math.round(rawValue * 236.59)
                        : rawValue;

                // 3b) read the rest
                int c = Integer.parseInt(etCalories.getText().toString().trim());
                int w = Integer.parseInt(etWorkout .getText().toString().trim());
                int s = Integer.parseInt(etSleep   .getText().toString().trim());

                // 3c) persist all
                prefs.edit()
                        .putString(Prefs.KEY_WATER_UNIT, chosenUnit)
                        .putInt   (Prefs.KEY_WATER_ML,   mlValue)
                        .putInt   (Prefs.KEY_CALORIES,     c)
                        .putInt   (Prefs.KEY_WORKOUT_MINS, w)
                        .putInt   (Prefs.KEY_SLEEP_HOURS,  s)
                        .apply();

                Toast.makeText(ctx, "Goals saved!", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            } catch (NumberFormatException e) {
                Toast.makeText(ctx, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            }
        });

        return v;
    }
}
