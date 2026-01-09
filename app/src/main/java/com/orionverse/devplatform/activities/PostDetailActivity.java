package com.orionverse.devplatform.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.orionverse.devplatform.R;
import com.orionverse.devplatform.models.Application;
import com.orionverse.devplatform.models.Post;
import com.orionverse.devplatform.models.User;
import com.orionverse.devplatform.utils.DateUtil;
import com.orionverse.devplatform.utils.FirebaseUtil;

public class PostDetailActivity extends AppCompatActivity {
    private TextView postTypeBadge, postTitle, authorName, postTime, postDescription;
    private MaterialButton applyButton, viewApplicationsButton;
    private String postId;
    private Post currentPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // Setup toolbar with back button
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        postId = getIntent().getStringExtra("postId");
        if (postId == null) {
            finish();
            return;
        }

        initializeViews();
        loadPostDetails();
    }

    private void initializeViews() {
        postTypeBadge = findViewById(R.id.postTypeBadge);
        postTitle = findViewById(R.id.postTitle);
        authorName = findViewById(R.id.authorName);
        postTime = findViewById(R.id.postTime);
        postDescription = findViewById(R.id.postDescription);
        applyButton = findViewById(R.id.applyButton);
        viewApplicationsButton = findViewById(R.id.viewApplicationsButton);
    }

    private void loadPostDetails() {
        FirebaseUtil.getPostsCollection().document(postId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    currentPost = documentSnapshot.toObject(Post.class);
                    if (currentPost != null) {
                        currentPost.setPostId(documentSnapshot.getId());
                        displayPost();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load post", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayPost() {
        postTitle.setText(currentPost.getTitle());
        postDescription.setText(currentPost.getDescription());
        authorName.setText(currentPost.getAuthorName());
        postTime.setText(DateUtil.getRelativeTime(currentPost.getCreatedAt()));

        // Set badge
        String type = currentPost.getPostType();
        postTypeBadge.setText(type);
        GradientDrawable background = (GradientDrawable) postTypeBadge.getBackground();
        if (type.equals("PROBLEM")) {
            background.setColor(getResources().getColor(R.color.problem_color));
        } else if (type.equals("SOLUTION")) {
            background.setColor(getResources().getColor(R.color.solution_color));
        } else {
            background.setColor(getResources().getColor(R.color.general_color));
        }

        // Show appropriate buttons
        String currentUserId = FirebaseUtil.getCurrentUserId();
        boolean isAuthor = currentPost.getAuthorId().equals(currentUserId);
        boolean isProblem = type.equals("PROBLEM");

        if (isAuthor && isProblem) {
            viewApplicationsButton.setVisibility(View.VISIBLE);
            viewApplicationsButton.setOnClickListener(v -> openApplicationsList());
        } else if (!isAuthor && isProblem) {
            applyButton.setVisibility(View.VISIBLE);
            applyButton.setOnClickListener(v -> showApplyDialog());
        }
    }

    private void openApplicationsList() {
        Intent intent = new Intent(this, ApplicationsListActivity.class);
        intent.putExtra("postId", postId);
        intent.putExtra("postTitle", currentPost.getTitle());
        startActivity(intent);
    }

    private void showApplyDialog() {
        // Get current user info first
        String currentUserId = FirebaseUtil.getCurrentUserId();
        if (currentUserId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if already applied
        FirebaseUtil.getApplicationsCollection()
                .whereEqualTo("postId", postId)
                .whereEqualTo("developerId", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Toast.makeText(this, "You have already applied to this post", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Show dialog
                    showApplicationDialog(currentUserId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking application status", Toast.LENGTH_SHORT).show();
                });
    }

    private void showApplicationDialog(String currentUserId) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_apply);
        dialog.setCancelable(true);

        EditText proposalEditText = dialog.findViewById(R.id.proposalEditText);
        EditText experienceEditText = dialog.findViewById(R.id.experienceEditText);
        MaterialButton submitButton = dialog.findViewById(R.id.submitButton);
        MaterialButton cancelButton = dialog.findViewById(R.id.cancelButton);

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        submitButton.setOnClickListener(v -> {
            String proposal = proposalEditText.getText().toString().trim();
            String experience = experienceEditText.getText().toString().trim();

            if (proposal.isEmpty()) {
                proposalEditText.setError("Proposal is required");
                return;
            }

            String fullProposal = "Proposal: " + proposal + "\n\nExperience: " + experience;
            submitApplication(currentUserId, fullProposal);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void submitApplication(String currentUserId, String proposal) {
        // Get current user's name
        FirebaseUtil.getUsersCollection().document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user == null) {
                        Toast.makeText(this, "Error loading user profile", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Create application
                    Application application = new Application(
                            postId,
                            currentPost.getTitle(),
                            currentUserId,
                            user.getUsername(),
                            proposal
                    );

                    // Save to Firestore
                    DocumentReference appRef = FirebaseUtil.getApplicationsCollection().document();
                    application.setApplicationId(appRef.getId());

                    appRef.set(application)
                            .addOnSuccessListener(aVoid -> {
                                // Update post's application count
                                FirebaseUtil.getPostsCollection().document(postId)
                                        .update("applicationsCount", currentPost.getApplicationsCount() + 1)
                                        .addOnSuccessListener(aVoid1 -> {
                                            Toast.makeText(this, "Application submitted successfully!", Toast.LENGTH_SHORT).show();
                                            applyButton.setEnabled(false);
                                            applyButton.setText("Applied");
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to submit application: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading user profile", Toast.LENGTH_SHORT).show();
                });
    }
}
