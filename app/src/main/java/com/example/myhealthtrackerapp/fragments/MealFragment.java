package com.example.myhealthtrackerapp.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myhealthtrackerapp.R;
import com.example.myhealthtrackerapp.models.Meal;
import com.example.myhealthtrackerapp.utils.FirebaseUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class MealFragment extends Fragment {

    private EditText etName, etCalories, etProtein, etCarbs, etFats, etSugars;
    private Button btnAddMeal;

    public MealFragment() { /* Required empty constructor */ }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_meal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etName     = view.findViewById(R.id.et_meal_name);
        etCalories = view.findViewById(R.id.et_calories);
        etProtein  = view.findViewById(R.id.et_protein);
        etCarbs    = view.findViewById(R.id.et_carbs);
        etFats     = view.findViewById(R.id.et_fats);
        etSugars   = view.findViewById(R.id.et_sugars);
        btnAddMeal = view.findViewById(R.id.btn_add_meal);

        btnAddMeal.setOnClickListener(v -> submitMeal());
    }

    private void submitMeal() {
        String name  = etName.getText().toString().trim();
        String calS  = etCalories.getText().toString().trim();
        String protS = etProtein.getText().toString().trim();
        String carbS = etCarbs.getText().toString().trim();
        String fatsS = etFats.getText().toString().trim();
        String sugS  = etSugars.getText().toString().trim();

        // all fields required
        if (TextUtils.isEmpty(name)
                || TextUtils.isEmpty(calS)
                || TextUtils.isEmpty(protS)
                || TextUtils.isEmpty(carbS)
                || TextUtils.isEmpty(fatsS)
                || TextUtils.isEmpty(sugS)) {
            Toast.makeText(getContext(),
                    "Please fill in all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // parse inputs
        int  calories = Integer.parseInt(calS);
        long protein  = Long.parseLong(protS);
        long carbs    = Long.parseLong(carbS);
        long fats     = Long.parseLong(fatsS);
        long sugars   = Long.parseLong(sugS);

        // get reference to /Users/{uid}/Meals
        DatabaseReference mealsRef = FirebaseUtil.getMealsRef();
        if (mealsRef == null) {
            Toast.makeText(getContext(),
                    "User not authenticated",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // generate new push-key
        String key = mealsRef.push().getKey();
        if (key == null) {
            Toast.makeText(getContext(),
                    "Could not generate entry ID",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // build model (timestamp is set inside Meal constructor)
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Meal meal = new Meal(key, name, calories, protein, carbs, fats, sugars, uid);

        // write it once to /Users/{uid}/Meals/{key}
        mealsRef.child(key)
                .setValue(meal)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(),
                            "Meal added!",
                            Toast.LENGTH_SHORT).show();
                    clearForm();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Error saving meal: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    private void clearForm() {
        etName.setText("");
        etCalories.setText("");
        etProtein.setText("");
        etCarbs.setText("");
        etFats.setText("");
        etSugars.setText("");
    }
}
