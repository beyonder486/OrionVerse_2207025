package com.orionverse.devplatform.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.orionverse.devplatform.R;
import com.orionverse.devplatform.models.Post;
import com.orionverse.devplatform.models.User;
import com.orionverse.devplatform.utils.FirebaseUtil;
import com.orionverse.devplatform.utils.ValidationUtil;

import java.util.Arrays;

public class CreatePostFragment extends Fragment {
    private RadioGroup postTypeRadioGroup;
    private EditText titleEditText, descriptionEditText, tagsEditText;
    private MaterialButton submitButton;
    private ProgressBar progressBar;
    private String currentUsername = "Anonymous";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_post, container, false);

        initializeViews(view);
        loadCurrentUser();
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        postTypeRadioGroup = view.findViewById(R.id.postTypeRadioGroup);
        titleEditText = view.findViewById(R.id.titleEditText);
        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        tagsEditText = view.findViewById(R.id.tagsEditText);
        submitButton = view.findViewById(R.id.submitButton);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void loadCurrentUser() {
        String userId = FirebaseUtil.getCurrentUserId();
        if (userId != null) {
            FirebaseUtil.getUsersCollection().document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            currentUsername = user.getUsername();
                        }
                    });
        }
    }

    private void setupListeners() {
        submitButton.setOnClickListener(v -> createPost());
    }

    private void createPost() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String tagsText = tagsEditText.getText().toString().trim();

        // Validation
        if (!ValidationUtil.isNotEmpty(title)) {
            titleEditText.setError(getString(R.string.error_empty_title));
            titleEditText.requestFocus();
            return;
        }

        if (!ValidationUtil.isNotEmpty(description)) {
            descriptionEditText.setError(getString(R.string.error_empty_description));
            descriptionEditText.requestFocus();
            return;
        }

        // Get selected post type
        Post.PostType postType = Post.PostType.PROBLEM;
        int selectedId = postTypeRadioGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.solutionRadio) {
            postType = Post.PostType.SOLUTION;
        } else if (selectedId == R.id.generalRadio) {
            postType = Post.PostType.GENERAL;
        }

        // Create post object
        Post post = new Post(
                FirebaseUtil.getCurrentUserId(),
                currentUsername,
                title,
                description,
                postType);

        // Add tags if provided
        if (!tagsText.isEmpty()) {
            post.setTags(Arrays.asList(tagsText.split(",")));
        }

        post.setCreatedAt(Timestamp.now());

        // Save to Firestore
        savePost(post);
    }

    private void savePost(Post post) {
        setLoading(true);

        FirebaseUtil.getPostsCollection()
                .add(post)
                .addOnSuccessListener(documentReference -> {
                    setLoading(false);
                    Toast.makeText(getContext(), "Post created successfully!", Toast.LENGTH_SHORT).show();
                    clearForm();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(getContext(), "Failed to create post: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void clearForm() {
        titleEditText.setText("");
        descriptionEditText.setText("");
        tagsEditText.setText("");
        postTypeRadioGroup.check(R.id.problemRadio);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        submitButton.setEnabled(!loading);
        titleEditText.setEnabled(!loading);
        descriptionEditText.setEnabled(!loading);
        tagsEditText.setEnabled(!loading);
    }
}
