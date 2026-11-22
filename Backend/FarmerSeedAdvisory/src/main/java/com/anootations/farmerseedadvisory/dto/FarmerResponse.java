package com.anootations.farmerseedadvisory.dto;

import com.anootations.farmerseedadvisory.entity.Farmer;

import java.time.LocalDateTime;

/**
 * Safe response DTO for farmer profile data.
 * Exposes only fields needed by the Android client.
 * Does NOT expose the internal database ID.
 */
public class FarmerResponse {

    private String name;
    private String mobile;
    private String email;
    private String state;
    private String district;
    private String village;
    private String preferredLanguage;
    private String primaryCrop;
    private String farmSize;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ----------------------------------------------------------------
    // Factory method from entity
    // ----------------------------------------------------------------

    public static FarmerResponse from(Farmer farmer) {
        FarmerResponse response = new FarmerResponse();
        response.name = farmer.getName();
        response.mobile = farmer.getMobile();
        response.email = farmer.getEmail();
        response.state = farmer.getState();
        response.district = farmer.getDistrict();
        response.village = farmer.getVillage();
        response.preferredLanguage = farmer.getPreferredLanguage();
        response.primaryCrop = farmer.getPrimaryCrop();
        response.farmSize = farmer.getFarmSize();
        response.createdAt = farmer.getCreatedAt();
        response.updatedAt = farmer.getUpdatedAt();
        return response;
    }

    // ----------------------------------------------------------------
    // Getters (no setters needed — use factory method)
    // ----------------------------------------------------------------

    public String getName() { return name; }
    public String getMobile() { return mobile; }
    public String getEmail() { return email; }
    public String getState() { return state; }
    public String getDistrict() { return district; }
    public String getVillage() { return village; }
    public String getPreferredLanguage() { return preferredLanguage; }
    public String getPrimaryCrop() { return primaryCrop; }
    public String getFarmSize() { return farmSize; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
