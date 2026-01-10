package com.orionverse.devplatform.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.orionverse.devplatform.R;
import com.orionverse.devplatform.models.User;
import com.orionverse.devplatform.utils.CloudinaryUtil;
import com.orionverse.devplatform.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.List;

public class EditProfileActivity extends AppCompatActivity {
    private ImageView profileImageView;
    private View changePhotoOverlay;
    private ProgressBar uploadProgressBar;
    private EditText usernameEditText, bioEditText, skillInputEditText;
    private ChipGroup skillsChipGroup;
    private Button addSkillButton, saveButton;
    private List<String> skills;
    
    private Uri selectedImageUri;
    private String currentProfileImageUrl = "";
    private String uploadedImageUrl = null;
    private boolean isUploading = false;
    
    // Activity Result Launchers
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        // Show preview
                        Glide.with(this)
                                .load(selectedImageUri)
                                .circleCrop()
                                .into(profileImageView);
                        
                        // Upload to Cloudinary
                        uploadImageToCloudinary();
                    }
                }
            }
    );
    
    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openImagePicker();
                } else {
                    Toast.makeText(this, "Permission required to select images", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initializeViews();
        loadUserData();
        setupListeners();
    }

    private void initializeViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        profileImageView = findViewById(R.id.profileImageView);
        changePhotoOverlay = findViewById(R.id.changePhotoOverlay);
        uploadProgressBar = findViewById(R.id.uploadProgressBar);
        usernameEditText = findViewById(R.id.usernameEditText);
        bioEditText = findViewById(R.id.bioEditText);
        skillInputEditText = findViewById(R.id.skillInputEditText);
        skillsChipGroup = findViewById(R.id.skillsChipGroup);
        addSkillButton = findViewById(R.id.addSkillButton);
        saveButton = findViewById(R.id.saveButton);

        skills = new ArrayList<>();
    }

    private void loadUserData() {
        String userId = FirebaseUtil.getCurrentUserId();
        if (userId == null) return;

        FirebaseUtil.getUsersCollection().document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        usernameEditText.setText(user.getUsername());
                        bioEditText.setText(user.getBio());

                        // Load profile image
                        currentProfileImageUrl = user.getProfileImageUrl();
                        if (currentProfileImageUrl != null && !currentProfileImageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(currentProfileImageUrl)
                                    .circleCrop()
                                    .placeholder(R.drawable.ic_profile_placeholder)
                                    .into(profileImageView);
                        }

                        if (user.getSkills() != null) {
                            skills = new ArrayList<>(user.getSkills());
                            displaySkills();
                        }
                    }
                });
    }

    private void setupListeners() {
        // Profile image click
        profileImageView.setOnClickListener(v -> checkPermissionAndPickImage());
        if (changePhotoOverlay != null) {
            changePhotoOverlay.setOnClickListener(v -> checkPermissionAndPickImage());
        }
        
        addSkillButton.setOnClickListener(v -> addSkill());
        saveButton.setOnClickListener(v -> saveProfile());
    }
    
    private void checkPermissionAndPickImage() {
        if (isUploading) {
            Toast.makeText(this, "Please wait for current upload to complete", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // For Android 13+ (API 33+), use READ_MEDIA_IMAGES
        // For older versions, use READ_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }
    
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }
    
    private void uploadImageToCloudinary() {
        if (selectedImageUri == null) return;
        
        String userId = FirebaseUtil.getCurrentUserId();
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        
        isUploading = true;
        showUploadProgress(true);
        
        CloudinaryUtil.uploadProfileImage(this, selectedImageUri, userId, new CloudinaryUtil.CloudinaryUploadCallback() {
            @Override
            public void onStart() {
                runOnUiThread(() -> {
                    Toast.makeText(EditProfileActivity.this, "Uploading image...", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onProgress(int progress) {
                runOnUiThread(() -> {
                    if (uploadProgressBar != null) {
                        uploadProgressBar.setProgress(progress);
                    }
                });
            }

            @Override
            public void onSuccess(String imageUrl) {
                runOnUiThread(() -> {
                    uploadedImageUrl = imageUrl;
                    isUploading = false;
                    showUploadProgress(false);
                    Toast.makeText(EditProfileActivity.this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    isUploading = false;
                    showUploadProgress(false);
                    Toast.makeText(EditProfileActivity.this, "Upload failed: " + error, Toast.LENGTH_LONG).show();
                    
                    // Reset to previous image
                    if (currentProfileImageUrl != null && !currentProfileImageUrl.isEmpty()) {
                        Glide.with(EditProfileActivity.this)
                                .load(currentProfileImageUrl)
                                .circleCrop()
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .into(profileImageView);
                    }
                });
            }
        });
    }
    
    private void showUploadProgress(boolean show) {
        if (uploadProgressBar != null) {
            uploadProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        saveButton.setEnabled(!show);
    }

    private void addSkill() {
        String skill = skillInputEditText.getText().toString().trim();
        if (skill.isEmpty()) {
            Toast.makeText(this, "Please enter a skill", Toast.LENGTH_SHORT).show();
            return;
        }

        if (skills.contains(skill)) {
            Toast.makeText(this, "Skill already added", Toast.LENGTH_SHORT).show();
            return;
        }

        skills.add(skill);
        displaySkills();
        skillInputEditText.setText("");
    }

    private void displaySkills() {
        skillsChipGroup.removeAllViews();
        for (String skill : skills) {
            Chip chip = new Chip(this);
            chip.setText(skill);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                skills.remove(skill);
                displaySkills();
            });
            skillsChipGroup.addView(chip);
        }
    }

    private void saveProfile() {
        if (isUploading) {
            Toast.makeText(this, "Please wait for image upload to complete", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String username = usernameEditText.getText().toString().trim();
        String bio = bioEditText.getText().toString().trim();

        if (username.isEmpty()) {
            usernameEditText.setError("Username is required");
            return;
        }

        String userId = FirebaseUtil.getCurrentUserId();
        if (userId == null) return;

        saveButton.setEnabled(false);
        
        // Determine which image URL to save
        String imageUrlToSave = uploadedImageUrl != null ? uploadedImageUrl : currentProfileImageUrl;
        if (imageUrlToSave == null) {
            imageUrlToSave = "";
        }

        FirebaseUtil.getUsersCollection().document(userId)
                .update(
                        "username", username,
                        "bio", bio,
                        "skills", skills,
                        "profileImageUrl", imageUrlToSave
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    saveButton.setEnabled(true);
                    Toast.makeText(this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
