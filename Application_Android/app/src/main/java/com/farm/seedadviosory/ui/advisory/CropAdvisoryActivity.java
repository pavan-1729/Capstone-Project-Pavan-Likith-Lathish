package com.farm.seedadviosory.ui.advisory;

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
import com.farm.seedadviosory.data.model.CropAdvisory;
import com.farm.seedadviosory.utils.LocaleHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen displaying timely agronomic advisories (irrigation, fertilization, pest control, harvesting)
 * for farmers.
 */
public class CropAdvisoryActivity extends AppCompatActivity {

    private LinearLayout layoutAdvisories;
    private List<CropAdvisory> allAdvisories;
    private String selectedCategory = "all";

    private Button chipAll, chipIrrigation, chipFertilizer, chipPest, chipDisease;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_advisory);

        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        layoutAdvisories = findViewById(R.id.layout_advisories);
        allAdvisories = MockDataProvider.getDemoAdvisories();

        chipAll = findViewById(R.id.chip_all);
        chipIrrigation = findViewById(R.id.chip_irrigation);
        chipFertilizer = findViewById(R.id.chip_fertilizer);
        chipPest = findViewById(R.id.chip_pest);
        chipDisease = findViewById(R.id.chip_disease);

        chipAll.setOnClickListener(v -> selectCategory("all"));
        chipIrrigation.setOnClickListener(v -> selectCategory("irrigation"));
        chipFertilizer.setOnClickListener(v -> selectCategory("fertilizer"));
        chipPest.setOnClickListener(v -> selectCategory("pest"));
        chipDisease.setOnClickListener(v -> selectCategory("disease"));

        renderAdvisories();
    }

    private void selectCategory(String category) {
        this.selectedCategory = category;
        updateChipStyles();
        renderAdvisories();
    }

    private void updateChipStyles() {
        setChipActive(chipAll, "all".equals(selectedCategory));
        setChipActive(chipIrrigation, "irrigation".equals(selectedCategory));
        setChipActive(chipFertilizer, "fertilizer".equals(selectedCategory));
        setChipActive(chipPest, "pest".equals(selectedCategory));
        setChipActive(chipDisease, "disease".equals(selectedCategory));
    }

    private void setChipActive(Button chip, boolean isActive) {
        chip.setBackgroundTintList(null);
        if (isActive) {
            chip.setBackgroundResource(R.drawable.bg_button_primary);
            chip.setTextColor(getColor(R.color.white));
        } else {
            chip.setBackgroundResource(R.drawable.bg_button_secondary);
            chip.setTextColor(getColor(R.color.primary));
        }
    }

    private void renderAdvisories() {
        layoutAdvisories.removeAllViews();
        List<CropAdvisory> filtered = new ArrayList<>();

        for (CropAdvisory ad : allAdvisories) {
            if ("all".equals(selectedCategory) || selectedCategory.equalsIgnoreCase(ad.getCategory())) {
                filtered.add(ad);
            }
        }

        if (filtered.isEmpty()) {
            TextView emptyTv = new TextView(this);
            emptyTv.setText("No advisories available for this category.");
            emptyTv.setTextColor(getColor(R.color.text_secondary));
            emptyTv.setPadding(0, 32, 0, 32);
            layoutAdvisories.addView(emptyTv);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (CropAdvisory item : filtered) {
            View card = inflater.inflate(R.layout.item_advisory_card, layoutAdvisories, false);

            TextView tvIcon = card.findViewById(R.id.tv_advisory_icon);
            TextView tvTitle = card.findViewById(R.id.tv_advisory_title);
            TextView tvDate = card.findViewById(R.id.tv_advisory_date);
            TextView tvDesc = card.findViewById(R.id.tv_advisory_desc);
            TextView tvPriority = card.findViewById(R.id.tv_priority_badge);

            tvIcon.setText(item.getIcon() != null ? item.getIcon() : "🌱");
            tvTitle.setText(item.getTitle());
            tvDate.setText(item.getDate() != null ? item.getDate() : "Current");
            tvDesc.setText(item.getDescription());

            String prio = item.getPriority() != null ? item.getPriority() : "Medium";
            tvPriority.setText(prio.toUpperCase());
            if ("High".equalsIgnoreCase(prio)) {
                tvPriority.setTextColor(getColor(R.color.error));
            } else if ("Medium".equalsIgnoreCase(prio)) {
                tvPriority.setTextColor(getColor(R.color.warning));
            } else {
                tvPriority.setTextColor(getColor(R.color.success));
            }

            layoutAdvisories.addView(card);
        }
    }
}
