package com.example.myhealthtrackerapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myhealthtrackerapp.databinding.ActivitySignUpBinding;
import com.example.myhealthtrackerapp.models.User;
import com.example.myhealthtrackerapp.utils.FirebaseUtil;
import com.example.myhealthtrackerapp.utils.ValidationUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Set up button click listeners
        binding.btnSignUp.setOnClickListener(v -> signUp());

        binding.tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void signUp() {
        // Get input values
        String firstName = binding.etFirstName.getText().toString().trim();
        String lastName = binding.etLastName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString();
        boolean termsAccepted = binding.cbTerms.isChecked();

        // Validate input
        if (!validateInput(firstName, lastName, email, password, termsAccepted)) {
            return;
        }

        // Show loading
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSignUp.setEnabled(false);

        // Create user in Firebase
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign up success
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            DatabaseReference userRef = FirebaseUtil.getUsersRef().child(userId);

                            // Save user details (you already have a User class, but we'll also push fullName here for convenience)
                            HashMap<String, Object> userData = new HashMap<>();
                            userData.put("userId", userId);
                            userData.put("firstName", firstName);
                            userData.put("lastName", lastName);
                            userData.put("email", email);
                            userData.put("fullName", firstName + " " + lastName);

                            userRef.setValue(userData).addOnCompleteListener(profileTask -> {
                                binding.progressBar.setVisibility(View.GONE);
                                if (profileTask.isSuccessful()) {
                                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(SignUpActivity.this,
                                            "Failed to save profile: " + profileTask.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                    binding.btnSignUp.setEnabled(true);
                                }
                            });
                        }
                    } else {
                        // Sign up failed
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnSignUp.setEnabled(true);
                        Toast.makeText(SignUpActivity.this,
                                "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateInput(String firstName, String lastName, String email, String password, boolean termsAccepted) {
        boolean isValid = true;

        if (!ValidationUtil.isNotEmpty(firstName)) {
            binding.tilFirstName.setError(getString(R.string.field_required));
            isValid = false;
        } else {
            binding.tilFirstName.setError(null);
        }

        if (!ValidationUtil.isNotEmpty(lastName)) {
            binding.tilLastName.setError(getString(R.string.field_required));
            isValid = false;
        } else {
            binding.tilLastName.setError(null);
        }

        if (!ValidationUtil.isValidEmail(email)) {
            binding.tilEmail.setError(getString(R.string.email_invalid));
            isValid = false;
        } else {
            binding.tilEmail.setError(null);
        }

        if (!ValidationUtil.isValidPassword(password)) {
            binding.tilPassword.setError(getString(R.string.password_too_short));
            isValid = false;
        } else {
            binding.tilPassword.setError(null);
        }

        if (!termsAccepted) {
            Toast.makeText(this, getString(R.string.terms_not_accepted), Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }
}
