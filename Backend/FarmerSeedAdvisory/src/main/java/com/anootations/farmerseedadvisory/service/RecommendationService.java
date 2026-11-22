package com.anootations.farmerseedadvisory.service;

import com.anootations.farmerseedadvisory.dto.MaizeRecommendationRequest;
import com.anootations.farmerseedadvisory.dto.RecommendationResponse;
import com.anootations.farmerseedadvisory.dto.RecommendationResponse.RecommendationItem;
import com.anootations.farmerseedadvisory.entity.CottonVariety;
import com.anootations.farmerseedadvisory.entity.MaizeVariety;
import com.anootations.farmerseedadvisory.entity.SoybeanVariety;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG Recommendation Pipeline for Maize seed selection.
 *
 * Pipeline:
 *   1. Metadata Filtering — reduces 302 varieties to ~10-20 candidates
 *   2. Context Building — creates a prompt with candidate details
 *   3. LLM Generation — sends prompt to local LLM
 *   4. Response Parsing — extracts structured JSON from LLM output
 *   5. Fallback Scoring — if LLM unavailable, use deterministic scoring
 *
 * The LLM is instructed to:
 *   - Use ONLY retrieved agricultural knowledge
 *   - NOT invent variety characteristics
 *   - Return structured JSON
 */
@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    @Value("${recommendation.top-k:5}")
    private int topK;

    @Value("${llm.model:llama3}")
    private String llmModel;

    private final MetadataFilterService metadataFilterService;
    private final LlmService llmService;
    private final Gson gson = new Gson();

    public RecommendationService(
            MetadataFilterService metadataFilterService,
            LlmService llmService
    ) {
        this.metadataFilterService = metadataFilterService;
        this.llmService = llmService;
    }

    // ----------------------------------------------------------------
    // Main recommendation pipeline
    // ----------------------------------------------------------------

    public RecommendationResponse recommend(MaizeRecommendationRequest request) {

        log.info("Starting recommendation for state={}, season={}, irrigation={}",
                request.getState(), request.getSeason(), request.getIrrigation());

        // STEP 1: Metadata Filtering
        List<MaizeVariety> candidates = metadataFilterService.filter(
                request.getState(),
                request.getSeason(),
                request.getIrrigation(),
                request.getMaximumMaturityDays()
        );

        log.info("Metadata filter: {} → {} candidates", "302 varieties", candidates.size());

        if (candidates.isEmpty()) {
            return buildEmptyResponse(request, 0);
        }

        // Limit candidates for context window (top 10 pre-ranked varieties)
        List<MaizeVariety> topCandidates = candidates.stream()
                .limit(10)
                .collect(Collectors.toList());

        // STEP 2: Check LLM availability
        boolean llmAvailable = llmService.isAvailable();
        log.info("LLM available: {}", llmAvailable);

        if (llmAvailable) {
            // STEP 3: Build RAG prompt
            String prompt = buildRagPrompt(request, topCandidates);

            // STEP 4: Call LLM
            String llmResponse = llmService.generate(prompt);

            if (llmResponse != null) {
                // STEP 5: Parse LLM response
                RecommendationResponse response = parseLlmResponse(
                        llmResponse, topCandidates, request, candidates.size()
                );
                if (response != null) {
                    response.setLlmAvailable(true);
                    return response;
                }
            }

            log.warn("LLM response parsing failed — falling back to deterministic scoring");
        }

        // STEP 6: Fallback — deterministic scoring when LLM unavailable
        return buildDeterministicResponse(request, topCandidates, candidates.size());
    }

    // ----------------------------------------------------------------
    // RAG prompt builder
    // ----------------------------------------------------------------

    private String buildRagPrompt(MaizeRecommendationRequest request, List<MaizeVariety> candidates) {

        StringBuilder prompt = new StringBuilder();

        prompt.append("You are an agricultural expert assistant helping Indian farmers select the best maize variety.\n\n");

        // Farmer requirements section
        prompt.append("=== FARMER REQUIREMENTS ===\n");
        if (request.getState() != null) prompt.append("State: ").append(request.getState()).append("\n");
        if (request.getDistrict() != null) prompt.append("District: ").append(request.getDistrict()).append("\n");
        if (request.getSeason() != null) prompt.append("Growing season: ").append(request.getSeason()).append("\n");
        if (request.getIrrigation() != null) prompt.append("Irrigation: ").append(request.getIrrigation()).append("\n");
        if (request.getSoilType() != null) prompt.append("Soil type: ").append(request.getSoilType()).append("\n");
        if (request.getMaximumMaturityDays() > 0) {
            prompt.append("Maximum maturity: ").append(request.getMaximumMaturityDays()).append(" days\n");
        }
        if (request.getClimateRisks() != null && !request.getClimateRisks().isEmpty()) {
            prompt.append("Climate risks: ").append(String.join(", ", request.getClimateRisks())).append("\n");
        }
        if (request.getPriority() != null) prompt.append("Priority: ").append(request.getPriority()).append("\n");
        if (request.getFarmerDescription() != null && !request.getFarmerDescription().isBlank()) {
            prompt.append("Farmer notes: ").append(request.getFarmerDescription()).append("\n");
        }

        prompt.append("\n=== RETRIEVED AGRICULTURAL KNOWLEDGE ===\n");
        prompt.append("The following maize varieties have been retrieved from the ICAR agricultural database\n");
        prompt.append("based on the farmer's location and conditions. Use ONLY this information.\n\n");

        // Add candidate variety details
        for (int i = 0; i < candidates.size(); i++) {
            MaizeVariety v = candidates.get(i);
            prompt.append("--- Variety ").append(i + 1).append(": ").append(v.getVarietyName()).append(" ---\n");
            prompt.append("ID: ").append(v.getVarietyId()).append("\n");
            if (v.getCategory() != null) prompt.append("Category: ").append(v.getCategory()).append("\n");
            if (v.getSeason() != null) prompt.append("Season: ").append(v.getSeason()).append("\n");
            if (v.getMaturityDaysMin() != null && v.getMaturityDaysMax() != null) {
                prompt.append("Maturity: ").append(v.getMaturityDaysMin()).append("-").append(v.getMaturityDaysMax()).append(" days\n");
            }
            if (v.getYieldValue() != null) prompt.append("Yield: ").append(v.getYieldValue()).append("\n");
            if (v.getRecommendedRegions() != null) prompt.append("Recommended regions: ").append(v.getRecommendedRegions()).append("\n");
            if (v.getIrrigationCondition() != null) prompt.append("Irrigation: ").append(v.getIrrigationCondition()).append("\n");
            if (v.getDroughtTolerance() != null) prompt.append("Drought tolerance: ").append(v.getDroughtTolerance()).append("\n");
            if (v.getWaterloggingTolerance() != null) prompt.append("Waterlogging tolerance: ").append(v.getWaterloggingTolerance()).append("\n");
            if (v.getHeatTolerance() != null) prompt.append("Heat tolerance: ").append(v.getHeatTolerance()).append("\n");
            if (v.getFallArmyworm() != null) prompt.append("Fall Armyworm: ").append(v.getFallArmyworm()).append("\n");
            if (v.getStemBorer() != null) prompt.append("Stem Borer: ").append(v.getStemBorer()).append("\n");
            if (v.getTurcicumLeafBlight() != null) prompt.append("Turcicum Leaf Blight: ").append(v.getTurcicumLeafBlight()).append("\n");
            if (v.getDownyMildew() != null) prompt.append("Downy Mildew: ").append(v.getDownyMildew()).append("\n");
            if (v.getSpecialTraits() != null) prompt.append("Special traits: ").append(v.getSpecialTraits()).append("\n");
            if (v.getSourceOrganization() != null) prompt.append("Source: ").append(v.getSourceOrganization()).append("\n");
            prompt.append("\n");
        }

        // Instructions
        prompt.append("=== INSTRUCTIONS ===\n");
        prompt.append("1. Select the TOP ").append(Math.min(topK, candidates.size())).append(" varieties most suitable for this farmer.\n");
        prompt.append("2. Use ONLY the information provided above. Do NOT invent yield values, resistance ratings, or characteristics.\n");
        prompt.append("3. If information is missing for a field, state 'Not documented' rather than guessing.\n");
        prompt.append("4. Explain WHY each variety was selected based on the farmer's specific conditions.\n");
        prompt.append("5. Highlight any climate risks or disease pressures relevant to the farmer.\n");
        prompt.append("6. Return a valid JSON response in EXACTLY this format:\n\n");

        prompt.append("```json\n");
        prompt.append("{\n");
        prompt.append("  \"recommendations\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"varietyId\": \"MZ001\",\n");
        prompt.append("      \"varietyName\": \"DHM-117\",\n");
        prompt.append("      \"score\": 0.91,\n");
        prompt.append("      \"suitability\": \"HIGH\",\n");
        prompt.append("      \"reason\": \"Explanation based on retrieved data...\",\n");
        prompt.append("      \"matchReasons\": [\"Matches Kharif season\", \"Suitable for Andhra Pradesh\"],\n");
        prompt.append("      \"warnings\": [\"Check local seed availability\"]\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"summary\": \"Overall recommendation summary...\",\n");
        prompt.append("  \"warnings\": [\"General warnings for the farmer\"]\n");
        prompt.append("}\n");
        prompt.append("```\n");
        prompt.append("\nRespond with ONLY the JSON. No additional text before or after the JSON.\n");

        return prompt.toString();
    }

    // ----------------------------------------------------------------
    // Parse LLM response
    // ----------------------------------------------------------------

    private RecommendationResponse parseLlmResponse(
            String llmResponse,
            List<MaizeVariety> candidates,
            MaizeRecommendationRequest request,
            int totalCandidates
    ) {
        try {
            // Extract JSON from the response (handle markdown code blocks)
            String json = extractJson(llmResponse);
            if (json == null) {
                log.warn("Could not extract JSON from LLM response");
                return null;
            }

            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            RecommendationResponse response = new RecommendationResponse();
            List<RecommendationItem> items = new ArrayList<>();

            if (root.has("recommendations")) {
                JsonArray recs = root.getAsJsonArray("recommendations");

                for (int i = 0; i < recs.size(); i++) {
                    JsonObject rec = recs.get(i).getAsJsonObject();
                    RecommendationItem item = new RecommendationItem();

                    String varietyId = safeGetString(rec, "varietyId");
                    item.setVarietyId(varietyId);
                    item.setVarietyName(safeGetString(rec, "varietyName"));
                    item.setScore(rec.has("score") ? rec.get("score").getAsDouble() : 0.8);
                    item.setSuitability(safeGetString(rec, "suitability"));
                    item.setReason(safeGetString(rec, "reason"));

                    // Parse matchReasons array
                    List<String> matchReasons = new ArrayList<>();
                    if (rec.has("matchReasons")) {
                        rec.getAsJsonArray("matchReasons")
                                .forEach(r -> matchReasons.add(r.getAsString()));
                    }
                    item.setMatchReasons(matchReasons);

                    // Parse warnings array
                    List<String> itemWarnings = new ArrayList<>();
                    if (rec.has("warnings")) {
                        rec.getAsJsonArray("warnings")
                                .forEach(w -> itemWarnings.add(w.getAsString()));
                    }
                    item.setWarnings(itemWarnings);

                    // Enrich with database fields (LLM only provides explanation; DB has the facts)
                    enrichFromDatabase(item, varietyId, candidates);

                    items.add(item);
                }
            }

            response.setRecommendations(items);

            if (root.has("summary")) {
                response.setSummary(root.get("summary").getAsString());
            }

            // Parse global warnings
            List<String> globalWarnings = new ArrayList<>();
            if (root.has("warnings")) {
                root.getAsJsonArray("warnings").forEach(w -> globalWarnings.add(w.getAsString()));
            }
            // Always add the standard disclaimer
            globalWarnings.add("Recommendations are based on documented agricultural research. Consult your local agricultural extension officer before finalizing seed selection.");
            response.setWarnings(globalWarnings);

            response.setCandidatesFound(totalCandidates);
            response.setModelUsed(llmService.getActiveModel());
            response.setGeneratedAt(LocalDateTime.now());

            return response;

        } catch (Exception e) {
            log.error("Failed to parse LLM JSON response: {}", e.getMessage());
            return null;
        }
    }

    // ----------------------------------------------------------------
    // Enrich item with full database fields
    // ----------------------------------------------------------------

    private void enrichFromDatabase(
            RecommendationItem item,
            String varietyId,
            List<MaizeVariety> candidates
    ) {
        candidates.stream()
                .filter(v -> v.getVarietyId() != null && v.getVarietyId().equals(varietyId))
                .findFirst()
                .ifPresent(v -> {
                    item.setCategory(v.getCategory());
                    item.setSeason(v.getSeason());
                    if (v.getMaturityDaysMin() != null && v.getMaturityDaysMax() != null) {
                        item.setMaturityDays(v.getMaturityDaysMin() + "–" + v.getMaturityDaysMax() + " days");
                    } else if (v.getMaturityDaysMax() != null) {
                        item.setMaturityDays("Up to " + v.getMaturityDaysMax() + " days");
                    }
                    item.setYieldRange(v.getYieldValue());
                    item.setIrrigationCondition(v.getIrrigationCondition());
                    item.setRecommendedRegions(v.getRecommendedRegions());
                    item.setDroughtTolerance(v.getDroughtTolerance());
                    item.setFallArmyworm(v.getFallArmyworm());
                    item.setStemBorer(v.getStemBorer());
                    item.setTurcicumLeafBlight(v.getTurcicumLeafBlight());
                    item.setDownyMildew(v.getDownyMildew());
                    item.setGrainType(v.getGrainType());
                    item.setSpecialTraits(v.getSpecialTraits());
                    item.setSourceOrganization(v.getSourceOrganization());
                    item.setSourceDocument(v.getSourceDocument());

                    if (item.getAdvantages() == null || item.getAdvantages().isEmpty()) {
                        List<String> advantages = new ArrayList<>();
                        if (v.getSpecialTraits() != null && !v.getSpecialTraits().isBlank()) {
                            advantages.add("✓ " + v.getSpecialTraits());
                        }
                        if (v.getYieldValue() != null) {
                            advantages.add("✓ Potential Yield: " + v.getYieldValue());
                        }
                        if (v.getMaturityDaysMax() != null) {
                            advantages.add("✓ Maturity cycle: " + v.getMaturityDaysMin() + "–" + v.getMaturityDaysMax() + " days");
                        }
                        item.setAdvantages(advantages);
                    }

                    if (item.getTradeOffs() == null || item.getTradeOffs().isEmpty()) {
                        item.setTradeOffs(List.of(
                                "⚠ Yield potential depends on actual field conditions and crop management.",
                                "⚠ Verify local seed availability before purchase."
                        ));
                    }
                });
    }

    // ----------------------------------------------------------------
    // Fallback: deterministic scoring when LLM is unavailable
    // ----------------------------------------------------------------

    private RecommendationResponse buildDeterministicResponse(
            MaizeRecommendationRequest request,
            List<MaizeVariety> candidates,
            int totalCandidates
    ) {
        log.info("Building deterministic recommendation (LLM unavailable) for {} candidates", candidates.size());

        List<ScoredVariety> scored = candidates.stream()
                .map(v -> new ScoredVariety(v, score(v, request)))
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .limit(topK)
                .collect(Collectors.toList());

        List<RecommendationItem> items = scored.stream()
                .map(sv -> buildItemFromVariety(sv.variety, sv.score, request))
                .collect(Collectors.toList());

        RecommendationResponse response = new RecommendationResponse();
        response.setRecommendations(items);
        response.setSummary(buildFallbackSummary(request, items.size(), totalCandidates));
        response.setWarnings(List.of(
                "Recommendations generated using metadata matching (AI ranking engine not available).",
                "Consult your local agricultural extension officer before finalizing seed selection."
        ));
        response.setCandidatesFound(totalCandidates);
        response.setModelUsed("metadata-scoring");
        response.setGeneratedAt(LocalDateTime.now());
        response.setLlmAvailable(false);
        return response;
    }

    private double score(MaizeVariety v, MaizeRecommendationRequest request) {
        double score = 0;

        // Region match (0–3 points)
        if (v.getRecommendedRegions() != null && request.getState() != null) {
            if (v.getRecommendedRegions().toLowerCase().contains(request.getState().toLowerCase())) score += 3;
            else if (v.getRecommendedRegions().toLowerCase().contains("all india")) score += 2;
        }

        // Season match (0–2 points)
        if (v.getSeason() != null && request.getSeason() != null) {
            if (v.getSeason().toLowerCase().contains(request.getSeason().toLowerCase())) score += 2;
        }

        // Climate risks (0–1 point each)
        if (request.getClimateRisks() != null) {
            for (String risk : request.getClimateRisks()) {
                String r = risk.toLowerCase();
                if (r.contains("drought") && "good".equalsIgnoreCase(v.getDroughtTolerance())) score += 1;
                if (r.contains("waterlog") && v.getWaterloggingTolerance() != null) score += 0.5;
                if (r.contains("heat") && v.getHeatTolerance() != null) score += 0.5;
                if (r.contains("fall armyworm") && v.getFallArmyworm() != null && v.getFallArmyworm().toLowerCase().contains("resistant")) score += 1;
                if (r.contains("stem borer") && v.getStemBorer() != null && v.getStemBorer().toLowerCase().contains("resistant")) score += 0.5;
            }
        }

        // Yield data available (0.5 points)
        if (v.getYieldValue() != null) score += 0.5;

        // Source documented (0.5 points)
        if (v.getSourceOrganization() != null) score += 0.5;

        return score;
    }

    private RecommendationItem buildItemFromVariety(
            MaizeVariety v,
            double rawScore,
            MaizeRecommendationRequest request
    ) {
        RecommendationItem item = new RecommendationItem();
        item.setVarietyId(v.getVarietyId());
        item.setVarietyName(v.getVarietyName());
        item.setCategory(v.getCategory());
        item.setSeason(v.getSeason());
        if (v.getMaturityDaysMin() != null && v.getMaturityDaysMax() != null) {
            item.setMaturityDays(v.getMaturityDaysMin() + "–" + v.getMaturityDaysMax() + " days");
        }
        item.setYieldRange(v.getYieldValue());
        item.setIrrigationCondition(v.getIrrigationCondition());
        item.setRecommendedRegions(v.getRecommendedRegions());
        item.setDroughtTolerance(v.getDroughtTolerance());
        item.setFallArmyworm(v.getFallArmyworm());
        item.setStemBorer(v.getStemBorer());
        item.setTurcicumLeafBlight(v.getTurcicumLeafBlight());
        item.setDownyMildew(v.getDownyMildew());
        item.setGrainType(v.getGrainType());
        item.setSpecialTraits(v.getSpecialTraits());
        item.setSourceOrganization(v.getSourceOrganization());
        item.setSourceDocument(v.getSourceDocument());

        // Normalize score to 0–1
        double normalizedScore = Math.min(1.0, rawScore / 8.0);
        item.setScore(Math.round(normalizedScore * 100.0) / 100.0);
        item.setSuitability(normalizedScore >= 0.6 ? "HIGH" : normalizedScore >= 0.35 ? "MODERATE" : "LOW");

        // Build match reasons
        List<String> matchReasons = new ArrayList<>();
        if (v.getRecommendedRegions() != null && request.getState() != null &&
                v.getRecommendedRegions().toLowerCase().contains(request.getState().toLowerCase())) {
            matchReasons.add("✓ Recommended for " + request.getState());
        }
        if (v.getSeason() != null && request.getSeason() != null &&
                v.getSeason().toLowerCase().contains(request.getSeason().toLowerCase())) {
            matchReasons.add("✓ Suitable for " + request.getSeason() + " season");
        }
        if (v.getIrrigationCondition() != null) {
            matchReasons.add("✓ " + v.getIrrigationCondition() + " conditions supported");
        }
        if ("Good".equalsIgnoreCase(v.getDroughtTolerance())) {
            matchReasons.add("✓ Good drought tolerance documented");
        }
        if (v.getSourceOrganization() != null) {
            matchReasons.add("✓ Documented by " + v.getSourceOrganization());
        }
        item.setMatchReasons(matchReasons);
        item.setReason("Selected based on regional suitability and documented characteristics from " +
                       (v.getSourceOrganization() != null ? v.getSourceOrganization() : "agricultural research data") + ".");

        List<String> advantages = new ArrayList<>();
        if (v.getSpecialTraits() != null && !v.getSpecialTraits().isBlank()) {
            advantages.add("✓ " + v.getSpecialTraits());
        }
        if (v.getYieldValue() != null) {
            advantages.add("✓ Potential Yield: " + v.getYieldValue());
        }
        if (v.getMaturityDaysMax() != null) {
            advantages.add("✓ Maturity cycle: " + v.getMaturityDaysMin() + "–" + v.getMaturityDaysMax() + " days");
        }
        item.setAdvantages(advantages);

        item.setTradeOffs(List.of(
                "⚠ Yield potential depends on actual field conditions and crop management.",
                "⚠ Verify local seed availability before purchase."
        ));

        item.setWarnings(List.of(
                "Yield potential depends on actual field conditions and crop management.",
                "Verify local seed availability before purchase."
        ));

        return item;
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private String extractJson(String llmResponse) {
        if (llmResponse == null) return null;

        // Handle markdown code blocks: ```json ... ```
        if (llmResponse.contains("```json")) {
            int start = llmResponse.indexOf("```json") + 7;
            int end = llmResponse.lastIndexOf("```");
            if (end > start) {
                return llmResponse.substring(start, end).trim();
            }
        }

        // Handle plain code blocks: ``` ... ```
        if (llmResponse.contains("```")) {
            int start = llmResponse.indexOf("```") + 3;
            int end = llmResponse.lastIndexOf("```");
            if (end > start) {
                return llmResponse.substring(start, end).trim();
            }
        }

        // Try raw JSON or find outermost JSON object { ... }
        String trimmed = llmResponse.trim();
        int firstBrace = trimmed.indexOf("{");
        int lastBrace = trimmed.lastIndexOf("}");
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            return trimmed.substring(firstBrace, lastBrace + 1).trim();
        }

        return null;
    }

    private String safeGetString(JsonObject obj, String key) {
        if (!obj.has(key) || obj.get(key).isJsonNull()) return null;
        return obj.get(key).getAsString();
    }

    private RecommendationResponse buildEmptyResponse(MaizeRecommendationRequest request, int candidates) {
        RecommendationResponse response = new RecommendationResponse();
        response.setRecommendations(new ArrayList<>());
        response.setSummary("No maize varieties matched the specified conditions.");
        response.setWarnings(List.of(
                "No varieties found for the specified state, season, and conditions.",
                "Try relaxing the maturity constraint or season filter.",
                "Consult your local agricultural extension office for specific recommendations."
        ));
        response.setCandidatesFound(0);
        response.setGeneratedAt(LocalDateTime.now());
        response.setLlmAvailable(false);
        return response;
    }

    private String buildFallbackSummary(MaizeRecommendationRequest request, int recommended, int total) {
        StringBuilder sb = new StringBuilder();
        sb.append(recommended).append(" maize ");
        sb.append(recommended == 1 ? "variety was" : "varieties were");
        sb.append(" identified from ").append(total).append(" candidates");
        if (request.getState() != null) sb.append(" for ").append(request.getState());
        if (request.getSeason() != null) sb.append(", ").append(request.getSeason()).append(" season");
        sb.append(". Selection is based on metadata matching against ICAR agricultural research data.");
        return sb.toString();
    }

    private static class ScoredVariety {
        final MaizeVariety variety;
        final double score;
        ScoredVariety(MaizeVariety variety, double score) {
            this.variety = variety;
            this.score = score;
        }
    }

    // ================================================================
    // SOYBEAN RECOMMENDATION PIPELINE
    // ================================================================

    public RecommendationResponse recommendSoybean(MaizeRecommendationRequest request) {

        log.info("Starting soybean recommendation for state={}, season={}, irrigation={}",
                request.getState(), request.getSeason(), request.getIrrigation());

        // STEP 1: Metadata Filtering
        List<SoybeanVariety> candidates = metadataFilterService.filterSoybean(
                request.getState(),
                request.getSeason(),
                request.getIrrigation(),
                request.getMaximumMaturityDays()
        );

        log.info("Soybean metadata filter: 184 varieties → {} candidates", candidates.size());

        if (candidates.isEmpty()) {
            return buildEmptySoybeanResponse(request, 0);
        }

        // STEP 1.5: Multidimensional Agronomic Searching & Ranking
        List<SoybeanVariety> rankedCandidates = metadataFilterService.searchAndRankSoybean(request, candidates);

        // Limit top candidates for context window (guarantees top 10 ranked varieties sent to LLM)
        List<SoybeanVariety> topCandidates = rankedCandidates.stream()
                .limit(10)
                .collect(Collectors.toList());

        // STEP 2: Check LLM availability
        boolean llmAvailable = llmService.isAvailable();
        log.info("LLM available: {}", llmAvailable);

        if (llmAvailable) {
            // STEP 3: Build RAG prompt
            String prompt = buildSoybeanRagPrompt(request, topCandidates);

            // STEP 4: Call LLM
            String llmResponse = llmService.generate(prompt);

            if (llmResponse != null) {
                // STEP 5: Parse LLM response
                RecommendationResponse response = parseSoybeanLlmResponse(
                        llmResponse, topCandidates, request, candidates.size()
                );
                if (response != null) {
                    response.setLlmAvailable(true);
                    return response;
                }
            }

            log.warn("LLM response parsing failed for soybean — falling back to deterministic scoring");
        }

        // STEP 6: Fallback — deterministic scoring when LLM unavailable
        return buildDeterministicSoybeanResponse(request, topCandidates, candidates.size());
    }

    private String buildSoybeanRagPrompt(MaizeRecommendationRequest request, List<SoybeanVariety> candidates) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are an agricultural expert assistant helping Indian farmers select the best soybean variety.\n\n");

        // Farmer requirements section
        prompt.append("=== FARMER REQUIREMENTS ===\n");
        if (request.getState() != null) prompt.append("State: ").append(request.getState()).append("\n");
        if (request.getDistrict() != null) prompt.append("District: ").append(request.getDistrict()).append("\n");
        if (request.getSeason() != null) prompt.append("Growing season: ").append(request.getSeason()).append("\n");
        if (request.getIrrigation() != null) prompt.append("Irrigation: ").append(request.getIrrigation()).append("\n");
        if (request.getSoilType() != null) prompt.append("Soil type: ").append(request.getSoilType()).append("\n");
        if (request.getMaximumMaturityDays() > 0) {
            prompt.append("Maximum maturity: ").append(request.getMaximumMaturityDays()).append(" days\n");
        }
        if (request.getClimateRisks() != null && !request.getClimateRisks().isEmpty()) {
            prompt.append("Climate risks: ").append(String.join(", ", request.getClimateRisks())).append("\n");
        }
        if (request.getPriority() != null) prompt.append("Priority: ").append(request.getPriority()).append("\n");
        if (request.getFarmerDescription() != null && !request.getFarmerDescription().isBlank()) {
            prompt.append("Farmer notes: ").append(request.getFarmerDescription()).append("\n");
        }

        prompt.append("\n=== RETRIEVED AGRICULTURAL KNOWLEDGE ===\n");
        prompt.append("The following soybean varieties have been retrieved from the ICAR/ICAR-IISR agricultural database\n");
        prompt.append("based on the farmer's location and conditions. Use ONLY this information.\n\n");

        // Add candidate variety details
        for (int i = 0; i < candidates.size(); i++) {
            SoybeanVariety v = candidates.get(i);
            prompt.append("--- Variety ").append(i + 1).append(": ").append(v.getVarietyName()).append(" ---\n");
            prompt.append("ID: ").append(v.getVarietyId()).append("\n");
            if (v.getCategory() != null) prompt.append("Category: ").append(v.getCategory()).append("\n");
            if (v.getSeason() != null) prompt.append("Season: ").append(v.getSeason()).append("\n");
            if (v.getMaturityDaysMin() != null && v.getMaturityDaysMax() != null) {
                prompt.append("Maturity: ").append(v.getMaturityDaysMin()).append("-").append(v.getMaturityDaysMax()).append(" days\n");
            }
            if (v.getYieldValue() != null) prompt.append("Yield: ").append(v.getYieldValue()).append("\n");
            if (v.getRecommendedRegions() != null) prompt.append("Recommended regions: ").append(v.getRecommendedRegions()).append("\n");
            if (v.getAgroClimaticZone() != null) prompt.append("Agro-climatic zone: ").append(v.getAgroClimaticZone()).append("\n");
            if (v.getIrrigationCondition() != null) prompt.append("Irrigation: ").append(v.getIrrigationCondition()).append("\n");
            if (v.getDroughtTolerance() != null) prompt.append("Drought tolerance: ").append(v.getDroughtTolerance()).append("\n");
            if (v.getWaterloggingTolerance() != null) prompt.append("Waterlogging tolerance: ").append(v.getWaterloggingTolerance()).append("\n");
            if (v.getLodgingResistance() != null) prompt.append("Lodging resistance: ").append(v.getLodgingResistance()).append("\n");
            if (v.getCharcoalRot() != null) prompt.append("Charcoal rot: ").append(v.getCharcoalRot()).append("\n");
            if (v.getStemBorer() != null) prompt.append("Stem borer / fly: ").append(v.getStemBorer()).append("\n");
            if (v.getSpecialTraits() != null) prompt.append("Special traits: ").append(v.getSpecialTraits()).append("\n");
            if (v.getSourceOrganization() != null) prompt.append("Source: ").append(v.getSourceOrganization()).append("\n");
            prompt.append("\n");
        }

        // Instructions
        prompt.append("=== INSTRUCTIONS ===\n");
        prompt.append("1. Select the TOP ").append(Math.min(topK, candidates.size())).append(" soybean varieties most suitable for this farmer.\n");
        prompt.append("2. Use ONLY the information provided above. Do NOT invent yield values, resistance ratings, or characteristics.\n");
        prompt.append("3. If information is missing for a field, state 'Not documented' rather than guessing.\n");
        prompt.append("4. Explain WHY each variety was selected based on the farmer's specific conditions.\n");
        prompt.append("5. Highlight any climate risks or disease pressures relevant to the farmer.\n");
        prompt.append("6. Return a valid JSON response in EXACTLY this format:\n\n");

        prompt.append("```json\n");
        prompt.append("{\n");
        prompt.append("  \"recommendations\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"varietyId\": \"SOY_VER_001\",\n");
        prompt.append("      \"varietyName\": \"JS 335\",\n");
        prompt.append("      \"score\": 0.92,\n");
        prompt.append("      \"suitability\": \"HIGH\",\n");
        prompt.append("      \"reason\": \"Explanation based on retrieved data...\",\n");
        prompt.append("      \"matchReasons\": [\"Matches Kharif season\", \"Suitable for Central Zone\"],\n");
        prompt.append("      \"warnings\": [\"Check local certified seed availability\"]\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"summary\": \"Overall recommendation summary...\",\n");
        prompt.append("  \"warnings\": [\"General warnings for the farmer\"]\n");
        prompt.append("}\n");
        prompt.append("```\n");
        prompt.append("\nRespond with ONLY the JSON. No additional text before or after the JSON.\n");

        return prompt.toString();
    }

    private RecommendationResponse parseSoybeanLlmResponse(
            String llmResponse,
            List<SoybeanVariety> candidates,
            MaizeRecommendationRequest request,
            int totalCandidates
    ) {
        try {
            String json = extractJson(llmResponse);
            if (json == null) {
                log.warn("Could not extract JSON from soybean LLM response");
                return null;
            }

            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            RecommendationResponse response = new RecommendationResponse();
            List<RecommendationItem> items = new ArrayList<>();

            if (root.has("recommendations")) {
                JsonArray recs = root.getAsJsonArray("recommendations");

                for (int i = 0; i < recs.size(); i++) {
                    JsonObject rec = recs.get(i).getAsJsonObject();
                    RecommendationItem item = new RecommendationItem();

                    String varietyId = safeGetString(rec, "varietyId");
                    item.setVarietyId(varietyId);
                    item.setVarietyName(safeGetString(rec, "varietyName"));
                    item.setScore(rec.has("score") ? rec.get("score").getAsDouble() : 0.8);
                    item.setSuitability(safeGetString(rec, "suitability"));
                    item.setReason(safeGetString(rec, "reason"));

                    List<String> matchReasons = new ArrayList<>();
                    if (rec.has("matchReasons")) {
                        rec.getAsJsonArray("matchReasons")
                                .forEach(r -> matchReasons.add(r.getAsString()));
                    }
                    item.setMatchReasons(matchReasons);

                    List<String> itemWarnings = new ArrayList<>();
                    if (rec.has("warnings")) {
                        rec.getAsJsonArray("warnings")
                                .forEach(w -> itemWarnings.add(w.getAsString()));
                    }
                    item.setWarnings(itemWarnings);

                    enrichSoybeanFromDatabase(item, varietyId, candidates);
                    items.add(item);
                }
            }

            response.setRecommendations(items);

            if (root.has("summary")) {
                response.setSummary(root.get("summary").getAsString());
            }

            List<String> globalWarnings = new ArrayList<>();
            if (root.has("warnings")) {
                root.getAsJsonArray("warnings").forEach(w -> globalWarnings.add(w.getAsString()));
            }
            globalWarnings.add("Recommendations are based on documented ICAR/ICAR-IISR agricultural research. Consult your local agricultural extension officer before finalizing seed selection.");
            response.setWarnings(globalWarnings);

            response.setCandidatesFound(totalCandidates);
            response.setModelUsed(llmService.getActiveModel());
            response.setGeneratedAt(LocalDateTime.now());

            return response;

        } catch (Exception e) {
            log.error("Failed to parse soybean LLM JSON response: {}", e.getMessage());
            return null;
        }
    }

    private void enrichSoybeanFromDatabase(
            RecommendationItem item,
            String varietyId,
            List<SoybeanVariety> candidates
    ) {
        candidates.stream()
                .filter(v -> v.getVarietyId() != null && v.getVarietyId().equals(varietyId))
                .findFirst()
                .ifPresent(v -> {
                    item.setCategory(v.getCategory());
                    item.setSeason(v.getSeason());
                    if (v.getMaturityDaysMin() != null && v.getMaturityDaysMax() != null) {
                        item.setMaturityDays(v.getMaturityDaysMin() + "–" + v.getMaturityDaysMax() + " days");
                    } else if (v.getMaturityDaysMax() != null) {
                        item.setMaturityDays("Up to " + v.getMaturityDaysMax() + " days");
                    }
                    item.setYieldRange(v.getYieldValue());
                    item.setIrrigationCondition(v.getIrrigationCondition());
                    item.setRecommendedRegions(v.getRecommendedRegions());
                    item.setDroughtTolerance(v.getDroughtTolerance());
                    item.setFallArmyworm(v.getFallArmyworm());
                    item.setStemBorer(v.getStemBorer());
                    item.setTurcicumLeafBlight(v.getTurcicumLeafBlight());
                    item.setDownyMildew(v.getDownyMildew());
                    item.setGrainType(v.getGrainType());
                    item.setSpecialTraits(v.getSpecialTraits());
                    item.setSourceOrganization(v.getSourceOrganization());
                    item.setSourceDocument(v.getSourceDocument());

                    if (item.getAdvantages() == null || item.getAdvantages().isEmpty()) {
                        List<String> advantages = new ArrayList<>();
                        if (v.getSpecialTraits() != null && !v.getSpecialTraits().isBlank()) {
                            advantages.add("✓ " + v.getSpecialTraits());
                        }
                        if (v.getYieldValue() != null) {
                            advantages.add("✓ Potential Yield: " + v.getYieldValue());
                        }
                        if (v.getMaturityDaysMax() != null) {
                            advantages.add("✓ Maturity cycle: " + v.getMaturityDaysMin() + "–" + v.getMaturityDaysMax() + " days");
                        }
                        item.setAdvantages(advantages);
                    }

                    if (item.getTradeOffs() == null || item.getTradeOffs().isEmpty()) {
                        item.setTradeOffs(List.of(
                                "⚠ Yield potential depends on actual field conditions and crop management.",
                                "⚠ Verify local seed availability before purchase."
                        ));
                    }
                });
    }

    private RecommendationResponse buildDeterministicSoybeanResponse(
            MaizeRecommendationRequest request,
            List<SoybeanVariety> candidates,
            int totalCandidates
    ) {
        log.info("Building deterministic soybean recommendation (LLM unavailable) for {} candidates", candidates.size());

        List<ScoredSoybeanVariety> scored = candidates.stream()
                .map(v -> new ScoredSoybeanVariety(v, scoreSoybean(v, request)))
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .limit(topK)
                .collect(Collectors.toList());

        List<RecommendationItem> items = scored.stream()
                .map(sv -> buildItemFromSoybeanVariety(sv.variety, sv.score, request))
                .collect(Collectors.toList());

        RecommendationResponse response = new RecommendationResponse();
        response.setRecommendations(items);
        response.setSummary(buildFallbackSoybeanSummary(request, items.size(), totalCandidates));
        response.setWarnings(List.of(
                "Soybean recommendations generated using metadata matching (AI ranking engine not available).",
                "Consult your local Krishi Vigyan Kendra (KVK) or agricultural extension officer before finalizing seed purchase."
        ));
        response.setCandidatesFound(totalCandidates);
        response.setModelUsed("metadata-scoring");
        response.setGeneratedAt(LocalDateTime.now());
        response.setLlmAvailable(false);
        return response;
    }

    private double scoreSoybean(SoybeanVariety v, MaizeRecommendationRequest request) {
        return metadataFilterService.computeSoybeanAgronomicScore(
                v,
                request.getState(),
                request.getDistrict(),
                request.getPriority() != null ? request.getPriority() : "",
                request.getSoilType() != null ? request.getSoilType() : "",
                request.getWaterAvailability() != null ? request.getWaterAvailability() :
                        (request.getIrrigation() != null ? request.getIrrigation() : ""),
                request.getMaximumMaturityDays(),
                request.getClimateRisks(),
                request.getFarmerDescription() != null ? request.getFarmerDescription() : ""
        );
    }

    private RecommendationItem buildItemFromSoybeanVariety(
            SoybeanVariety v,
            double rawScore,
            MaizeRecommendationRequest request
    ) {
        RecommendationItem item = new RecommendationItem();
        item.setVarietyId(v.getVarietyId());
        item.setVarietyName(v.getVarietyName());
        item.setCategory(v.getCategory());
        item.setSeason(v.getSeason());
        if (v.getMaturityDaysMin() != null && v.getMaturityDaysMax() != null) {
            item.setMaturityDays(v.getMaturityDaysMin() + "–" + v.getMaturityDaysMax() + " days");
        }
        item.setYieldRange(v.getYieldValue());
        item.setIrrigationCondition(v.getIrrigationCondition());
        item.setRecommendedRegions(v.getRecommendedRegions());
        item.setDroughtTolerance(v.getDroughtTolerance());
        item.setFallArmyworm(v.getFallArmyworm());
        item.setStemBorer(v.getStemBorer());
        item.setTurcicumLeafBlight(v.getTurcicumLeafBlight());
        item.setDownyMildew(v.getDownyMildew());
        item.setGrainType(v.getGrainType());
        item.setSpecialTraits(v.getSpecialTraits());
        item.setSourceOrganization(v.getSourceOrganization());
        item.setSourceDocument(v.getSourceDocument());

        // Normalize composite agronomic score to 0.65–0.98 scale
        double normalizedScore = Math.min(0.98, Math.max(0.65, rawScore / 45.0));
        item.setScore(Math.round(normalizedScore * 100.0) / 100.0);
        item.setSuitability(normalizedScore >= 0.82 ? "HIGH" : "MODERATE");

        List<String> matchReasons = new ArrayList<>();
        if (request.getState() != null && metadataFilterService.isStateInSoybeanZone(request.getState(), v.getRecommendedRegions(), v.getAgroClimaticZone())) {
            matchReasons.add("✓ High regional adaptability for " + request.getState() + " (" + (v.getAgroClimaticZone() != null ? v.getAgroClimaticZone() : "ICAR Recommended") + ")");
        }
        if (request.getSeason() != null && metadataFilterService.isSoybeanSeasonMatch(v.getSeason(), request.getSeason())) {
            matchReasons.add("✓ Suitable for " + request.getSeason() + " cultivation (" + (v.getSeason() != null ? v.getSeason() : "Adapted") + ")");
        }
        if (v.getYieldNormalizedQHaMax() != null && v.getYieldNormalizedQHaMax() >= 24.0) {
            matchReasons.add("✓ Excellent yield potential up to " + v.getYieldValue());
        }
        if (request.getSoilType() != null && request.getSoilType().toLowerCase().contains("black")) {
            matchReasons.add("✓ Strong root anchorage and adaptability for Black Cotton Soil");
        }
        if (v.getIrrigationCondition() != null) {
            matchReasons.add("✓ " + v.getIrrigationCondition() + " conditions supported");
        }
        if (v.getDroughtTolerance() != null &&
                (v.getDroughtTolerance().toLowerCase().contains("good") || v.getDroughtTolerance().toLowerCase().contains("resistant"))) {
            matchReasons.add("✓ Documented drought resilience during pod formation");
        }
        if (v.getCharcoalRot() != null && v.getCharcoalRot().toLowerCase().contains("resistant")) {
            matchReasons.add("✓ Charcoal rot (Macrophomina) resistance documented");
        }
        if (v.getSpecialTraits() != null && !v.getSpecialTraits().isBlank()) {
            matchReasons.add("✓ " + v.getSpecialTraits());
        }
        if (v.getSourceOrganization() != null) {
            matchReasons.add("✓ Documented by " + v.getSourceOrganization());
        }
        item.setMatchReasons(matchReasons);
        item.setReason("Ranked using multidimensional ICAR-IISR agronomic compatibility for " +
                (request.getState() != null ? request.getState() : "your region") + ".");

        List<String> advantages = new ArrayList<>();
        if (v.getSpecialTraits() != null && !v.getSpecialTraits().isBlank()) {
            advantages.add("✓ " + v.getSpecialTraits());
        }
        if (v.getYieldValue() != null) {
            advantages.add("✓ Potential Yield: " + v.getYieldValue());
        }
        if (v.getMaturityDaysMax() != null) {
            advantages.add("✓ Maturity cycle: " + v.getMaturityDaysMin() + "–" + v.getMaturityDaysMax() + " days");
        }
        item.setAdvantages(advantages);

        item.setWarnings(List.of(
                "Perform germination test (≥70%) before sowing.",
                "Seed treatment with Rhizobium + PSB biofertilizers is recommended for optimal nodulation."
        ));

        return item;
    }

    private RecommendationResponse buildEmptySoybeanResponse(MaizeRecommendationRequest request, int candidates) {
        RecommendationResponse response = new RecommendationResponse();
        response.setRecommendations(new ArrayList<>());
        response.setSummary("No soybean varieties matched the specified conditions.");
        response.setWarnings(List.of(
                "No soybean varieties found for the specified state, season, and conditions.",
                "Try relaxing the maturity constraint or season filter.",
                "Consult your local agricultural extension office or KVK for specific recommendations."
        ));
        response.setCandidatesFound(0);
        response.setGeneratedAt(LocalDateTime.now());
        response.setLlmAvailable(false);
        return response;
    }

    private String buildFallbackSoybeanSummary(MaizeRecommendationRequest request, int recommended, int total) {
        StringBuilder sb = new StringBuilder();
        sb.append(recommended).append(" soybean ");
        sb.append(recommended == 1 ? "variety was" : "varieties were");
        sb.append(" identified from ").append(total).append(" candidates");
        if (request.getState() != null) sb.append(" for ").append(request.getState());
        if (request.getSeason() != null) sb.append(", ").append(request.getSeason()).append(" season");
        sb.append(". Selection is based on metadata matching against ICAR/ICAR-IISR agricultural research data.");
        return sb.toString();
    }

    private static class ScoredSoybeanVariety {
        final SoybeanVariety variety;
        final double score;
        ScoredSoybeanVariety(SoybeanVariety variety, double score) {
            this.variety = variety;
            this.score = score;
        }
    }

    // ================================================================
    // COTTON RECOMMENDATION PIPELINE
    // ================================================================

    public RecommendationResponse recommendCotton(MaizeRecommendationRequest request) {

        log.info("Starting cotton recommendation for state={}, season={}, irrigation={}",
                request.getState(), request.getSeason(), request.getIrrigation());

        // STEP 1: Metadata Filtering
        List<CottonVariety> candidates = metadataFilterService.filterCotton(
                request.getState(),
                request.getSeason(),
                request.getIrrigation(),
                request.getMaximumMaturityDays()
        );

        log.info("Cotton metadata filter: 265 varieties → {} candidates", candidates.size());

        if (candidates.isEmpty()) {
            return buildEmptyCottonResponse(request, 0);
        }

        // Pre-rank candidates using multidimensional agronomic scoring
        List<CottonVariety> rankedCandidates = metadataFilterService.searchAndRankCotton(request, candidates);

        // Limit candidates for context window (top 10 varieties)
        List<CottonVariety> topCandidates = rankedCandidates.stream()
                .limit(10)
                .collect(Collectors.toList());

        // STEP 2: Check LLM availability
        boolean llmAvailable = llmService.isAvailable();
        log.info("LLM available for cotton: {}", llmAvailable);

        if (llmAvailable) {
            // STEP 3: Build RAG prompt
            String prompt = buildCottonRagPrompt(request, topCandidates);

            // STEP 4: Call LLM
            String llmResponse = llmService.generate(prompt);

            if (llmResponse != null) {
                // STEP 5: Parse LLM response
                RecommendationResponse response = parseCottonLlmResponse(
                        llmResponse,
                        topCandidates,
                        request,
                        candidates.size()
                );

                if (response != null && !response.getRecommendations().isEmpty()) {
                    return response;
                }
            }
            log.warn("LLM response parsing failed for cotton — falling back to deterministic scoring");
        }

        // STEP 6: Deterministic Fallback
        return buildDeterministicCottonResponse(request, topCandidates, candidates.size());
    }

    private String buildCottonRagPrompt(MaizeRecommendationRequest request, List<CottonVariety> candidates) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are an agricultural expert assistant helping Indian farmers select the best cotton variety.\n\n");

        prompt.append("=== FARMER REQUIREMENTS ===\n");
        if (request.getState() != null) prompt.append("State: ").append(request.getState()).append("\n");
        if (request.getDistrict() != null) prompt.append("District: ").append(request.getDistrict()).append("\n");
        if (request.getSeason() != null) prompt.append("Growing season: ").append(request.getSeason()).append("\n");
        if (request.getIrrigation() != null) prompt.append("Irrigation: ").append(request.getIrrigation()).append("\n");
        if (request.getSoilType() != null) prompt.append("Soil type: ").append(request.getSoilType()).append("\n");
        if (request.getMaximumMaturityDays() > 0) {
            prompt.append("Maximum maturity: ").append(request.getMaximumMaturityDays()).append(" days\n");
        }
        if (request.getClimateRisks() != null && !request.getClimateRisks().isEmpty()) {
            prompt.append("Climate risks: ").append(String.join(", ", request.getClimateRisks())).append("\n");
        }
        if (request.getPriority() != null) prompt.append("Priority: ").append(request.getPriority()).append("\n");
        if (request.getFarmerDescription() != null && !request.getFarmerDescription().isBlank()) {
            prompt.append("Farmer notes: ").append(request.getFarmerDescription()).append("\n");
        }

        prompt.append("\n=== RETRIEVED AGRICULTURAL KNOWLEDGE ===\n");
        prompt.append("The following cotton varieties have been retrieved from the ICAR/ICAR-CICR agricultural database\n");
        prompt.append("based on the farmer's location and conditions. Use ONLY this information.\n\n");

        for (int i = 0; i < candidates.size(); i++) {
            CottonVariety v = candidates.get(i);
            prompt.append("--- Variety ").append(i + 1).append(": ").append(v.getVarietyName()).append(" ---\n");
            prompt.append("ID: ").append(v.getVarietyId()).append("\n");
            if (v.getCategory() != null) prompt.append("Category: ").append(v.getCategory()).append("\n");
            if (v.getSeason() != null) prompt.append("Season: ").append(v.getSeason()).append("\n");
            if (v.getMaturityDaysMin() != null && v.getMaturityDaysMax() != null) {
                prompt.append("Maturity: ").append(v.getMaturityDaysMin()).append("-").append(v.getMaturityDaysMax()).append(" days\n");
            }
            if (v.getYieldValue() != null) prompt.append("Yield: ").append(v.getYieldValue()).append("\n");
            if (v.getRecommendedRegions() != null) prompt.append("Recommended regions: ").append(v.getRecommendedRegions()).append("\n");
            if (v.getAgroClimaticZone() != null) prompt.append("Agro-climatic zone: ").append(v.getAgroClimaticZone()).append("\n");
            if (v.getIrrigationCondition() != null) prompt.append("Irrigation: ").append(v.getIrrigationCondition()).append("\n");
            if (v.getDroughtTolerance() != null) prompt.append("Drought tolerance: ").append(v.getDroughtTolerance()).append("\n");
            if (v.getWaterloggingTolerance() != null) prompt.append("Waterlogging tolerance: ").append(v.getWaterloggingTolerance()).append("\n");
            if (v.getLodgingResistance() != null) prompt.append("Lodging resistance: ").append(v.getLodgingResistance()).append("\n");
            if (v.getPinkStemBorer() != null) prompt.append("Pink bollworm resistance: ").append(v.getPinkStemBorer()).append("\n");
            if (v.getGrainType() != null) prompt.append("Staple length / fiber: ").append(v.getGrainType()).append("\n");
            if (v.getSpecialTraits() != null) prompt.append("Special traits: ").append(v.getSpecialTraits()).append("\n");
            if (v.getSourceOrganization() != null) prompt.append("Source: ").append(v.getSourceOrganization()).append("\n");
            prompt.append("\n");
        }

        prompt.append("=== INSTRUCTIONS ===\n");
        prompt.append("1. Select the TOP ").append(Math.min(topK, candidates.size())).append(" cotton varieties most suitable for this farmer.\n");
        prompt.append("2. Use ONLY the information provided above. Do NOT invent yield values, fiber characteristics, or resistance ratings.\n");
        prompt.append("3. If information is missing for a field, state 'Not documented' rather than guessing.\n");
        prompt.append("4. Explain WHY each variety was selected based on the farmer's specific conditions (zone, irrigation, soil, pest resistance).\n");
        prompt.append("5. Highlight staple length, ginning traits, and bollworm/sucking pest resilience where appropriate.\n");
        prompt.append("6. Return a valid JSON response in EXACTLY this format:\n\n");

        prompt.append("```json\n");
        prompt.append("{\n");
        prompt.append("  \"recommendations\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"varietyId\": \"COT_VER_001\",\n");
        prompt.append("      \"varietyName\": \"Variety Name\",\n");
        prompt.append("      \"score\": 0.94,\n");
        prompt.append("      \"suitability\": \"HIGH\",\n");
        prompt.append("      \"reason\": \"Agronomic explanation citing zone, yield, and trait suitability...\",\n");
        prompt.append("      \"matchReasons\": [\"Matches Kharif season\", \"Recommended for Southern Zone\", \"Bt bollworm tolerance\"],\n");
        prompt.append("      \"warnings\": [\"Monitor for sucking pests during vegetative stage\"]\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"summary\": \"Overall cotton recommendation summary...\",\n");
        prompt.append("  \"warnings\": [\"General crop management alerts\"]\n");
        prompt.append("}\n");
        prompt.append("```\n");
        prompt.append("\nRespond with ONLY the JSON. No additional text before or after the JSON.\n");

        return prompt.toString();
    }

    private RecommendationResponse parseCottonLlmResponse(
            String llmResponse,
            List<CottonVariety> candidates,
            MaizeRecommendationRequest request,
            int totalCandidates
    ) {
        try {
            String json = extractJson(llmResponse);
            if (json == null) {
                log.warn("Could not extract JSON from cotton LLM response");
                return null;
            }

            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            RecommendationResponse response = new RecommendationResponse();
            List<RecommendationItem> items = new ArrayList<>();

            if (root.has("recommendations")) {
                JsonArray recs = root.getAsJsonArray("recommendations");

                for (int i = 0; i < recs.size(); i++) {
                    JsonObject rec = recs.get(i).getAsJsonObject();
                    RecommendationItem item = new RecommendationItem();

                    String varietyId = safeGetString(rec, "varietyId");
                    item.setVarietyId(varietyId);
                    item.setVarietyName(safeGetString(rec, "varietyName"));
                    item.setScore(rec.has("score") ? rec.get("score").getAsDouble() : 0.85);
                    item.setSuitability(safeGetString(rec, "suitability"));
                    item.setReason(safeGetString(rec, "reason"));

                    List<String> matchReasons = new ArrayList<>();
                    if (rec.has("matchReasons")) {
                        rec.getAsJsonArray("matchReasons")
                                .forEach(r -> matchReasons.add(r.getAsString()));
                    }
                    item.setMatchReasons(matchReasons);

                    List<String> itemWarnings = new ArrayList<>();
                    if (rec.has("warnings")) {
                        rec.getAsJsonArray("warnings")
                                .forEach(w -> itemWarnings.add(w.getAsString()));
                    }
                    item.setWarnings(itemWarnings);

                    enrichCottonFromDatabase(item, varietyId, candidates);
                    items.add(item);
                }
            }

            response.setRecommendations(items);

            if (root.has("summary")) {
                response.setSummary(root.get("summary").getAsString());
            }

            List<String> globalWarnings = new ArrayList<>();
            if (root.has("warnings")) {
                root.getAsJsonArray("warnings").forEach(w -> globalWarnings.add(w.getAsString()));
            }
            globalWarnings.add("Recommendations are based on documented ICAR/ICAR-CICR agricultural research. Consult your local agricultural extension officer or CICR station before finalizing seed selection.");
            response.setWarnings(globalWarnings);

            response.setCandidatesFound(totalCandidates);
            response.setModelUsed(llmService.getActiveModel());
            response.setGeneratedAt(LocalDateTime.now());

            return response;

        } catch (Exception e) {
            log.error("Failed to parse cotton LLM JSON response: {}", e.getMessage());
            return null;
        }
    }

    private void enrichCottonFromDatabase(
            RecommendationItem item,
            String varietyId,
            List<CottonVariety> candidates
    ) {
        candidates.stream()
                .filter(v -> v.getVarietyId() != null && v.getVarietyId().equals(varietyId))
                .findFirst()
                .ifPresent(v -> {
                    item.setCategory(v.getCategory());
                    item.setSeason(v.getSeason());
                    if (v.getMaturityDaysMin() != null && v.getMaturityDaysMax() != null) {
                        item.setMaturityDays(v.getMaturityDaysMin() + "–" + v.getMaturityDaysMax() + " days");
                    } else if (v.getMaturityDaysMax() != null) {
                        item.setMaturityDays("Up to " + v.getMaturityDaysMax() + " days");
                    }
                    item.setYieldRange(v.getYieldValue());
                    item.setIrrigationCondition(v.getIrrigationCondition());
                    item.setRecommendedRegions(v.getRecommendedRegions());
                    item.setDroughtTolerance(v.getDroughtTolerance());
                    item.setFallArmyworm(v.getFallArmyworm());
                    item.setStemBorer(v.getStemBorer());
                    item.setTurcicumLeafBlight(v.getTurcicumLeafBlight());
                    item.setDownyMildew(v.getDownyMildew());
                    item.setGrainType(v.getGrainType());
                    item.setSpecialTraits(v.getSpecialTraits());
                    item.setSourceOrganization(v.getSourceOrganization());
                    item.setSourceDocument(v.getSourceDocument());

                    if (item.getAdvantages() == null || item.getAdvantages().isEmpty()) {
                        List<String> advantages = new ArrayList<>();
                        if (v.getSpecialTraits() != null && !v.getSpecialTraits().isBlank()) {
                            advantages.add("✓ " + v.getSpecialTraits());
                        }
                        if (v.getYieldValue() != null) {
                            advantages.add("✓ Potential Yield: " + v.getYieldValue());
                        }
                        if (v.getGrainType() != null && !v.getGrainType().isBlank()) {
                            advantages.add("✓ Fiber/Staple: " + v.getGrainType());
                        }
                        if (v.getMaturityDaysMax() != null) {
                            advantages.add("✓ Maturity duration: " + v.getMaturityDaysMin() + "–" + v.getMaturityDaysMax() + " days");
                        }
                        item.setAdvantages(advantages);
                    }

                    if (item.getTradeOffs() == null || item.getTradeOffs().isEmpty()) {
                        item.setTradeOffs(List.of(
                                "⚠ Ensure optimal plant spacing and monitor for sucking pests.",
                                "⚠ Verify certified seed source to prevent spurious Bt seed."
                        ));
                    }
                });
    }

    private RecommendationResponse buildDeterministicCottonResponse(
            MaizeRecommendationRequest request,
            List<CottonVariety> candidates,
            int totalCandidates
    ) {
        log.info("Building deterministic cotton recommendation (LLM unavailable) for {} candidates", candidates.size());

        List<ScoredCottonVariety> scored = candidates.stream()
                .map(v -> new ScoredCottonVariety(v, scoreCotton(v, request)))
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .limit(topK)
                .collect(Collectors.toList());

        List<RecommendationItem> items = scored.stream()
                .map(cv -> buildItemFromCottonVariety(cv.variety, cv.score, request))
                .collect(Collectors.toList());

        RecommendationResponse response = new RecommendationResponse();
        response.setRecommendations(items);
        response.setSummary(buildFallbackCottonSummary(request, items.size(), totalCandidates));
        response.setWarnings(List.of(
                "Cotton recommendations generated using metadata matching (AI ranking engine not available).",
                "Recommendations are based on ICAR/ICAR-CICR published trials.",
                "Consult your nearest Krishi Vigyan Kendra (KVK) or CICR extension centre before purchase."
        ));
        response.setCandidatesFound(totalCandidates);
        response.setModelUsed("deterministic-scoring");
        response.setGeneratedAt(LocalDateTime.now());
        response.setLlmAvailable(false);

        return response;
    }

    private double scoreCotton(CottonVariety v, MaizeRecommendationRequest request) {
        return metadataFilterService.computeCottonAgronomicScore(
                v,
                request.getState(),
                request.getDistrict(),
                request.getPriority() != null ? request.getPriority() : "",
                request.getSoilType() != null ? request.getSoilType() : "",
                request.getWaterAvailability() != null ? request.getWaterAvailability() :
                        (request.getIrrigation() != null ? request.getIrrigation() : ""),
                request.getMaximumMaturityDays(),
                request.getClimateRisks(),
                request.getFarmerDescription() != null ? request.getFarmerDescription() : ""
        );
    }

    private RecommendationItem buildItemFromCottonVariety(
            CottonVariety v,
            double rawScore,
            MaizeRecommendationRequest request
    ) {
        RecommendationItem item = new RecommendationItem();
        item.setVarietyId(v.getVarietyId());
        item.setVarietyName(v.getVarietyName());
        item.setCategory(v.getCategory());
        item.setSeason(v.getSeason());
        if (v.getMaturityDaysMin() != null && v.getMaturityDaysMax() != null) {
            item.setMaturityDays(v.getMaturityDaysMin() + "–" + v.getMaturityDaysMax() + " days");
        }
        item.setYieldRange(v.getYieldValue());
        item.setIrrigationCondition(v.getIrrigationCondition());
        item.setRecommendedRegions(v.getRecommendedRegions());
        item.setDroughtTolerance(v.getDroughtTolerance());
        item.setFallArmyworm(v.getFallArmyworm());
        item.setStemBorer(v.getStemBorer());
        item.setTurcicumLeafBlight(v.getTurcicumLeafBlight());
        item.setDownyMildew(v.getDownyMildew());
        item.setGrainType(v.getGrainType());
        item.setSpecialTraits(v.getSpecialTraits());
        item.setSourceOrganization(v.getSourceOrganization());
        item.setSourceDocument(v.getSourceDocument());

        // Normalize composite score to 0.65–0.98 scale
        double normalizedScore = Math.min(0.98, Math.max(0.65, rawScore / 45.0));
        item.setScore(Math.round(normalizedScore * 100.0) / 100.0);
        item.setSuitability(normalizedScore >= 0.82 ? "HIGH" : "MODERATE");

        List<String> matchReasons = new ArrayList<>();
        if (request.getState() != null && metadataFilterService.isStateInCottonZone(request.getState(), v.getRecommendedRegions(), v.getAgroClimaticZone())) {
            matchReasons.add("✓ High regional adaptability for " + request.getState() + " (" + (v.getAgroClimaticZone() != null ? v.getAgroClimaticZone() : "ICAR Recommended") + ")");
        }
        if (request.getSeason() != null && metadataFilterService.isCottonSeasonMatch(v.getSeason(), request.getSeason())) {
            matchReasons.add("✓ Suitable for " + request.getSeason() + " cultivation (" + (v.getSeason() != null ? v.getSeason() : "Adapted") + ")");
        }
        if (v.getYieldNormalizedQHaMax() != null && v.getYieldNormalizedQHaMax() >= 18.0) {
            matchReasons.add("✓ High yield potential up to " + v.getYieldValue());
        }
        if (request.getSoilType() != null && (request.getSoilType().toLowerCase().contains("black") || request.getSoilType().toLowerCase().contains("regur"))) {
            matchReasons.add("✓ Deep taproot and high performance in Black Cotton Soil");
        }
        if (v.getIrrigationCondition() != null) {
            matchReasons.add("✓ " + v.getIrrigationCondition() + " conditions supported");
        }
        if (v.getCategory() != null && v.getCategory().toLowerCase().contains("bt")) {
            matchReasons.add("✓ Bt technology for effective bollworm protection");
        }
        if (v.getGrainType() != null && !v.getGrainType().isBlank()) {
            matchReasons.add("✓ Fiber quality: " + v.getGrainType());
        }
        if (v.getSpecialTraits() != null && !v.getSpecialTraits().isBlank()) {
            matchReasons.add("✓ " + v.getSpecialTraits());
        }
        if (v.getSourceOrganization() != null) {
            matchReasons.add("✓ Documented by " + v.getSourceOrganization());
        }
        item.setMatchReasons(matchReasons);
        item.setReason("Ranked using multidimensional ICAR-CICR agronomic compatibility for " +
                (request.getState() != null ? request.getState() : "your region") + ".");

        List<String> advantages = new ArrayList<>();
        if (v.getSpecialTraits() != null && !v.getSpecialTraits().isBlank()) {
            advantages.add("✓ " + v.getSpecialTraits());
        }
        if (v.getYieldValue() != null) {
            advantages.add("✓ Potential Yield: " + v.getYieldValue());
        }
        if (v.getGrainType() != null && !v.getGrainType().isBlank()) {
            advantages.add("✓ Fiber Quality: " + v.getGrainType());
        }
        if (v.getMaturityDaysMax() != null) {
            advantages.add("✓ Maturity duration: " + v.getMaturityDaysMin() + "–" + v.getMaturityDaysMax() + " days");
        }
        item.setAdvantages(advantages);

        item.setWarnings(List.of(
                "Verify refuge crop planting (non-Bt border rows) to manage bollworm resistance.",
                "Maintain optimal plant geometry (e.g. 90x60 cm or 120x45 cm based on soil type)."
        ));

        return item;
    }

    private RecommendationResponse buildEmptyCottonResponse(MaizeRecommendationRequest request, int candidates) {
        RecommendationResponse response = new RecommendationResponse();
        response.setRecommendations(new ArrayList<>());
        response.setSummary("No cotton varieties matched the specified conditions.");
        response.setWarnings(List.of(
                "No cotton varieties found for the specified state, season, and conditions.",
                "Try relaxing the maturity constraint or season filter.",
                "Consult your nearest CICR research station or local KVK for recommended cotton hybrids."
        ));
        response.setCandidatesFound(0);
        response.setGeneratedAt(LocalDateTime.now());
        response.setLlmAvailable(false);
        return response;
    }

    private String buildFallbackCottonSummary(MaizeRecommendationRequest request, int recommended, int total) {
        StringBuilder sb = new StringBuilder();
        sb.append(recommended).append(" cotton ");
        sb.append(recommended == 1 ? "variety was" : "varieties were");
        sb.append(" identified from ").append(total).append(" candidates");
        if (request.getState() != null) sb.append(" for ").append(request.getState());
        if (request.getSeason() != null) sb.append(", ").append(request.getSeason()).append(" season");
        sb.append(". Selection is based on metadata matching against ICAR/ICAR-CICR agricultural research data.");
        return sb.toString();
    }

    private static class ScoredCottonVariety {
        final CottonVariety variety;
        final double score;
        ScoredCottonVariety(CottonVariety variety, double score) {
            this.variety = variety;
            this.score = score;
        }
    }
}
