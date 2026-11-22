package com.farm.seedadviosory.ui.recommendation.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.farm.seedadviosory.R;
import com.farm.seedadviosory.data.mock.MockDataProvider;
import com.farm.seedadviosory.ui.recommendation.RecommendationViewModel;
import com.farm.seedadviosory.ui.recommendation.RecommendationWizardActivity;

import java.util.List;

public class WizardLocationFragment extends Fragment {

    private Spinner spinnerState, spinnerDistrict;
    private EditText etVillage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wizard_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecommendationViewModel vm = ((RecommendationWizardActivity) requireActivity()).getWizardViewModel();

        spinnerState = view.findViewById(R.id.spinner_state);
        spinnerDistrict = view.findViewById(R.id.spinner_district);
        etVillage = view.findViewById(R.id.et_village);

        // States
        List<String> states = MockDataProvider.getStates();
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, states);
        spinnerState.setAdapter(stateAdapter);

        // Set default
        int statePos = states.indexOf(vm.getState());
        if (statePos >= 0) spinnerState.setSelection(statePos);

        spinnerState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                String state = states.get(pos);
                vm.setState(state);
                loadDistricts(state, vm);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        etVillage.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) vm.setVillage(etVillage.getText().toString().trim());
        });
    }

    private void loadDistricts(String state, RecommendationViewModel vm) {
        List<String> districts = MockDataProvider.getDistricts(state);
        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, districts);
        spinnerDistrict.setAdapter(districtAdapter);

        int distPos = districts.indexOf(vm.getDistrict());
        if (distPos >= 0) spinnerDistrict.setSelection(distPos);

        spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                vm.setDistrict(districts.get(pos));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}
