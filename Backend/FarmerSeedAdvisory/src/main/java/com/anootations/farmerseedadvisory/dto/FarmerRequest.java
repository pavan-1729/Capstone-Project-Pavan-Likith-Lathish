package com.anootations.farmerseedadvisory.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for farmer registration and profile update requests.
 * The Firebase UID is NOT accepted here — it is always extracted
 * from the verified Firebase ID token on the server side.
 */
public class FarmerRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Mobile number is required")
    @Size(min = 10, max = 15, message = "Mobile number must be 10–15 digits")
    private String mobile;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "State is required")
    private String state;

    private String district;

    private String village;

    /** ISO 639-1 code: en, te, hi, ta */
    private String preferredLanguage;

    /** Maize / Soybean / Cotton */
    private String primaryCrop;

    /** Free-text farm size: "2 acres", "5 hectares" */
    private String farmSize;

    // ----------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------

    public FarmerRequest() {}

    // ----------------------------------------------------------------
    // Getters and Setters
    // ----------------------------------------------------------------

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }

    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }

    public String getPrimaryCrop() { return primaryCrop; }
    public void setPrimaryCrop(String primaryCrop) { this.primaryCrop = primaryCrop; }

    public String getFarmSize() { return farmSize; }
    public void setFarmSize(String farmSize) { this.farmSize = farmSize; }
}
