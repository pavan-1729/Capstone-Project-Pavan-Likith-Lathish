package com.farm.seedadviosory.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Request model for the maize seed recommendation API.
 */
public class MaizeRecommendationRequest {

    private String crop = "maize";
    private String language;
    private String state;
    private String district;
    private String village;
    private String season;

    @SerializedName("soilType")
    private String soilType;

    @SerializedName("soilPh")
    private String soilPh;

    private String nitrogen;
    private String phosphorus;
    private String potassium;

    @SerializedName("waterAvailability")
    private String waterAvailability;

    @SerializedName("irrigation")
    private String irrigation;

    @SerializedName("maximumMaturityDays")
    private int maximumMaturityDays;

    @SerializedName("climateRisks")
    private List<String> climateRisks;

    private String purpose;
    private String priority;

    @SerializedName("farmerDescription")
    private String farmerDescription;

    // Getters and Setters
    public String getCrop() { return crop; }
    public void setCrop(String crop) { this.crop = crop; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

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
    public void setNitrogen(String nitrogen) { this.nitrogen = nitrogen; }

    public String getPhosphorus() { return phosphorus; }
    public void setPhosphorus(String phosphorus) { this.phosphorus = phosphorus; }

    public String getPotassium() { return potassium; }
    public void setPotassium(String potassium) { this.potassium = potassium; }

    public String getWaterAvailability() { return waterAvailability; }
    public void setWaterAvailability(String w) { 
        this.waterAvailability = w; 
        if (this.irrigation == null) this.irrigation = w;
    }

    public String getIrrigation() { return irrigation; }
    public void setIrrigation(String i) { 
        this.irrigation = i; 
        if (this.waterAvailability == null) this.waterAvailability = i;
    }

    public int getMaximumMaturityDays() { return maximumMaturityDays; }
    public void setMaximumMaturityDays(int m) { this.maximumMaturityDays = m; }

    public List<String> getClimateRisks() { return climateRisks; }
    public void setClimateRisks(List<String> climateRisks) { this.climateRisks = climateRisks; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getFarmerDescription() { return farmerDescription; }
    public void setFarmerDescription(String f) { this.farmerDescription = f; }
}
