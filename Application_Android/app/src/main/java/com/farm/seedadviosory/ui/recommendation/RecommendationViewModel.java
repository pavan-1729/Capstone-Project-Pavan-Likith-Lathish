package com.farm.seedadviosory.ui.recommendation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.farm.seedadviosory.data.model.*;
import com.farm.seedadviosory.data.repository.RecommendationRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for the recommendation wizard and results.
 * Stores wizard state across fragments and triggers recommendation.
 */
public class RecommendationViewModel extends ViewModel {

    private final RecommendationRepository repository;

    // Wizard state
    private String crop = "Maize";
    private String state = "Andhra Pradesh";
    private String district = "Kurnool";
    private String village = "";
    private String season = "";
    private String soilType = "";
    private String soilPh = "";
    private String nitrogen = "";
    private String phosphorus = "";
    private String potassium = "";
    private String waterAvailability = "";
    private List<String> climateRisks = new ArrayList<>();
    private String farmerDescription = "";
    private int maximumMaturityDays = 100;
    private String purpose = "Grain";
    private String priority = "Balanced";

    // Results
    private final MutableLiveData<RecommendationResponse> recommendationResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public RecommendationViewModel() {
        repository = new RecommendationRepository();
    }

    // Getters for LiveData
    public LiveData<RecommendationResponse> getRecommendationResult() { return recommendationResult; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    // Wizard state getters/setters
    public String getCrop() { return crop; }
    public void setCrop(String crop) { this.crop = crop; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }

    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }

    public String getSoilType() { return soilType; }
    public void setSoilType(String soilType) { this.soilType = soilType; }

    public String getSoilPh() { return soilPh; }
    public void setSoilPh(String soilPh) { this.soilPh = soilPh; }

    public String getNitrogen() { return nitrogen; }
    public void setNitrogen(String n) { this.nitrogen = n; }

    public String getPhosphorus() { return phosphorus; }
    public void setPhosphorus(String p) { this.phosphorus = p; }

    public String getPotassium() { return potassium; }
    public void setPotassium(String k) { this.potassium = k; }

    public String getWaterAvailability() { return waterAvailability; }
    public void setWaterAvailability(String w) { this.waterAvailability = w; }

    public List<String> getClimateRisks() { return climateRisks; }
    public void setClimateRisks(List<String> risks) { this.climateRisks = risks; }

    public String getFarmerDescription() { return farmerDescription; }
    public void setFarmerDescription(String d) { this.farmerDescription = d; }

    public int getMaximumMaturityDays() { return maximumMaturityDays; }
    public void setMaximumMaturityDays(int d) { this.maximumMaturityDays = d; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    /**
     * Build request from wizard state and fetch recommendations.
     */
    public void fetchRecommendations(String language) {
        isLoading.setValue(true);

        MaizeRecommendationRequest request = new MaizeRecommendationRequest();
        request.setCrop(crop != null ? crop.toLowerCase() : "maize");
        request.setLanguage(language);
        request.setState(state);
        request.setDistrict(district);
        request.setVillage(village);
        request.setSeason(season);
        request.setSoilType(soilType);
        request.setSoilPh(soilPh);
        request.setNitrogen(nitrogen);
        request.setPhosphorus(phosphorus);
        request.setPotassium(potassium);
        request.setWaterAvailability(waterAvailability);
        request.setMaximumMaturityDays(maximumMaturityDays);
        request.setClimateRisks(climateRisks);
        request.setPurpose(purpose);
        request.setPriority(priority);
        request.setFarmerDescription(farmerDescription);

        // Make async API call
        repository.getRecommendations(request).enqueue(new retrofit2.Callback<RecommendationResponse>() {
            @Override
            public void onResponse(retrofit2.Call<RecommendationResponse> call, retrofit2.Response<RecommendationResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    RecommendationResponse result = response.body();
                    if (result.getRecommendations() != null && !result.getRecommendations().isEmpty()) {
                        recommendationResult.setValue(result);
                    } else {
                        errorMessage.setValue("No suitable variety was found using the available agricultural evidence.");
                    }
                } else {
                    errorMessage.setValue("Failed to get recommendations: " + response.message());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<RecommendationResponse> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Network error: " + t.getMessage());
            }
        });
    }
}
