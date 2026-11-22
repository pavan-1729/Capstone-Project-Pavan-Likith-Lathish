package com.farm.seedadviosory.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

/**
 * Helper class for runtime language switching.
 * Supports English (en), Telugu (te), Tamil (ta), and Hindi (hi).
 */
public class LocaleHelper {

    private LocaleHelper() { }

    /**
     * Apply the saved locale to the context.
     * Call this in Activity.attachBaseContext() and Application.attachBaseContext().
     */
    public static Context applyLocale(Context context) {
        PreferenceManager prefs = new PreferenceManager(context);
        String languageCode = prefs.getLanguage();
        return updateResources(context, languageCode);
    }

    /**
     * Set a new locale and persist the choice.
     */
    public static Context setLocale(Context context, String languageCode) {
        PreferenceManager prefs = new PreferenceManager(context);
        prefs.setLanguage(languageCode);
        prefs.setLanguageSelected(true);
        return updateResources(context, languageCode);
    }

    /**
     * Get the display name of a language code.
     */
    public static String getLanguageDisplayName(String languageCode) {
        switch (languageCode) {
            case "te": return "తెలుగు";
            case "ta": return "தமிழ்";
            case "hi": return "हिन्दी";
            default: return "English";
        }
    }

    private static Context updateResources(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);
        config.setLayoutDirection(locale);

        return context.createConfigurationContext(config);
    }
}
