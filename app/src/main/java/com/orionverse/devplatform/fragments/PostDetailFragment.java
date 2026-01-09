package com.orionverse.devplatform.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.orionverse.devplatform.R;
import com.orionverse.devplatform.activities.ApplicationsListActivity;
import com.orionverse.devplatform.models.Application;
import com.orionverse.devplatform.models.Notification;
import com.orionverse.devplatform.models.Post;
import com.orionverse.devplatform.models.User;
import com.orionverse.devplatform.utils.DateUtil;
import com.orionverse.devplatform.utils.FirebaseUtil;

public class PostDetailFragment extends Fragment {
    private TextView postTypeBadge, postTitle, authorName, postTime, postDescription;
    private MaterialButton applyButton, viewApplicationsButton, backButton;
    private String postId;
    private Post currentPost;

    public static PostDetailFragment newInstance(String postId) {
        PostDetailFragment fragment = new PostDetailFragment();
        Bundle args = new Bundle();
        args.putString("postId", postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_detail, container, false);

        if (getArguments() != null) {
            postId = getArguments().getString("postId");
        }

        if (postId == null) {
            navigateBack();
            return view;
        }

        initializeViews(view);
        loadPostDetails();

        return view;
    }

    private void initializeViews(View view) {
        postTypeBadge = view.findViewById(R.id.postTypeBadge);
        postTitle = view.findViewById(R.id.postTitle);
        authorName = view.findViewById(R.id.authorName);
        postTime = view.findViewById(R.id.postTime);
        postDescription = view.findViewById(R.id.postDescription);
        applyButton = view.findViewById(R.id.applyButton);
        viewApplicationsButton = view.findViewById(R.id.viewApplicationsButton);
        backButton = view.findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> navigateBack());
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
                    Toast.makeText(getContext(), "Failed to load post", Toast.LENGTH_SHORT).show();
                    navigateBack();
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
        Intent intent = new Intent(getContext(), ApplicationsListActivity.class);
        intent.putExtra("postId", postId);
        intent.putExtra("postTitle", currentPost.getTitle());
        startActivity(intent);
    }

    private void showApplyDialog() {
        String currentUserId = FirebaseUtil.getCurrentUserId();
        if (currentUserId == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if already applied
        FirebaseUtil.getApplicationsCollection()
                .whereEqualTo("postId", postId)
                .whereEqualTo("developerId", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Toast.makeText(getContext(), "You have already applied to this post", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    showApplicationDialog(currentUserId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error checking application status", Toast.LENGTH_SHORT).show();
                });
    }

    private void showApplicationDialog(String currentUserId) {
        Dialog dialog = new Dialog(requireContext());
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
        android.util.Log.d("PostDetailFragment", "=== STARTING APPLICATION SUBMISSION ===");
        android.util.Log.d("PostDetailFragment", "Current User ID: " + currentUserId);
        android.util.Log.d("PostDetailFragment", "Post ID: " + postId);
        android.util.Log.d("PostDetailFragment", "Post Owner ID: " + currentPost.getAuthorId());
        
        FirebaseUtil.getUsersCollection().document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user == null) {
                        Toast.makeText(getContext(), "Error loading user profile", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Application application = new Application(
                            postId,
                            currentPost.getTitle(),
                            currentUserId,
                            user.getUsername(),
                            proposal
                    );

                    DocumentReference appRef = FirebaseUtil.getApplicationsCollection().document();
                    application.setApplicationId(appRef.getId());

                    appRef.set(application)
                            .addOnSuccessListener(aVoid -> {
                                android.util.Log.d("PostDetailFragment", "Application saved to Firestore successfully");
                                
                                FirebaseUtil.getPostsCollection().document(postId)
                                        .update("applicationsCount", currentPost.getApplicationsCount() + 1)
                                        .addOnSuccessListener(aVoid1 -> {
                                            android.util.Log.d("PostDetailFragment", "Application count updated");
                                            Toast.makeText(getContext(), "Application submitted successfully!", Toast.LENGTH_SHORT).show();
                                            applyButton.setEnabled(false);
                                            applyButton.setText("Applied");
                                            
                                            android.util.Log.d("PostDetailFragment", ">>> NOW SENDING NOTIFICATION TO POST OWNER <<<");
                                            // Notify post owner about new application
                                            sendApplicationNotificationToPostOwner(user.getUsername(), currentPost);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to submit application: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading user profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateBack() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private void sendApplicationNotificationToPostOwner(String applicantName, Post post) {
        android.util.Log.d("PostDetailFragment", "Preparing to send notification to post owner: " + post.getAuthorId());
        
        Notification notification = new Notification(
                post.getAuthorId(),
                Notification.NotificationType.APPLICATION,
                "New Application",
                applicantName + " applied to your post: " + post.getTitle(),
                post.getPostId()
        );
        
        android.util.Log.d("PostDetailFragment", "Notification created - userId: " + notification.getUserId() 
                + ", type: " + notification.getType() 
                + ", title: " + notification.getTitle());
        
        FirebaseUtil.getNotificationsCollection()
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    android.util.Log.d("PostDetailFragment", "✅ Notification sent successfully to post owner! Doc ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("PostDetailFragment", "❌ Failed to send notification to post owner", e);
                });
    }
}
