package com.farm.seedadviosory.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.farm.seedadviosory.R;
import com.farm.seedadviosory.config.AppConfig;
import com.farm.seedadviosory.data.mock.MockDataProvider;
import com.farm.seedadviosory.data.model.Farmer;
import com.farm.seedadviosory.data.model.WeatherInfo;
import com.farm.seedadviosory.data.remote.ApiService;
import com.farm.seedadviosory.data.remote.RetrofitClient;
import com.farm.seedadviosory.ui.about.HowItWorksActivity;
import com.farm.seedadviosory.ui.advisory.CropAdvisoryActivity;
import com.farm.seedadviosory.ui.calendar.CropCalendarActivity;
import com.farm.seedadviosory.ui.recommendation.RecommendationWizardActivity;
import com.farm.seedadviosory.utils.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Home screen fragment.
 *
 * Data flow:
 *   1. Show cached profile immediately (fast UX)
 *   2. Fetch real farmer profile from backend → update UI
 *   3. Fetch weather using farmer's state/district → update UI
 *
 * Demo mode only runs when AppConfig.DEMO_MODE = true AND user is not
 * signed in via Firebase (i.e., clicked "Demo" button on login screen).
 */
public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private PreferenceManager prefs;
    private ApiService apiService;

    // UI views
    private TextView tvDemoBadge;
    private TextView tvWeatherStatusBadge;
    private TextView tvGreeting;
    private TextView tvLocation;
    private TextView tvTemperature;
    private TextView tvHumidity;
    private TextView tvRain;
    private TextView tvWind;
    private TextView tvWeatherLastUpdated;
    private TextView tvSowingDate;
    private TextView tvCurrentStage;
    private TextView tvNextActivity;
    private WeatherInfo currentWeather;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = new PreferenceManager(requireContext());
        apiService = RetrofitClient.getApiService();

        initViews(view);
        setupButtons(view);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        boolean isDemo = AppConfig.DEMO_MODE && prefs.isDemoMode();

        if (isDemo || user == null) {
            loadDemoMode();
        } else {
            loadRealMode();
        }
    }

    // ----------------------------------------------------------------
    // View initialization
    // ----------------------------------------------------------------

    private void initViews(View view) {
        tvDemoBadge          = view.findViewById(R.id.tv_demo_badge);
        tvWeatherStatusBadge = view.findViewById(R.id.tv_weather_demo_badge);
        tvGreeting           = view.findViewById(R.id.tv_greeting);
        tvLocation           = view.findViewById(R.id.tv_location);
        tvTemperature        = view.findViewById(R.id.tv_temperature);
        tvHumidity           = view.findViewById(R.id.tv_humidity);
        tvRain               = view.findViewById(R.id.tv_rain);
        tvWind               = view.findViewById(R.id.tv_wind);
        // tvWeatherLastUpdated = view.findViewById(R.id.tv_weather_last_updated);
        tvSowingDate         = view.findViewById(R.id.tv_sowing_date);
        tvCurrentStage       = view.findViewById(R.id.tv_current_stage);
        tvNextActivity       = view.findViewById(R.id.tv_next_activity);
    }

    private void setupButtons(View view) {
        Button btnStartRecommendation = view.findViewById(R.id.btn_start_recommendation);
        if (btnStartRecommendation != null) {
            btnStartRecommendation.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), RecommendationWizardActivity.class)));
        }

        LinearLayout cardMaize = view.findViewById(R.id.card_maize);
        if (cardMaize != null) {
            cardMaize.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), RecommendationWizardActivity.class);
                intent.putExtra("crop", "Maize");
                startActivity(intent);
            });
        }

        LinearLayout cardSoybean = view.findViewById(R.id.card_soybean);
        if (cardSoybean != null) {
            cardSoybean.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), RecommendationWizardActivity.class);
                intent.putExtra("crop", "Soybean");
                startActivity(intent);
            });
        }

        LinearLayout cardCotton = view.findViewById(R.id.card_cotton);
        if (cardCotton != null) {
            cardCotton.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), RecommendationWizardActivity.class);
                intent.putExtra("crop", "Cotton");
                startActivity(intent);
            });
        }

        Button btnCropPlan = view.findViewById(R.id.btn_view_crop_plan);
        if (btnCropPlan != null) {
            btnCropPlan.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), CropCalendarActivity.class)));
        }

        Button btnAdvisories = view.findViewById(R.id.btn_view_crop_advisories);
        if (btnAdvisories != null) {
            btnAdvisories.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), CropAdvisoryActivity.class)));
        }

        TextView tvHowItWorks = view.findViewById(R.id.tv_how_it_works);
        if (tvHowItWorks != null) {
            tvHowItWorks.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), HowItWorksActivity.class)));
        }

        View cardWeather = view.findViewById(R.id.card_weather);
        if (cardWeather != null) {
            cardWeather.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), com.farm.seedadviosory.ui.weather.WeatherDetailActivity.class);
                if (currentWeather != null) {
                    intent.putExtra("weather_info", currentWeather);
                }
                intent.putExtra("state", prefs.getFarmerState());
                intent.putExtra("district", prefs.getFarmerDistrict());
                startActivity(intent);
            });
        }
    }

    // ----------------------------------------------------------------
    // Demo mode (only when AppConfig.DEMO_MODE = true)
    // ----------------------------------------------------------------

    private void loadDemoMode() {
        if (tvDemoBadge != null) tvDemoBadge.setVisibility(View.VISIBLE);

        String name = prefs.getFarmerName();
        setGreeting(name.isEmpty() ? "Farmer" : name);
        updateLocation(prefs.getFarmerDistrict(), prefs.getFarmerState());
        showDemoWeather();
        showDemoCropStatus();
    }

    // ----------------------------------------------------------------
    // Real backend mode
    // ----------------------------------------------------------------

    private void loadRealMode() {
        if (tvDemoBadge != null) tvDemoBadge.setVisibility(View.GONE);

        // STEP 1: Show cached data immediately for responsive UI
        String cachedName = prefs.getFarmerName();
        setGreeting(cachedName.isEmpty() ? "Farmer" : cachedName);
        updateLocation(prefs.getFarmerDistrict(), prefs.getFarmerState());

        // STEP 2: Show "Loading..." for weather while we fetch
        showWeatherLoading();

        // STEP 3: Fetch real farmer profile
        fetchFarmerProfile();
    }

    // ----------------------------------------------------------------
    // Fetch real farmer profile from backend
    // ----------------------------------------------------------------

    private void fetchFarmerProfile() {
        apiService.getFarmerProfile().enqueue(new Callback<Farmer>() {
            @Override
            public void onResponse(@NonNull Call<Farmer> call, @NonNull Response<Farmer> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    Farmer farmer = response.body();
                    Log.d(TAG, "Real farmer profile loaded: " + farmer.getName());
                    updateUiWithFarmer(farmer);

                    // Fetch weather after we have the location
                    if (farmer.getState() != null && !farmer.getState().isEmpty()) {
                        fetchWeather(farmer.getState(), farmer.getDistrict());
                    } else {
                        showWeatherUnavailable();
                    }

                } else if (response.code() == 404) {
                    Log.w(TAG, "Farmer profile not found (404)");
                    showWeatherUnavailable();
                } else if (response.code() == 401 || response.code() == 403) {
                    Log.e(TAG, "Auth error " + response.code());
                    Toast.makeText(requireContext(), "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
                    showWeatherUnavailable();
                } else {
                    Log.w(TAG, "Profile API returned " + response.code());
                    // Fall back to cached profile + try weather with cached location
                    String cachedState = prefs.getFarmerState();
                    String cachedDistrict = prefs.getFarmerDistrict();
                    if (!cachedState.isEmpty()) {
                        fetchWeather(cachedState, cachedDistrict);
                    } else {
                        showWeatherUnavailable();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Farmer> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "Profile API failure: " + t.getMessage());
                // Try weather with cached location
                String cachedState = prefs.getFarmerState();
                String cachedDistrict = prefs.getFarmerDistrict();
                if (!cachedState.isEmpty()) {
                    fetchWeather(cachedState, cachedDistrict);
                } else {
                    showWeatherUnavailable();
                }
            }
        });
    }

    private void updateUiWithFarmer(Farmer farmer) {
        // Update greeting
        String name = farmer.getName() != null && !farmer.getName().isEmpty()
                ? farmer.getName() : "Farmer";
        setGreeting(name);

        // Update location
        updateLocation(farmer.getDistrict(), farmer.getState());

        // Cache fresh profile locally
        prefs.saveFarmerProfile(
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
    // Fetch weather from backend
    // ----------------------------------------------------------------

    private void fetchWeather(String state, String district) {
        Log.d(TAG, "Fetching weather for state=" + state + " district=" + district);

        apiService.getWeather(state, district).enqueue(new Callback<WeatherInfo>() {
            @Override
            public void onResponse(@NonNull Call<WeatherInfo> call, @NonNull Response<WeatherInfo> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    WeatherInfo weather = response.body();
                    displayWeather(weather);
                    Log.d(TAG, "Weather loaded: " + weather.getCondition()
                            + " " + weather.getTemperature() + "°C");
                } else {
                    Log.w(TAG, "Weather API returned " + response.code());
                    showWeatherUnavailable();
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherInfo> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "Weather API failure: " + t.getMessage());
                showWeatherUnavailable();
            }
        });
    }

    // ----------------------------------------------------------------
    // UI update helpers
    // ----------------------------------------------------------------

    private void displayWeather(@NonNull WeatherInfo weather) {
        this.currentWeather = weather;
        if (tvWeatherStatusBadge != null) {
            if (weather.isCached()) {
                tvWeatherStatusBadge.setText("📡 Cached");
                tvWeatherStatusBadge.setVisibility(View.VISIBLE);
            } else {
                tvWeatherStatusBadge.setVisibility(View.GONE);
            }
        }

        if (tvTemperature != null) {
            tvTemperature.setText(getString(R.string.temperature_format, (int) weather.getTemperature()));
        }
        if (tvHumidity != null) {
            tvHumidity.setText(getString(R.string.humidity_format, weather.getHumidity()));
        }
        if (tvRain != null) {
            tvRain.setText(getString(R.string.rain_probability_format, weather.getRainProbability()));
        }
        if (tvWind != null) {
            tvWind.setText(getString(R.string.wind_speed_format, (int) weather.getWindSpeed()));
        }
        if (tvWeatherLastUpdated != null && weather.getLastUpdated() != null) {
            tvWeatherLastUpdated.setText("Updated: " + formatTimestamp(weather.getLastUpdated()));
            tvWeatherLastUpdated.setVisibility(View.VISIBLE);
        }
    }

    private void showWeatherLoading() {
        if (tvTemperature != null) tvTemperature.setText("--°C");
        if (tvHumidity != null) tvHumidity.setText("--%");
        if (tvRain != null) tvRain.setText("--%");
        if (tvWind != null) tvWind.setText("-- km/h");
        if (tvWeatherStatusBadge != null) {
            tvWeatherStatusBadge.setText("Loading...");
            tvWeatherStatusBadge.setVisibility(View.VISIBLE);
        }
    }

    private void showWeatherUnavailable() {
        if (tvWeatherStatusBadge != null) {
            tvWeatherStatusBadge.setText("⚠ Unavailable");
            tvWeatherStatusBadge.setVisibility(View.VISIBLE);
        }
        if (tvTemperature != null) tvTemperature.setText("--°C");
        if (tvHumidity != null) tvHumidity.setText("--%");
        if (tvRain != null) tvRain.setText("--%");
        if (tvWind != null) tvWind.setText("-- km/h");
    }

    private void showDemoWeather() {
        WeatherInfo weather = MockDataProvider.getDemoWeather();
        this.currentWeather = weather;
        if (tvWeatherStatusBadge != null) {
            tvWeatherStatusBadge.setText("📋 Demo");
            tvWeatherStatusBadge.setVisibility(View.VISIBLE);
        }
        if (tvTemperature != null) tvTemperature.setText(getString(R.string.temperature_format, (int) weather.getTemperature()));
        if (tvHumidity != null) tvHumidity.setText(getString(R.string.humidity_format, weather.getHumidity()));
        if (tvRain != null) tvRain.setText(getString(R.string.rain_probability_format, weather.getRainProbability()));
        if (tvWind != null) tvWind.setText(getString(R.string.wind_speed_format, (int) weather.getWindSpeed()));
    }

    private void showDemoCropStatus() {
        if (tvSowingDate != null) tvSowingDate.setText(getString(R.string.demo_sowing_date));
        if (tvCurrentStage != null) tvCurrentStage.setText(getString(R.string.demo_current_stage));
        if (tvNextActivity != null) tvNextActivity.setText(getString(R.string.demo_next_activity));
    }

    private void setGreeting(String name) {
        if (tvGreeting == null) return;
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour < 12) {
            greeting = getString(R.string.greeting_morning, name);
        } else if (hour < 17) {
            greeting = getString(R.string.greeting_afternoon, name);
        } else {
            greeting = getString(R.string.greeting_evening, name);
        }
        tvGreeting.setText(greeting);
    }

    private void updateLocation(@Nullable String district, @Nullable String state) {
        if (tvLocation == null) return;
        boolean hasDistrict = district != null && !district.trim().isEmpty();
        boolean hasState    = state != null && !state.trim().isEmpty();
        if (hasDistrict && hasState) {
            tvLocation.setText(getString(R.string.location_format, district.trim(), state.trim()));
        } else if (hasState) {
            tvLocation.setText("📍 " + state.trim());
        } else if (hasDistrict) {
            tvLocation.setText("📍 " + district.trim());
        } else {
            tvLocation.setText("📍 Location not set");
        }
    }

    private String formatTimestamp(String isoTimestamp) {
        // Simple formatting — show just the time portion
        if (isoTimestamp == null) return "";
        if (isoTimestamp.contains("T")) {
            String time = isoTimestamp.split("T")[1];
            return time.length() >= 5 ? time.substring(0, 5) : time;
        }
        return isoTimestamp;
    }
}
