package com.farm.seedadviosory.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.farm.seedadviosory.R;
import com.farm.seedadviosory.data.mock.MockDataProvider;
import com.farm.seedadviosory.data.model.Farmer;
import com.farm.seedadviosory.data.model.FarmerRequest;
import com.farm.seedadviosory.data.remote.ApiService;
import com.farm.seedadviosory.data.remote.RetrofitClient;
import com.farm.seedadviosory.ui.main.MainActivity;
import com.farm.seedadviosory.utils.LocaleHelper;
import com.farm.seedadviosory.utils.PreferenceManager;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private FirebaseAuth firebaseAuth;
    private PreferenceManager preferenceManager;

    private EditText etName, etMobile, etEmail, etPassword, etConfirmPassword, etVillage;
    private Spinner spinnerState, spinnerDistrict, spinnerLanguage, spinnerPrimaryCrop;
    private ProgressBar progressBar;
    private Button btnCreate, btnBack;
    private TextView tvAlreadyHaveAccount;

    private final String[] languageDisplayNames = {"English", "తెలుగు (Telugu)", "தமிழ் (Tamil)", "हिन्दी (Hindi)"};
    private final String[] languageCodes        = {"en", "te", "ta", "hi"};
    private final String[] crops                = {"Maize", "Soybean", "Cotton"};

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        preferenceManager = new PreferenceManager(this);

        initViews();
        setupSpinners();

        btnCreate.setOnClickListener(v -> registerUser());
        btnBack.setOnClickListener(v -> finish());
        tvAlreadyHaveAccount.setOnClickListener(v -> finish());
    }

    private void initViews() {
        etName           = findViewById(R.id.et_name);
        etMobile         = findViewById(R.id.et_mobile);
        etEmail          = findViewById(R.id.et_email);
        etPassword       = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        spinnerState     = findViewById(R.id.spinner_state);
        spinnerDistrict  = findViewById(R.id.spinner_district);
        etVillage        = findViewById(R.id.et_village);
        spinnerLanguage  = findViewById(R.id.spinner_language);
        spinnerPrimaryCrop = findViewById(R.id.spinner_primary_crop);
        progressBar      = findViewById(R.id.progress_bar);
        btnCreate        = findViewById(R.id.btn_create_account);
        btnBack          = findViewById(R.id.btn_back);
        tvAlreadyHaveAccount = findViewById(R.id.tv_already_have_account);
    }

    private void setupSpinners() {
        // States — using MockDataProvider static list (not mock data — it's just a list of state names)
        List<String> states = MockDataProvider.getStates();
        spinnerState.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, states));

        spinnerState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedState = states.get(position);
                List<String> districts = MockDataProvider.getDistricts(selectedState);
                spinnerDistrict.setAdapter(new ArrayAdapter<>(
                        RegisterActivity.this,
                        android.R.layout.simple_spinner_dropdown_item,
                        districts));
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Language
        spinnerLanguage.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, languageDisplayNames));

        // Primary crop
        spinnerPrimaryCrop.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, crops));
    }

    private void registerUser() {
        String name            = getText(etName);
        String mobile          = getText(etMobile);
        String email           = getText(etEmail);
        String password        = getText(etPassword);
        String confirmPassword = getText(etConfirmPassword);
        String village         = getText(etVillage);
        String state           = spinnerState.getSelectedItem() != null
                                   ? spinnerState.getSelectedItem().toString() : "";
        String district        = spinnerDistrict.getSelectedItem() != null
                                   ? spinnerDistrict.getSelectedItem().toString() : "";
        int langIdx            = spinnerLanguage.getSelectedItemPosition();
        String language        = (langIdx >= 0 && langIdx < languageCodes.length)
                                   ? languageCodes[langIdx] : "en";
        String primaryCrop     = spinnerPrimaryCrop.getSelectedItem() != null
                                   ? spinnerPrimaryCrop.getSelectedItem().toString() : "Maize";

        // Validation
        if (name.isEmpty()) { setError(etName, "Enter your name"); return; }
        if (mobile.isEmpty()) { setError(etMobile, "Enter mobile number"); return; }
        if (mobile.length() < 10 || !mobile.matches("\\d+")) {
            setError(etMobile, "Enter a valid 10-digit mobile number"); return;
        }
        if (email.isEmpty()) { setError(etEmail, "Enter your email"); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            setError(etEmail, "Enter a valid email address"); return;
        }
        if (password.isEmpty()) { setError(etPassword, "Enter password"); return; }
        if (password.length() < 6) { setError(etPassword, "Password must be at least 6 characters"); return; }
        if (!password.equals(confirmPassword)) {
            setError(etConfirmPassword, "Passwords do not match"); return;
        }
        if (state.isEmpty()) { Toast.makeText(this, "Please select your state", Toast.LENGTH_SHORT).show(); return; }
        if (district.isEmpty()) { Toast.makeText(this, "Please select your district", Toast.LENGTH_SHORT).show(); return; }
        if (village.isEmpty()) { setError(etVillage, "Enter village"); return; }

        showProgress();

        // Step 1: Create Firebase account
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful()) {
                        hideProgress();
                        handleFirebaseError(task.getException());
                        return;
                    }

                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user == null) {
                        hideProgress();
                        Toast.makeText(this, "Authentication failed. Please try again.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Log.d(TAG, "Firebase account created. UID: " + user.getUid());

                    // Step 2: Register profile in Spring Boot
                    // FirebaseTokenInterceptor handles the token automatically.
                    FarmerRequest farmerRequest = new FarmerRequest(
                            name, mobile, email, state, district, village, language, primaryCrop
                    );
                    sendProfileToBackend(farmerRequest);
                });
    }

    private void sendProfileToBackend(FarmerRequest farmerRequest) {
        ApiService apiService = RetrofitClient.getApiService();

        apiService.registerFarmer(farmerRequest).enqueue(new Callback<Farmer>() {
            @Override
            public void onResponse(@NonNull Call<Farmer> call, @NonNull Response<Farmer> response) {
                hideProgress();

                if (response.isSuccessful()) {
                    Log.d(TAG, "Farmer profile saved in backend");

                    // Cache locally
                    preferenceManager.saveFarmerProfile(
                            farmerRequest.getName(),
                            farmerRequest.getMobile(),
                            farmerRequest.getEmail(),
                            farmerRequest.getState(),
                            farmerRequest.getDistrict(),
                            farmerRequest.getVillage(),
                            farmerRequest.getPreferredLanguage(),
                            farmerRequest.getPrimaryCrop(),
                            null
                    );
                    preferenceManager.setLoggedIn(true);
                    preferenceManager.setDemoMode(false);

                    Toast.makeText(RegisterActivity.this,
                            "Account created successfully!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                } else if (response.code() == 409) {
                    Log.w(TAG, "Profile already exists (409)");
                    Toast.makeText(RegisterActivity.this,
                            "An account with this email already exists. Please login.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Log.e(TAG, "Backend registration failed: HTTP " + response.code());
                    Toast.makeText(RegisterActivity.this,
                            "Account created, but profile could not be saved to server (Code: "
                                    + response.code() + "). Please try logging in.",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Farmer> call, @NonNull Throwable t) {
                hideProgress();
                Log.e(TAG, "Cannot reach server: " + t.getMessage(), t);
                Toast.makeText(RegisterActivity.this,
                        "Account created, but server is unreachable. Please login.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleFirebaseError(Exception exception) {
        String message;
        if (exception instanceof FirebaseAuthUserCollisionException) {
            message = "An account with this email already exists. Please login.";
        } else if (exception instanceof FirebaseAuthWeakPasswordException) {
            message = "Password is too weak. Use at least 6 characters.";
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            message = "Invalid email format.";
        } else if (exception instanceof FirebaseNetworkException) {
            message = "Network error. Check your internet connection.";
        } else if (exception != null && exception.getMessage() != null) {
            message = exception.getMessage();
        } else {
            message = "Registration failed. Please try again.";
        }
        Log.e(TAG, "Firebase registration error: " + message, exception);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private String getText(EditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void setError(EditText et, String message) {
        et.setError(message);
        et.requestFocus();
    }

    private void showProgress() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        btnCreate.setEnabled(false);
    }

    private void hideProgress() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        btnCreate.setEnabled(true);
    }
}
