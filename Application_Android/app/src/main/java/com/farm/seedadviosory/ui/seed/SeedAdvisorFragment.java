package com.farm.seedadviosory.ui.seed;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.farm.seedadviosory.R;
import com.farm.seedadviosory.ui.recommendation.RecommendationWizardActivity;

/**
 * Seed Advisor fragment - entry point for crop-specific recommendation.
 */
public class SeedAdvisorFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_seed_advisor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnStartMaize = view.findViewById(R.id.btn_start_maize);
        if (btnStartMaize != null) {
            btnStartMaize.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), RecommendationWizardActivity.class);
                intent.putExtra("crop", "Maize");
                startActivity(intent);
            });
        }

        Button btnStartSoybean = view.findViewById(R.id.btn_start_soybean);
        if (btnStartSoybean != null) {
            btnStartSoybean.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), RecommendationWizardActivity.class);
                intent.putExtra("crop", "Soybean");
                startActivity(intent);
            });
        }

        Button btnStartCotton = view.findViewById(R.id.btn_start_cotton);
        if (btnStartCotton != null) {
            btnStartCotton.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), RecommendationWizardActivity.class);
                intent.putExtra("crop", "Cotton");
                startActivity(intent);
            });
        }
    }
}
