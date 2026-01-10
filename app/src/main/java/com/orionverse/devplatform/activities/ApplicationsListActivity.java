package com.orionverse.devplatform.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentReference;
import com.orionverse.devplatform.R;
import com.orionverse.devplatform.adapters.ApplicationAdapter;
import com.orionverse.devplatform.models.Application;
import com.orionverse.devplatform.models.Notification;
import com.orionverse.devplatform.models.PendingProject;
import com.orionverse.devplatform.models.Post;
import com.orionverse.devplatform.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.List;

public class ApplicationsListActivity extends AppCompatActivity {
    private static final String TAG = "ApplicationsListActivity";
    private RecyclerView applicationsRecyclerView;
    private ApplicationAdapter adapter;
    private ProgressBar progressBar;
    private View emptyState;
    private String postId;
    private String postTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applications_list);

        // Get post ID from intent
        postId = getIntent().getStringExtra("postId");
        postTitle = getIntent().getStringExtra("postTitle");

        if (postId == null) {
            Toast.makeText(this, "Error: Post ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (postTitle != null) {
            toolbar.setTitle("Applications: " + postTitle);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        initializeViews();
        setupRecyclerView();
        loadApplications();
    }

    private void initializeViews() {
        applicationsRecyclerView = findViewById(R.id.applicationsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyState = findViewById(R.id.emptyState);
    }

    private void setupRecyclerView() {
        adapter = new ApplicationAdapter(this);
        applicationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        applicationsRecyclerView.setAdapter(adapter);

        adapter.setOnApplicationActionListener(new ApplicationAdapter.OnApplicationActionListener() {
            @Override
            public void onAccept(Application application) {
                showConfirmDialog("Accept Application", 
                    "Are you sure you want to accept " + application.getDeveloperName() + "'s application?",
                    () -> updateApplicationStatus(application, "ACCEPTED"));
            }

            @Override
            public void onReject(Application application) {
                showConfirmDialog("Reject Application", 
                    "Are you sure you want to reject " + application.getDeveloperName() + "'s application?",
                    () -> updateApplicationStatus(application, "REJECTED"));
            }

            @Override
            public void onViewDetails(Application application) {
                showApplicationDetailsDialog(application);
            }
        });
    }

    private void loadApplications() {
        progressBar.setVisibility(View.VISIBLE);
        applicationsRecyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);

        FirebaseUtil.getApplicationsCollection()
                .whereEqualTo("postId", postId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);

                    List<Application> applications = new ArrayList<>();
                    querySnapshot.forEach(doc -> {
                        Application app = doc.toObject(Application.class);
                        app.setApplicationId(doc.getId());
                        applications.add(app);
                    });

                    if (applications.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                    } else {
                        applicationsRecyclerView.setVisibility(View.VISIBLE);
                        adapter.setApplications(applications);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load applications: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
    }

    private void updateApplicationStatus(Application application, String newStatus) {
        FirebaseUtil.getApplicationsCollection()
                .document(application.getApplicationId())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    // Send notification to applicant
                    sendNotificationToApplicant(application, newStatus);
                    
                    // If accepted, create pending project
                    if (newStatus.equals("ACCEPTED")) {
                        createPendingProject(application);
                    }
                    
                    Toast.makeText(this, "Application " + newStatus.toLowerCase(), 
                        Toast.LENGTH_SHORT).show();
                    loadApplications(); // Reload to refresh UI
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update status: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
    }

    private void sendNotificationToApplicant(Application application, String status) {
        Log.d(TAG, "Sending notification to developer: " + application.getDeveloperId());
        
        Notification.NotificationType type = status.equals("ACCEPTED") 
                ? Notification.NotificationType.APPLICATION_ACCEPTED
                : Notification.NotificationType.APPLICATION_REJECTED;
        
        String title = status.equals("ACCEPTED") 
                ? "Application Accepted!" 
                : "Application Rejected";
        
        String message = status.equals("ACCEPTED")
                ? "Congratulations! Your application for \"" + application.getPostTitle() + "\" has been accepted."
                : "Your application for \"" + application.getPostTitle() + "\" has been rejected.";
        
        Notification notification = new Notification(
                application.getDeveloperId(),
                type,
                title,
                message,
                application.getApplicationId()
        );
        
        DocumentReference notifRef = FirebaseUtil.getNotificationsCollection().document();
        notification.setNotificationId(notifRef.getId());
        
        Log.d(TAG, "Saving notification with ID: " + notifRef.getId());
        
        notifRef.set(notification)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Notification sent successfully to " + application.getDeveloperName());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to send notification: " + e.getMessage(), e);
                });
    }

    private void createPendingProject(Application application) {
        Log.d(TAG, "Creating pending project for post: " + application.getPostId());
        
        // First, fetch the full post details
        FirebaseUtil.getPostsCollection()
                .document(application.getPostId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Post post = documentSnapshot.toObject(Post.class);
                    if (post == null) {
                        Log.e(TAG, "Post not found: " + application.getPostId());
                        return;
                    }
                    
                    PendingProject project = new PendingProject(
                            application.getPostId(),
                            application.getPostTitle(),
                            post.getDescription(),
                            post.getAuthorId(),
                            post.getAuthorName(),
                            application.getDeveloperId(),
                            application.getDeveloperName(),
                            application.getApplicationId()
                    );
                    
                    DocumentReference projectRef = FirebaseUtil.getPendingProjectsCollection().document();
                    project.setProjectId(projectRef.getId());
                    
                    Log.d(TAG, "Saving pending project with ID: " + projectRef.getId());
                    
                    projectRef.set(project)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Pending project created successfully");
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to create pending project: " + e.getMessage(), e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch post details: " + e.getMessage(), e);
                });
    }

    private void showConfirmDialog(String title, String message, Runnable onConfirm) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm);
        dialog.setCancelable(true);

        TextView titleView = dialog.findViewById(R.id.dialogTitle);
        TextView messageView = dialog.findViewById(R.id.dialogMessage);

        titleView.setText(title);
        messageView.setText(message);

        dialog.findViewById(R.id.cancelButton).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.confirmButton).setOnClickListener(v -> {
            onConfirm.run();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showApplicationDetailsDialog(Application application) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_application_details);
        dialog.setCancelable(true);

        TextView developerName = dialog.findViewById(R.id.developerName);
        TextView proposal = dialog.findViewById(R.id.proposalText);
        TextView status = dialog.findViewById(R.id.statusText);

        developerName.setText(application.getDeveloperName());
        proposal.setText(application.getProposal());
        status.setText("Status: " + application.getStatus());

        dialog.findViewById(R.id.closeButton).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
