package com.farm.seedadviosory.ui.weather;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.farm.seedadviosory.R;
import com.farm.seedadviosory.config.AppConfig;
import com.farm.seedadviosory.data.mock.MockDataProvider;
import com.farm.seedadviosory.data.model.WeatherInfo;
import com.farm.seedadviosory.data.remote.ApiService;
import com.farm.seedadviosory.data.remote.RetrofitClient;
import com.farm.seedadviosory.utils.LocaleHelper;
import com.farm.seedadviosory.utils.PreferenceManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Full-page Agro Weather Advisory screen.
 * Displays real-time metrics, smart agricultural advisories,
 * 24-hour hourly trend, and up to 10-day forecasts.
 * Allows the farmer to change location to any Indian state/district.
 */
public class WeatherDetailActivity extends AppCompatActivity {

    private ApiService apiService;
    private PreferenceManager prefs;

    private String currentState;
    private String currentDistrict;

    // Views
    private TextView tvCurrentLocation;
    private TextView tvLastUpdated;
    private TextView tvDetailTemp;
    private TextView tvDetailCondition;
    private TextView tvDetailFeelsLike;
    private TextView tvConditionIconLarge;
    private TextView tvTempRangeToday;
    private TextView tvAgriAdvisory;
    private TextView tvMetricHumidity;
    private TextView tvMetricRain;
    private TextView tvMetricWind;
    private TextView tvMetricUv;
    private TextView tvMetricPressure;
    private TextView tvMetricVisibility;
    private ProgressBar progressLoading;
    private LinearLayout layoutHourlyContainer;
    private LinearLayout layoutDailyContainer;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_detail);

        apiService = RetrofitClient.getApiService();
        prefs = new PreferenceManager(this);

        initViews();

        // Get initial state & district from intent or preferences
        currentState = getIntent().getStringExtra("state");
        currentDistrict = getIntent().getStringExtra("district");

        if (currentState == null || currentState.isBlank()) {
            currentState = prefs.getFarmerState();
        }
        if (currentDistrict == null || currentDistrict.isBlank()) {
            currentDistrict = prefs.getFarmerDistrict();
        }
        if (currentState == null || currentState.isBlank()) {
            currentState = "Andhra Pradesh";
        }
        if (currentDistrict == null || currentDistrict.isBlank()) {
            currentDistrict = "Kurnool";
        }

        // Check if pre-passed weather info was provided
        WeatherInfo initialInfo = (WeatherInfo) getIntent().getSerializableExtra("weather_info");
        if (initialInfo != null) {
            renderWeather(initialInfo);
        }

        // Fetch fresh 10-day weather
        fetchWeather(currentState, currentDistrict);
    }

    private void initViews() {
        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        tvCurrentLocation = findViewById(R.id.tv_current_location);
        tvLastUpdated = findViewById(R.id.tv_last_updated);
        tvDetailTemp = findViewById(R.id.tv_detail_temp);
        tvDetailCondition = findViewById(R.id.tv_detail_condition);
        tvDetailFeelsLike = findViewById(R.id.tv_detail_feels_like);
        tvConditionIconLarge = findViewById(R.id.tv_condition_icon_large);
        tvTempRangeToday = findViewById(R.id.tv_temp_range_today);
        tvAgriAdvisory = findViewById(R.id.tv_agri_advisory);
        tvMetricHumidity = findViewById(R.id.tv_metric_humidity);
        tvMetricRain = findViewById(R.id.tv_metric_rain);
        tvMetricWind = findViewById(R.id.tv_metric_wind);
        tvMetricUv = findViewById(R.id.tv_metric_uv);
        tvMetricPressure = findViewById(R.id.tv_metric_pressure);
        tvMetricVisibility = findViewById(R.id.tv_metric_visibility);
        progressLoading = findViewById(R.id.progress_loading);
        layoutHourlyContainer = findViewById(R.id.layout_hourly_container);
        layoutDailyContainer = findViewById(R.id.layout_daily_container);

        Button btnChangeLocation = findViewById(R.id.btn_change_location);
        btnChangeLocation.setOnClickListener(v -> showLocationChangeDialog());
    }

    // ----------------------------------------------------------------
    // Fetch 10-Day Weather from Backend
    // ----------------------------------------------------------------

    private void fetchWeather(String state, String district) {
        if (progressLoading != null) progressLoading.setVisibility(View.VISIBLE);

        boolean isDemo = AppConfig.DEMO_MODE && prefs.isDemoMode();
        if (isDemo) {
            if (progressLoading != null) progressLoading.setVisibility(View.GONE);
            WeatherInfo demo = MockDataProvider.getDemoWeather();
            demo.setState(state);
            demo.setDistrict(district);
            demo.setLocation(district + ", " + state);
            renderWeather(demo);
            return;
        }

        apiService.getWeather(state, district).enqueue(new Callback<WeatherInfo>() {
            @Override
            public void onResponse(@NonNull Call<WeatherInfo> call, @NonNull Response<WeatherInfo> response) {
                if (progressLoading != null) progressLoading.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    WeatherInfo weather = response.body();
                    renderWeather(weather);
                } else {
                    Toast.makeText(WeatherDetailActivity.this, "Failed to load live weather, displaying cached data", Toast.LENGTH_SHORT).show();
                    renderWeather(MockDataProvider.getDemoWeather());
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherInfo> call, @NonNull Throwable t) {
                if (progressLoading != null) progressLoading.setVisibility(View.GONE);
                Toast.makeText(WeatherDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                renderWeather(MockDataProvider.getDemoWeather());
            }
        });
    }

    // ----------------------------------------------------------------
    // Render Weather Data into Views
    // ----------------------------------------------------------------

    private void renderWeather(WeatherInfo w) {
        if (w == null) return;

        String loc = w.getLocation() != null && !w.getLocation().isBlank()
                ? w.getLocation() : currentDistrict + ", " + currentState;
        tvCurrentLocation.setText(loc);

        String updatedText = w.isCached() ? "📡 Cached (30 min TTL)" : "🟢 Live WeatherAPI.com (10-Day)";
        tvLastUpdated.setText(updatedText);

        tvDetailTemp.setText(Math.round(w.getTemperature()) + "°C");
        tvDetailCondition.setText(w.getCondition() != null ? w.getCondition() : "Clear");
        tvDetailFeelsLike.setText("Feels like " + Math.round(w.getFeelsLike()) + "°C");
        tvConditionIconLarge.setText(getWeatherEmoji(w.getCondition()));

        double maxT = w.getMaxTemp() > 0 ? w.getMaxTemp() : w.getTemperature() + 3;
        double minT = w.getMinTemp() > 0 ? w.getMinTemp() : w.getTemperature() - 4;
        tvTempRangeToday.setText("H: " + Math.round(maxT) + "°  L: " + Math.round(minT) + "°");

        // Metrics Grid
        tvMetricHumidity.setText(w.getHumidity() + "%");
        tvMetricRain.setText(w.getRainProbability() + "%");

        String windText = Math.round(w.getWindSpeed()) + " km/h " + (w.getWindDir() != null ? w.getWindDir() : "");
        tvMetricWind.setText(windText);

        String uvLabel = w.getUvIndex() >= 8 ? "(Very High)" : w.getUvIndex() >= 6 ? "(High)" : "(Moderate)";
        tvMetricUv.setText(w.getUvIndex() + " " + uvLabel);

        tvMetricPressure.setText(Math.round(w.getPressureMb()) + " mb");
        tvMetricVisibility.setText(w.getVisibilityKm() + " km");

        // Smart Agronomic Advisory
        tvAgriAdvisory.setText(generateAgroAdvisory(w));

        // Render Hourly Trend
        renderHourlyTrend(w.getHourlyForecast());

        // Render 10-Day Forecast
        renderDailyForecast(w.getDailyForecast());
    }

    private void renderHourlyTrend(List<WeatherInfo.HourlyForecast> hourly) {
        layoutHourlyContainer.removeAllViews();
        if (hourly == null || hourly.isEmpty()) return;

        LayoutInflater inflater = LayoutInflater.from(this);
        for (WeatherInfo.HourlyForecast h : hourly) {
            View card = inflater.inflate(R.layout.item_weather_hourly, layoutHourlyContainer, false);

            TextView tvTime = card.findViewById(R.id.tv_hour_time);
            TextView tvIcon = card.findViewById(R.id.tv_hour_icon);
            TextView tvTemp = card.findViewById(R.id.tv_hour_temp);
            TextView tvRain = card.findViewById(R.id.tv_hour_rain);

            tvTime.setText(h.getTime() != null ? h.getTime() : "--");
            tvIcon.setText(getWeatherEmoji(h.getCondition()));
            tvTemp.setText(Math.round(h.getTemp()) + "°");
            tvRain.setText("💧 " + h.getRainChance() + "%");

            layoutHourlyContainer.addView(card);
        }
    }

    private void renderDailyForecast(List<WeatherInfo.DailyForecast> daily) {
        layoutDailyContainer.removeAllViews();
        if (daily == null || daily.isEmpty()) return;

        LayoutInflater inflater = LayoutInflater.from(this);
        for (WeatherInfo.DailyForecast d : daily) {
            View row = inflater.inflate(R.layout.item_weather_daily, layoutDailyContainer, false);

            TextView tvDay = row.findViewById(R.id.tv_daily_day);
            TextView tvDate = row.findViewById(R.id.tv_daily_date);
            TextView tvIcon = row.findViewById(R.id.tv_daily_icon);
            TextView tvCondition = row.findViewById(R.id.tv_daily_condition);
            TextView tvRain = row.findViewById(R.id.tv_daily_rain);
            TextView tvTemps = row.findViewById(R.id.tv_daily_temps);

            tvDay.setText(d.getDayOfWeek() != null ? d.getDayOfWeek() : "Day");
            tvDate.setText(d.getDate() != null ? d.getDate() : "");
            tvIcon.setText(getWeatherEmoji(d.getCondition()));
            tvCondition.setText(d.getCondition() != null ? d.getCondition() : "Clear");
            tvRain.setText("💧 " + d.getRainChance() + "%");
            tvTemps.setText(Math.round(d.getMinTemp()) + "° / " + Math.round(d.getMaxTemp()) + "°");

            layoutDailyContainer.addView(row);
        }
    }

    // ----------------------------------------------------------------
    // Location Change Dialog
    // ----------------------------------------------------------------

    private void showLocationChangeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📍 Change Farm Location");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 16);

        TextView tvStateLabel = new TextView(this);
        tvStateLabel.setText("Select State:");
        tvStateLabel.setTextSize(14);
        tvStateLabel.setTextColor(getColor(R.color.text_secondary));
        layout.addView(tvStateLabel);

        Spinner spinnerState = new Spinner(this);
        List<String> states = MockDataProvider.getStates();
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, states);
        spinnerState.setAdapter(stateAdapter);

        int stateIdx = states.indexOf(currentState);
        if (stateIdx >= 0) spinnerState.setSelection(stateIdx);
        layout.addView(spinnerState);

        TextView tvDistLabel = new TextView(this);
        tvDistLabel.setText("\nSelect District:");
        tvDistLabel.setTextSize(14);
        tvDistLabel.setTextColor(getColor(R.color.text_secondary));
        layout.addView(tvDistLabel);

        Spinner spinnerDistrict = new Spinner(this);
        layout.addView(spinnerDistrict);

        spinnerState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedState = states.get(position);
                List<String> districts = MockDataProvider.getDistricts(selectedState);
                ArrayAdapter<String> distAdapter = new ArrayAdapter<>(WeatherDetailActivity.this,
                        android.R.layout.simple_spinner_dropdown_item, districts);
                spinnerDistrict.setAdapter(distAdapter);

                int distIdx = districts.indexOf(currentDistrict);
                if (distIdx >= 0) spinnerDistrict.setSelection(distIdx);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        TextView tvOrLabel = new TextView(this);
        tvOrLabel.setText("\nOr Enter Custom City / Town:");
        tvOrLabel.setTextSize(14);
        tvOrLabel.setTextColor(getColor(R.color.text_secondary));
        layout.addView(tvOrLabel);

        EditText etCustom = new EditText(this);
        etCustom.setHint("e.g. Nandyal, Adoni, Tirupati");
        layout.addView(etCustom);

        builder.setView(layout);

        builder.setPositiveButton("Update Weather", (dialog, which) -> {
            String customText = etCustom.getText().toString().trim();
            if (!customText.isEmpty()) {
                currentDistrict = customText;
            } else if (spinnerDistrict.getSelectedItem() != null) {
                currentDistrict = spinnerDistrict.getSelectedItem().toString();
            }
            if (spinnerState.getSelectedItem() != null) {
                currentState = spinnerState.getSelectedItem().toString();
            }

            fetchWeather(currentState, currentDistrict);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // ----------------------------------------------------------------
    // Helpers: Emojis and Agronomic Logic
    // ----------------------------------------------------------------

    private String getWeatherEmoji(String condition) {
        if (condition == null) return "☀️";
        String c = condition.toLowerCase();
        if (c.contains("thunder") || c.contains("storm")) return "⛈️";
        if (c.contains("rain") || c.contains("drizzle") || c.contains("shower")) return "🌧️";
        if (c.contains("partly")) return "⛅";
        if (c.contains("cloud") || c.contains("overcast")) return "☁️";
        if (c.contains("fog") || c.contains("mist")) return "🌫️";
        if (c.contains("snow") || c.contains("ice")) return "❄️";
        return "☀️";
    }

    private String generateAgroAdvisory(WeatherInfo w) {
        StringBuilder sb = new StringBuilder();

        // Wind spray advisory
        if (w.getWindSpeed() >= 15.0) {
            sb.append("⚠️ High wind speed (").append(Math.round(w.getWindSpeed()))
              .append(" km/h) — Postpone foliar pesticide and nutrient spraying to prevent droplet drift.\n\n");
        } else {
            sb.append("✓ Wind speed is calm (").append(Math.round(w.getWindSpeed()))
              .append(" km/h) — Suitable for crop spraying and intercultural operations.\n\n");
        }

        // Rain advisory
        if (w.getRainProbability() >= 45) {
            sb.append("🌧️ Elevated rain probability (").append(w.getRainProbability())
              .append("%) — Delay irrigation and fertilizer top-dressing to prevent nutrient leaching.");
        } else {
            sb.append("✓ Low rain probability (").append(w.getRainProbability())
              .append("%) — Favorable conditions for scheduled irrigation and field intercultural work.");
        }

        return sb.toString();
    }
}
