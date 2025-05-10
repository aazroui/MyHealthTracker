package com.example.myhealthtrackerapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.myhealthtrackerapp.LoginActivity;
import com.example.myhealthtrackerapp.R;
import com.example.myhealthtrackerapp.utils.FirebaseUtil;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ProfileFragment extends Fragment {

    private ImageView ivProfileImage;
    private TextView  tvUserName, tvUserEmail;
    private MaterialButton btnLogout;
    private ActivityResultLauncher<String> pickImageLauncher;

    public ProfileFragment() { }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register the image-picker launcher
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        ivProfileImage.setImageURI(uri);
                        uploadProfileImage(uri);
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // UI refs
        ivProfileImage = view.findViewById(R.id.iv_profile_image);
        tvUserName     = view.findViewById(R.id.tv_user_name);
        tvUserEmail    = view.findViewById(R.id.tv_user_email);
        btnLogout      = view.findViewById(R.id.btn_logout);

        // Load current avatar URL from database (if any)
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(user.getUid());

            userRef.child("avatarUrl")
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        String url = snapshot.getValue(String.class);
                        if (url != null && !url.isEmpty()) {
                            Glide.with(this)
                                    .load(url)
                                    .circleCrop()
                                    .into(ivProfileImage);
                        }
                    });

            // Display name & email
            tvUserEmail.setText(user.getEmail());
            userRef.get().addOnSuccessListener(snapshot -> {
                String first = snapshot.child("firstName").getValue(String.class);
                String last  = snapshot.child("lastName").getValue(String.class);
                String full  = ((first != null) ? first : "") + " " +
                        ((last != null)  ? last  : "");
                tvUserName.setText(full.trim().isEmpty() ? "User" : full.trim());
            }).addOnFailureListener(e -> tvUserName.setText("User"));
        } else {
            tvUserEmail.setText("Not signed in");
            tvUserName.setText("User");
        }

        // Tapping avatar launches gallery picker
        ivProfileImage.setOnClickListener(v ->
                pickImageLauncher.launch("image/*")
        );

        // LOG OUT
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getContext(),
                    "Logged out successfully!",
                    Toast.LENGTH_SHORT).show();
            Intent i = new Intent(getActivity(), LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });

        // ABOUT → GoalsFragment
        view.findViewById(R.id.layout_about)
                .setOnClickListener(v ->
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container,
                                        new GoalsFragment())
                                .addToBackStack(null)
                                .commit()
                );

        return view;
    }

    /**
     * Uploads the picked image to Firebase Storage under "avatars/{uid}.jpg"
     * and saves the download URL back into the user's Realtime Database node.
     */
    private void uploadProfileImage(Uri uri) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("avatars/" + uid + ".jpg");

        ref.putFile(uri)
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                        "Upload failed: " + e.getMessage(),
                                        Toast.LENGTH_SHORT)
                                .show()
                )
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl()
                                .addOnSuccessListener(downloadUri -> {
                                    // save URL to Realtime Database
                                    FirebaseDatabase.getInstance()
                                            .getReference("Users")
                                            .child(uid)
                                            .child("avatarUrl")
                                            .setValue(downloadUri.toString());
                                })
                );
    }
}
