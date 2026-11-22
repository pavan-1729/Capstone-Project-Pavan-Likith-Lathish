package com.farm.seedadviosory.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Wrapper around SharedPreferences for managing local app state.
 *
 * Important:
 *   - Defaults return empty strings (not hardcoded demo values).
 *   - Demo farmer profile is only populated when the user explicitly
 *     chooses "Continue as Demo" in LoginActivity.
 *   - isDemoMode() defaults to false (production mode).
 */
public class PreferenceManager {

    private static final String PREF_NAME = "farmer_aware_prefs";

    private static final String KEY_LOGGED_IN          = "is_logged_in";
    private static final String KEY_LANGUAGE           = "selected_language";
    private static final String KEY_LANGUAGE_SELECTED  = "language_selected";
    private static final String KEY_DEMO_MODE          = "demo_mode";
    private static final String KEY_FARMER_NAME        = "farmer_name";
    private static final String KEY_FARMER_MOBILE      = "farmer_mobile";
    private static final String KEY_FARMER_EMAIL       = "farmer_email";
    private static final String KEY_FARMER_STATE       = "farmer_state";
    private static final String KEY_FARMER_DISTRICT    = "farmer_district";
    private static final String KEY_FARMER_VILLAGE     = "farmer_village";
    private static final String KEY_PRIMARY_CROP       = "primary_crop";
    private static final String KEY_FARM_SIZE          = "farm_size";
    private static final String KEY_PREFERRED_LANGUAGE = "preferred_language";
    private static final String KEY_ALERT_WEATHER      = "alert_weather";
    private static final String KEY_ALERT_CROP         = "alert_crop";
    private static final String KEY_ALERT_DISEASE      = "alert_disease";

    private final SharedPreferences prefs;

    public PreferenceManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // ----------------------------------------------------------------
    // Login state
    // ----------------------------------------------------------------

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public void setLoggedIn(boolean loggedIn) {
        prefs.edit().putBoolean(KEY_LOGGED_IN, loggedIn).apply();
    }

    // ----------------------------------------------------------------
    // Demo mode
    // IMPORTANT: defaults to FALSE — production mode by default
    // ----------------------------------------------------------------

    public boolean isDemoMode() {
        return prefs.getBoolean(KEY_DEMO_MODE, false);
    }

    public void setDemoMode(boolean demoMode) {
        prefs.edit().putBoolean(KEY_DEMO_MODE, demoMode).apply();
    }

    // ----------------------------------------------------------------
    // Language
    // ----------------------------------------------------------------

    public String getLanguage() {
        return prefs.getString(KEY_LANGUAGE, "en");
    }

    public void setLanguage(String languageCode) {
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply();
    }

    public void setPreferredLanguage(String languageCode) {
        prefs.edit()
             .putString(KEY_LANGUAGE, languageCode)
             .putString(KEY_PREFERRED_LANGUAGE, languageCode)
             .apply();
    }

    public String getPreferredLanguage() {
        return prefs.getString(KEY_PREFERRED_LANGUAGE, prefs.getString(KEY_LANGUAGE, "en"));
    }

    public boolean isLanguageSelected() {
        return prefs.getBoolean(KEY_LANGUAGE_SELECTED, false);
    }

    public void setLanguageSelected(boolean selected) {
        prefs.edit().putBoolean(KEY_LANGUAGE_SELECTED, selected).apply();
    }

    // ----------------------------------------------------------------
    // Farmer profile — all defaults are empty strings, not demo values
    // ----------------------------------------------------------------

    public String getFarmerName() {
        return prefs.getString(KEY_FARMER_NAME, "");
    }

    public void setFarmerName(String name) {
        prefs.edit().putString(KEY_FARMER_NAME, name).apply();
    }

    public String getFarmerMobile() {
        return prefs.getString(KEY_FARMER_MOBILE, "");
    }

    public void setFarmerMobile(String mobile) {
        prefs.edit().putString(KEY_FARMER_MOBILE, mobile).apply();
    }

    public String getFarmerEmail() {
        return prefs.getString(KEY_FARMER_EMAIL, "");
    }

    public void setFarmerEmail(String email) {
        prefs.edit().putString(KEY_FARMER_EMAIL, email).apply();
    }

    public String getFarmerState() {
        return prefs.getString(KEY_FARMER_STATE, "");
    }

    public void setFarmerState(String state) {
        prefs.edit().putString(KEY_FARMER_STATE, state).apply();
    }

    public String getFarmerDistrict() {
        return prefs.getString(KEY_FARMER_DISTRICT, "");
    }

    public void setFarmerDistrict(String district) {
        prefs.edit().putString(KEY_FARMER_DISTRICT, district).apply();
    }

    public String getFarmerVillage() {
        return prefs.getString(KEY_FARMER_VILLAGE, "");
    }

    public void setFarmerVillage(String village) {
        prefs.edit().putString(KEY_FARMER_VILLAGE, village).apply();
    }

    public String getPrimaryCrop() {
        return prefs.getString(KEY_PRIMARY_CROP, "Maize");
    }

    public void setPrimaryCrop(String crop) {
        prefs.edit().putString(KEY_PRIMARY_CROP, crop).apply();
    }

    public String getFarmSize() {
        return prefs.getString(KEY_FARM_SIZE, "");
    }

    public void setFarmSize(String farmSize) {
        prefs.edit().putString(KEY_FARM_SIZE, farmSize).apply();
    }

    // ----------------------------------------------------------------
    // Notification preferences
    // ----------------------------------------------------------------

    public boolean isWeatherAlertsEnabled() {
        return prefs.getBoolean(KEY_ALERT_WEATHER, true);
    }

    public void setWeatherAlertsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ALERT_WEATHER, enabled).apply();
    }

    public boolean isCropRemindersEnabled() {
        return prefs.getBoolean(KEY_ALERT_CROP, true);
    }

    public void setCropRemindersEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ALERT_CROP, enabled).apply();
    }

    public boolean isDiseaseAlertsEnabled() {
        return prefs.getBoolean(KEY_ALERT_DISEASE, true);
    }

    public void setDiseaseAlertsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ALERT_DISEASE, enabled).apply();
    }

    // ----------------------------------------------------------------
    // Save farmer profile from API response
    // ----------------------------------------------------------------

    public void saveFarmerProfile(
            String name, String mobile, String email,
            String state, String district, String village,
            String preferredLanguage, String primaryCrop, String farmSize
    ) {
        SharedPreferences.Editor editor = prefs.edit();
        if (name != null)             editor.putString(KEY_FARMER_NAME, name);
        if (mobile != null)           editor.putString(KEY_FARMER_MOBILE, mobile);
        if (email != null)            editor.putString(KEY_FARMER_EMAIL, email);
        if (state != null)            editor.putString(KEY_FARMER_STATE, state);
        if (district != null)         editor.putString(KEY_FARMER_DISTRICT, district);
        if (village != null)          editor.putString(KEY_FARMER_VILLAGE, village);
        if (preferredLanguage != null) {
            editor.putString(KEY_PREFERRED_LANGUAGE, preferredLanguage);
            editor.putString(KEY_LANGUAGE, preferredLanguage);
        }
        if (primaryCrop != null)      editor.putString(KEY_PRIMARY_CROP, primaryCrop);
        if (farmSize != null)         editor.putString(KEY_FARM_SIZE, farmSize);
        editor.apply();
    }

    // ----------------------------------------------------------------
    // Demo farmer profile — only set explicitly on demo login
    // ----------------------------------------------------------------

    public void saveDemoFarmerProfile() {
        prefs.edit()
                .putString(KEY_FARMER_NAME, "Demo Farmer")
                .putString(KEY_FARMER_MOBILE, "9876543210")
                .putString(KEY_FARMER_EMAIL, "demo@farmeraware.com")
                .putString(KEY_FARMER_STATE, "Andhra Pradesh")
                .putString(KEY_FARMER_DISTRICT, "Kurnool")
                .putString(KEY_FARMER_VILLAGE, "")
                .putString(KEY_PRIMARY_CROP, "Maize")
                .putBoolean(KEY_DEMO_MODE, true)
                .apply();
    }

    // ----------------------------------------------------------------
    // Clear all data on logout
    // ----------------------------------------------------------------

    public void clearAll() {
        String lang = getLanguage();
        boolean langSelected = isLanguageSelected();
        prefs.edit().clear().apply();
        setLanguage(lang);
        setLanguageSelected(langSelected);
    }
}
