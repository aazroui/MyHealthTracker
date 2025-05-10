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

public class WaterFragment extends Fragment {

    private EditText etWaterAmount;
    private RadioGroup rgUnit;
    private Slider sliderWater;
    private TextView tvSliderValue, tvEquivalent;
    private MaterialButton btnAddWater;

    public WaterFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_water, container, false);

        etWaterAmount = view.findViewById(R.id.et_water_amount);
        rgUnit = view.findViewById(R.id.radio_water_unit);
        sliderWater = view.findViewById(R.id.slider_water);
        tvSliderValue = view.findViewById(R.id.tv_slider_value);
        tvEquivalent = view.findViewById(R.id.tv_water_equivalent);
        btnAddWater = view.findViewById(R.id.btn_add_water);

        // Slider listener
        sliderWater.addOnChangeListener((slider, value, fromUser) -> {
            int cups = (int) value;
            String unit = rgUnit.getCheckedRadioButtonId() == R.id.rb_cups ? "cups" : "ml";
            float displayValue = unit.equals("cups") ? cups : cups * 240f;

            // Show value in UI
            etWaterAmount.setText(String.valueOf(displayValue));
            tvSliderValue.setText(cups + " cups");
            tvEquivalent.setText("(" + (int)(cups * 240f) + " ml)");
        });

        // Unit change listener (update display when switching units)
        rgUnit.setOnCheckedChangeListener((group, checkedId) -> {
            float value = sliderWater.getValue(); // cups
            if (checkedId == R.id.rb_cups) {
                etWaterAmount.setText(String.valueOf((int) value));
                tvSliderValue.setText((int)value + " cups");
                tvEquivalent.setText("(" + (int)(value * 240f) + " ml)");
            } else {
                float ml = value * 240f;
                etWaterAmount.setText(String.valueOf((int) ml));
                tvSliderValue.setText((int)value + " cups");
                tvEquivalent.setText("(" + (int) ml + " ml)");
            }
        });

        btnAddWater.setOnClickListener(v -> saveWater());

        return view;
    }

    private void saveWater() {
        String amountStr = etWaterAmount.getText().toString().trim();
        if (TextUtils.isEmpty(amountStr)) {
            Toast.makeText(getContext(), "Please enter water amount", Toast.LENGTH_SHORT).show();
            return;
        }

        String unit = rgUnit.getCheckedRadioButtonId() == R.id.rb_cups ? "cups" : "ml";

        HashMap<String, Object> data = new HashMap<>();
        data.put("amount", amountStr);
        data.put("unit", unit);
        data.put("timestamp", System.currentTimeMillis());

        DatabaseReference ref = FirebaseUtil.getWaterRef();
        if (ref != null) {
            ref.push().setValue(data)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(getContext(), "Water added!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}