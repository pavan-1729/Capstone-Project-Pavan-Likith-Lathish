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

public class WizardWaterFragment extends Fragment {
    private LinearLayout selectedCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wizard_water, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecommendationViewModel vm = ((RecommendationWizardActivity) requireActivity()).getWizardViewModel();

        int[] ids = {R.id.card_rainfed, R.id.card_limited, R.id.card_assured, R.id.card_waterlogging};
        String[] waters = {"Rainfed", "Limited Irrigation", "Assured Irrigation", "Waterlogging Problem"};

        for (int i = 0; i < ids.length; i++) {
            LinearLayout card = view.findViewById(ids[i]);
            final String water = waters[i];
            card.setOnClickListener(v -> selectWater(card, water, vm));
            if (water.equals(vm.getWaterAvailability())) selectWater(card, water, vm);
        }
    }

    private void selectWater(LinearLayout card, String water, RecommendationViewModel vm) {
        if (selectedCard != null) selectedCard.setSelected(false);
        card.setSelected(true);
        selectedCard = card;
        vm.setWaterAvailability(water);
    }
}
