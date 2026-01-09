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
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.orionverse.devplatform.R;
import com.orionverse.devplatform.models.User;
import com.orionverse.devplatform.utils.FirebaseUtil;
import com.orionverse.devplatform.utils.ValidationUtil;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private EditText usernameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private MaterialButton registerButton;
    private ProgressBar progressBar;
    private TextView loginTextView;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize theme BEFORE super.onCreate to prevent black screen
        com.orionverse.devplatform.utils.ThemeManager.initTheme(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Setup toolbar with back button
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        auth = FirebaseUtil.getAuth();
        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        progressBar = findViewById(R.id.progressBar);
        loginTextView = findViewById(R.id.loginTextView);
    }

    private void setupListeners() {
        registerButton.setOnClickListener(v -> {
            Log.d(TAG, "Register button clicked");
            Toast.makeText(this, "Register button clicked!", Toast.LENGTH_SHORT).show();
            attemptRegister();
        });

        loginTextView.setOnClickListener(v -> {
            Log.d(TAG, "Login text clicked, going back");
            finish(); // Go back to login
        });
    }

    private void attemptRegister() {
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        // Validation
        if (!ValidationUtil.isValidUsername(username)) {
            usernameEditText.setError(getString(R.string.error_invalid_username));
            usernameEditText.requestFocus();
            return;
        }

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

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError(getString(R.string.error_passwords_dont_match));
            confirmPasswordEditText.requestFocus();
            return;
        }

        performRegister(username, email, password);
    }

    private void performRegister(String username, String email, String password) {
        setLoading(true);
        Log.d(TAG, "performRegister: Creating account for " + email);

        Toast.makeText(this, "Creating account...", Toast.LENGTH_SHORT).show();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "performRegister: Account created successfully");
                        Toast.makeText(this, "Account created! Setting up profile...", Toast.LENGTH_SHORT).show();
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            createUserDocument(firebaseUser.getUid(), username, email);
                        } else {
                            setLoading(false);
                            Log.e(TAG, "performRegister: FirebaseUser is null after creation");
                            Toast.makeText(this, "Error: User is null", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        setLoading(false);
                        String errorMessage = task.getException() != null ? task.getException().getMessage()
                                : "Registration failed";
                        Log.e(TAG, "performRegister: Registration failed - " + errorMessage);
                        Toast.makeText(this, "Registration failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Log.e(TAG, "performRegister: Exception", e);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void createUserDocument(String userId, String username, String email) {
        User user = new User(userId, username, email);
        user.setCreatedAt(Timestamp.now());

        Log.d(TAG, "createUserDocument: Saving user to Firestore");
        Toast.makeText(this, "Saving profile to database...", Toast.LENGTH_SHORT).show();

        FirebaseUtil.getUsersCollection()
                .document(userId)
                .set(user)
                .addOnCompleteListener(task -> {
                    setLoading(false);

                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserDocument: Profile saved successfully");
                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Log.e(TAG, "createUserDocument: Failed to save profile - " + error);
                        Toast.makeText(this, "Failed to create profile: " + error, Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Log.e(TAG, "createUserDocument: Firestore exception", e);
                    Toast.makeText(this, "Firestore error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!loading);
        usernameEditText.setEnabled(!loading);
        emailEditText.setEnabled(!loading);
        passwordEditText.setEnabled(!loading);
        confirmPasswordEditText.setEnabled(!loading);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
