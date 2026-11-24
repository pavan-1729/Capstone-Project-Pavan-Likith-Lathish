package com.farm.seedadviosory.ui.recommendation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.farm.seedadviosory.R;
import com.farm.seedadviosory.data.model.MaizeRecommendationRequest;
import com.farm.seedadviosory.data.model.RecommendationResponse;
import com.farm.seedadviosory.data.remote.ApiService;
import com.farm.seedadviosory.data.remote.RetrofitClient;
import com.farm.seedadviosory.utils.LocaleHelper;
import com.google.gson.Gson;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Loading screen shown while the RAG recommendation runs.
 * Displays animated progress messages and calls the real backend.
 *
 * Flow:
 *   1. Display animated loading messages
 *   2. POST to /api/recommendations/maize (Spring Boot → RAG → LLM)
 *   3. Navigate to RecommendationResultsActivity with the response
 *   4. On error: show message + allow retry or go back
 */
public class RecommendationLoadingActivity extends AppCompatActivity {

    private static final String TAG = "RecommendationLoading";

    private final String[] loadingStepsMaize = {
            "📡 Analyzing your farm conditions...",
            "🌱 Searching agricultural knowledge base (302 varieties)...",
            "📊 Applying climate and regional filters...",
            "🔬 Retrieving relevant maize variety evidence...",
            "🤖 Generating AI-powered recommendations...",
            "✅ Preparing your personalized advice..."
    };

    private final String[] loadingStepsSoybean = {
            "📡 Analyzing your farm conditions...",
            "🌱 Searching agricultural knowledge base (184 varieties)...",
            "📊 Applying climate and regional filters...",
            "🔬 Retrieving relevant soybean variety evidence...",
            "🤖 Generating AI-powered recommendations...",
            "✅ Preparing your personalized advice..."
    };

    private final String[] loadingStepsCotton = {
            "📡 Analyzing your farm conditions...",
            "🌱 Searching agricultural knowledge base (265 cotton varieties)...",
            "📊 Applying climate and regional filters...",
            "🔬 Retrieving relevant cotton variety evidence...",
            "🤖 Generating AI-powered recommendations...",
            "✅ Preparing your personalized advice..."
    };

    private TextView tvLoadingStep;
    private TextView tvLoadingSubtitle;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private boolean navigationComplete = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation_loading);

        tvLoadingStep     = findViewById(R.id.tv_loading_step);
        // tvLoadingSubtitle = findViewById(R.id.tv_loading_subtitle);

        // Build request from wizard intent extras
        MaizeRecommendationRequest request = buildRequestFromIntent();
        boolean isSoybean = "soybean".equalsIgnoreCase(request.getCrop());
        boolean isCotton = "cotton".equalsIgnoreCase(request.getCrop());

        // Animate loading messages in background
        String[] steps = isCotton ? loadingStepsCotton : (isSoybean ? loadingStepsSoybean : loadingStepsMaize);
        animateSteps(steps);

        // Call real backend
        callRecommendationApi(request);
    }

    // ----------------------------------------------------------------
    // Animated loading messages
    // ----------------------------------------------------------------

    private void animateSteps(String[] steps) {
        final int[] index = {0};
        Runnable stepRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isFinishing() && !navigationComplete && tvLoadingStep != null) {
                    tvLoadingStep.setText(steps[index[0] % steps.length]);
                    index[0]++;
                    uiHandler.postDelayed(this, 3000L);
                }
            }
        };
        uiHandler.post(stepRunnable);
    }

    // ----------------------------------------------------------------
    // Real API call
    // ----------------------------------------------------------------

    private void callRecommendationApi(MaizeRecommendationRequest request) {
        ApiService apiService = RetrofitClient.getApiService();
        boolean isSoybean = "soybean".equalsIgnoreCase(request.getCrop());
        boolean isCotton = "cotton".equalsIgnoreCase(request.getCrop());

        String endpoint = isCotton ? "cotton" : (isSoybean ? "soybean" : "maize");
        Log.d(TAG, "Calling POST /api/recommendations/" + endpoint
                + " for state=" + request.getState() + " season=" + request.getSeason());

        Call<RecommendationResponse> apiCall;
        if (isCotton) {
            apiCall = apiService.recommendCotton(request);
        } else if (isSoybean) {
            apiCall = apiService.recommendSoybean(request);
        } else {
            apiCall = apiService.recommendMaize(request);
        }

        apiCall.enqueue(new Callback<RecommendationResponse>() {
            @Override
            public void onResponse(@NonNull Call<RecommendationResponse> call,
                                   @NonNull Response<RecommendationResponse> response) {
                if (isFinishing() || navigationComplete) return;

                if (response.isSuccessful() && response.body() != null) {
                    RecommendationResponse result = response.body();
                    Log.d(TAG, "Recommendation API success. Recommendations: "
                            + (result.getRecommendations() != null ? result.getRecommendations().size() : 0));
                    navigateToResults(result);

                } else {
                    Log.w(TAG, "Recommendation API error: HTTP " + response.code());
                    showError("Could not get recommendations (Error " + response.code() + "). Please try again.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<RecommendationResponse> call, @NonNull Throwable t) {
                if (isFinishing() || navigationComplete) return;
                Log.e(TAG, "Recommendation API connection failed: " + t.getMessage(), t);
                showError("Unable to connect to the advisory service.\nPlease check your internet connection and try again.");
            }
        });
    }

    // ----------------------------------------------------------------
    // Navigation
    // ----------------------------------------------------------------

    private void navigateToResults(RecommendationResponse response) {
        navigationComplete = true;

        // Wait for at least 3 steps of animation before navigating
        long minimumDelay = 3000L;
        uiHandler.postDelayed(() -> {
            Intent intent = new Intent(RecommendationLoadingActivity.this,
                    RecommendationResultsActivity.class);
            intent.putExtra("response_json", new Gson().toJson(response));
            startActivity(intent);
            finish();
        }, minimumDelay);
    }

    private void showError(String message) {
        uiHandler.post(() -> {
            if (isFinishing()) return;
            if (tvLoadingStep != null) {
                tvLoadingStep.setText("⚠ Unable to load recommendations");
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            // Return to wizard after a short delay
            uiHandler.postDelayed(this::finish, 2500);
        });
    }

    // ----------------------------------------------------------------
    // Build request from wizard intent extras
    // ----------------------------------------------------------------

    private MaizeRecommendationRequest buildRequestFromIntent() {
        Intent i = getIntent();
        MaizeRecommendationRequest request = new MaizeRecommendationRequest();
        String crop = i.getStringExtra("crop");
        if (crop != null && !crop.isBlank()) {
            request.setCrop(crop.toLowerCase());
        }
        request.setState(i.getStringExtra("state"));
        request.setDistrict(i.getStringExtra("district"));
        request.setVillage(i.getStringExtra("village"));
        request.setSeason(i.getStringExtra("season"));
        request.setSoilType(i.getStringExtra("soilType"));

        // Map "waterAvailability" wizard field → "irrigation" backend field
        String waterAvailability = i.getStringExtra("waterAvailability");
        request.setWaterAvailability(waterAvailability);
        request.setIrrigation(waterAvailability);

        request.setMaximumMaturityDays(i.getIntExtra("maximumMaturityDays", 0));
        request.setPurpose(i.getStringExtra("purpose"));
        request.setPriority(i.getStringExtra("priority"));
        request.setFarmerDescription(i.getStringExtra("farmerDescription"));

        ArrayList<String> climateRisks = i.getStringArrayListExtra("climateRisks");
        if (climateRisks != null) {
            request.setClimateRisks(climateRisks);
        }

        request.setLanguage(i.getStringExtra("language"));
        return request;
    }
}
