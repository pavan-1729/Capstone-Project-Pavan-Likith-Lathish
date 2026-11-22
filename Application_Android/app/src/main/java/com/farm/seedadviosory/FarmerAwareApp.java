package com.farm.seedadviosory;

import android.app.Application;
import android.content.Context;

import com.farm.seedadviosory.utils.LocaleHelper;

/**
 * Custom Application class.
 * Applies saved locale on app startup.
 */
public class FarmerAwareApp extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.applyLocale(base));
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
