package com.farm.seedadviosory.data.model;

import java.util.List;

/**
 * A single recommendation item within the response.
 */
public class RecommendationItem {

    private MaizeVariety variety;
    private String suitability; // "HIGH" or "MODERATE"
    private String reason;
    private List<String> matchReasons;
    private List<String> advantages;
    private List<String> tradeOffs;
    private List<RecommendationEvidence> evidence;

    public RecommendationItem() { }

    public MaizeVariety getVariety() { return variety; }
    public void setVariety(MaizeVariety variety) { this.variety = variety; }

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

    public List<RecommendationEvidence> getEvidence() { return evidence; }
    public void setEvidence(List<RecommendationEvidence> evidence) { this.evidence = evidence; }
}
