package com.farm.seedadviosory.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.farm.seedadviosory.R;
import com.farm.seedadviosory.utils.LocaleHelper;
import com.farm.seedadviosory.utils.PreferenceManager;

/**
 * Language selection screen shown on first launch.
 */
public class LanguageSelectionActivity extends AppCompatActivity {

    private RadioGroup radioGroup;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selection);

        radioGroup = findViewById(R.id.radio_group_language);
        Button btnContinue = findViewById(R.id.btn_continue);

        // Set default selection
        radioGroup.check(R.id.radio_english);

        btnContinue.setOnClickListener(v -> {
            String selectedLanguage = getSelectedLanguage();
            LocaleHelper.setLocale(this, selectedLanguage);

            PreferenceManager prefs = new PreferenceManager(this);
            prefs.setLanguageSelected(true);

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private String getSelectedLanguage() {
        int checkedId = radioGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.radio_telugu) return "te";
        if (checkedId == R.id.radio_tamil) return "ta";
        if (checkedId == R.id.radio_hindi) return "hi";
        return "en";
    }
}
