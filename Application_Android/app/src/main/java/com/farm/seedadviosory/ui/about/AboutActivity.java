package com.farm.seedadviosory.ui.about;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.farm.seedadviosory.R;
import com.farm.seedadviosory.config.AppConfig;
import com.farm.seedadviosory.utils.LocaleHelper;

/**
 * About screen displaying research project details, methodology, ICAR data citations,
 * and academic background.
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        TextView tvVersion = findViewById(R.id.tv_about_version);
        if (tvVersion != null) {
            tvVersion.setText("Version " + AppConfig.APP_VERSION + " (" + AppConfig.BUILD_TYPE + ") • Demo Mode Active");
        }

        Button btnHowItWorks = findViewById(R.id.btn_how_it_works);
        if (btnHowItWorks != null) {
            btnHowItWorks.setOnClickListener(v ->
                    startActivity(new Intent(this, HowItWorksActivity.class)));
        }
    }
}
