package com.farm.seedadviosory.ui.calendar;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.farm.seedadviosory.R;
import com.farm.seedadviosory.data.mock.MockDataProvider;
import com.farm.seedadviosory.data.model.CropCalendarStage;
import com.farm.seedadviosory.utils.LocaleHelper;

import java.util.List;

public class CropCalendarActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_calendar);

        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        LinearLayout container = findViewById(R.id.layout_stages);
        List<CropCalendarStage> stages = MockDataProvider.getDemoCropCalendar();

        for (CropCalendarStage stage : stages) {
            View card = LayoutInflater.from(this).inflate(R.layout.item_calendar_stage, container, false);

            ((TextView) card.findViewById(R.id.tv_icon)).setText(stage.getIcon());
            ((TextView) card.findViewById(R.id.tv_stage_name)).setText(stage.getStageName());
            ((TextView) card.findViewById(R.id.tv_day_number)).setText("Day " + stage.getDayNumber());
            ((TextView) card.findViewById(R.id.tv_description)).setText(stage.getDescription());

            if (stage.isCurrentStage()) {
                card.setBackgroundResource(R.drawable.bg_weather_card);
                ((TextView) card.findViewById(R.id.tv_current_badge)).setVisibility(View.VISIBLE);
            }

            container.addView(card);
        }
    }
}
