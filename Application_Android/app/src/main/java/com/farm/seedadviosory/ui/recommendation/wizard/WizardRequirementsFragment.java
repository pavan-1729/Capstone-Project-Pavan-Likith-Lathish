package com.farm.seedadviosory.ui.recommendation.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.farm.seedadviosory.R;
import com.farm.seedadviosory.ui.recommendation.RecommendationViewModel;
import com.farm.seedadviosory.ui.recommendation.RecommendationWizardActivity;

public class WizardRequirementsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wizard_requirements, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecommendationViewModel vm = ((RecommendationWizardActivity) requireActivity()).getWizardViewModel();

        SeekBar seekMaturity = view.findViewById(R.id.seek_maturity);
        TextView tvMaturityValue = view.findViewById(R.id.tv_maturity_value);
        TextView tvMaturityHint = view.findViewById(R.id.tv_maturity_hint);

        boolean isSoybean = "soybean".equalsIgnoreCase(vm.getCrop());
        boolean isCotton = "cotton".equalsIgnoreCase(vm.getCrop());

        if (isCotton) {
            if (vm.getMaximumMaturityDays() < 130) {
                vm.setMaximumMaturityDays(175);
            }
            if (tvMaturityHint != null) {
                tvMaturityHint.setText("Cotton varieties typically mature in 140–185 days (e.g. Medium: ~150–165 days, Late/Bt: ~165–180 days).");
            }
        } else if (isSoybean) {
            if (vm.getMaximumMaturityDays() == 100) {
                // Default 110 days for soybean to include benchmark varieties like JS 335 (105-113 days)
                vm.setMaximumMaturityDays(110);
            }
            if (tvMaturityHint != null) {
                tvMaturityHint.setText("Soybean varieties typically mature in 85–118 days (e.g. JS 335: ~105–113 days, JS 93-05: ~90–95 days).");
            }
        }

        final int baseDays = isCotton ? 130 : 60;
        final int maxSpan = isCotton ? 70 : 80; // Cotton: 130-200 days; Maize/Soybean: 60-140 days

        seekMaturity.setMax(maxSpan);
        int currentProgress = Math.max(0, Math.min(maxSpan, vm.getMaximumMaturityDays() - baseDays));
        seekMaturity.setProgress(currentProgress);
        tvMaturityValue.setText(vm.getMaximumMaturityDays() + " " + getString(R.string.days));

        seekMaturity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int days = progress + baseDays;
                tvMaturityValue.setText(days + " " + getString(R.string.days));
                vm.setMaximumMaturityDays(days);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
}
