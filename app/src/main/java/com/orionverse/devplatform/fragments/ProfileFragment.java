package com.orionverse.devplatform.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.orionverse.devplatform.R;
import com.orionverse.devplatform.activities.EditProfileActivity;
import com.orionverse.devplatform.activities.LoginActivity;
import com.orionverse.devplatform.adapters.PostAdapter;
import com.orionverse.devplatform.models.Post;
import com.orionverse.devplatform.models.User;
import com.orionverse.devplatform.utils.FirebaseUtil;
import com.orionverse.devplatform.utils.ThemeManager;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {
    private ImageView profileImageView;
    private TextView usernameTextView, bioTextView, ratingTextView, ratingsCountTextView, skillsTextView;
    private MaterialButton editProfileButton, logoutButton, themeButton;
    private RecyclerView myPostsRecyclerView;
    private PostAdapter postAdapter;
    private AlertDialog themeDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initializeViews(view);
        setupRecyclerView();
        loadUserProfile();
        loadUserPosts();
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        profileImageView = view.findViewById(R.id.profileImageView);
        usernameTextView = view.findViewById(R.id.usernameTextView);
        bioTextView = view.findViewById(R.id.bioTextView);
        ratingTextView = view.findViewById(R.id.ratingTextView);
        ratingsCountTextView = view.findViewById(R.id.ratingsCountTextView);
        skillsTextView = view.findViewById(R.id.skillsTextView);
        editProfileButton = view.findViewById(R.id.editProfileButton);
        themeButton = view.findViewById(R.id.themeButton);
        logoutButton = view.findViewById(R.id.logoutButton);
        myPostsRecyclerView = view.findViewById(R.id.myPostsRecyclerView);
        
        // Update theme button text
        updateThemeButtonText();
    }

    private void setupRecyclerView() {
        postAdapter = new PostAdapter(getContext());
        myPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        myPostsRecyclerView.setAdapter(postAdapter);
    }

    private void loadUserProfile() {
        String userId = FirebaseUtil.getCurrentUserId();
        if (userId == null)
            return;

        FirebaseUtil.getUsersCollection().document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        displayUserInfo(user);
                    }
                });
    }

    private void displayUserInfo(User user) {
        usernameTextView.setText(user.getUsername());
        bioTextView.setText(user.getBio().isEmpty() ? "No bio yet" : user.getBio());

        // Rating
        ratingTextView.setText(String.format("%.1f", user.getAverageRating()));
        ratingsCountTextView.setText("(" + user.getTotalRatings() + " " + getString(R.string.ratings) + ")");

        // Skills
        if (user.getSkills() != null && !user.getSkills().isEmpty()) {
            skillsTextView.setText(String.join(", ", user.getSkills()));
        } else {
            skillsTextView.setText("No skills added yet");
        }
    }

    private void loadUserPosts() {
        String userId = FirebaseUtil.getCurrentUserId();
        if (userId == null)
            return;

        FirebaseUtil.getPostsCollection()
                .whereEqualTo("authorId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Post> posts = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Post post = document.toObject(Post.class);
                        post.setPostId(document.getId());
                        posts.add(post);
                    }
                    postAdapter.setPosts(posts);
                })
                .addOnFailureListener(e -> {
                    // Firestore index might be missing - this is expected
                    // User can still use the app, just won't see their posts in profile
                    android.util.Log.w("ProfileFragment", "Failed to load user posts: " + e.getMessage());
                });
    }

    private void setupListeners() {
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        themeButton.setOnClickListener(v -> showThemeDialog());

        logoutButton.setOnClickListener(v -> {
            FirebaseUtil.getAuth().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    private void showThemeDialog() {
        String[] themes = {
            getString(R.string.light_theme),
            getString(R.string.dark_theme),
            getString(R.string.system_default)
        };
        
        int currentTheme = ThemeManager.getSavedTheme(requireContext());

        themeDialog = new AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.theme))
            .setSingleChoiceItems(themes, currentTheme, (dialog, which) -> {
                ThemeManager.saveTheme(requireContext(), which);
                updateThemeButtonText();
                dialog.dismiss();
            })
            .setNegativeButton(getString(R.string.cancel), null)
            .create();
        
        themeDialog.show();
    }

    private void updateThemeButtonText() {
        if (themeButton != null && getContext() != null) {
            int currentTheme = ThemeManager.getSavedTheme(requireContext());
            String themeName = ThemeManager.getThemeName(requireContext(), currentTheme);
            themeButton.setText("Theme: " + themeName);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Dismiss dialog to prevent window leak
        if (themeDialog != null && themeDialog.isShowing()) {
            themeDialog.dismiss();
        }
    }
}
