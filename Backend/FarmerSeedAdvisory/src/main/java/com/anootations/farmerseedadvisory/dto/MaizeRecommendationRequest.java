package com.anootations.farmerseedadvisory.dto;

import java.util.List;

/**
 * Request DTO for the Maize RAG recommendation endpoint.
 * Sent from the Android wizard to Spring Boot.
 */
public class MaizeRecommendationRequest {

    private String state;
    private String district;
    private String village;
    private String season;
    private String soilType;
    private String irrigation;           // "Rainfed" or "Irrigated"
    private String waterAvailability;    // Alias for irrigation from Android wizard
    private int maximumMaturityDays;     // 0 = no constraint
    private List<String> climateRisks;  // e.g., ["Drought", "Fall Armyworm"]
    private String purpose;             // e.g., "Grain", "Fodder", "Silage"
    private String priority;            // e.g., "High Yield", "Disease Resistance"
    private String farmerDescription;   // Free-text from farmer
    private String language;            // ISO 639-1: "en", "te", "hi"

    // ----------------------------------------------------------------
    // Getters and Setters
    // ----------------------------------------------------------------

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

    public String getIrrigation() { 
        return (irrigation != null && !irrigation.isBlank()) ? irrigation : waterAvailability; 
    }
    public void setIrrigation(String irrigation) { this.irrigation = irrigation; }

    public String getWaterAvailability() { return waterAvailability; }
    public void setWaterAvailability(String waterAvailability) { 
        this.waterAvailability = waterAvailability; 
        if (this.irrigation == null || this.irrigation.isBlank()) {
            this.irrigation = waterAvailability;
        }
    }

    public int getMaximumMaturityDays() { return maximumMaturityDays; }
    public void setMaximumMaturityDays(int maximumMaturityDays) { this.maximumMaturityDays = maximumMaturityDays; }

    public List<String> getClimateRisks() { return climateRisks; }
    public void setClimateRisks(List<String> climateRisks) { this.climateRisks = climateRisks; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getFarmerDescription() { return farmerDescription; }
    public void setFarmerDescription(String farmerDescription) { this.farmerDescription = farmerDescription; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
}
