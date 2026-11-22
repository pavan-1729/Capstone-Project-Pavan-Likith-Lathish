package com.farm.seedadviosory.ui.crophealth;

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
import com.farm.seedadviosory.data.mock.MockDataProvider;

import java.util.List;

/**
 * Crop Health tab - shows maize disease knowledge base.
 */
public class CropHealthFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_crop_health, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout container = view.findViewById(R.id.layout_diseases);
        List<String[]> diseases = MockDataProvider.getDiseaseKnowledge();

        for (String[] disease : diseases) {
            View card = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_disease_card, container, false);

            ((TextView) card.findViewById(R.id.tv_disease_name)).setText(disease[0]);
            ((TextView) card.findViewById(R.id.tv_symptoms)).setText(disease[1]);
            ((TextView) card.findViewById(R.id.tv_management)).setText(disease[2]);
            ((TextView) card.findViewById(R.id.tv_source)).setText(disease[3]);

            container.addView(card);
        }
    }
}
