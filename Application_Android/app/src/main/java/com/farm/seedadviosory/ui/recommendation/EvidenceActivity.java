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

import java.util.List;

/**
 * Dedicated Activity showing the full RAG knowledge retrieval citations and evidence trail
 * for a recommended variety.
 */
public class EvidenceActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evidence);

        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

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

        MaizeVariety variety = item.getVariety();
        if (variety == null) {
            try {
                com.farm.seedadviosory.data.model.RecommendationResponse.RecommendationItem respItem =
                        new Gson().fromJson(json, com.farm.seedadviosory.data.model.RecommendationResponse.RecommendationItem.class);
                if (respItem != null) {
                    variety = respItem.getVariety();
                    if (item.getEvidence() == null) item.setEvidence(respItem.getEvidence());
                }
            } catch (Exception ignored) {}
        }

        if (variety == null) {
            finish();
            return;
        }
        TextView tvVarietyName = findViewById(R.id.tv_variety_name);
        TextView tvVarietySubtitle = findViewById(R.id.tv_variety_subtitle);
        TextView tvEvidenceCount = findViewById(R.id.tv_evidence_count);

        tvVarietyName.setText(variety.getVarietyName());
        tvVarietySubtitle.setText(variety.getCategory() + " • " +
                (variety.getDeveloperOrCompany() != null ? variety.getDeveloperOrCompany() : "ICAR") +
                " (" + (variety.getReleaseYear() != null ? variety.getReleaseYear() : "") + ")");

        List<RecommendationEvidence> evidenceList = item.getEvidence();
        int count = evidenceList != null ? evidenceList.size() : 0;
        tvEvidenceCount.setText(count + " Grounded Agricultural Sources Retrieved");

        LinearLayout layoutEvidenceList = findViewById(R.id.layout_evidence_list);

        if (evidenceList != null && !evidenceList.isEmpty()) {
            for (RecommendationEvidence ev : evidenceList) {
                addEvidenceCard(layoutEvidenceList, ev);
            }
        } else {
            TextView emptyTv = new TextView(this);
            emptyTv.setText(R.string.error_source_unavailable);
            emptyTv.setTextColor(getColor(R.color.text_secondary));
            emptyTv.setPadding(0, 16, 0, 16);
            layoutEvidenceList.addView(emptyTv);
        }
    }

    private void addEvidenceCard(LinearLayout parent, RecommendationEvidence ev) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_option_card);
        card.setPadding(32, 24, 32, 24);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 12, 0, 12);
        card.setLayoutParams(params);

        // Source Organization Badge
        if (ev.getSourceOrganization() != null && !ev.getSourceOrganization().isEmpty()) {
            TextView tvOrg = new TextView(this);
            tvOrg.setText("🏛️ " + ev.getSourceOrganization());
            tvOrg.setTextColor(getColor(R.color.primary_dark));
            tvOrg.setTextSize(14);
            tvOrg.setTypeface(null, android.graphics.Typeface.BOLD);
            card.addView(tvOrg);
        }

        // Document Title
        if (ev.getSourceDocument() != null && !ev.getSourceDocument().isEmpty()) {
            TextView tvDoc = new TextView(this);
            tvDoc.setText("📄 Document: " + ev.getSourceDocument());
            tvDoc.setTextColor(getColor(R.color.text_primary));
            tvDoc.setTextSize(13);
            tvDoc.setTypeface(null, android.graphics.Typeface.BOLD);
            tvDoc.setPadding(0, 6, 0, 0);
            card.addView(tvDoc);
        }

        // Page references
        StringBuilder pageRef = new StringBuilder("📍 Location: ");
        if (ev.getPdfPage() != null && !ev.getPdfPage().isEmpty()) {
            pageRef.append("PDF p. ").append(ev.getPdfPage());
        }
        if (ev.getPrintedPage() != null && !ev.getPrintedPage().isEmpty()) {
            pageRef.append(" | Printed p. ").append(ev.getPrintedPage());
        }
        if (ev.getSourceType() != null && !ev.getSourceType().isEmpty()) {
            pageRef.append(" | ").append(ev.getSourceType());
        }

        TextView tvPage = new TextView(this);
        tvPage.setText(pageRef.toString());
        tvPage.setTextColor(getColor(R.color.text_hint));
        tvPage.setTextSize(12);
        tvPage.setPadding(0, 4, 0, 0);
        card.addView(tvPage);

        // Verbatim quoted text
        if (ev.getSourceText() != null && !ev.getSourceText().isEmpty()) {
            TextView tvQuote = new TextView(this);
            tvQuote.setText("\"" + ev.getSourceText() + "\"");
            tvQuote.setTextColor(getColor(R.color.text_secondary));
            tvQuote.setTextSize(13);
            tvQuote.setTypeface(null, android.graphics.Typeface.ITALIC);
            tvQuote.setPadding(12, 10, 12, 4);
            tvQuote.setLineSpacing(4, 1);
            card.addView(tvQuote);
        }

        parent.addView(card);
    }
}
