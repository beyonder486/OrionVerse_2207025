package com.orionverse.devplatform.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.orionverse.devplatform.R;
import com.orionverse.devplatform.utils.FirebaseUtil;
import com.orionverse.devplatform.utils.ThemeManager;
import com.orionverse.devplatform.utils.ValidationUtil;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText emailEditText, passwordEditText;
    private MaterialButton loginButton;
    private ProgressBar progressBar;
    private TextView registerTextView;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize theme BEFORE super.onCreate to prevent black screen
        ThemeManager.initTheme(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views immediately
        initializeViews();
        
        // Setup listeners immediately
        setupListeners();
        
        // Check Firebase auth in background (non-blocking)
        checkAuthenticationStatus();
    }

    private void checkAuthenticationStatus() {
        new Thread(() -> {
            try {
                Thread.sleep(100);
                
                auth = FirebaseUtil.getAuth();
                boolean isLoggedIn = auth != null && auth.getCurrentUser() != null;
                
                if (isLoggedIn) {
                    runOnUiThread(() -> navigateToMain());
                }
            } catch (Exception e) {
                // Silent fail - user can still use login form
            }
        }).start();
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);
        registerTextView = findViewById(R.id.registerTextView);
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> {
            Log.d(TAG, "Login button clicked");
            Toast.makeText(this, "Login button clicked!", Toast.LENGTH_SHORT).show();
            attemptLogin();
        });

        registerTextView.setOnClickListener(v -> {
            Log.d(TAG, "Register text clicked");
            Toast.makeText(this, "Opening registration...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void attemptLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        // Validation
        if (!ValidationUtil.isValidEmail(email)) {
            emailEditText.setError(getString(R.string.error_invalid_email));
            emailEditText.requestFocus();
            return;
        }

        if (!ValidationUtil.isValidPassword(password)) {
            passwordEditText.setError(getString(R.string.error_weak_password));
            passwordEditText.requestFocus();
            return;
        }

        performLogin(email, password);
    }

    private void performLogin(String email, String password) {
        setLoading(true);
        Log.d(TAG, "performLogin: Attempting login for " + email);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    setLoading(false);

                    if (task.isSuccessful()) {
                        Log.d(TAG, "performLogin: Login successful");
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage()
                                : "Login failed";
                        Log.e(TAG, "performLogin: Login failed - " + errorMessage);
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Log.e(TAG, "performLogin: Login exception", e);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!loading);
        emailEditText.setEnabled(!loading);
        passwordEditText.setEnabled(!loading);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0); // No animation to prevent black flash
    }
}
