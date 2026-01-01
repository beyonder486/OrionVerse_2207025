package com.orionverse.devplatform.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.orionverse.devplatform.R;
import com.orionverse.devplatform.fragments.CreatePostFragment;
import com.orionverse.devplatform.fragments.HomeFragment;
import com.orionverse.devplatform.fragments.ProfileFragment;
import com.orionverse.devplatform.fragments.SearchFragment;
import com.orionverse.devplatform.utils.FirebaseUtil;
import com.orionverse.devplatform.utils.ThemeManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize theme before super.onCreate()
        ThemeManager.initTheme(this);
        
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: MainActivity starting");

        // Check if user is logged in
        if (!FirebaseUtil.isUserLoggedIn()) {
            Log.d(TAG, "onCreate: User not logged in, navigating to login");
            navigateToLogin();
            return;
        }

        Log.d(TAG, "onCreate: User is logged in");
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        setupBottomNavigation();

        // Load default fragment
        if (savedInstanceState == null) {
            Log.d(TAG, "onCreate: Loading home fragment");
            loadFragment(new HomeFragment());
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (itemId == R.id.nav_search) {
                fragment = new SearchFragment();
            } else if (itemId == R.id.nav_post) {
                fragment = new CreatePostFragment();
            } else if (itemId == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }

            return fragment != null && loadFragment(fragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
