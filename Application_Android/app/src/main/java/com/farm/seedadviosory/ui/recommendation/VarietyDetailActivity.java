package com.farm.seedadviosory.ui.recommendation;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.farm.seedadviosory.R;
import com.farm.seedadviosory.data.model.MaizeVariety;
import com.farm.seedadviosory.data.model.RecommendationEvidence;
import com.farm.seedadviosory.data.model.RecommendationItem;
import com.farm.seedadviosory.utils.LocaleHelper;
import com.google.gson.Gson;

/**
 * Detailed view of a recommended maize variety.
 * Shows basic info, advantages, trade-offs, evidence/sources.
 */
public class VarietyDetailActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_variety_detail);

        String json = getIntent().getStringExtra("item_json");
        if (json == null || json.isEmpty()) {
            finish();
            return;
        }

        RecommendationItem item = new Gson().fromJson(json, RecommendationItem.class);
        if (item == null) {
            finish();
            return;
        }

        MaizeVariety v = item.getVariety();
        if (v == null) {
            // In case serialized from RecommendationResponse.RecommendationItem
            try {
                com.farm.seedadviosory.data.model.RecommendationResponse.RecommendationItem respItem =
                        new Gson().fromJson(json, com.farm.seedadviosory.data.model.RecommendationResponse.RecommendationItem.class);
                if (respItem != null) {
                    v = respItem.getVariety();
                    if (item.getAdvantages() == null) item.setAdvantages(respItem.getAdvantages());
                    if (item.getTradeOffs() == null) item.setTradeOffs(respItem.getTradeOffs());
                    if (item.getEvidence() == null) item.setEvidence(respItem.getEvidence());
                    if (item.getReason() == null) item.setReason(respItem.getReason());
                    if (item.getSuitability() == null) item.setSuitability(respItem.getSuitability());
                }
            } catch (Exception ignored) {}
        }

        if (v == null) {
            finish();
            return;
        }

        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(vi -> finish());

        // Header
        TextView tvName = findViewById(R.id.tv_variety_name);
        TextView tvCategory = findViewById(R.id.tv_category);
        TextView tvSuitability = findViewById(R.id.tv_suitability);

        tvName.setText(v.getVarietyName() != null ? v.getVarietyName() : "Recommended Variety");
        tvCategory.setText((v.getCategory() != null ? v.getCategory() : "ICAR Variety") + " • " + (v.getType() != null ? v.getType() : "Standard"));

        boolean isHigh = "HIGH".equals(item.getSuitability());
        tvSuitability.setText(isHigh ? "✓ HIGH SUITABILITY" : "~ MODERATE SUITABILITY");
        tvSuitability.setBackgroundResource(isHigh ?
                R.drawable.bg_suitability_high : R.drawable.bg_suitability_moderate);
        tvSuitability.setTextColor(getColor(isHigh ?
                R.color.suitability_high : R.color.suitability_moderate));

        // AI Recommendation Explanation Reason
        LinearLayout layoutReasonCard = findViewById(R.id.layout_reason_card);
        TextView tvReason = findViewById(R.id.tv_reason);
        if (item.getReason() != null && !item.getReason().isBlank()) {
            layoutReasonCard.setVisibility(View.VISIBLE);
            tvReason.setText(item.getReason());
        } else {
            layoutReasonCard.setVisibility(View.GONE);
        }

        // Basic Info
        addInfoRow(findViewById(R.id.layout_basic_info), "Developer", v.getDeveloperOrCompany());
        addInfoRow(findViewById(R.id.layout_basic_info), "Release Year", v.getReleaseYear());
        addInfoRow(findViewById(R.id.layout_basic_info), "Season", v.getSeason());
        addInfoRow(findViewById(R.id.layout_basic_info), "Maturity", v.getMaturityDaysMin() + "-" + v.getMaturityDaysMax() + " days (" + v.getMaturityClass() + ")");
        addInfoRow(findViewById(R.id.layout_basic_info), "Yield", v.getYieldValue());
        addInfoRow(findViewById(R.id.layout_basic_info), "Regions", v.getRecommendedRegions());
        addInfoRow(findViewById(R.id.layout_basic_info), "Irrigation", v.getIrrigationCondition());
        addInfoRow(findViewById(R.id.layout_basic_info), "Grain Type", v.getGrainType());
        addInfoRow(findViewById(R.id.layout_basic_info), "Grain Colour", v.getGrainColour());
        addInfoRow(findViewById(R.id.layout_basic_info), "Plant Height", v.getPlantHeight());
        addInfoRow(findViewById(R.id.layout_basic_info), "Lodging Resistance", v.getLodgingResistance());
        addInfoRow(findViewById(R.id.layout_basic_info), "Drought Tolerance", v.getDroughtTolerance());
        addInfoRow(findViewById(R.id.layout_basic_info), "Heat Tolerance", v.getHeatTolerance());
        addInfoRow(findViewById(R.id.layout_basic_info), "Special Traits", v.getSpecialTraits());

        // Advantages
        LinearLayout layoutAdvantages = findViewById(R.id.layout_advantages);
        if (item.getAdvantages() != null) {
            for (String adv : item.getAdvantages()) {
                addBullet(layoutAdvantages, adv, R.color.success);
            }
        }

        // Trade-offs
        LinearLayout layoutTradeOffs = findViewById(R.id.layout_trade_offs);
        if (item.getTradeOffs() != null) {
            for (String to : item.getTradeOffs()) {
                addBullet(layoutTradeOffs, to, R.color.warning);
            }
        }

        // Evidence
        Button btnViewFullEvidence = findViewById(R.id.btn_view_full_evidence);
        if (btnViewFullEvidence != null) {
            btnViewFullEvidence.setOnClickListener(vi -> {
                android.content.Intent intent = new android.content.Intent(this, EvidenceActivity.class);
                intent.putExtra("item_json", json);
                startActivity(intent);
            });
        }

        LinearLayout layoutEvidence = findViewById(R.id.layout_evidence);
        if (item.getEvidence() != null) {
            for (RecommendationEvidence ev : item.getEvidence()) {
                addEvidenceCard(layoutEvidence, ev);
            }
        }
    }

    private void addInfoRow(LinearLayout parent, String label, String value) {
        if (value == null || value.isEmpty() || "null".equals(value)) return;

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 8, 0, 8);

        TextView tvLabel = new TextView(this);
        tvLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        tvLabel.setText(label);
        tvLabel.setTextColor(getColor(R.color.text_secondary));
        tvLabel.setTextSize(14);

        TextView tvValue = new TextView(this);
        tvValue.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f));
        tvValue.setText(value);
        tvValue.setTextColor(getColor(R.color.text_primary));
        tvValue.setTextSize(14);

        row.addView(tvLabel);
        row.addView(tvValue);
        parent.addView(row);
    }

    private void addBullet(LinearLayout parent, String text, int colorRes) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(14);
        tv.setTextColor(getColor(R.color.text_primary));
        tv.setPadding(0, 6, 0, 6);
        tv.setLineSpacing(4, 1);
        parent.addView(tv);
    }

    private void addEvidenceCard(LinearLayout parent, RecommendationEvidence ev) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_weather_card);
        card.setPadding(24, 20, 24, 20);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 12, 0, 12);
        card.setLayoutParams(params);

        if (ev.getSourceOrganization() != null) {
            TextView tvOrg = new TextView(this);
            tvOrg.setText("📋 " + ev.getSourceOrganization());
            tvOrg.setTextColor(getColor(R.color.primary_dark));
            tvOrg.setTextSize(14);
            tvOrg.setTypeface(null, android.graphics.Typeface.BOLD);
            card.addView(tvOrg);
        }

        if (ev.getSourceDocument() != null) {
            TextView tvDoc = new TextView(this);
            tvDoc.setText("📄 " + ev.getSourceDocument());
            tvDoc.setTextColor(getColor(R.color.text_secondary));
            tvDoc.setTextSize(13);
            tvDoc.setPadding(0, 4, 0, 0);
            card.addView(tvDoc);
        }

        if (ev.getPdfPage() != null) {
            TextView tvPage = new TextView(this);
            tvPage.setText("Page: " + ev.getPdfPage() + " (PDF) / " +
                    (ev.getPrintedPage() != null ? ev.getPrintedPage() : "—") + " (Printed)");
            tvPage.setTextColor(getColor(R.color.text_hint));
            tvPage.setTextSize(12);
            tvPage.setPadding(0, 4, 0, 0);
            card.addView(tvPage);
        }

        if (ev.getSourceText() != null) {
            TextView tvText = new TextView(this);
            tvText.setText("\"" + ev.getSourceText() + "\"");
            tvText.setTextColor(getColor(R.color.text_secondary));
            tvText.setTextSize(13);
            tvText.setTypeface(null, android.graphics.Typeface.ITALIC);
            tvText.setPadding(0, 8, 0, 0);
            tvText.setLineSpacing(4, 1);
            card.addView(tvText);
        }

        parent.addView(card);
    }
}
