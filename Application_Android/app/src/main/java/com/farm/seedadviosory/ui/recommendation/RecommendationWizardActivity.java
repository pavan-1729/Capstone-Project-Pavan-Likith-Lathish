package com.farm.seedadviosory.ui.recommendation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.farm.seedadviosory.R;
import com.farm.seedadviosory.ui.recommendation.wizard.*;
import com.farm.seedadviosory.utils.LocaleHelper;
import com.farm.seedadviosory.utils.PreferenceManager;

/**
 * Multi-step recommendation wizard using ViewPager2.
 * 6 steps: Location → Season → Soil → Water → Climate → Requirements
 */
public class RecommendationWizardActivity extends AppCompatActivity {

    private static final int TOTAL_STEPS = 6;

    private ViewPager2 viewPager;
    private ProgressBar progressBar;
    private TextView tvStepInfo, tvStepTitle;
    private Button btnBack, btnNext;
    private RecommendationViewModel viewModel;

    private final String[] stepTitles = new String[TOTAL_STEPS];

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation_wizard);

        viewModel = new ViewModelProvider(this).get(RecommendationViewModel.class);

        // Initialize step titles from resources
        stepTitles[0] = getString(R.string.wizard_location_title);
        stepTitles[1] = getString(R.string.wizard_season_title);
        stepTitles[2] = getString(R.string.wizard_soil_title);
        stepTitles[3] = getString(R.string.wizard_water_title);
        stepTitles[4] = getString(R.string.wizard_climate_title);
        stepTitles[5] = getString(R.string.wizard_requirements_title);

        // Set default location from profile
        PreferenceManager prefs = new PreferenceManager(this);
        viewModel.setState(prefs.getFarmerState());
        viewModel.setDistrict(prefs.getFarmerDistrict());

        String crop = getIntent().getStringExtra("crop");
        if (crop != null && !crop.isBlank()) {
            viewModel.setCrop(crop);
        }

        viewPager = findViewById(R.id.view_pager);
        progressBar = findViewById(R.id.progress_bar);
        tvStepInfo = findViewById(R.id.tv_step_info);
        tvStepTitle = findViewById(R.id.tv_step_title);
        btnBack = findViewById(R.id.btn_back);
        btnNext = findViewById(R.id.btn_next);

        // Setup ViewPager
        viewPager.setAdapter(new WizardPagerAdapter(this));
        viewPager.setUserInputEnabled(false); // Disable swipe

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateStepUI(position);
            }
        });

        btnBack.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() > 0) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            } else {
                finish();
            }
        });

        btnNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < TOTAL_STEPS - 1) {
                viewPager.setCurrentItem(current + 1);
            } else {
                // Final step - submit
                submitRecommendation();
            }
        });

        updateStepUI(0);
    }

    private void updateStepUI(int position) {
        tvStepInfo.setText(getString(R.string.step_format, position + 1, TOTAL_STEPS));
        tvStepTitle.setText(stepTitles[position]);
        progressBar.setMax(TOTAL_STEPS);
        progressBar.setProgress(position + 1);

        btnBack.setText(position == 0 ? "✕" : getString(R.string.btn_back));

        if (position == TOTAL_STEPS - 1) {
            btnNext.setText(getString(R.string.btn_find_seeds));
        } else {
            btnNext.setText(getString(R.string.btn_next));
        }
    }

    private void submitRecommendation() {
        PreferenceManager prefs = new PreferenceManager(this);
        Intent intent = new Intent(this, RecommendationLoadingActivity.class);

        // Pass wizard data via intent extras
        intent.putExtra("crop", viewModel.getCrop());
        intent.putExtra("state", viewModel.getState());
        intent.putExtra("district", viewModel.getDistrict());
        intent.putExtra("village", viewModel.getVillage());
        intent.putExtra("season", viewModel.getSeason());
        intent.putExtra("soilType", viewModel.getSoilType());
        intent.putExtra("waterAvailability", viewModel.getWaterAvailability());
        intent.putExtra("maximumMaturityDays", viewModel.getMaximumMaturityDays());
        intent.putExtra("purpose", viewModel.getPurpose());
        intent.putExtra("priority", viewModel.getPriority());
        intent.putExtra("farmerDescription", viewModel.getFarmerDescription());
        intent.putStringArrayListExtra("climateRisks",
                new java.util.ArrayList<>(viewModel.getClimateRisks()));
        intent.putExtra("language", prefs.getLanguage());

        startActivity(intent);
    }

    public RecommendationViewModel getWizardViewModel() {
        return viewModel;
    }

    /**
     * Adapter for wizard step fragments.
     */
    private static class WizardPagerAdapter extends FragmentStateAdapter {

        public WizardPagerAdapter(AppCompatActivity activity) {
            super(activity);
        }

        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new WizardLocationFragment();
                case 1: return new WizardSeasonFragment();
                case 2: return new WizardSoilFragment();
                case 3: return new WizardWaterFragment();
                case 4: return new WizardClimateFragment();
                case 5: return new WizardRequirementsFragment();
                default: return new WizardLocationFragment();
            }
        }

        @Override
        public int getItemCount() {
            return TOTAL_STEPS;
        }
    }
}
