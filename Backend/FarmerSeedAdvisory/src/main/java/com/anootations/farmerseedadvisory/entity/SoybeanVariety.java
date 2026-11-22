package com.anootations.farmerseedadvisory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Stores all 184 soybean variety records from the ICAR/ICAR-IISR dataset.
 *
 * Loaded from soybean_varieties.csv on first startup.
 * Preserves all original dataset fields.
 */
@Entity
@Table(
        name = "soybean_varieties",
        indexes = {
                @Index(
                        name = "idx_sv_variety_id",
                        columnList = "variety_id",
                        unique = true
                ),
                @Index(
                        name = "idx_sv_season",
                        columnList = "season"
                ),
                @Index(
                        name = "idx_sv_category",
                        columnList = "category"
                )
        }
)
public class SoybeanVariety {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ------------------------------------------------------------
    // Basic variety information
    // ------------------------------------------------------------

    @Column(
            name = "variety_id",
            nullable = false,
            unique = true,
            length = 100
    )
    private String varietyId;

    @Column(
            name = "variety_name",
            nullable = false,
            length = 150
    )
    private String varietyName;

    @Column(length = 200)
    private String crop;

    @Column(length = 200)
    private String category;

    @Column(length = 200)
    private String type;

    @Column(
            name = "developer_or_company",
            length = 200
    )
    private String developerOrCompany;

    @Column(
            name = "parents",
            columnDefinition = "TEXT"
    )
    private String parents;

    @Column(
            name = "release_year",
            length = 10
    )
    private String releaseYear;

    @Column(
            name = "gazette_notification",
            columnDefinition = "TEXT"
    )
    private String gazetteNotification;

    @Column(
            name = "season",
            length = 100
    )
    private String season;

    @Column(
            name = "maturity_class",
            length = 100
    )
    private String maturityClass;

    // ------------------------------------------------------------
    // Maturity
    // ------------------------------------------------------------

    @Column(name = "maturity_days_min")
    private Integer maturityDaysMin;

    @Column(name = "maturity_days_max")
    private Integer maturityDaysMax;

    // ------------------------------------------------------------
    // Yield
    // ------------------------------------------------------------

    @Column(
            name = "yield_value",
            length = 100
    )
    private String yieldValue;

    @Column(
            name = "yield_unit",
            length = 50
    )
    private String yieldUnit;

    @Column(name = "yield_normalized_q_ha_min")
    private Double yieldNormalizedQHaMin;

    @Column(name = "yield_normalized_q_ha_max")
    private Double yieldNormalizedQHaMax;

    // ------------------------------------------------------------
    // Geographic / agricultural conditions
    // ------------------------------------------------------------

    @Column(
            name = "recommended_regions",
            columnDefinition = "TEXT"
    )
    private String recommendedRegions;

    @Column(
            name = "agro_climatic_zone",
            columnDefinition = "TEXT"
    )
    private String agroClimaticZone;

    @Column(
            name = "irrigation_condition",
            columnDefinition = "TEXT"
    )
    private String irrigationCondition;

    // ------------------------------------------------------------
    // Plant morphology
    // ------------------------------------------------------------

    @Column(
            name = "plant_height",
            columnDefinition = "TEXT"
    )
    private String plantHeight;

    @Column(
            name = "ear_height",
            columnDefinition = "TEXT"
    )
    private String earHeight;

    @Column(
            name = "lodging_resistance",
            columnDefinition = "TEXT"
    )
    private String lodgingResistance;

    @Column(
            name = "leaf_orientation",
            columnDefinition = "TEXT"
    )
    private String leafOrientation;

    @Column(
            name = "stay_green",
            columnDefinition = "TEXT"
    )
    private String stayGreen;

    // ------------------------------------------------------------
    // Environmental tolerance
    // ------------------------------------------------------------

    @Column(
            name = "drought_tolerance",
            columnDefinition = "TEXT"
    )
    private String droughtTolerance;

    @Column(
            name = "waterlogging_tolerance",
            columnDefinition = "TEXT"
    )
    private String waterloggingTolerance;

    @Column(
            name = "heat_tolerance",
            columnDefinition = "TEXT"
    )
    private String heatTolerance;

    @Column(
            name = "cold_stress_tolerance",
            columnDefinition = "TEXT"
    )
    private String coldStressTolerance;

    // ------------------------------------------------------------
    // Pest resistance / tolerance
    // ------------------------------------------------------------

    @Column(
            name = "fall_armyworm",
            columnDefinition = "TEXT"
    )
    private String fallArmyworm;

    @Column(
            name = "stem_borer",
            columnDefinition = "TEXT"
    )
    private String stemBorer;

    @Column(
            name = "pink_stem_borer",
            columnDefinition = "TEXT"
    )
    private String pinkStemBorer;

    // ------------------------------------------------------------
    // Disease resistance / tolerance
    // ------------------------------------------------------------

    @Column(
            name = "turcicum_leaf_blight",
            columnDefinition = "TEXT"
    )
    private String turcicumLeafBlight;

    @Column(
            name = "maydis_leaf_blight",
            columnDefinition = "TEXT"
    )
    private String maydisLeafBlight;

    @Column(
            name = "curvularia_leaf_spot",
            columnDefinition = "TEXT"
    )
    private String curvulariaLeafSpot;

    @Column(
            name = "banded_leaf_sheath_blight",
            columnDefinition = "TEXT"
    )
    private String bandedLeafSheathBlight;

    @Column(
            name = "charcoal_rot",
            columnDefinition = "TEXT"
    )
    private String charcoalRot;

    @Column(
            name = "fusarium_stalk_rot",
            columnDefinition = "TEXT"
    )
    private String fusariumStalkRot;

    @Column(
            name = "downy_mildew",
            columnDefinition = "TEXT"
    )
    private String downyMildew;

    // ------------------------------------------------------------
    // Grain characteristics
    // ------------------------------------------------------------

    @Column(
            name = "grain_type",
            columnDefinition = "TEXT"
    )
    private String grainType;

    @Column(
            name = "grain_colour",
            columnDefinition = "TEXT"
    )
    private String grainColour;

    @Column(
            name = "special_traits",
            columnDefinition = "TEXT"
    )
    private String specialTraits;

    // ------------------------------------------------------------
    // Source / provenance information
    // ------------------------------------------------------------

    @Column(
            name = "source_document",
            columnDefinition = "TEXT"
    )
    private String sourceDocument;

    @Column(
            name = "source_pdf_page",
            length = 50
    )
    private String sourcePdfPage;

    @Column(
            name = "source_printed_page",
            length = 50
    )
    private String sourcePrintedPage;

    @Column(
            name = "source_organization",
            columnDefinition = "TEXT"
    )
    private String sourceOrganization;

    @Column(
            name = "source_type",
            columnDefinition = "TEXT"
    )
    private String sourceType;

    @Column(
            name = "source_url",
            columnDefinition = "TEXT"
    )
    private String sourceUrl;

    @Column(
            name = "source_text",
            columnDefinition = "TEXT"
    )
    private String sourceText;

    @Column(
            name = "data_quality_notes",
            columnDefinition = "TEXT"
    )
    private String dataQualityNotes;

    // ------------------------------------------------------------
    // Getters and Setters
    // ------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public String getVarietyId() {
        return varietyId;
    }

    public void setVarietyId(String varietyId) {
        this.varietyId = varietyId;
    }

    public String getVarietyName() {
        return varietyName;
    }

    public void setVarietyName(String varietyName) {
        this.varietyName = varietyName;
    }

    public String getCrop() {
        return crop;
    }

    public void setCrop(String crop) {
        this.crop = crop;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDeveloperOrCompany() {
        return developerOrCompany;
    }

    public void setDeveloperOrCompany(String developerOrCompany) {
        this.developerOrCompany = developerOrCompany;
    }

    public String getParents() {
        return parents;
    }

    public void setParents(String parents) {
        this.parents = parents;
    }

    public String getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(String releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getGazetteNotification() {
        return gazetteNotification;
    }

    public void setGazetteNotification(String gazetteNotification) {
        this.gazetteNotification = gazetteNotification;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getMaturityClass() {
        return maturityClass;
    }

    public void setMaturityClass(String maturityClass) {
        this.maturityClass = maturityClass;
    }

    public Integer getMaturityDaysMin() {
        return maturityDaysMin;
    }

    public void setMaturityDaysMin(Integer maturityDaysMin) {
        this.maturityDaysMin = maturityDaysMin;
    }

    public Integer getMaturityDaysMax() {
        return maturityDaysMax;
    }

    public void setMaturityDaysMax(Integer maturityDaysMax) {
        this.maturityDaysMax = maturityDaysMax;
    }

    public String getYieldValue() {
        return yieldValue;
    }

    public void setYieldValue(String yieldValue) {
        this.yieldValue = yieldValue;
    }

    public String getYieldUnit() {
        return yieldUnit;
    }

    public void setYieldUnit(String yieldUnit) {
        this.yieldUnit = yieldUnit;
    }

    public Double getYieldNormalizedQHaMin() {
        return yieldNormalizedQHaMin;
    }

    public void setYieldNormalizedQHaMin(Double yieldNormalizedQHaMin) {
        this.yieldNormalizedQHaMin = yieldNormalizedQHaMin;
    }

    public Double getYieldNormalizedQHaMax() {
        return yieldNormalizedQHaMax;
    }

    public void setYieldNormalizedQHaMax(Double yieldNormalizedQHaMax) {
        this.yieldNormalizedQHaMax = yieldNormalizedQHaMax;
    }

    public String getRecommendedRegions() {
        return recommendedRegions;
    }

    public void setRecommendedRegions(String recommendedRegions) {
        this.recommendedRegions = recommendedRegions;
    }

    public String getAgroClimaticZone() {
        return agroClimaticZone;
    }

    public void setAgroClimaticZone(String agroClimaticZone) {
        this.agroClimaticZone = agroClimaticZone;
    }

    public String getIrrigationCondition() {
        return irrigationCondition;
    }

    public void setIrrigationCondition(String irrigationCondition) {
        this.irrigationCondition = irrigationCondition;
    }

    public String getPlantHeight() {
        return plantHeight;
    }

    public void setPlantHeight(String plantHeight) {
        this.plantHeight = plantHeight;
    }

    public String getEarHeight() {
        return earHeight;
    }

    public void setEarHeight(String earHeight) {
        this.earHeight = earHeight;
    }

    public String getLodgingResistance() {
        return lodgingResistance;
    }

    public void setLodgingResistance(String lodgingResistance) {
        this.lodgingResistance = lodgingResistance;
    }

    public String getLeafOrientation() {
        return leafOrientation;
    }

    public void setLeafOrientation(String leafOrientation) {
        this.leafOrientation = leafOrientation;
    }

    public String getStayGreen() {
        return stayGreen;
    }

    public void setStayGreen(String stayGreen) {
        this.stayGreen = stayGreen;
    }

    public String getDroughtTolerance() {
        return droughtTolerance;
    }

    public void setDroughtTolerance(String droughtTolerance) {
        this.droughtTolerance = droughtTolerance;
    }

    public String getWaterloggingTolerance() {
        return waterloggingTolerance;
    }

    public void setWaterloggingTolerance(String waterloggingTolerance) {
        this.waterloggingTolerance = waterloggingTolerance;
    }

    public String getHeatTolerance() {
        return heatTolerance;
    }

    public void setHeatTolerance(String heatTolerance) {
        this.heatTolerance = heatTolerance;
    }

    public String getColdStressTolerance() {
        return coldStressTolerance;
    }

    public void setColdStressTolerance(String coldStressTolerance) {
        this.coldStressTolerance = coldStressTolerance;
    }

    public String getFallArmyworm() {
        return fallArmyworm;
    }

    public void setFallArmyworm(String fallArmyworm) {
        this.fallArmyworm = fallArmyworm;
    }

    public String getStemBorer() {
        return stemBorer;
    }

    public void setStemBorer(String stemBorer) {
        this.stemBorer = stemBorer;
    }

    public String getPinkStemBorer() {
        return pinkStemBorer;
    }

    public void setPinkStemBorer(String pinkStemBorer) {
        this.pinkStemBorer = pinkStemBorer;
    }

    public String getTurcicumLeafBlight() {
        return turcicumLeafBlight;
    }

    public void setTurcicumLeafBlight(String turcicumLeafBlight) {
        this.turcicumLeafBlight = turcicumLeafBlight;
    }

    public String getMaydisLeafBlight() {
        return maydisLeafBlight;
    }

    public void setMaydisLeafBlight(String maydisLeafBlight) {
        this.maydisLeafBlight = maydisLeafBlight;
    }

    public String getCurvulariaLeafSpot() {
        return curvulariaLeafSpot;
    }

    public void setCurvulariaLeafSpot(String curvulariaLeafSpot) {
        this.curvulariaLeafSpot = curvulariaLeafSpot;
    }

    public String getBandedLeafSheathBlight() {
        return bandedLeafSheathBlight;
    }

    public void setBandedLeafSheathBlight(String bandedLeafSheathBlight) {
        this.bandedLeafSheathBlight = bandedLeafSheathBlight;
    }

    public String getCharcoalRot() {
        return charcoalRot;
    }

    public void setCharcoalRot(String charcoalRot) {
        this.charcoalRot = charcoalRot;
    }

    public String getFusariumStalkRot() {
        return fusariumStalkRot;
    }

    public void setFusariumStalkRot(String fusariumStalkRot) {
        this.fusariumStalkRot = fusariumStalkRot;
    }

    public String getDownyMildew() {
        return downyMildew;
    }

    public void setDownyMildew(String downyMildew) {
        this.downyMildew = downyMildew;
    }

    public String getGrainType() {
        return grainType;
    }

    public void setGrainType(String grainType) {
        this.grainType = grainType;
    }

    public String getGrainColour() {
        return grainColour;
    }

    public void setGrainColour(String grainColour) {
        this.grainColour = grainColour;
    }

    public String getSpecialTraits() {
        return specialTraits;
    }

    public void setSpecialTraits(String specialTraits) {
        this.specialTraits = specialTraits;
    }

    public String getSourceDocument() {
        return sourceDocument;
    }

    public void setSourceDocument(String sourceDocument) {
        this.sourceDocument = sourceDocument;
    }

    public String getSourcePdfPage() {
        return sourcePdfPage;
    }

    public void setSourcePdfPage(String sourcePdfPage) {
        this.sourcePdfPage = sourcePdfPage;
    }

    public String getSourcePrintedPage() {
        return sourcePrintedPage;
    }

    public void setSourcePrintedPage(String sourcePrintedPage) {
        this.sourcePrintedPage = sourcePrintedPage;
    }

    public String getSourceOrganization() {
        return sourceOrganization;
    }

    public void setSourceOrganization(String sourceOrganization) {
        this.sourceOrganization = sourceOrganization;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getSourceText() {
        return sourceText;
    }

    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }

    public String getDataQualityNotes() {
        return dataQualityNotes;
    }

    public void setDataQualityNotes(String dataQualityNotes) {
        this.dataQualityNotes = dataQualityNotes;
    }
}
