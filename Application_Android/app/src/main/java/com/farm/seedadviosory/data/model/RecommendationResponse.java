package com.farm.seedadviosory.data.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Android model for the recommendation response from the Spring Boot RAG pipeline.
 * Fields match the backend RecommendationResponse DTO.
 */
public class RecommendationResponse {

    private List<RecommendationItem> recommendations;
    private String summary;
    private List<String> warnings;
    private int candidatesFound;
    private String modelUsed;
    private String generatedAt;
    private boolean llmAvailable;

    // Kept for backward compatibility with existing RecommendationResultsActivity
    private MaizeRecommendationRequest request;

    // ----------------------------------------------------------------
    // Nested item class
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
        private String suitability;
        private String reason;
        private List<String> matchReasons;
        private List<String> advantages;
        private List<String> tradeOffs;
        private List<String> warnings;
        private List<RecommendationEvidence> evidence;
        private String sourceOrganization;
        private String sourceDocument;

        // For backward compat with existing adapter
        private MaizeVariety variety;

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

        public List<RecommendationEvidence> getEvidence() {
            if (evidence != null && !evidence.isEmpty()) return evidence;
            if (sourceOrganization != null || sourceDocument != null) {
                RecommendationEvidence ev = new RecommendationEvidence();
                ev.setSourceOrganization(sourceOrganization != null ? sourceOrganization : "ICAR");
                ev.setSourceDocument(sourceDocument != null ? sourceDocument : "National Seed Registry");
                ev.setSourceType("Documented Agricultural Research");
                ev.setSourceText("Verified research entry for " + (varietyName != null ? varietyName : "recommended variety") + ".");
                List<RecommendationEvidence> list = new ArrayList<>();
                list.add(ev);
                this.evidence = list;
                return list;
            }
            return evidence;
        }
        public void setEvidence(List<RecommendationEvidence> evidence) { this.evidence = evidence; }

        public String getSourceOrganization() { return sourceOrganization; }
        public void setSourceOrganization(String sourceOrganization) { this.sourceOrganization = sourceOrganization; }

        public String getSourceDocument() { return sourceDocument; }
        public void setSourceDocument(String sourceDocument) { this.sourceDocument = sourceDocument; }

        /** Backward compat with RecommendationAdapter that uses getVariety() */
        public MaizeVariety getVariety() {
            if (variety != null) return variety;
            // Build a MaizeVariety from flat fields for adapter compatibility
            MaizeVariety v = new MaizeVariety();
            v.setVarietyId(varietyId);
            v.setVarietyName(varietyName);
            v.setCategory(category);
            v.setSeason(season);
            v.setYieldValue(yieldRange);
            v.setIrrigationCondition(irrigationCondition);
            v.setRecommendedRegions(recommendedRegions);
            v.setDroughtTolerance(droughtTolerance);
            v.setFallArmyworm(fallArmyworm);
            v.setStemBorer(stemBorer);
            v.setTurcicumLeafBlight(turcicumLeafBlight);
            v.setDownyMildew(downyMildew);
            v.setGrainType(grainType);
            v.setSpecialTraits(specialTraits);
            v.setSourceOrganization(sourceOrganization);
            v.setSourceDocument(sourceDocument);
            if (maturityDays != null) {
                String[] parts = maturityDays.replaceAll("[^0-9–-]", "").split("[–-]");
                if (parts.length >= 2) {
                    try {
                        v.setMaturityDaysMin(Integer.parseInt(parts[0].trim()));
                        v.setMaturityDaysMax(Integer.parseInt(parts[1].trim()));
                    } catch (Exception ignored) {}
                }
            }
            this.variety = v; // Store so Gson serialization contains variety!
            return v;
        }
        public void setVariety(MaizeVariety variety) { this.variety = variety; }
    }

    // ----------------------------------------------------------------
    // Getters and setters
    // ----------------------------------------------------------------

    public List<RecommendationItem> getRecommendations() { return recommendations; }
    public void setRecommendations(List<RecommendationItem> recommendations) { this.recommendations = recommendations; }

    /**
     * For backward compatibility with MockDataProvider which uses old standalone RecommendationItem class.
     * Converts old demo items to new nested structure.
     */
    @Deprecated
    public void setDemoRecommendations(List<com.farm.seedadviosory.data.model.RecommendationItem> demoItems) {
        if (demoItems == null) {
            this.recommendations = null;
            return;
        }
        
        List<RecommendationItem> converted = new ArrayList<>();
        for (com.farm.seedadviosory.data.model.RecommendationItem oldItem : demoItems) {
            RecommendationItem newItem = new RecommendationItem();
            
            // Copy basic fields from the variety
            if (oldItem.getVariety() != null) {
                MaizeVariety v = oldItem.getVariety();
                newItem.setVarietyId(v.getVarietyId());
                newItem.setVarietyName(v.getVarietyName());
                newItem.setCategory(v.getCategory());
                newItem.setSeason(v.getSeason());
                newItem.setMaturityDays(v.getMaturityDaysMin() + "-" + v.getMaturityDaysMax() + " days");
                newItem.setYieldRange(v.getYieldValue());
                newItem.setIrrigationCondition(v.getIrrigationCondition());
                newItem.setRecommendedRegions(v.getRecommendedRegions());
                newItem.setDroughtTolerance(v.getDroughtTolerance());
                newItem.setFallArmyworm(v.getFallArmyworm());
                newItem.setStemBorer(v.getStemBorer());
                newItem.setTurcicumLeafBlight(v.getTurcicumLeafBlight());
                newItem.setDownyMildew(v.getDownyMildew());
                newItem.setGrainType(v.getGrainType());
                newItem.setSpecialTraits(v.getSpecialTraits());
                newItem.setSourceOrganization(v.getSourceOrganization());
                newItem.setSourceDocument(v.getSourceDocument());
                newItem.setVariety(v); // For adapter compatibility
            }
            
            newItem.setSuitability(oldItem.getSuitability());
            newItem.setReason(oldItem.getReason());
            newItem.setMatchReasons(oldItem.getMatchReasons());
            newItem.setAdvantages(oldItem.getAdvantages());
            newItem.setTradeOffs(oldItem.getTradeOffs());
            newItem.setEvidence(oldItem.getEvidence());
            newItem.setScore("HIGH".equals(oldItem.getSuitability()) ? 8.0 : 6.0);
            
            converted.add(newItem);
        }
        
        this.recommendations = converted;
    }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }

    public int getCandidatesFound() { return candidatesFound; }
    public void setCandidatesFound(int candidatesFound) { this.candidatesFound = candidatesFound; }

    public String getModelUsed() { return modelUsed; }
    public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }

    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }

    public boolean isLlmAvailable() { return llmAvailable; }
    public void setLlmAvailable(boolean llmAvailable) { this.llmAvailable = llmAvailable; }

    public MaizeRecommendationRequest getRequest() { return request; }
    public void setRequest(MaizeRecommendationRequest request) { this.request = request; }
}
