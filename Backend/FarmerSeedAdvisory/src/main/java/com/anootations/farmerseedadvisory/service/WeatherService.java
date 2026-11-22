package com.anootations.farmerseedadvisory.service;

import com.anootations.farmerseedadvisory.dto.WeatherResponse;
import com.anootations.farmerseedadvisory.dto.WeatherResponse.DailyForecast;
import com.anootations.farmerseedadvisory.dto.WeatherResponse.HourlyForecast;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Fetches real-time weather, 24-hour hourly trend, and up to 10-day forecasts from WeatherAPI.com.
 * The Android application never contacts the weather provider directly —
 * all API credentials are held here on the server.
 *
 * Includes an in-memory cache with configurable TTL to minimize API quota usage.
 */
@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    @Value("${weather.api.key:}")
    private String apiKey;

    @Value("${weather.base-url:https://api.weatherapi.com/v1/}")
    private String baseUrl;

    @Value("${weather.cache.ttl-minutes:30}")
    private int cacheTtlMinutes;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    // Cache: key = "state:district", value = cached response + timestamp
    private final Map<String, CachedWeather> cache = new ConcurrentHashMap<>();

    // ----------------------------------------------------------------
    // Public API
    // ----------------------------------------------------------------

    public WeatherResponse getWeather(String state, String district) {
        if (state == null || state.isBlank()) {
            throw new IllegalArgumentException("State is required for weather lookup");
        }

        String cacheKey = buildCacheKey(state, district);

        // Return cached data if still fresh
        CachedWeather cached = cache.get(cacheKey);
        if (cached != null && !cached.isExpired(cacheTtlMinutes)) {
            log.debug("Returning cached weather for: {}", cacheKey);
            cached.response.setCached(true);
            return cached.response;
        }

        // Fetch fresh weather
        String query = buildLocationQuery(state, district);
        WeatherResponse response = fetchFromWeatherApi(query, state, district);

        cache.put(cacheKey, new CachedWeather(response, System.currentTimeMillis()));
        return response;
    }

    // ----------------------------------------------------------------
    // Internal: fetch from WeatherAPI.com with 10-day forecast
    // ----------------------------------------------------------------

    private WeatherResponse fetchFromWeatherApi(String query, String state, String district) {

        if (apiKey == null || apiKey.isBlank() || "YOUR_OPENWEATHERMAP_API_KEY".equals(apiKey)
                || "YOUR_WEATHERAPI_KEY".equals(apiKey)) {
            log.warn("Weather API key not configured — returning fallback weather");
            return buildFallbackWeather(state, district);
        }

        // Request 10 days forecast + hourly trend
        String formattedBaseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = formattedBaseUrl + "forecast.json?key=" + apiKey
                + "&q=" + encodedQuery
                + "&days=10&aqi=no&alerts=no";

        log.info("Fetching 10-day weather from WeatherAPI.com for query: {}", query);

        Request request = new Request.Builder().url(url).build();

        try (Response httpResponse = httpClient.newCall(request).execute()) {

            if (!httpResponse.isSuccessful() || httpResponse.body() == null) {
                log.warn("WeatherAPI.com returned HTTP {}", httpResponse.code());
                return buildFallbackWeather(state, district);
            }

            String body = httpResponse.body().string();
            return parseWeatherApiResponse(body, state, district);

        } catch (IOException e) {
            log.error("Failed to fetch weather from WeatherAPI.com: {}", e.getMessage());
            return buildFallbackWeather(state, district);
        }
    }

    // ----------------------------------------------------------------
    // Parse WeatherAPI.com JSON response (Current + Hourly + 10 Days)
    // ----------------------------------------------------------------

    private WeatherResponse parseWeatherApiResponse(String json, String state, String district) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            if (!root.has("current")) {
                log.warn("WeatherAPI response does not contain 'current' object");
                return buildFallbackWeather(state, district);
            }

            JsonObject current = root.getAsJsonObject("current");

            double temperature = current.has("temp_c") ? current.get("temp_c").getAsDouble() : 28.0;
            double feelsLike = current.has("feelslike_c") ? current.get("feelslike_c").getAsDouble() : temperature;
            int humidity = current.has("humidity") ? current.get("humidity").getAsInt() : 60;
            double windSpeed = current.has("wind_kph") ? current.get("wind_kph").getAsDouble() : 10.0;
            String windDir = current.has("wind_dir") ? current.get("wind_dir").getAsString() : "N";
            double uvIndex = current.has("uv") ? current.get("uv").getAsDouble() : 5.0;
            double pressureMb = current.has("pressure_mb") ? current.get("pressure_mb").getAsDouble() : 1012.0;
            double visibilityKm = current.has("vis_km") ? current.get("vis_km").getAsDouble() : 10.0;

            String condition = "Clear";
            String conditionIcon = "";
            if (current.has("condition")) {
                JsonObject condObj = current.getAsJsonObject("condition");
                if (condObj.has("text")) condition = condObj.get("text").getAsString();
                if (condObj.has("icon")) conditionIcon = condObj.get("icon").getAsString();
            }

            // Parse location from response if available
            String locationName = district != null && !district.isBlank() ? district + ", " + state : state;
            if (root.has("location")) {
                JsonObject loc = root.getAsJsonObject("location");
                String name = loc.has("name") ? loc.get("name").getAsString() : "";
                String region = loc.has("region") ? loc.get("region").getAsString() : "";
                if (!name.isBlank() && !region.isBlank()) {
                    locationName = name + ", " + region;
                }
            }

            int todayRainChance = 0;
            double todayMaxTemp = temperature;
            double todayMinTemp = temperature;

            List<HourlyForecast> hourlyList = new ArrayList<>();
            List<DailyForecast> dailyList = new ArrayList<>();

            if (root.has("forecast")) {
                JsonObject forecast = root.getAsJsonObject("forecast");
                if (forecast.has("forecastday")) {
                    JsonArray forecastDays = forecast.getAsJsonArray("forecastday");

                    for (int i = 0; i < forecastDays.size(); i++) {
                        JsonObject dayObj = forecastDays.get(i).getAsJsonObject();
                        String dateStr = dayObj.has("date") ? dayObj.get("date").getAsString() : "";

                        // Day of week
                        String dayOfWeek = "Day " + (i + 1);
                        if (!dateStr.isBlank()) {
                            try {
                                LocalDate date = LocalDate.parse(dateStr);
                                if (i == 0) {
                                    dayOfWeek = "Today";
                                } else {
                                    dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                                }
                            } catch (Exception ignored) {}
                        }

                        double maxT = temperature;
                        double minT = temperature;
                        int rainChance = 0;
                        int avgHumidity = humidity;
                        double maxWind = windSpeed;
                        String dayCondition = condition;
                        String dayIcon = conditionIcon;

                        if (dayObj.has("day")) {
                            JsonObject dayData = dayObj.getAsJsonObject("day");
                            if (dayData.has("maxtemp_c")) maxT = dayData.get("maxtemp_c").getAsDouble();
                            if (dayData.has("mintemp_c")) minT = dayData.get("mintemp_c").getAsDouble();
                            if (dayData.has("daily_chance_of_rain")) rainChance = dayData.get("daily_chance_of_rain").getAsInt();
                            if (dayData.has("avghumidity")) avgHumidity = dayData.get("avghumidity").getAsInt();
                            if (dayData.has("maxwind_kph")) maxWind = dayData.get("maxwind_kph").getAsDouble();

                            if (dayData.has("condition")) {
                                JsonObject c = dayData.getAsJsonObject("condition");
                                if (c.has("text")) dayCondition = c.get("text").getAsString();
                                if (c.has("icon")) dayIcon = c.get("icon").getAsString();
                            }
                        }

                        if (i == 0) {
                            todayRainChance = rainChance;
                            todayMaxTemp = maxT;
                            todayMinTemp = minT;

                            // Parse hourly forecast for today
                            if (dayObj.has("hour")) {
                                JsonArray hours = dayObj.getAsJsonArray("hour");
                                for (JsonElement hElem : hours) {
                                    JsonObject hObj = hElem.getAsJsonObject();
                                    String rawTime = hObj.has("time") ? hObj.get("time").getAsString() : "";
                                    String timeLabel = rawTime.contains(" ") ? rawTime.split(" ")[1] : rawTime;

                                    double hTemp = hObj.has("temp_c") ? hObj.get("temp_c").getAsDouble() : 0.0;
                                    int hRain = hObj.has("chance_of_rain") ? hObj.get("chance_of_rain").getAsInt() : 0;
                                    String hCond = "";
                                    String hIcon = "";
                                    if (hObj.has("condition")) {
                                        JsonObject hc = hObj.getAsJsonObject("condition");
                                        if (hc.has("text")) hCond = hc.get("text").getAsString();
                                        if (hc.has("icon")) hIcon = hc.get("icon").getAsString();
                                    }
                                    hourlyList.add(new HourlyForecast(timeLabel, hTemp, hRain, hCond, hIcon));
                                }
                            }
                        }

                        dailyList.add(new DailyForecast(dateStr, dayOfWeek, maxT, minT, rainChance, avgHumidity, maxWind, dayCondition, dayIcon));
                    }
                }
            }

            WeatherResponse response = new WeatherResponse();
            response.setTemperature(Math.round(temperature * 10.0) / 10.0);
            response.setFeelsLike(Math.round(feelsLike * 10.0) / 10.0);
            response.setMaxTemp(Math.round(todayMaxTemp * 10.0) / 10.0);
            response.setMinTemp(Math.round(todayMinTemp * 10.0) / 10.0);
            response.setHumidity(humidity);
            response.setRainProbability(todayRainChance);
            response.setWindSpeed(Math.round(windSpeed * 10.0) / 10.0);
            response.setWindDir(windDir);
            response.setUvIndex(uvIndex);
            response.setPressureMb(pressureMb);
            response.setVisibilityKm(visibilityKm);
            response.setCondition(condition);
            response.setConditionIcon(conditionIcon);
            response.setLocation(locationName);
            response.setState(state);
            response.setDistrict(district);
            response.setHourlyForecast(hourlyList);
            response.setDailyForecast(dailyList);
            response.setLastUpdated(java.time.LocalDateTime.now());
            response.setCached(false);

            return response;

        } catch (Exception e) {
            log.error("Failed to parse WeatherAPI response: {}", e.getMessage(), e);
            return buildFallbackWeather(state, district);
        }
    }

    // ----------------------------------------------------------------
    // Fallback when API is unavailable/not configured
    // ----------------------------------------------------------------

    private WeatherResponse buildFallbackWeather(String state, String district) {
        String location = (district != null && !district.isBlank())
                ? district + ", " + state
                : state;

        WeatherResponse fallback = WeatherResponse.of(28.0, 65, 20, 12.0, "Unavailable", location, state, district);
        fallback.setCached(false);
        return fallback;
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private String buildLocationQuery(String state, String district) {
        if (district != null && !district.isBlank()) {
            return district + ", " + state + ", India";
        }
        return state + ", India";
    }

    private String buildCacheKey(String state, String district) {
        return (state + ":" + (district != null ? district : "")).toLowerCase();
    }

    // ----------------------------------------------------------------
    // Cache entry
    // ----------------------------------------------------------------

    private static class CachedWeather {
        final WeatherResponse response;
        final long fetchedAtMs;

        CachedWeather(WeatherResponse response, long fetchedAtMs) {
            this.response = response;
            this.fetchedAtMs = fetchedAtMs;
        }

        boolean isExpired(int ttlMinutes) {
            long ageMs = System.currentTimeMillis() - fetchedAtMs;
            return ageMs > (long) ttlMinutes * 60 * 1000;
        }
    }
}
