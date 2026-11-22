package com.farm.seedadviosory.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.farm.seedadviosory.R;
import com.farm.seedadviosory.config.AppConfig;
import com.farm.seedadviosory.ui.about.AboutActivity;
import com.farm.seedadviosory.ui.about.HowItWorksActivity;
import com.farm.seedadviosory.ui.auth.LanguageSelectionActivity;
import com.farm.seedadviosory.utils.LocaleHelper;
import com.farm.seedadviosory.utils.PreferenceManager;

/**
 * Settings screen allowing farmers to configure language, alerts, demo mode, and clear cache.
 */
public class SettingsActivity extends AppCompatActivity {

    private PreferenceManager prefs;
    private TextView tvCurrentLanguage;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = new PreferenceManager(this);

        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Language
        tvCurrentLanguage = findViewById(R.id.tv_current_language);
        updateLanguageLabel();

        Button btnChangeLanguage = findViewById(R.id.btn_change_language);
        btnChangeLanguage.setOnClickListener(v -> {
            Intent intent = new Intent(this, LanguageSelectionActivity.class);
            startActivity(intent);
        });

        // Notification switches
        SwitchCompat switchWeather = findViewById(R.id.switch_weather_alerts);
        SwitchCompat switchCrop = findViewById(R.id.switch_crop_reminders);
        SwitchCompat switchDisease = findViewById(R.id.switch_disease_alerts);

        switchWeather.setChecked(prefs.isWeatherAlertsEnabled());
        switchCrop.setChecked(prefs.isCropRemindersEnabled());
        switchDisease.setChecked(prefs.isDiseaseAlertsEnabled());

        switchWeather.setOnCheckedChangeListener((bv, isChecked) -> {
            prefs.setWeatherAlertsEnabled(isChecked);
            Toast.makeText(this, isChecked ? "Weather alerts enabled" : "Weather alerts disabled", Toast.LENGTH_SHORT).show();
        });

        switchCrop.setOnCheckedChangeListener((bv, isChecked) -> {
            prefs.setCropRemindersEnabled(isChecked);
            Toast.makeText(this, isChecked ? "Crop reminders enabled" : "Crop reminders disabled", Toast.LENGTH_SHORT).show();
        });

        switchDisease.setOnCheckedChangeListener((bv, isChecked) -> {
            prefs.setDiseaseAlertsEnabled(isChecked);
            Toast.makeText(this, isChecked ? "Disease alerts enabled" : "Disease alerts disabled", Toast.LENGTH_SHORT).show();
        });

        // Demo mode switch
        SwitchCompat switchDemo = findViewById(R.id.switch_demo_mode);
        switchDemo.setChecked(prefs.isDemoMode());
        switchDemo.setOnCheckedChangeListener((bv, isChecked) -> {
            prefs.setDemoMode(isChecked);
            Toast.makeText(this, isChecked ? "Demo mode active (ICAR local data)" : "Real API mode selected", Toast.LENGTH_SHORT).show();
        });

        // Clear cache
        Button btnClearCache = findViewById(R.id.btn_clear_cache);
        btnClearCache.setOnClickListener(v ->
                Toast.makeText(this, "Cached recommendations and advisory data cleared successfully.", Toast.LENGTH_SHORT).show());

        // About & How it works
        Button btnAboutApp = findViewById(R.id.btn_about_app);
        btnAboutApp.setOnClickListener(v ->
                startActivity(new Intent(this, AboutActivity.class)));

        Button btnHowItWorks = findViewById(R.id.btn_how_it_works);
        btnHowItWorks.setOnClickListener(v ->
                startActivity(new Intent(this, HowItWorksActivity.class)));

        // Version info
        TextView tvVersion = findViewById(R.id.tv_settings_app_version);
        tvVersion.setText("Farmer-Aware v" + AppConfig.APP_VERSION + " (" + AppConfig.BUILD_TYPE + ")");
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLanguageLabel();
    }

    private void updateLanguageLabel() {
        String langCode = prefs.getLanguage();
        String langName;
        switch (langCode) {
            case "te":
                langName = "తెలుగు (Telugu)";
                break;
            case "ta":
                langName = "தமிழ் (Tamil)";
                break;
            case "hi":
                langName = "हिन्दी (Hindi)";
                break;
            default:
                langName = "English";
                break;
        }
        tvCurrentLanguage.setText(langName);
    }
}
