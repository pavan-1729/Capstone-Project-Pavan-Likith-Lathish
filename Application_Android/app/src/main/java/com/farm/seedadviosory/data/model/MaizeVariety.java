package com.farm.seedadviosory.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Comprehensive maize variety model with all ~50 attributes from the dataset.
 * Fields that are missing in the source data should be null.
 */
public class MaizeVariety {

    @SerializedName("variety_id")
    private String varietyId;

    @SerializedName("variety_name")
    private String varietyName;

    private String crop;
    private String category;
    private String type;

    @SerializedName("developer_or_company")
    private String developerOrCompany;

    private String parents;

    @SerializedName("release_year")
    private String releaseYear;

    @SerializedName("gazette_notification")
    private String gazetteNotification;

    private String season;

    @SerializedName("maturity_class")
    private String maturityClass;

    @SerializedName("maturity_days_min")
    private Integer maturityDaysMin;

    @SerializedName("maturity_days_max")
    private Integer maturityDaysMax;

    @SerializedName("yield_value")
    private String yieldValue;

    @SerializedName("yield_unit")
    private String yieldUnit;

    @SerializedName("yield_normalized_q_ha_min")
    private Double yieldNormalizedMin;

    @SerializedName("yield_normalized_q_ha_max")
    private Double yieldNormalizedMax;

    @SerializedName("recommended_regions")
    private String recommendedRegions;

    @SerializedName("agro_climatic_zone")
    private String agroClimaticZone;

    @SerializedName("irrigation_condition")
    private String irrigationCondition;

    @SerializedName("plant_height")
    private String plantHeight;

    @SerializedName("ear_height")
    private String earHeight;

    @SerializedName("lodging_resistance")
    private String lodgingResistance;

    @SerializedName("leaf_orientation")
    private String leafOrientation;

    @SerializedName("stay_green")
    private String stayGreen;

    @SerializedName("drought_tolerance")
    private String droughtTolerance;

    @SerializedName("waterlogging_tolerance")
    private String waterloggingTolerance;

    @SerializedName("heat_tolerance")
    private String heatTolerance;

    @SerializedName("cold_stress_tolerance")
    private String coldStressTolerance;

    @SerializedName("fall_armyworm")
    private String fallArmyworm;

    @SerializedName("stem_borer")
    private String stemBorer;

    @SerializedName("pink_stem_borer")
    private String pinkStemBorer;

    @SerializedName("turcicum_leaf_blight")
    private String turcicumLeafBlight;

    @SerializedName("maydis_leaf_blight")
    private String maydisLeafBlight;

    @SerializedName("curvularia_leaf_spot")
    private String curvulariaLeafSpot;

    @SerializedName("banded_leaf_sheath_blight")
    private String bandedLeafSheathBlight;

    @SerializedName("charcoal_rot")
    private String charcoalRot;

    @SerializedName("fusarium_stalk_rot")
    private String fusariumStalkRot;

    @SerializedName("downy_mildew")
    private String downyMildew;

    @SerializedName("grain_type")
    private String grainType;

    @SerializedName("grain_colour")
    private String grainColour;

    @SerializedName("special_traits")
    private String specialTraits;

    @SerializedName("source_document")
    private String sourceDocument;

    @SerializedName("source_pdf_page")
    private String sourcePdfPage;

    @SerializedName("source_printed_page")
    private String sourcePrintedPage;

    @SerializedName("source_organization")
    private String sourceOrganization;

    @SerializedName("source_type")
    private String sourceType;

    @SerializedName("source_text")
    private String sourceText;

    @SerializedName("source_url")
    private String sourceUrl;

    @SerializedName("data_quality_notes")
    private String dataQualityNotes;

    // Getters and Setters
    public String getVarietyId() { return varietyId; }
    public void setVarietyId(String varietyId) { this.varietyId = varietyId; }

    public String getVarietyName() { return varietyName; }
    public void setVarietyName(String varietyName) { this.varietyName = varietyName; }

    public String getCrop() { return crop; }
    public void setCrop(String crop) { this.crop = crop; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDeveloperOrCompany() { return developerOrCompany; }
    public void setDeveloperOrCompany(String d) { this.developerOrCompany = d; }

    public String getParents() { return parents; }
    public void setParents(String parents) { this.parents = parents; }

    public String getReleaseYear() { return releaseYear; }
    public void setReleaseYear(String releaseYear) { this.releaseYear = releaseYear; }

    public String getGazetteNotification() { return gazetteNotification; }
    public void setGazetteNotification(String g) { this.gazetteNotification = g; }

    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }

    public String getMaturityClass() { return maturityClass; }
    public void setMaturityClass(String maturityClass) { this.maturityClass = maturityClass; }

    public Integer getMaturityDaysMin() { return maturityDaysMin; }
    public void setMaturityDaysMin(Integer maturityDaysMin) { this.maturityDaysMin = maturityDaysMin; }

    public Integer getMaturityDaysMax() { return maturityDaysMax; }
    public void setMaturityDaysMax(Integer maturityDaysMax) { this.maturityDaysMax = maturityDaysMax; }

    public String getYieldValue() { return yieldValue; }
    public void setYieldValue(String yieldValue) { this.yieldValue = yieldValue; }

    public String getYieldUnit() { return yieldUnit; }
    public void setYieldUnit(String yieldUnit) { this.yieldUnit = yieldUnit; }

    public Double getYieldNormalizedMin() { return yieldNormalizedMin; }
    public void setYieldNormalizedMin(Double v) { this.yieldNormalizedMin = v; }

    public Double getYieldNormalizedMax() { return yieldNormalizedMax; }
    public void setYieldNormalizedMax(Double v) { this.yieldNormalizedMax = v; }

    public String getRecommendedRegions() { return recommendedRegions; }
    public void setRecommendedRegions(String r) { this.recommendedRegions = r; }

    public String getAgroClimaticZone() { return agroClimaticZone; }
    public void setAgroClimaticZone(String z) { this.agroClimaticZone = z; }

    public String getIrrigationCondition() { return irrigationCondition; }
    public void setIrrigationCondition(String i) { this.irrigationCondition = i; }

    public String getPlantHeight() { return plantHeight; }
    public void setPlantHeight(String plantHeight) { this.plantHeight = plantHeight; }

    public String getEarHeight() { return earHeight; }
    public void setEarHeight(String earHeight) { this.earHeight = earHeight; }

    public String getLodgingResistance() { return lodgingResistance; }
    public void setLodgingResistance(String l) { this.lodgingResistance = l; }

    public String getLeafOrientation() { return leafOrientation; }
    public void setLeafOrientation(String l) { this.leafOrientation = l; }

    public String getStayGreen() { return stayGreen; }
    public void setStayGreen(String stayGreen) { this.stayGreen = stayGreen; }

    public String getDroughtTolerance() { return droughtTolerance; }
    public void setDroughtTolerance(String d) { this.droughtTolerance = d; }

    public String getWaterloggingTolerance() { return waterloggingTolerance; }
    public void setWaterloggingTolerance(String w) { this.waterloggingTolerance = w; }

    public String getHeatTolerance() { return heatTolerance; }
    public void setHeatTolerance(String h) { this.heatTolerance = h; }

    public String getColdStressTolerance() { return coldStressTolerance; }
    public void setColdStressTolerance(String c) { this.coldStressTolerance = c; }

    public String getFallArmyworm() { return fallArmyworm; }
    public void setFallArmyworm(String f) { this.fallArmyworm = f; }

    public String getStemBorer() { return stemBorer; }
    public void setStemBorer(String s) { this.stemBorer = s; }

    public String getPinkStemBorer() { return pinkStemBorer; }
    public void setPinkStemBorer(String p) { this.pinkStemBorer = p; }

    public String getTurcicumLeafBlight() { return turcicumLeafBlight; }
    public void setTurcicumLeafBlight(String t) { this.turcicumLeafBlight = t; }

    public String getMaydisLeafBlight() { return maydisLeafBlight; }
    public void setMaydisLeafBlight(String m) { this.maydisLeafBlight = m; }

    public String getCurvulariaLeafSpot() { return curvulariaLeafSpot; }
    public void setCurvulariaLeafSpot(String c) { this.curvulariaLeafSpot = c; }

    public String getBandedLeafSheathBlight() { return bandedLeafSheathBlight; }
    public void setBandedLeafSheathBlight(String b) { this.bandedLeafSheathBlight = b; }

    public String getCharcoalRot() { return charcoalRot; }
    public void setCharcoalRot(String c) { this.charcoalRot = c; }

    public String getFusariumStalkRot() { return fusariumStalkRot; }
    public void setFusariumStalkRot(String f) { this.fusariumStalkRot = f; }

    public String getDownyMildew() { return downyMildew; }
    public void setDownyMildew(String d) { this.downyMildew = d; }

    public String getGrainType() { return grainType; }
    public void setGrainType(String grainType) { this.grainType = grainType; }

    public String getGrainColour() { return grainColour; }
    public void setGrainColour(String grainColour) { this.grainColour = grainColour; }

    public String getSpecialTraits() { return specialTraits; }
    public void setSpecialTraits(String specialTraits) { this.specialTraits = specialTraits; }

    public String getSourceDocument() { return sourceDocument; }
    public void setSourceDocument(String s) { this.sourceDocument = s; }

    public String getSourcePdfPage() { return sourcePdfPage; }
    public void setSourcePdfPage(String s) { this.sourcePdfPage = s; }

    public String getSourcePrintedPage() { return sourcePrintedPage; }
    public void setSourcePrintedPage(String s) { this.sourcePrintedPage = s; }

    public String getSourceOrganization() { return sourceOrganization; }
    public void setSourceOrganization(String s) { this.sourceOrganization = s; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String s) { this.sourceType = s; }

    public String getSourceText() { return sourceText; }
    public void setSourceText(String s) { this.sourceText = s; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String s) { this.sourceUrl = s; }

    public String getDataQualityNotes() { return dataQualityNotes; }
    public void setDataQualityNotes(String d) { this.dataQualityNotes = d; }
}
