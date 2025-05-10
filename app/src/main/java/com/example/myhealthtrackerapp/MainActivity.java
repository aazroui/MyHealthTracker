package com.example.myhealthtrackerapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.myhealthtrackerapp.fragments.AddEntryFragment;
import com.example.myhealthtrackerapp.fragments.DashboardFragment;    // make sure this import is present
import com.example.myhealthtrackerapp.fragments.ProfileFragment;
import com.example.myhealthtrackerapp.fragments.SummaryFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 1) Load Dashboard by default instead of AddEntry
        loadFragment(new DashboardFragment());
        // 2) Also highlight the Dashboard icon in the bottom nav
        bottomNavigationView.setSelectedItemId(R.id.navigation_dashboard);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.navigation_dashboard) {
                selectedFragment = new DashboardFragment();
            } else if (id == R.id.navigation_add) {
                selectedFragment = new AddEntryFragment();
            } else if (id == R.id.navigation_summary) {
                selectedFragment = new SummaryFragment();
            } else if (id == R.id.navigation_profile) {
                selectedFragment = new ProfileFragment();
            }

            return loadFragment(selectedFragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment == null) return false;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        return true;
    }
}
