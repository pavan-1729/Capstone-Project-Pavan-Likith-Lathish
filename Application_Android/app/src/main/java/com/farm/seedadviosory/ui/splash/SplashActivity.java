package com.farm.seedadviosory.ui.splash;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.farm.seedadviosory.R;
import com.farm.seedadviosory.ui.auth.LanguageSelectionActivity;
import com.farm.seedadviosory.ui.auth.LoginActivity;
import com.farm.seedadviosory.ui.main.MainActivity;
import com.farm.seedadviosory.utils.LocaleHelper;
import com.farm.seedadviosory.utils.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Splash screen with app branding.
 * Routes to Language Selection, Login, or Home based on state.
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2500;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(this::navigateNext, SPLASH_DELAY);
    }

    private void navigateNext() {
        PreferenceManager prefs = new PreferenceManager(this);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Intent intent;

        if (!prefs.isLanguageSelected()) {
            intent = new Intent(this, LanguageSelectionActivity.class);
        } else if (currentUser != null) {
            // Real authenticated session via Firebase Auth
            intent = new Intent(this, MainActivity.class);
        } else if (prefs.isLoggedIn() && prefs.isDemoMode()) {
            // UI testing session via Demo Mode
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
