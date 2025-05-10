package com.example.myhealthtrackerapp.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myhealthtrackerapp.R;
import com.example.myhealthtrackerapp.utils.FirebaseUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;

public class SleepFragment extends Fragment {

    private EditText etSleepHours;
    private TextView tvSliderSleepValue;
    private Slider sliderSleepHours;
    private RadioGroup rgSleepQuality;
    private MaterialButton btnAddSleep;

    public SleepFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_sleep, container, false);

        etSleepHours = view.findViewById(R.id.et_sleep_hours);
        sliderSleepHours = view.findViewById(R.id.slider_sleep_hours);
        tvSliderSleepValue = view.findViewById(R.id.tv_slider_sleep_value);
        rgSleepQuality = view.findViewById(R.id.radio_sleep_quality);
        btnAddSleep = view.findViewById(R.id.btn_add_sleep);

        // Update EditText and label when slider changes
        sliderSleepHours.addOnChangeListener((slider, value, fromUser) -> {
            float rounded = Math.round(value * 2f) / 2f;  // Round to nearest 0.5
            etSleepHours.setText(String.valueOf(rounded));
            tvSliderSleepValue.setText((int) rounded + " hours");
        });

        btnAddSleep.setOnClickListener(v -> saveSleep());

        return view;
    }

    private void saveSleep() {
        String hoursStr = etSleepHours.getText().toString().trim();
        if (TextUtils.isEmpty(hoursStr)) {
            Toast.makeText(getContext(), "Please enter sleep hours", Toast.LENGTH_SHORT).show();
            return;
        }

        int selected = rgSleepQuality.getCheckedRadioButtonId();
        String quality;

        if (selected == R.id.rb_poor) {
            quality = "Poor";
        } else if (selected == R.id.rb_fair) {
            quality = "Fair";
        } else if (selected == R.id.rb_good) {
            quality = "Good";
        } else if (selected == R.id.rb_excellent) {
            quality = "Excellent";
        } else {
            Toast.makeText(getContext(), "Select sleep quality", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> data = new HashMap<>();
        data.put("hours", hoursStr);
        data.put("quality", quality);
        data.put("timestamp", System.currentTimeMillis());

        DatabaseReference ref = FirebaseUtil.getSleepRef();
        if (ref != null) {
            ref.push().setValue(data)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(getContext(), "Sleep entry saved!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}
