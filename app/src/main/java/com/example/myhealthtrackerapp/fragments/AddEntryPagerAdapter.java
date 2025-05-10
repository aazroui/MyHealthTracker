package com.example.myhealthtrackerapp.fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AddEntryPagerAdapter extends FragmentStateAdapter {

    public AddEntryPagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    @NonNull @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new MealFragment();    // back to your original
            case 1: return new WaterFragment();
            case 2: return new WorkoutFragment();
            case 3: return new SleepFragment();
            default: return new MealFragment();
        }
    }

    @Override public int getItemCount() { return 4; }
}
