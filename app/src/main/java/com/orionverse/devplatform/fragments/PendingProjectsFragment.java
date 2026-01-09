package com.orionverse.devplatform.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.orionverse.devplatform.R;
import com.orionverse.devplatform.adapters.PendingProjectAdapter;
import com.orionverse.devplatform.models.PendingProject;
import com.orionverse.devplatform.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.List;

public class PendingProjectsFragment extends Fragment {
    private RecyclerView projectsRecyclerView;
    private PendingProjectAdapter adapter;
    private ProgressBar progressBar;
    private View emptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending_projects, container, false);

        initializeViews(view);
        setupRecyclerView();
        loadProjects();

        return view;
    }

    private void initializeViews(View view) {
        projectsRecyclerView = view.findViewById(R.id.projectsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyState = view.findViewById(R.id.emptyState);
    }

    private void setupRecyclerView() {
        adapter = new PendingProjectAdapter(getContext());
        projectsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        projectsRecyclerView.setAdapter(adapter);

        adapter.setOnProjectActionListener(new PendingProjectAdapter.OnProjectActionListener() {
            @Override
            public void onViewDetails(PendingProject project) {
                showProjectDetailsDialog(project);
            }

            @Override
            public void onMarkCompleted(PendingProject project) {
                showCompletionConfirmDialog(project);
            }
        });
    }

    private void loadProjects() {
        String currentUserId = FirebaseUtil.getCurrentUserId();
        if (currentUserId == null) return;

        progressBar.setVisibility(View.VISIBLE);
        projectsRecyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);

        // Load projects where user is either developer or author
        FirebaseUtil.getPendingProjectsCollection()
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);

                    List<PendingProject> projects = new ArrayList<>();
                    querySnapshot.forEach(doc -> {
                        PendingProject project = doc.toObject(PendingProject.class);
                        project.setProjectId(doc.getId());
                        
                        // Include if user is developer or author
                        if (project.getDeveloperId().equals(currentUserId) || 
                            project.getAuthorId().equals(currentUserId)) {
                            projects.add(project);
                        }
                    });

                    if (projects.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                    } else {
                        projectsRecyclerView.setVisibility(View.VISIBLE);
                        adapter.setProjects(projects);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to load projects", Toast.LENGTH_SHORT).show();
                });
    }

    private void showCompletionConfirmDialog(PendingProject project) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm);
        dialog.setCancelable(true);

        TextView titleView = dialog.findViewById(R.id.dialogTitle);
        TextView messageView = dialog.findViewById(R.id.dialogMessage);

        titleView.setText("Complete Project");
        messageView.setText("Are you sure you want to mark \"" + project.getPostTitle() + "\" as completed?");

        dialog.findViewById(R.id.cancelButton).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.confirmButton).setOnClickListener(v -> {
            markAsCompleted(project);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void markAsCompleted(PendingProject project) {
        FirebaseUtil.getPendingProjectsCollection()
                .document(project.getProjectId())
                .update("status", "COMPLETED", "completedAt", Timestamp.now())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Project marked as completed!", Toast.LENGTH_SHORT).show();
                    loadProjects();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update project", Toast.LENGTH_SHORT).show();
                });
    }

    private void showProjectDetailsDialog(PendingProject project) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_project_details);
        dialog.setCancelable(true);

        TextView projectTitle = dialog.findViewById(R.id.projectTitle);
        TextView projectDescription = dialog.findViewById(R.id.projectDescription);
        TextView authorName = dialog.findViewById(R.id.authorName);
        TextView developerName = dialog.findViewById(R.id.developerName);
        TextView status = dialog.findViewById(R.id.statusText);

        projectTitle.setText(project.getPostTitle());
        projectDescription.setText(project.getPostDescription());
        authorName.setText("Client: " + project.getAuthorName());
        developerName.setText("Developer: " + project.getDeveloperName());
        status.setText("Status: " + project.getStatus());

        dialog.findViewById(R.id.closeButton).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            loadProjects();
        }
    }
}
