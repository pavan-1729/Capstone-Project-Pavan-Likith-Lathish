package com.farm.seedadviosory.data.model;

public class FarmerRequest {

    private String name;
    private String mobile;
    private String email;
    private String state;
    private String district;
    private String village;
    private String preferredLanguage;
    private String primaryCrop;
    private String farmSize;

    public FarmerRequest() {
    }

    public FarmerRequest(
            String name,
            String mobile,
            String email,
            String state,
            String village
    ) {
        this.name = name;
        this.mobile = mobile;
        this.email = email;
        this.state = state;
        this.village = village;
    }

    public FarmerRequest(
            String name,
            String mobile,
            String email,
            String state,
            String district,
            String village,
            String preferredLanguage,
            String primaryCrop
    ) {
        this.name = name;
        this.mobile = mobile;
        this.email = email;
        this.state = state;
        this.district = district;
        this.village = village;
        this.preferredLanguage = preferredLanguage;
        this.primaryCrop = primaryCrop;
    }

    public FarmerRequest(
            String name,
            String mobile,
            String email,
            String state,
            String district,
            String village,
            String preferredLanguage,
            String primaryCrop,
            String farmSize
    ) {
        this.name = name;
        this.mobile = mobile;
        this.email = email;
        this.state = state;
        this.district = district;
        this.village = village;
        this.preferredLanguage = preferredLanguage;
        this.primaryCrop = primaryCrop;
        this.farmSize = farmSize;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public String getPrimaryCrop() {
        return primaryCrop;
    }

    public void setPrimaryCrop(String primaryCrop) {
        this.primaryCrop = primaryCrop;
    }

    public String getFarmSize() {
        return farmSize;
    }

    public void setFarmSize(String farmSize) {
        this.farmSize = farmSize;
    }
}