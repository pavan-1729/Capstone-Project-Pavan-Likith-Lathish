package com.farm.seedadviosory.ui.recommendation.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.farm.seedadviosory.R;
import com.farm.seedadviosory.ui.recommendation.RecommendationViewModel;
import com.farm.seedadviosory.ui.recommendation.RecommendationWizardActivity;

import java.util.ArrayList;
import java.util.List;

public class WizardClimateFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wizard_climate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecommendationViewModel vm = ((RecommendationWizardActivity) requireActivity()).getWizardViewModel();

        CheckBox cbWind = view.findViewById(R.id.cb_wind);
        CheckBox cbRain = view.findViewById(R.id.cb_rain);
        CheckBox cbWaterlog = view.findViewById(R.id.cb_waterlog);
        CheckBox cbDrought = view.findViewById(R.id.cb_drought);
        CheckBox cbHeat = view.findViewById(R.id.cb_heat);
        CheckBox cbCold = view.findViewById(R.id.cb_cold);
        CheckBox cbPest = view.findViewById(R.id.cb_pest);
        CheckBox cbDisease = view.findViewById(R.id.cb_disease);
        EditText etDescription = view.findViewById(R.id.et_description);

        boolean isSoybean = "soybean".equalsIgnoreCase(vm.getCrop());
        boolean isCotton = "cotton".equalsIgnoreCase(vm.getCrop());
        if (isSoybean) {
            cbPest.setText("Pests (Stem Fly, Girdle Beetle, Caterpillars)");
            cbDisease.setText("Diseases (Yellow Mosaic Virus, Charcoal Rot, Rust)");
            etDescription.setHint("e.g., Looking for high oil content, pod shattering resistance, or drought tolerance.");
        } else if (isCotton) {
            cbPest.setText("Pests (Bollworms, Whitefly, Jassids, Thrips, Aphids)");
            cbDisease.setText("Diseases (Bacterial Blight, Leaf Curl Virus CLCuV, Root Rot)");
            etDescription.setHint("e.g., Looking for long staple fiber, sucking pest tolerance, or high ginning outturn.");
        }

        CheckBox[] boxes = {cbWind, cbRain, cbWaterlog, cbDrought, cbHeat, cbCold, cbPest, cbDisease};
        String[] risks = {"Strong Wind", "Heavy Rain", "Waterlogging", "Drought",
                "High Temperature", "Cold", "Pest Pressure", "Disease Pressure"};

        // Restore previous selections
        List<String> prevRisks = vm.getClimateRisks();
        for (int i = 0; i < boxes.length; i++) {
            if (prevRisks.contains(risks[i])) boxes[i].setChecked(true);
        }

        // Update VM on change
        View.OnClickListener updateListener = v -> {
            List<String> selected = new ArrayList<>();
            for (int i = 0; i < boxes.length; i++) {
                if (boxes[i].isChecked()) selected.add(risks[i]);
            }
            vm.setClimateRisks(selected);
        };

        for (CheckBox cb : boxes) {
            cb.setOnClickListener(updateListener);
        }

        etDescription.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) vm.setFarmerDescription(etDescription.getText().toString().trim());
        });
    }
}
