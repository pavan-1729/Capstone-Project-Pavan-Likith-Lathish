package com.farm.seedadviosory.data.repository;

import com.farm.seedadviosory.config.AppConfig;
import com.farm.seedadviosory.data.mock.MockDataProvider;
import com.farm.seedadviosory.data.model.*;

import java.util.List;

/**
 * Repository for crop-related data (calendar, advisories, disease knowledge).
 */
public class CropRepository {

    public List<CropCalendarStage> getCropCalendar() {
        if (AppConfig.DEMO_MODE) {
            return MockDataProvider.getDemoCropCalendar();
        }
        return null;
    }

    public List<CropAdvisory> getAdvisories() {
        if (AppConfig.DEMO_MODE) {
            return MockDataProvider.getDemoAdvisories();
        }
        return null;
    }

    public List<String[]> getDiseaseKnowledge() {
        if (AppConfig.DEMO_MODE) {
            return MockDataProvider.getDiseaseKnowledge();
        }
        return null;
    }

    public WeatherInfo getWeather() {
        if (AppConfig.DEMO_MODE) {
            return MockDataProvider.getDemoWeather();
        }
        return null;
    }

    public List<NotificationItem> getNotifications() {
        if (AppConfig.DEMO_MODE) {
            return MockDataProvider.getDemoNotifications();
        }
        return null;
    }
}
