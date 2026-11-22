package com.anootations.farmerseedadvisory.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for maize seed recommendations.
 * Contains the structured output from the RAG + LLM pipeline.
 */
public class RecommendationResponse {

    private List<RecommendationItem> recommendations;
    private String summary;
    private List<String> warnings;
    private int candidatesFound;     // How many varieties passed metadata filtering
    private String modelUsed;
    private LocalDateTime generatedAt;
    private boolean llmAvailable;

    // ----------------------------------------------------------------
    // Nested DTOs
    // ----------------------------------------------------------------

    public static class RecommendationItem {
        private String varietyId;
        private String varietyName;
        private String category;
        private String season;
        private String maturityDays;
        private String yieldRange;
        private String irrigationCondition;
        private String recommendedRegions;
        private String droughtTolerance;
        private String fallArmyworm;
        private String stemBorer;
        private String turcicumLeafBlight;
        private String downyMildew;
        private String grainType;
        private String specialTraits;
        private double score;
        private String suitability;     // HIGH / MODERATE / LOW
        private String reason;
        private List<String> matchReasons;
        private List<String> advantages;
        private List<String> tradeOffs;
        private List<String> warnings;
        private String sourceOrganization;
        private String sourceDocument;

        // Getters and setters
        public String getVarietyId() { return varietyId; }
        public void setVarietyId(String varietyId) { this.varietyId = varietyId; }

        public String getVarietyName() { return varietyName; }
        public void setVarietyName(String varietyName) { this.varietyName = varietyName; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getSeason() { return season; }
        public void setSeason(String season) { this.season = season; }

        public String getMaturityDays() { return maturityDays; }
        public void setMaturityDays(String maturityDays) { this.maturityDays = maturityDays; }

        public String getYieldRange() { return yieldRange; }
        public void setYieldRange(String yieldRange) { this.yieldRange = yieldRange; }

        public String getIrrigationCondition() { return irrigationCondition; }
        public void setIrrigationCondition(String irrigationCondition) { this.irrigationCondition = irrigationCondition; }

        public String getRecommendedRegions() { return recommendedRegions; }
        public void setRecommendedRegions(String recommendedRegions) { this.recommendedRegions = recommendedRegions; }

        public String getDroughtTolerance() { return droughtTolerance; }
        public void setDroughtTolerance(String droughtTolerance) { this.droughtTolerance = droughtTolerance; }

        public String getFallArmyworm() { return fallArmyworm; }
        public void setFallArmyworm(String fallArmyworm) { this.fallArmyworm = fallArmyworm; }

        public String getStemBorer() { return stemBorer; }
        public void setStemBorer(String stemBorer) { this.stemBorer = stemBorer; }

        public String getTurcicumLeafBlight() { return turcicumLeafBlight; }
        public void setTurcicumLeafBlight(String turcicumLeafBlight) { this.turcicumLeafBlight = turcicumLeafBlight; }

        public String getDownyMildew() { return downyMildew; }
        public void setDownyMildew(String downyMildew) { this.downyMildew = downyMildew; }

        public String getGrainType() { return grainType; }
        public void setGrainType(String grainType) { this.grainType = grainType; }

        public String getSpecialTraits() { return specialTraits; }
        public void setSpecialTraits(String specialTraits) { this.specialTraits = specialTraits; }

        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }

        public String getSuitability() { return suitability; }
        public void setSuitability(String suitability) { this.suitability = suitability; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public List<String> getMatchReasons() { return matchReasons; }
        public void setMatchReasons(List<String> matchReasons) { this.matchReasons = matchReasons; }

        public List<String> getAdvantages() { return advantages; }
        public void setAdvantages(List<String> advantages) { this.advantages = advantages; }

        public List<String> getTradeOffs() { return tradeOffs; }
        public void setTradeOffs(List<String> tradeOffs) { this.tradeOffs = tradeOffs; }

        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }

        public String getSourceOrganization() { return sourceOrganization; }
        public void setSourceOrganization(String sourceOrganization) { this.sourceOrganization = sourceOrganization; }

        public String getSourceDocument() { return sourceDocument; }
        public void setSourceDocument(String sourceDocument) { this.sourceDocument = sourceDocument; }
    }

    // ----------------------------------------------------------------
    // Getters and setters
    // ----------------------------------------------------------------

    public List<RecommendationItem> getRecommendations() { return recommendations; }
    public void setRecommendations(List<RecommendationItem> recommendations) { this.recommendations = recommendations; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }

    public int getCandidatesFound() { return candidatesFound; }
    public void setCandidatesFound(int candidatesFound) { this.candidatesFound = candidatesFound; }

    public String getModelUsed() { return modelUsed; }
    public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public boolean isLlmAvailable() { return llmAvailable; }
    public void setLlmAvailable(boolean llmAvailable) { this.llmAvailable = llmAvailable; }
}
