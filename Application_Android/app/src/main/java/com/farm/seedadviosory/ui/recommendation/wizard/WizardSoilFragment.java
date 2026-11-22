package com.farm.seedadviosory.ui.recommendation.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.farm.seedadviosory.R;
import com.farm.seedadviosory.ui.recommendation.RecommendationViewModel;
import com.farm.seedadviosory.ui.recommendation.RecommendationWizardActivity;

public class WizardSoilFragment extends Fragment {

    private LinearLayout selectedCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wizard_soil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecommendationViewModel vm = ((RecommendationWizardActivity) requireActivity()).getWizardViewModel();

        int[] ids = {R.id.card_red, R.id.card_black, R.id.card_alluvial,
                R.id.card_sandy, R.id.card_loamy, R.id.card_other};
        String[] soils = {"Red Soil", "Black Soil", "Alluvial Soil",
                "Sandy Soil", "Loamy Soil", "Other"};

        for (int i = 0; i < ids.length; i++) {
            LinearLayout card = view.findViewById(ids[i]);
            final String soil = soils[i];
            card.setOnClickListener(v -> selectSoil(card, soil, vm));
            if (soil.equals(vm.getSoilType())) selectSoil(card, soil, vm);
        }
    }

    private void selectSoil(LinearLayout card, String soil, RecommendationViewModel vm) {
        if (selectedCard != null) selectedCard.setSelected(false);
        card.setSelected(true);
        selectedCard = card;
        vm.setSoilType(soil);
    }
}
