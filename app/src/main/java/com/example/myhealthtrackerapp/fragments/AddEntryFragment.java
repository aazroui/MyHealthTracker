package com.example.myhealthtrackerapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myhealthtrackerapp.R;
import com.example.myhealthtrackerapp.models.Meal;
import com.example.myhealthtrackerapp.utils.FirebaseUtil;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class AddEntryFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    public AddEntryFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_entry, container, false);

        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);
        viewPager.setAdapter(new AddEntryPagerAdapter(requireActivity()));

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, pos) -> {
                    switch (pos) {
                        case 0: tab.setText(getString(R.string.meals));   break;
                        case 1: tab.setText(getString(R.string.water));   break;
                        case 2: tab.setText(getString(R.string.workout)); break;
                        case 3: tab.setText(getString(R.string.sleep));   break;
                    }
                }
        ).attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) setupMealSubmission();
            }
            @Override public void onTabUnselected(TabLayout.Tab t) { }
            @Override public void onTabReselected(TabLayout.Tab t) { }
        });

        return view;
    }

    private void setupMealSubmission() {
        Fragment fragment = getChildFragmentManager()
                .findFragmentByTag("f" + viewPager.getCurrentItem());
        if (!(fragment instanceof MealsEntryFragment)) return;

        MealsEntryFragment mealsFrag = (MealsEntryFragment) fragment;
        mealsFrag.setOnSubmitListener((mealName, calories, protein, carbs, fats, sugars) -> {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // ← Changed these two lines:
            String key = FirebaseUtil.getMealsRef()       // no-arg
                    .child(uid)          // add the uid
                    .push()
                    .getKey();

            DatabaseReference ref = FirebaseUtil.getMealsRef()
                    .child(uid)
                    .child(key);

            Meal meal = new Meal(key, mealName, calories, protein, carbs, fats, sugars, uid);
            ref.setValue(meal);
        });
    }
}
