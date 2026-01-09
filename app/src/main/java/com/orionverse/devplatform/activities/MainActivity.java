package com.orionverse.devplatform.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.orionverse.devplatform.R;
import com.orionverse.devplatform.fragments.CreatePostFragment;
import com.orionverse.devplatform.fragments.HomeFragment;
import com.orionverse.devplatform.fragments.ProfileFragment;
import com.orionverse.devplatform.fragments.SearchFragment;
import com.orionverse.devplatform.utils.FirebaseUtil;
import com.orionverse.devplatform.utils.ThemeManager;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    private BottomNavigationView bottomNavigation;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private TextView toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set content view FIRST to show UI immediately
        setContentView(R.layout.activity_main);

        // Initialize views immediately
        initializeViews();
        
        // Check auth asynchronously to avoid blocking
        checkUserAuthStatus(savedInstanceState);
    }

    private void checkUserAuthStatus(Bundle savedInstanceState) {
        // Check auth in background to avoid ANY blocking
        new Thread(() -> {
            try {
                Thread.sleep(50);
                boolean isLoggedIn = FirebaseUtil.isUserLoggedIn();
                
                runOnUiThread(() -> {
                    if (!isLoggedIn) {
                        navigateToLogin();
                        return;
                    }
                    
                    setupMainActivity(savedInstanceState);
                });
            } catch (Exception e) {
                runOnUiThread(() -> navigateToLogin());
            }
        }).start();
    }
    
    private void setupMainActivity(Bundle savedInstanceState) {
        try {
            setupToolbar();
            setupDrawer();
            setupBottomNavigation();

            // Load default fragment
            if (savedInstanceState == null) {
                loadFragment(new HomeFragment(), "Home");
                navigationView.setCheckedItem(R.id.nav_drawer_home);
            }
        } catch (Exception e) {
            android.widget.Toast.makeText(this, "Error: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        
        // Set initial title
        toolbarTitle.setText("OrionVerse - Home");

        toolbar.setNavigationOnClickListener(v -> {
            if (drawerLayout != null) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });
    }

    private void setupDrawer() {
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            String title = "";
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                fragment = new HomeFragment();
                title = "Home";
                navigationView.setCheckedItem(R.id.nav_drawer_home);
            } else if (itemId == R.id.nav_search) {
                fragment = new SearchFragment();
                title = "Search";
                navigationView.setCheckedItem(R.id.nav_drawer_search);
            } else if (itemId == R.id.nav_post) {
                fragment = new CreatePostFragment();
                title = "Create Post";
                navigationView.setCheckedItem(R.id.nav_drawer_create);
            } else if (itemId == R.id.nav_profile) {
                fragment = new ProfileFragment();
                title = "Profile";
                navigationView.setCheckedItem(R.id.nav_drawer_profile);
            }

            return fragment != null && loadFragment(fragment, title);
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        String title = "";
        int itemId = item.getItemId();

        if (itemId == R.id.nav_drawer_home) {
            fragment = new HomeFragment();
            title = "Home";
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        } else if (itemId == R.id.nav_drawer_search) {
            fragment = new SearchFragment();
            title = "Search";
            bottomNavigation.setSelectedItemId(R.id.nav_search);
        } else if (itemId == R.id.nav_drawer_create) {
            fragment = new CreatePostFragment();
            title = "Create Post";
            bottomNavigation.setSelectedItemId(R.id.nav_post);
        } else if (itemId == R.id.nav_drawer_profile) {
            fragment = new ProfileFragment();
            title = "Profile";
            bottomNavigation.setSelectedItemId(R.id.nav_profile);
        } else if (itemId == R.id.nav_drawer_settings) {
            title = "Settings";
            // TODO: Create SettingsFragment
        }

        if (fragment != null) {
            loadFragment(fragment, title);
        } else {
            updateTitle(title);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean loadFragment(Fragment fragment, String title) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            updateTitle(title);
            return true;
        }
        return false;
    }

    private void updateTitle(String title) {
        if (toolbarTitle != null) {
            toolbarTitle.setText("OrionVerse - " + title);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
