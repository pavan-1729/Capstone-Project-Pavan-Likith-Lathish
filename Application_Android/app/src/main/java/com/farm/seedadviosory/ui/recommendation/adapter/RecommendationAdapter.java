package com.farm.seedadviosory.ui.recommendation.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.farm.seedadviosory.R;
import com.farm.seedadviosory.data.model.RecommendationResponse;
import com.farm.seedadviosory.ui.recommendation.VarietyDetailActivity;
import com.google.gson.Gson;

import java.util.List;

/**
 * Adapter for displaying recommendation result cards.
 */
public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.ViewHolder> {

    private final Context context;
    private final List<RecommendationResponse.RecommendationItem> items;

    public RecommendationAdapter(Context context, List<RecommendationResponse.RecommendationItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recommendation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecommendationResponse.RecommendationItem item = items.get(position);

        holder.tvVarietyName.setText(item.getVariety().getVarietyName());
        holder.tvCategory.setText(item.getVariety().getCategory() + " • " +
                (item.getVariety().getType() != null ? item.getVariety().getType() : ""));

        // Suitability badge
        boolean isHigh = "HIGH".equals(item.getSuitability());
        holder.tvSuitability.setText(isHigh ? "✓ HIGH" : "~ MODERATE");
        holder.tvSuitability.setBackgroundResource(isHigh ?
                R.drawable.bg_suitability_high : R.drawable.bg_suitability_moderate);
        holder.tvSuitability.setTextColor(context.getColor(isHigh ?
                R.color.suitability_high : R.color.suitability_moderate));

        // LLM / AI Explanation Reason
        if (item.getReason() != null && !item.getReason().isBlank()) {
            holder.layoutReason.setVisibility(View.VISIBLE);
            holder.tvItemReason.setText(item.getReason());
        } else {
            holder.layoutReason.setVisibility(View.GONE);
        }

        // Match reasons
        holder.layoutReasons.removeAllViews();
        if (item.getMatchReasons() != null) {
            for (String reason : item.getMatchReasons()) {
                TextView tv = new TextView(context);
                tv.setText(reason);
                tv.setTextSize(13);
                tv.setTextColor(context.getColor(R.color.text_secondary));
                tv.setPadding(0, 4, 0, 4);
                holder.layoutReasons.addView(tv);
            }
        }

        // Key info
        com.farm.seedadviosory.data.model.MaizeVariety variety = item.getVariety();
        holder.tvYield.setText(variety != null && variety.getYieldValue() != null ?
                variety.getYieldValue() : (item.getYieldRange() != null ? item.getYieldRange() : "—"));
        holder.tvMaturity.setText(variety != null && variety.getMaturityDaysMin() != null ?
                variety.getMaturityDaysMin() + "-" + variety.getMaturityDaysMax() + " days" :
                (item.getMaturityDays() != null ? item.getMaturityDays() : "—"));
        holder.tvDeveloper.setText(variety != null && variety.getDeveloperOrCompany() != null ?
                variety.getDeveloperOrCompany() :
                (item.getSourceOrganization() != null ? item.getSourceOrganization() : "ICAR"));

        holder.btnDetails.setOnClickListener(v -> {
            // Ensure variety and evidence fields are initialized before passing to detail activity
            item.getVariety();
            item.getEvidence();
            Intent intent = new Intent(context, VarietyDetailActivity.class);
            intent.putExtra("item_json", new Gson().toJson(item));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvVarietyName, tvCategory, tvSuitability, tvYield, tvMaturity, tvDeveloper, tvItemReason;
        LinearLayout layoutReasons, layoutReason;
        Button btnDetails;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVarietyName = itemView.findViewById(R.id.tv_variety_name);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvSuitability = itemView.findViewById(R.id.tv_suitability);
            layoutReason = itemView.findViewById(R.id.layout_reason);
            tvItemReason = itemView.findViewById(R.id.tv_item_reason);
            tvYield = itemView.findViewById(R.id.tv_yield);
            tvMaturity = itemView.findViewById(R.id.tv_maturity);
            tvDeveloper = itemView.findViewById(R.id.tv_developer);
            layoutReasons = itemView.findViewById(R.id.layout_reasons);
            btnDetails = itemView.findViewById(R.id.btn_details);
        }
    }
}
