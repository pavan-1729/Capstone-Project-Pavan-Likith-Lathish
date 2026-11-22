package com.farm.seedadviosory.ui.main;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.farm.seedadviosory.R;
import com.farm.seedadviosory.ui.crophealth.CropHealthFragment;
import com.farm.seedadviosory.ui.home.HomeFragment;
import com.farm.seedadviosory.ui.notifications.NotificationsFragment;
import com.farm.seedadviosory.ui.profile.ProfileFragment;
import com.farm.seedadviosory.ui.seed.SeedAdvisorFragment;
import com.farm.seedadviosory.utils.LocaleHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Main activity hosting fragments via BottomNavigationView.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (itemId == R.id.nav_seeds) {
                fragment = new SeedAdvisorFragment();
            } else if (itemId == R.id.nav_crop_health) {
                fragment = new CropHealthFragment();
            } else if (itemId == R.id.nav_notifications) {
                fragment = new NotificationsFragment();
            } else if (itemId == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            return true;
        });

        // Default fragment
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }
}
