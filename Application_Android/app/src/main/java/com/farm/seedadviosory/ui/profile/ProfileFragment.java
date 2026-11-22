package com.farm.seedadviosory.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.farm.seedadviosory.R;
import com.farm.seedadviosory.config.AppConfig;
import com.farm.seedadviosory.ui.about.HowItWorksActivity;
import com.farm.seedadviosory.ui.auth.LanguageSelectionActivity;
import com.farm.seedadviosory.ui.auth.LoginActivity;
import com.farm.seedadviosory.utils.PreferenceManager;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PreferenceManager prefs = new PreferenceManager(requireContext());

        // Profile info
        ((TextView) view.findViewById(R.id.tv_farmer_name)).setText(prefs.getFarmerName());
        ((TextView) view.findViewById(R.id.tv_farmer_mobile)).setText(prefs.getFarmerMobile());
        ((TextView) view.findViewById(R.id.tv_farmer_email)).setText(prefs.getFarmerEmail());
        ((TextView) view.findViewById(R.id.tv_farmer_location)).setText(
                prefs.getFarmerDistrict() + ", " + prefs.getFarmerState());
        ((TextView) view.findViewById(R.id.tv_primary_crop)).setText(prefs.getPrimaryCrop());
        ((TextView) view.findViewById(R.id.tv_app_version)).setText(
                "v" + AppConfig.APP_VERSION + " (" + AppConfig.BUILD_TYPE + ")");

        // Settings
        View btnSettings = view.findViewById(R.id.btn_settings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), com.farm.seedadviosory.ui.settings.SettingsActivity.class)));
        }

        // Change Language
        view.findViewById(R.id.btn_change_language).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), LanguageSelectionActivity.class);
            startActivity(intent);
        });

        // About
        view.findViewById(R.id.btn_about).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), com.farm.seedadviosory.ui.about.AboutActivity.class)));

        // Logout
        Button btnLogout = view.findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
            prefs.clearAll();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
