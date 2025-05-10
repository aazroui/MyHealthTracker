package com.example.myhealthtrackerapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Delay and then check auth status
        new Handler(Looper.getMainLooper()).postDelayed(this::checkAuthStatus, SPLASH_DELAY);
    }

    private void checkAuthStatus() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        Intent intent;
        if (currentUser != null) {
            // User is signed in, go to main activity
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            // No user is signed in, go to welcome activity
            intent = new Intent(SplashActivity.this, WelcomeActivity.class);
        }
        
        // Start the next activity and finish this one
        startActivity(intent);
        finish();
    }
} 