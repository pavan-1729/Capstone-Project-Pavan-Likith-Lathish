package com.farm.seedadviosory.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Android model for weather data returned by the Spring Boot backend.
 * Matches the backend WeatherResponse DTO.
 */
public class WeatherInfo implements Serializable {

    private double temperature;
    private double feelsLike;
    private double maxTemp;
    private double minTemp;
    private int humidity;
    private int rainProbability;
    private double windSpeed;
    private String windDir;
    private double uvIndex;
    private double pressureMb;
    private double visibilityKm;
    private String condition;
    private String conditionIcon;
    private String location;
    private String state;
    private String district;
    private String lastUpdated;
    private boolean cached;

    private List<HourlyForecast> hourlyForecast = new ArrayList<>();
    private List<DailyForecast> dailyForecast = new ArrayList<>();

    public WeatherInfo() {}

    // Convenience constructor for demo/fallback data
    public WeatherInfo(int temperature, int humidity, int rainProbability,
                       int windSpeed, String condition, boolean cached) {
        this.temperature = temperature;
        this.feelsLike = temperature;
        this.maxTemp = temperature + 3;
        this.minTemp = temperature - 4;
        this.humidity = humidity;
        this.rainProbability = rainProbability;
        this.windSpeed = windSpeed;
        this.windDir = "NE";
        this.uvIndex = 6.0;
        this.pressureMb = 1012.0;
        this.visibilityKm = 10.0;
        this.condition = condition;
        this.cached = cached;
    }

    public static class HourlyForecast implements Serializable {
        private String time;
        private double temp;
        private int rainChance;
        private String condition;
        private String icon;

        public HourlyForecast() {}

        public HourlyForecast(String time, double temp, int rainChance, String condition, String icon) {
            this.time = time;
            this.temp = temp;
            this.rainChance = rainChance;
            this.condition = condition;
            this.icon = icon;
        }

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }

        public double getTemp() { return temp; }
        public void setTemp(double temp) { this.temp = temp; }

        public int getRainChance() { return rainChance; }
        public void setRainChance(int rainChance) { this.rainChance = rainChance; }

        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }

        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
    }

    public static class DailyForecast implements Serializable {
        private String date;
        private String dayOfWeek;
        private double maxTemp;
        private double minTemp;
        private int rainChance;
        private int humidity;
        private double maxWind;
        private String condition;
        private String icon;

        public DailyForecast() {}

        public DailyForecast(String date, String dayOfWeek, double maxTemp, double minTemp,
                             int rainChance, int humidity, double maxWind, String condition, String icon) {
            this.date = date;
            this.dayOfWeek = dayOfWeek;
            this.maxTemp = maxTemp;
            this.minTemp = minTemp;
            this.rainChance = rainChance;
            this.humidity = humidity;
            this.maxWind = maxWind;
            this.condition = condition;
            this.icon = icon;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public String getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

        public double getMaxTemp() { return maxTemp; }
        public void setMaxTemp(double maxTemp) { this.maxTemp = maxTemp; }

        public double getMinTemp() { return minTemp; }
        public void setMinTemp(double minTemp) { this.minTemp = minTemp; }

        public int getRainChance() { return rainChance; }
        public void setRainChance(int rainChance) { this.rainChance = rainChance; }

        public int getHumidity() { return humidity; }
        public void setHumidity(int humidity) { this.humidity = humidity; }

        public double getMaxWind() { return maxWind; }
        public void setMaxWind(double maxWind) { this.maxWind = maxWind; }

        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }

        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
    }

    // Getters and Setters
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public double getFeelsLike() { return feelsLike; }
    public void setFeelsLike(double feelsLike) { this.feelsLike = feelsLike; }

    public double getMaxTemp() { return maxTemp; }
    public void setMaxTemp(double maxTemp) { this.maxTemp = maxTemp; }

    public double getMinTemp() { return minTemp; }
    public void setMinTemp(double minTemp) { this.minTemp = minTemp; }

    public int getHumidity() { return humidity; }
    public void setHumidity(int humidity) { this.humidity = humidity; }

    public int getRainProbability() { return rainProbability; }
    public void setRainProbability(int rainProbability) { this.rainProbability = rainProbability; }

    public double getWindSpeed() { return windSpeed; }
    public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }

    public String getWindDir() { return windDir; }
    public void setWindDir(String windDir) { this.windDir = windDir; }

    public double getUvIndex() { return uvIndex; }
    public void setUvIndex(double uvIndex) { this.uvIndex = uvIndex; }

    public double getPressureMb() { return pressureMb; }
    public void setPressureMb(double pressureMb) { this.pressureMb = pressureMb; }

    public double getVisibilityKm() { return visibilityKm; }
    public void setVisibilityKm(double visibilityKm) { this.visibilityKm = visibilityKm; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getConditionIcon() { return conditionIcon; }
    public void setConditionIcon(String conditionIcon) { this.conditionIcon = conditionIcon; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }

    public boolean isCached() { return cached; }
    public void setCached(boolean cached) { this.cached = cached; }

    public List<HourlyForecast> getHourlyForecast() { return hourlyForecast; }
    public void setHourlyForecast(List<HourlyForecast> hourlyForecast) { this.hourlyForecast = hourlyForecast; }

    public List<DailyForecast> getDailyForecast() { return dailyForecast; }
    public void setDailyForecast(List<DailyForecast> dailyForecast) { this.dailyForecast = dailyForecast; }

    public boolean isDemo() { return false; }
}
