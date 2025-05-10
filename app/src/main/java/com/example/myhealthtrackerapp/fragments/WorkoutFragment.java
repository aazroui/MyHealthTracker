package com.example.myhealthtrackerapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myhealthtrackerapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class WorkoutFragment extends Fragment {

    private AutoCompleteTextView spWorkoutType;
    private EditText etDuration;
    private MaterialButton btnAddWorkout;
    private DatabaseReference workoutRef;

    public WorkoutFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_workout, container, false);

        spWorkoutType = view.findViewById(R.id.dropdown_workout_type);
        etDuration    = view.findViewById(R.id.et_workout_duration);
        btnAddWorkout = view.findViewById(R.id.btn_add_workout);

        // Set up workout type dropdown
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.workout_types,
                android.R.layout.simple_dropdown_item_1line
        );
        spWorkoutType.setAdapter(adapter);

        // Firebase reference
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        workoutRef = FirebaseDatabase.getInstance()
                .getReference("workout")
                .child(userId);

        btnAddWorkout.setOnClickListener(v -> saveWorkout());
        return view;
    }

    private void saveWorkout() {
        String type = spWorkoutType.getText().toString().trim();
        String durationStr = etDuration.getText().toString().trim();

        if (type.isEmpty() || durationStr.isEmpty()) {
            Toast.makeText(getContext(),
                    "Please fill in all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int duration;
        try {
            duration = Integer.parseInt(durationStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(),
                    "Invalid duration",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> workout = new HashMap<>();
        // ◀ key changed here to match your model's 'workoutType' field
        workout.put("workoutType", type);
        workout.put("duration",    duration);
        workout.put("timestamp",   System.currentTimeMillis());

        workoutRef.push()
                .setValue(workout)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(),
                            "Workout logged!",
                            Toast.LENGTH_SHORT).show();
                    spWorkoutType.setText("");
                    etDuration.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),
                            "Error saving workout: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
