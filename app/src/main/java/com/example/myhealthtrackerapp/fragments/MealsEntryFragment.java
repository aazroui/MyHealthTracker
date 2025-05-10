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

public class MealsEntryFragment extends Fragment {

    private OnSubmitListener submitListener;

    public interface OnSubmitListener {
        void onSubmit(String mealName,
                      int calories,
                      long protein,
                      long carbs,
                      long fats,
                      long sugars);
    }

    /** Parent can call this to receive form submissions */
    public void setOnSubmitListener(OnSubmitListener listener) {
        this.submitListener = listener;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meals_entry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText etName     = view.findViewById(R.id.etMealName);
        EditText etCalories = view.findViewById(R.id.etMealCalories);
        EditText etProtein  = view.findViewById(R.id.etMealProtein);
        EditText etCarbs    = view.findViewById(R.id.etMealCarbs);
        EditText etFats     = view.findViewById(R.id.etMealFats);
        EditText etSugars   = view.findViewById(R.id.etMealSugars);
        Button  btnSubmit   = view.findViewById(R.id.btnMealSubmit);

        btnSubmit.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String cal  = etCalories.getText().toString().trim();
            String prot = etProtein.getText().toString().trim();
            String carb = etCarbs.getText().toString().trim();
            String fat  = etFats.getText().toString().trim();
            String sug  = etSugars.getText().toString().trim();

            if (TextUtils.isEmpty(name)
                    || TextUtils.isEmpty(cal)
                    || TextUtils.isEmpty(prot)
                    || TextUtils.isEmpty(carb)
                    || TextUtils.isEmpty(fat)
                    || TextUtils.isEmpty(sug)) {
                Toast.makeText(getContext(),
                                "Please fill in all fields",
                                Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            // Notify parent
            if (submitListener != null) {
                submitListener.onSubmit(
                        name,
                        Integer.parseInt(cal),
                        Long.parseLong(prot),
                        Long.parseLong(carb),
                        Long.parseLong(fat),
                        Long.parseLong(sug)
                );
            }
        });
    }
}
