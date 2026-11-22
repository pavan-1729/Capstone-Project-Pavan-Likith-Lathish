package com.farm.seedadviosory.ui.auth;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.farm.seedadviosory.R;
import com.farm.seedadviosory.utils.LocaleHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordActivity";

    private TextInputEditText etEmail;
    private Button btnSendReset;
    private Button btnBack;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Firebase Auth instance
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize UI Views
        etEmail = findViewById(R.id.et_forgot_email);
        btnSendReset = findViewById(R.id.btn_send_reset);
        btnBack = findViewById(R.id.btn_back);
        progressBar = findViewById(R.id.progress_bar);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Send reset email button
        btnSendReset.setOnClickListener(v -> sendPasswordResetEmail());
    }

    private void sendPasswordResetEmail() {
        String email = etEmail.getText() != null
                ? etEmail.getText().toString().trim()
                : "";

        // ====================================================
        // EMAIL VALIDATION
        // ====================================================
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Please enter your registered email");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        // ====================================================
        // SHOW LOADING STATE & PREVENT MULTIPLE REQUESTS
        // ====================================================
        showProgress();

        // ====================================================
        // REAL FIREBASE PASSWORD RESET
        // ====================================================
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    hideProgress();

                    if (task.isSuccessful()) {
                        Log.d(TAG, "Password reset email sent successfully to: " + email);

                        Toast.makeText(
                                ForgotPasswordActivity.this,
                                "Password reset email sent. Please check your inbox and spam folder.",
                                Toast.LENGTH_LONG
                        ).show();

                        // Return back to Login screen
                        finish();

                    } else {
                        Exception exception = task.getException();
                        String userMessage;

                        if (exception instanceof FirebaseAuthInvalidUserException) {
                            userMessage = "No account found with this email address.";
                        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                            userMessage = "Invalid email format. Please check your email.";
                        } else if (exception instanceof FirebaseNetworkException) {
                            userMessage = "Network error. Please check your internet connection.";
                        } else {
                            userMessage = "Unable to send reset email. Please try again later.";
                        }

                        Log.e(TAG, "Firebase password reset failed: " + (exception != null ? exception.getMessage() : "Unknown error"));

                        Toast.makeText(
                                ForgotPasswordActivity.this,
                                userMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void showProgress() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        btnSendReset.setEnabled(false);
    }

    private void hideProgress() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        btnSendReset.setEnabled(true);
    }
}