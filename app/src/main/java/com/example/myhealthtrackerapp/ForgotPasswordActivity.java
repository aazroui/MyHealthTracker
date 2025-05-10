package com.example.myhealthtrackerapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myhealthtrackerapp.databinding.ActivityForgotPasswordBinding;
import com.example.myhealthtrackerapp.utils.ValidationUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Set up click listeners
        binding.btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

        binding.tvBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to previous activity
            }
        });
    }

    private void resetPassword() {
        String email = binding.etEmail.getText().toString().trim();

        // Validate the email
        if (!ValidationUtil.isValidEmail(email)) {
            binding.tilEmail.setError(getString(R.string.email_invalid));
            return;
        }

        // Show progress bar
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnReset.setEnabled(false);

        // Send reset email
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnReset.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPasswordActivity.this, 
                                    getString(R.string.reset_sent), 
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ForgotPasswordActivity.this, 
                                    "Error: " + task.getException().getMessage(), 
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
} 