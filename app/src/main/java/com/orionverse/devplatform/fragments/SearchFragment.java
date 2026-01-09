package com.orionverse.devplatform.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.orionverse.devplatform.R;
import com.orionverse.devplatform.adapters.PostAdapter;
import com.orionverse.devplatform.models.Post;
import com.orionverse.devplatform.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    private EditText searchEditText;
    private TabLayout searchTabLayout;
    private RecyclerView searchRecyclerView;
    private PostAdapter postAdapter;
    private boolean searchingUsers = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        searchEditText = view.findViewById(R.id.searchEditText);
        searchTabLayout = view.findViewById(R.id.searchTabLayout);
        searchRecyclerView = view.findViewById(R.id.searchRecyclerView);
    }

    private void setupRecyclerView() {
        postAdapter = new PostAdapter(getContext());
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchRecyclerView.setAdapter(postAdapter);
    }

    private void setupListeners() {
        searchTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                searchingUsers = tab.getPosition() == 0;
                performSearch(searchEditText.getText().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void performSearch(String query) {
        if (query.trim().isEmpty()) {
            postAdapter.setPosts(new ArrayList<>());
            return;
        }

        if (searchingUsers) {
            searchUsers(query);
        } else {
            searchPosts(query);
        }
    }

    private void searchUsers(String query) {
        // Simple search - in production, use Algolia or similar
        FirebaseUtil.getUsersCollection()
                .orderBy("username")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // TODO: Display users with UserAdapter
                });
    }

    private void searchPosts(String query) {
        // Search posts by title
        FirebaseUtil.getPostsCollection()
                .orderBy("title")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Post> posts = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Post post = document.toObject(Post.class);
                        post.setPostId(document.getId());
                        posts.add(post);
                    }
                    postAdapter.setPosts(posts);
                });
    }
}
