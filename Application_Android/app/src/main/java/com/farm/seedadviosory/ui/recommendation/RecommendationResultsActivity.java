package com.farm.seedadviosory.ui.recommendation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.farm.seedadviosory.R;
import com.farm.seedadviosory.data.model.RecommendationResponse;
import com.farm.seedadviosory.ui.recommendation.adapter.RecommendationAdapter;
import com.farm.seedadviosory.utils.LocaleHelper;
import com.google.gson.Gson;

/**
 * Displays recommendation results from the wizard.
 */
public class RecommendationResultsActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation_results);

        String responseJson = getIntent().getStringExtra("response_json");
        RecommendationResponse response = new Gson().fromJson(responseJson, RecommendationResponse.class);

        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // AI Agronomic Advisory & Explanation
        androidx.cardview.widget.CardView cardAiSummary = findViewById(R.id.card_ai_summary);
        TextView tvAiSummary = findViewById(R.id.tv_ai_summary);
        TextView tvModelBadge = findViewById(R.id.tv_model_badge);

        if (response != null && response.getSummary() != null && !response.getSummary().isBlank()) {
            cardAiSummary.setVisibility(View.VISIBLE);
            tvAiSummary.setText(response.getSummary());
            if (response.getModelUsed() != null && !response.getModelUsed().isBlank()) {
                tvModelBadge.setText(response.getModelUsed());
            } else if (response.isLlmAvailable()) {
                tvModelBadge.setText("LLM Reasoner");
            } else {
                tvModelBadge.setText("ICAR RAG Rules");
            }
        } else {
            cardAiSummary.setVisibility(View.GONE);
        }

        if (response != null && response.getRecommendations() != null
                && !response.getRecommendations().isEmpty()) {
            // Show results
            RecyclerView recyclerView = findViewById(R.id.recycler_results);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new RecommendationAdapter(this, response.getRecommendations()));

            TextView tvCount = findViewById(R.id.tv_results_count);
            tvCount.setText(getString(R.string.results_count,
                    response.getRecommendations().size()));
        } else {
            // Show empty state
            LinearLayout emptyView = findViewById(R.id.layout_empty);
            emptyView.setVisibility(View.VISIBLE);
            findViewById(R.id.recycler_results).setVisibility(View.GONE);
        }

        // Disclaimers
        TextView tvDisclaimer = findViewById(R.id.tv_disclaimer);
        tvDisclaimer.setText(getString(R.string.disclaimer_evidence) + "\n" +
                getString(R.string.disclaimer_expert));
    }
}
