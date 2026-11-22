package com.farm.seedadviosory.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.farm.seedadviosory.R;
import com.farm.seedadviosory.config.AppConfig;
import com.farm.seedadviosory.data.model.Farmer;
import com.farm.seedadviosory.data.remote.ApiService;
import com.farm.seedadviosory.data.remote.RetrofitClient;
import com.farm.seedadviosory.ui.main.MainActivity;
import com.farm.seedadviosory.utils.LocaleHelper;
import com.farm.seedadviosory.utils.PreferenceManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextInputEditText etMobileEmail;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private Button btnDemo;
    private TextView tvCreateAccount;
    private TextView tvForgotPassword;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private PreferenceManager preferenceManager;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        preferenceManager = new PreferenceManager(this);
        firebaseAuth = FirebaseAuth.getInstance();

        etMobileEmail = findViewById(R.id.et_mobile_email);
        etPassword    = findViewById(R.id.et_password);
        btnLogin      = findViewById(R.id.btn_login);
        btnDemo       = findViewById(R.id.btn_demo);
        tvCreateAccount  = findViewById(R.id.tv_create_account);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        progressBar   = findViewById(R.id.progress_bar);

        // Demo button is only visible in DEMO_MODE
        if (btnDemo != null) {
            btnDemo.setVisibility(AppConfig.DEMO_MODE ? View.VISIBLE : View.GONE);
        }

        btnLogin.setOnClickListener(v -> loginUser());

        if (btnDemo != null) {
            btnDemo.setOnClickListener(v -> loginAsDemoFarmer());
        }

        tvCreateAccount.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Auto-navigate if already authenticated
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            Log.d(TAG, "User already authenticated: " + user.getUid());
            navigateToHome();
        }
    }

    // ----------------------------------------------------------------
    // Firebase login
    // ----------------------------------------------------------------

    private void loginUser() {
        String email = etMobileEmail.getText() != null
                ? etMobileEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null
                ? etPassword.getText().toString() : "";

        if (email.isEmpty()) {
            etMobileEmail.setError("Enter your email");
            etMobileEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etMobileEmail.setError("Enter a valid email address");
            etMobileEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Enter your password");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        showProgress();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful()) {
                        hideProgress();
                        handleFirebaseError(task.getException());
                        return;
                    }

                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user == null) {
                        hideProgress();
                        Toast.makeText(this, "Authentication error. Please try again.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Log.d(TAG, "Firebase login successful. UID: " + user.getUid());

                    // Fetch farmer profile from Spring Boot.
                    // FirebaseTokenInterceptor handles the token automatically.
                    fetchFarmerProfile(user);
                });
    }

    private void fetchFarmerProfile(FirebaseUser user) {
        ApiService apiService = RetrofitClient.getApiService();

        apiService.getFarmerProfile().enqueue(new Callback<Farmer>() {
            @Override
            public void onResponse(@NonNull Call<Farmer> call, @NonNull Response<Farmer> response) {
                hideProgress();

                if (response.isSuccessful() && response.body() != null) {
                    Farmer farmer = response.body();
                    Log.d(TAG, "Farmer profile retrieved: " + farmer.getName());
                    cacheFarmerLocally(farmer);
                    preferenceManager.setLoggedIn(true);
                    preferenceManager.setDemoMode(false);
                    String name = (farmer.getName() != null && !farmer.getName().isEmpty())
                            ? farmer.getName() : "Farmer";
                    Toast.makeText(LoginActivity.this, "Welcome, " + name + "!", Toast.LENGTH_SHORT).show();
                    navigateToHome();

                } else if (response.code() == 404) {
                    // Authenticated but no profile yet — go to home, user can set up profile
                    Log.w(TAG, "No farmer profile found (404) — proceeding to home");
                    preferenceManager.setFarmerEmail(user.getEmail() != null ? user.getEmail() : "");
                    preferenceManager.setLoggedIn(true);
                    preferenceManager.setDemoMode(false);
                    Toast.makeText(LoginActivity.this,
                            "Welcome! Please complete your profile.", Toast.LENGTH_SHORT).show();
                    navigateToHome();

                } else if (response.code() == 401 || response.code() == 403) {
                    Log.e(TAG, "Authentication error: " + response.code());
                    firebaseAuth.signOut();
                    Toast.makeText(LoginActivity.this,
                            "Authentication failed. Please login again.", Toast.LENGTH_LONG).show();

                } else {
                    // Server error — allow login with cached/partial profile
                    Log.w(TAG, "Profile API returned " + response.code() + " — proceeding with partial session");
                    preferenceManager.setFarmerEmail(user.getEmail() != null ? user.getEmail() : "");
                    preferenceManager.setLoggedIn(true);
                    preferenceManager.setDemoMode(false);
                    Toast.makeText(LoginActivity.this,
                            "Logged in. Server temporarily unavailable.", Toast.LENGTH_SHORT).show();
                    navigateToHome();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Farmer> call, @NonNull Throwable t) {
                hideProgress();
                Log.e(TAG, "Cannot reach server: " + t.getMessage(), t);
                // Firebase login succeeded — allow access, but notify about connectivity
                preferenceManager.setFarmerEmail(user.getEmail() != null ? user.getEmail() : "");
                preferenceManager.setLoggedIn(true);
                preferenceManager.setDemoMode(false);
                Toast.makeText(LoginActivity.this,
                        "Logged in. Could not reach server — check connection.",
                        Toast.LENGTH_SHORT).show();
                navigateToHome();
            }
        });
    }

    private void cacheFarmerLocally(Farmer farmer) {
        preferenceManager.saveFarmerProfile(
                farmer.getName(),
                farmer.getMobile(),
                farmer.getEmail(),
                farmer.getState(),
                farmer.getDistrict(),
                farmer.getVillage(),
                farmer.getPreferredLanguage(),
                farmer.getPrimaryCrop(),
                farmer.getFarmSize()
        );
    }

    // ----------------------------------------------------------------
    // Demo mode (only available when AppConfig.DEMO_MODE = true)
    // ----------------------------------------------------------------

    private void loginAsDemoFarmer() {
        if (!AppConfig.DEMO_MODE) return;
        preferenceManager.saveDemoFarmerProfile();
        preferenceManager.setLoggedIn(true);
        Toast.makeText(this, "Continuing as Demo Farmer", Toast.LENGTH_SHORT).show();
        navigateToHome();
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void handleFirebaseError(Exception exception) {
        String errorMessage;
        if (exception instanceof FirebaseAuthInvalidUserException) {
            errorMessage = "No account found with this email. Please create an account.";
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = "Invalid email or password.";
        } else if (exception instanceof FirebaseNetworkException) {
            errorMessage = "Network error. Please check your internet connection.";
        } else if (exception != null && exception.getMessage() != null) {
            errorMessage = exception.getMessage();
        } else {
            errorMessage = "Login failed. Please check your credentials.";
        }
        Log.e(TAG, "Firebase login failed: " + errorMessage, exception);
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void showProgress() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);
        if (btnDemo != null) btnDemo.setEnabled(false);
    }

    private void hideProgress() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        btnLogin.setEnabled(true);
        if (btnDemo != null) btnDemo.setEnabled(true);
    }
}
