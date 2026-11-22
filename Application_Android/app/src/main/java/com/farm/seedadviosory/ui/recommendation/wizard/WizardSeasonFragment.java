package com.farm.seedadviosory.ui.recommendation.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.farm.seedadviosory.R;
import com.farm.seedadviosory.ui.recommendation.RecommendationViewModel;
import com.farm.seedadviosory.ui.recommendation.RecommendationWizardActivity;

public class WizardSeasonFragment extends Fragment {

    private LinearLayout cardKharif, cardRabi, cardSpring;
    private LinearLayout selectedCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wizard_season, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecommendationViewModel vm = ((RecommendationWizardActivity) requireActivity()).getWizardViewModel();

        cardKharif = view.findViewById(R.id.card_kharif);
        cardRabi = view.findViewById(R.id.card_rabi);
        cardSpring = view.findViewById(R.id.card_spring);

        TextView tvWhenGrow = view.findViewById(R.id.tv_when_grow);
        TextView tvKharifDesc = view.findViewById(R.id.tv_kharif_desc);
        TextView tvRabiDesc = view.findViewById(R.id.tv_rabi_desc);
        TextView tvSpringTitle = view.findViewById(R.id.tv_spring_title);
        TextView tvSpringDesc = view.findViewById(R.id.tv_spring_desc);

        boolean isSoybean = "soybean".equalsIgnoreCase(vm.getCrop());
        boolean isCotton = "cotton".equalsIgnoreCase(vm.getCrop());
        if (isSoybean) {
            if (tvWhenGrow != null) tvWhenGrow.setText(R.string.when_grow_soybean);
            if (tvKharifDesc != null) tvKharifDesc.setText("Monsoon (Jun – Oct). Main soybean season across India (95%+ acreage).");
            if (tvSpringTitle != null) tvSpringTitle.setText("Summer / Spring (Feb – Jun)");
            if (tvSpringDesc != null) tvSpringDesc.setText("Irrigated summer crop with high pod setting and yield potential.");
            if (tvRabiDesc != null) tvRabiDesc.setText("Winter (Oct – Feb). Note: Soybean in India is mostly Kharif/Summer.");
        } else if (isCotton) {
            if (tvWhenGrow != null) tvWhenGrow.setText(R.string.when_grow_cotton);
            if (tvKharifDesc != null) tvKharifDesc.setText("Monsoon (May – Nov). Primary cotton season across North, Central, and South zones.");
            if (tvSpringTitle != null) tvSpringTitle.setText("Summer / Winter Cotton (Aug – Feb / Feb – Jun)");
            if (tvSpringDesc != null) tvSpringDesc.setText("Irrigated tract cultivation in Southern zone and parts of Tamil Nadu.");
            if (tvRabiDesc != null) tvRabiDesc.setText("Late season / Winter sowing (mainly Southern rice fallows).");
        }

        cardKharif.setOnClickListener(v -> selectSeason(cardKharif, "Kharif", vm));
        cardRabi.setOnClickListener(v -> selectSeason(cardRabi, "Rabi", vm));
        cardSpring.setOnClickListener(v -> selectSeason(cardSpring, "Spring", vm));

        // Restore selection or default to Kharif for soybean and cotton
        if ("Kharif".equals(vm.getSeason())) selectSeason(cardKharif, "Kharif", vm);
        else if ("Rabi".equals(vm.getSeason())) selectSeason(cardRabi, "Rabi", vm);
        else if ("Spring".equals(vm.getSeason())) selectSeason(cardSpring, "Spring", vm);
        else if (isSoybean || isCotton) {
            selectSeason(cardKharif, "Kharif", vm);
        }
    }

    private void selectSeason(LinearLayout card, String season, RecommendationViewModel vm) {
        if (selectedCard != null) selectedCard.setSelected(false);
        card.setSelected(true);
        selectedCard = card;
        vm.setSeason(season);
    }
}
