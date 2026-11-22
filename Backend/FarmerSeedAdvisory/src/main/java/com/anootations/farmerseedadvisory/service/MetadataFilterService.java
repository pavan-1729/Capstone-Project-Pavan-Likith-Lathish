package com.anootations.farmerseedadvisory.service;

import com.anootations.farmerseedadvisory.entity.CottonVariety;
import com.anootations.farmerseedadvisory.entity.MaizeVariety;
import com.anootations.farmerseedadvisory.entity.SoybeanVariety;
import com.anootations.farmerseedadvisory.repository.CottonVarietyRepository;
import com.anootations.farmerseedadvisory.repository.MaizeVarietyRepository;
import com.anootations.farmerseedadvisory.repository.SoybeanVarietyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Java port of the Python metadata_filter.py for Maize and Soybean.
 *
 * Applies deterministic, structured filtering to narrow
 * the variety dataset down to a relevant candidate set
 * before semantic/LLM ranking.
 *
 * Filter logic preserved exactly from the Python implementation:
 *
 * HARD FILTERS (remove if no match):
 *   - State / recommended region / zone
 *   - Season
 *   - Maturity days (max constraint)
 *
 * SOFT FILTER (retain UNKNOWN, remove explicit NO_MATCH):
 *   - Irrigation condition
 */
@Service
public class MetadataFilterService {

    private static final Logger log = LoggerFactory.getLogger(MetadataFilterService.class);

    private final MaizeVarietyRepository maizeVarietyRepository;
    private final SoybeanVarietyRepository soybeanVarietyRepository;
    private final CottonVarietyRepository cottonVarietyRepository;

    public MetadataFilterService(
            MaizeVarietyRepository maizeVarietyRepository,
            SoybeanVarietyRepository soybeanVarietyRepository,
            CottonVarietyRepository cottonVarietyRepository
    ) {
        this.maizeVarietyRepository = maizeVarietyRepository;
        this.soybeanVarietyRepository = soybeanVarietyRepository;
        this.cottonVarietyRepository = cottonVarietyRepository;
    }

    // ----------------------------------------------------------------
    // Public API
    // ----------------------------------------------------------------

    /**
     * Apply all metadata filters and return candidate maize varieties.
     *
     * @param state            Farmer's state (e.g., "Andhra Pradesh")
     * @param season           Growing season (e.g., "Kharif", "Rabi")
     * @param irrigation       Irrigation condition ("Rainfed" or "Irrigated")
     * @param maxMaturityDays  Maximum acceptable maturity in days (0 = no constraint)
     * @return Filtered list of candidate varieties
     */
    public List<MaizeVariety> filter(
            String state,
            String season,
            String irrigation,
            int maxMaturityDays
    ) {
        List<MaizeVariety> all = maizeVarietyRepository.findAll();

        return all.stream()
                .filter(v -> regionMatches(v.getRecommendedRegions(), state))
                .filter(v -> seasonMatches(v.getSeason(), season))
                .filter(v -> irrigationDoesNotConflict(v.getIrrigationCondition(), irrigation))
                .filter(v -> maturityMatches(v.getMaturityDaysMax(), maxMaturityDays))
                .collect(Collectors.toList());
    }

    /**
     * Apply all metadata filters and return candidate soybean varieties.
     * Uses multi-pass relaxation so valid candidates are always identified.
     *
     * @param state            Farmer's state (e.g., "Madhya Pradesh", "Maharashtra")
     * @param season           Growing season (e.g., "Kharif", "Summer")
     * @param irrigation       Irrigation condition ("Rainfed" or "Irrigated")
     * @param maxMaturityDays  Maximum acceptable maturity in days (0 = no constraint)
     * @return Filtered list of candidate varieties
     */
    public List<SoybeanVariety> filterSoybean(
            String state,
            String season,
            String irrigation,
            int maxMaturityDays
    ) {
        List<SoybeanVariety> all = soybeanVarietyRepository.findAll();
        if (all == null || all.isEmpty()) {
            return Collections.emptyList();
        }

        // Pass 1: Full agricultural constraints (Zone + Season + Irrigation + Maturity)
        List<SoybeanVariety> candidates = all.stream()
                .filter(v -> isStateInSoybeanZone(state, v.getRecommendedRegions(), v.getAgroClimaticZone()))
                .filter(v -> isSoybeanSeasonMatch(v.getSeason(), season))
                .filter(v -> irrigationDoesNotConflict(v.getIrrigationCondition(), irrigation))
                .filter(v -> isSoybeanMaturityMatch(v.getMaturityDaysMax(), maxMaturityDays))
                .collect(Collectors.toList());

        // Pass 2: Relax maturity constraint if empty
        if (candidates.isEmpty()) {
            log.info("Soybean Pass 1 empty. Relaxing maturity constraint for state={}, season={}", state, season);
            candidates = all.stream()
                    .filter(v -> isStateInSoybeanZone(state, v.getRecommendedRegions(), v.getAgroClimaticZone()))
                    .filter(v -> isSoybeanSeasonMatch(v.getSeason(), season))
                    .filter(v -> irrigationDoesNotConflict(v.getIrrigationCondition(), irrigation))
                    .collect(Collectors.toList());
        }

        // Pass 3: Relax season constraint if still empty
        if (candidates.isEmpty()) {
            log.info("Soybean Pass 2 empty. Relaxing season constraint for state={}", state);
            candidates = all.stream()
                    .filter(v -> isStateInSoybeanZone(state, v.getRecommendedRegions(), v.getAgroClimaticZone()))
                    .filter(v -> irrigationDoesNotConflict(v.getIrrigationCondition(), irrigation))
                    .collect(Collectors.toList());
        }

        // Pass 4: Fallback to all varieties if state is unmapped or zero match
        if (candidates.isEmpty()) {
            log.warn("Soybean Pass 3 empty. Falling back to all varieties for state={}", state);
            candidates = all.stream()
                    .limit(30)
                    .collect(Collectors.toList());
        }

        log.info("Soybean filter returning {} candidate varieties", candidates.size());
        return candidates;
    }

    /**
     * Apply all metadata filters and return candidate cotton varieties.
     * Uses multi-pass relaxation so valid candidates are always identified.
     *
     * @param state            Farmer's state (e.g., "Maharashtra", "Gujarat", "Telangana")
     * @param season           Growing season (e.g., "Kharif")
     * @param irrigation       Irrigation condition ("Rainfed", "Irrigated", or "Protective Irrigated")
     * @param maxMaturityDays  Maximum acceptable maturity in days (0 = no constraint)
     * @return Filtered list of candidate varieties
     */
    public List<CottonVariety> filterCotton(
            String state,
            String season,
            String irrigation,
            int maxMaturityDays
    ) {
        List<CottonVariety> all = cottonVarietyRepository.findAll();
        if (all == null || all.isEmpty()) {
            return Collections.emptyList();
        }

        // Pass 1: Full agricultural constraints (Zone + Season + Irrigation + Maturity)
        List<CottonVariety> candidates = all.stream()
                .filter(v -> isStateInCottonZone(state, v.getRecommendedRegions(), v.getAgroClimaticZone()))
                .filter(v -> isCottonSeasonMatch(v.getSeason(), season))
                .filter(v -> irrigationDoesNotConflict(v.getIrrigationCondition(), irrigation))
                .filter(v -> isCottonMaturityMatch(v.getMaturityDaysMax(), maxMaturityDays))
                .collect(Collectors.toList());

        // Pass 2: Relax maturity constraint if empty
        if (candidates.isEmpty()) {
            log.info("Cotton Pass 1 empty. Relaxing maturity constraint for state={}, season={}", state, season);
            candidates = all.stream()
                    .filter(v -> isStateInCottonZone(state, v.getRecommendedRegions(), v.getAgroClimaticZone()))
                    .filter(v -> isCottonSeasonMatch(v.getSeason(), season))
                    .filter(v -> irrigationDoesNotConflict(v.getIrrigationCondition(), irrigation))
                    .collect(Collectors.toList());
        }

        // Pass 3: Relax season constraint if still empty
        if (candidates.isEmpty()) {
            log.info("Cotton Pass 2 empty. Relaxing season constraint for state={}", state);
            candidates = all.stream()
                    .filter(v -> isStateInCottonZone(state, v.getRecommendedRegions(), v.getAgroClimaticZone()))
                    .filter(v -> irrigationDoesNotConflict(v.getIrrigationCondition(), irrigation))
                    .collect(Collectors.toList());
        }

        // Pass 4: Fallback to all varieties if state is unmapped or zero match
        if (candidates.isEmpty()) {
            log.warn("Cotton Pass 3 empty. Falling back to all varieties for state={}", state);
            candidates = all.stream()
                    .limit(30)
                    .collect(Collectors.toList());
        }

        log.info("Cotton filter returning {} candidate varieties", candidates.size());
        return candidates;
    }

    /**
     * Maps Indian states to ICAR-IISR agro-climatic zones and abbreviations:
     * - Central Zone (CZ): MP, MH, RJ, Gujarat, Chhattisgarh
     * - Southern Zone (SZ): Karnataka, Andhra Pradesh, Telangana, Tamil Nadu, Kerala
     * - Northern Plain Zone (NPZ): Punjab, Haryana, UP, Uttarakhand, Delhi
     * - North Eastern Zone (NEZ): Bihar, Jharkhand, West Bengal, Assam, Odisha
     */
    public boolean isStateInSoybeanZone(String requestedState, String recommendedRegions, String agroClimaticZone) {
        if (requestedState == null || requestedState.isBlank()) {
            return true;
        }

        String regions = (recommendedRegions != null) ? recommendedRegions.toLowerCase() : "";
        String zone = (agroClimaticZone != null) ? agroClimaticZone.toLowerCase() : "";
        String combined = regions + " " + zone;

        if (combined.contains("all india")) {
            return true;
        }

        String s = requestedState.toLowerCase().trim();
        if (combined.contains(s)) {
            return true;
        }

        switch (s) {
            case "madhya pradesh":
            case "mp":
                return combined.contains("cz") || combined.contains("central") || combined.contains("mp");
            case "maharashtra":
            case "mh":
                return combined.contains("cz") || combined.contains("central") || combined.contains("mh") || combined.contains("maha");
            case "rajasthan":
            case "rj":
                return combined.contains("cz") || combined.contains("central") || combined.contains("rj") || combined.contains("raj");
            case "gujarat":
            case "gj":
                return combined.contains("cz") || combined.contains("central") || combined.contains("gj");
            case "chhattisgarh":
            case "cg":
                return combined.contains("cz") || combined.contains("central") || combined.contains("nez") || combined.contains("cg");

            case "karnataka":
            case "ka":
                return combined.contains("sz") || combined.contains("southern") || combined.contains("ka") || combined.contains("kar");
            case "andhra pradesh":
            case "ap":
                return combined.contains("sz") || combined.contains("southern") || combined.contains("ap");
            case "telangana":
            case "ts":
            case "tg":
                return combined.contains("sz") || combined.contains("southern") || combined.contains("ap") || combined.contains("ts") || combined.contains("tg");
            case "tamil nadu":
            case "tn":
                return combined.contains("sz") || combined.contains("southern") || combined.contains("tn");
            case "kerala":
                return combined.contains("sz") || combined.contains("southern");

            case "punjab":
            case "pb":
                return combined.contains("npz") || combined.contains("northern plain") || combined.contains("pb");
            case "haryana":
            case "hr":
                return combined.contains("npz") || combined.contains("northern plain") || combined.contains("hr");
            case "uttar pradesh":
            case "up":
                return combined.contains("npz") || combined.contains("northern plain") || combined.contains("up") || combined.contains("central");
            case "uttarakhand":
            case "uk":
                return combined.contains("npz") || combined.contains("northern plain") || combined.contains("uk");
            case "delhi":
                return combined.contains("npz") || combined.contains("northern plain");

            case "bihar":
            case "br":
                return combined.contains("nez") || combined.contains("north eastern") || combined.contains("bihar");
            case "jharkhand":
            case "jh":
                return combined.contains("nez") || combined.contains("north eastern") || combined.contains("jh");
            case "west bengal":
            case "wb":
                return combined.contains("nez") || combined.contains("north eastern") || combined.contains("wb");
            case "assam":
            case "as":
                return combined.contains("nez") || combined.contains("north eastern") || combined.contains("assam");
            case "odisha":
            case "orissa":
            case "od":
                return combined.contains("nez") || combined.contains("north eastern") || combined.contains("od");

            default:
                return false;
        }
    }

    public boolean isSoybeanSeasonMatch(String varietySeason, String requestedSeason) {
        if (requestedSeason == null || requestedSeason.isBlank()) {
            return true;
        }
        if (varietySeason == null || varietySeason.isBlank()) {
            return true;
        }
        String varSeason = varietySeason.toLowerCase();
        String reqSeason = requestedSeason.toLowerCase().trim();

        if (reqSeason.contains("kharif")) {
            return varSeason.contains("kharif");
        }
        if (reqSeason.contains("summer") || reqSeason.contains("spring") || reqSeason.contains("zaid")) {
            return varSeason.contains("summer") || varSeason.contains("spring");
        }
        if (reqSeason.contains("rabi")) {
            // Soybean in India is predominantly Kharif/Summer; do not block if user chose Rabi
            return true;
        }
        return varSeason.contains(reqSeason);
    }

    public boolean isSoybeanMaturityMatch(Integer varietyMaxDays, int requestedMaxDays) {
        if (requestedMaxDays <= 0) {
            return true;
        }
        if (varietyMaxDays == null) {
            return true;
        }
        // Allow up to 10 days tolerance so JS 335 (113 days) or similar top varieties aren't excluded
        return varietyMaxDays <= (requestedMaxDays + 10);
    }

    /**
     * Maps Indian states to ICAR-CICR cotton agro-climatic zones and abbreviations:
     * - Northern Zone (NZ): Punjab, Haryana, Rajasthan
     * - Central Zone (CZ): Gujarat, Maharashtra, Madhya Pradesh, Odisha
     * - Southern Zone (SZ): Andhra Pradesh, Telangana, Tamil Nadu, Karnataka
     */
    public boolean isStateInCottonZone(String requestedState, String recommendedRegions, String agroClimaticZone) {
        if (requestedState == null || requestedState.isBlank()) {
            return true;
        }

        String regions = (recommendedRegions != null) ? recommendedRegions.toLowerCase() : "";
        String zone = (agroClimaticZone != null) ? agroClimaticZone.toLowerCase() : "";
        String combined = regions + " " + zone;

        if (combined.contains("all india")) {
            return true;
        }

        String s = requestedState.toLowerCase().trim();
        if (combined.contains(s)) {
            return true;
        }

        switch (s) {
            // Northern Zone
            case "punjab":
            case "pb":
                return combined.contains("nz") || combined.contains("northern") || combined.contains("pb");
            case "haryana":
            case "hr":
                return combined.contains("nz") || combined.contains("northern") || combined.contains("hr");
            case "rajasthan":
            case "rj":
                return combined.contains("nz") || combined.contains("northern") || combined.contains("rj") || combined.contains("cz");

            // Central Zone
            case "gujarat":
            case "gj":
                return combined.contains("cz") || combined.contains("central") || combined.contains("gj");
            case "maharashtra":
            case "mh":
                return combined.contains("cz") || combined.contains("central") || combined.contains("mh") || combined.contains("vidarbha") || combined.contains("marathwada");
            case "madhya pradesh":
            case "mp":
                return combined.contains("cz") || combined.contains("central") || combined.contains("mp");
            case "odisha":
            case "orissa":
            case "od":
                return combined.contains("cz") || combined.contains("central") || combined.contains("od");

            // Southern Zone
            case "andhra pradesh":
            case "ap":
                return combined.contains("sz") || combined.contains("southern") || combined.contains("ap");
            case "telangana":
            case "ts":
            case "tg":
                return combined.contains("sz") || combined.contains("southern") || combined.contains("ts") || combined.contains("tg") || combined.contains("ap");
            case "tamil nadu":
            case "tn":
                return combined.contains("sz") || combined.contains("southern") || combined.contains("tn");
            case "karnataka":
            case "ka":
                return combined.contains("sz") || combined.contains("southern") || combined.contains("ka");

            default:
                return false;
        }
    }

    public boolean isCottonSeasonMatch(String varietySeason, String requestedSeason) {
        if (requestedSeason == null || requestedSeason.isBlank()) {
            return true;
        }
        if (varietySeason == null || varietySeason.isBlank()) {
            return true;
        }
        String varSeason = varietySeason.toLowerCase();
        String reqSeason = requestedSeason.toLowerCase().trim();

        if (reqSeason.contains("kharif")) {
            return varSeason.contains("kharif");
        }
        if (reqSeason.contains("summer") || reqSeason.contains("spring") || reqSeason.contains("zaid")) {
            return varSeason.contains("summer") || varSeason.contains("spring") || varSeason.contains("kharif");
        }
        if (reqSeason.contains("rabi")) {
            // Cotton in India is predominantly Kharif/late Kharif
            return true;
        }
        return varSeason.contains(reqSeason);
    }

    public boolean isCottonMaturityMatch(Integer varietyMaxDays, int requestedMaxDays) {
        if (requestedMaxDays <= 0) {
            return true;
        }
        if (varietyMaxDays == null) {
            return true;
        }
        // Allow up to 15 days tolerance for cotton varieties (long duration crop: 140-185 days)
        return varietyMaxDays <= (requestedMaxDays + 15);
    }

    /**
     * Calculates state & district match score:
     * - Tier 1 (10.0 pts): Direct state code / state name / district match in recommended regions
     * - Tier 2 (8.0 pts): Official ICAR agro-climatic zone match (CZ, SZ, NPZ, NEZ)
     * - Tier 3 (6.0 pts): "All India" / national release
     * - Tier 4 (4.0 pts): Contiguous agro-climatic zone match
     * - Tier 5 (1.5 pts): General adaptability
     */
    public double calculateStateRelevanceScore(String requestedState, String requestedDistrict, String recommendedRegions, String agroClimaticZone) {
        if (requestedState == null || requestedState.isBlank()) {
            return 5.0; // neutral
        }

        String reg = (recommendedRegions != null ? recommendedRegions.toLowerCase() : "");
        String zone = (agroClimaticZone != null ? agroClimaticZone.toLowerCase() : "");
        String comb = reg + " " + zone;

        String s = requestedState.trim().toLowerCase();
        String dist = (requestedDistrict != null) ? requestedDistrict.trim().toLowerCase() : "";

        // District match bonus
        if (!dist.isBlank() && (reg.contains(dist) || comb.contains(dist))) {
            return 10.0;
        }

        // Direct state name in text
        if (reg.contains(s)) {
            return 10.0;
        }

        // State abbreviation checks
        switch (s) {
            case "madhya pradesh":
            case "mp":
                if (reg.contains("mp") || reg.contains("indore") || reg.contains("sehor") || reg.contains("malwa")) return 10.0;
                if (comb.contains("cz") || comb.contains("central")) return 8.0;
                if (comb.contains("all india")) return 6.0;
                if (comb.contains("sz") || comb.contains("nez")) return 3.5;
                break;

            case "maharashtra":
            case "mh":
                if (reg.contains("mh") || reg.contains("maha") || reg.contains("vidarbha") || reg.contains("marathwada")) return 10.0;
                if (comb.contains("cz") || comb.contains("central")) return 8.0;
                if (comb.contains("all india")) return 6.0;
                if (comb.contains("sz") || comb.contains("southern")) return 4.5;
                break;

            case "rajasthan":
            case "rj":
                if (reg.contains("rj") || reg.contains("raj") || reg.contains("kota")) return 10.0;
                if (comb.contains("cz") || comb.contains("central")) return 8.0;
                if (comb.contains("all india")) return 6.0;
                if (comb.contains("npz")) return 4.0;
                break;

            case "gujarat":
            case "gj":
                if (reg.contains("gj") || reg.contains("gujarat")) return 10.0;
                if (comb.contains("cz") || comb.contains("central")) return 8.0;
                if (comb.contains("all india")) return 6.0;
                break;

            case "chhattisgarh":
            case "cg":
                if (reg.contains("cg") || reg.contains("chhattisgarh")) return 10.0;
                if (comb.contains("cz") || comb.contains("central") || comb.contains("nez")) return 8.0;
                if (comb.contains("all india")) return 6.0;
                break;

            case "karnataka":
            case "ka":
                if (reg.contains("ka") || reg.contains("kar") || reg.contains("dharwad") || reg.contains("belagavi")) return 10.0;
                if (comb.contains("sz") || comb.contains("southern")) return 8.0;
                if (comb.contains("all india")) return 6.0;
                if (comb.contains("cz") || comb.contains("central")) return 4.0;
                break;

            case "andhra pradesh":
            case "ap":
                if (reg.contains("ap") || reg.contains("andhra")) return 10.0;
                if (comb.contains("sz") || comb.contains("southern")) return 8.0;
                if (comb.contains("all india")) return 6.0;
                break;

            case "telangana":
            case "ts":
            case "tg":
                if (reg.contains("ts") || reg.contains("tg") || reg.contains("ap") || reg.contains("nizamabad")) return 10.0;
                if (comb.contains("sz") || comb.contains("southern")) return 8.0;
                if (comb.contains("all india")) return 6.0;
                if (comb.contains("cz") || comb.contains("central")) return 4.0;
                break;

            case "tamil nadu":
            case "tn":
                if (reg.contains("tn") || reg.contains("tamil")) return 10.0;
                if (comb.contains("sz") || comb.contains("southern")) return 8.0;
                if (comb.contains("all india")) return 6.0;
                break;

            case "kerala":
                if (comb.contains("sz") || comb.contains("southern")) return 8.0;
                if (comb.contains("all india")) return 6.0;
                break;

            case "punjab":
            case "pb":
                if (reg.contains("pb") || reg.contains("punjab")) return 10.0;
                if (comb.contains("npz") || comb.contains("northern plain")) return 8.0;
                if (comb.contains("all india")) return 6.0;
                break;

            case "haryana":
            case "hr":
                if (reg.contains("hr") || reg.contains("haryana")) return 10.0;
                if (comb.contains("npz") || comb.contains("northern plain")) return 8.0;
                if (comb.contains("all india")) return 6.0;
                break;

            case "uttar pradesh":
            case "up":
                if (reg.contains("up") || reg.contains("bundelkhand") || reg.contains("uttar")) return 10.0;
                if (comb.contains("npz") || comb.contains("northern plain")) return 8.0;
                if (comb.contains("nez") || comb.contains("central")) return 5.0;
                if (comb.contains("all india")) return 6.0;
                break;

            case "uttarakhand":
            case "uk":
                if (reg.contains("uk") || reg.contains("pantnagar")) return 10.0;
                if (comb.contains("npz") || comb.contains("northern plain")) return 8.0;
                if (comb.contains("all india")) return 6.0;
                break;

            case "bihar":
            case "br":
                if (reg.contains("br") || reg.contains("bihar")) return 10.0;
                if (comb.contains("nez") || comb.contains("north eastern")) return 8.0;
                if (comb.contains("all india")) return 6.0;
                break;

            case "jharkhand":
            case "jh":
                if (reg.contains("jh") || reg.contains("jharkhand")) return 10.0;
                if (comb.contains("nez") || comb.contains("north eastern")) return 8.0;
                if (comb.contains("all india")) return 6.0;
                break;

            case "west bengal":
            case "wb":
                if (reg.contains("wb") || reg.contains("bengal")) return 10.0;
                if (comb.contains("nez") || comb.contains("north eastern")) return 8.0;
                if (comb.contains("all india")) return 6.0;
                break;

            case "assam":
            case "as":
                if (reg.contains("as") || reg.contains("assam")) return 10.0;
                if (comb.contains("nez") || comb.contains("north eastern")) return 8.0;
                if (comb.contains("all india")) return 6.0;
                break;

            case "odisha":
            case "orissa":
            case "od":
                if (reg.contains("od") || reg.contains("odisha")) return 10.0;
                if (comb.contains("nez") || comb.contains("north eastern")) return 8.0;
                if (comb.contains("all india")) return 6.0;
                break;

            default:
                if (comb.contains(s)) return 9.0;
                if (comb.contains("all india")) return 6.0;
                return 2.0;
        }

        if (comb.contains("all india")) return 6.0;
        return 1.5;
    }

    /**
     * Multidimensional searching & ranking algorithm for soybean varieties.
     * Evaluates state relevance, yield potential, soil type, irrigation regime,
     * maturity alignment, biotic/abiotic stress resilience, and farmer priorities.
     */
    public List<SoybeanVariety> searchAndRankSoybean(com.anootations.farmerseedadvisory.dto.MaizeRecommendationRequest request, List<SoybeanVariety> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }

        String state = request.getState();
        String district = request.getDistrict();
        String priority = request.getPriority() != null ? request.getPriority().toLowerCase() : "";
        String soilType = request.getSoilType() != null ? request.getSoilType().toLowerCase() : "";
        String water = request.getWaterAvailability() != null ? request.getWaterAvailability().toLowerCase() :
                       (request.getIrrigation() != null ? request.getIrrigation().toLowerCase() : "");
        int maxMaturity = request.getMaximumMaturityDays();
        List<String> climateRisks = request.getClimateRisks();
        String query = request.getFarmerDescription() != null ? request.getFarmerDescription().toLowerCase() : "";

        return candidates.stream()
                .sorted((a, b) -> {
                    double scoreB = computeSoybeanAgronomicScore(b, state, district, priority, soilType, water, maxMaturity, climateRisks, query);
                    double scoreA = computeSoybeanAgronomicScore(a, state, district, priority, soilType, water, maxMaturity, climateRisks, query);
                    return Double.compare(scoreB, scoreA);
                })
                .collect(Collectors.toList());
    }

    public double computeSoybeanAgronomicScore(
            SoybeanVariety v,
            String state,
            String district,
            String priority,
            String soilType,
            String water,
            int maxMaturity,
            List<String> climateRisks,
            String query
    ) {
        double score = 0.0;

        // 1. Regional State & Agro-Climatic Match (0–10 pts)
        score += calculateStateRelevanceScore(state, district, v.getRecommendedRegions(), v.getAgroClimaticZone());

        // 2. Yield Potential Score (0–12 pts)
        Double yMax = v.getYieldNormalizedQHaMax();
        if (yMax != null && yMax > 15.0) {
            double yieldScore = (yMax - 15.0) * 0.7; // e.g. 25 q/ha = 7.0 pts, 30 q/ha = 10.5 pts
            if (priority.contains("yield")) {
                yieldScore *= 1.5; // Boosted for high yield priority
            }
            score += yieldScore;
        }

        // 3. Soil Type Suitability (0–5 pts)
        if (!soilType.isBlank()) {
            if (soilType.contains("black")) {
                // Black Cotton Soil (Regur/Vertisols) - dominant for soybean in MP/MH/KA
                if (v.getLodgingResistance() != null && v.getLodgingResistance().toLowerCase().contains("resistant")) score += 3.0;
                if (v.getDroughtTolerance() != null && v.getDroughtTolerance().toLowerCase().contains("resistant")) score += 2.0;
            } else if (soilType.contains("red")) {
                // Red soil dries faster - rewards drought resilience and early maturity
                if (v.getDroughtTolerance() != null && v.getDroughtTolerance().toLowerCase().contains("resistant")) score += 3.5;
                if ("early".equalsIgnoreCase(v.getMaturityClass())) score += 2.0;
            } else if (soilType.contains("alluvial") || soilType.contains("loam")) {
                if (yMax != null && yMax >= 24.0) score += 3.5;
            }
        }

        // 4. Water Regime & Moisture Management (0–6 pts)
        if (!water.isBlank()) {
            if (water.contains("rainfed")) {
                if (v.getDroughtTolerance() != null && v.getDroughtTolerance().toLowerCase().contains("resistant")) score += 4.0;
                if (v.getIrrigationCondition() != null && v.getIrrigationCondition().toLowerCase().contains("rainfed")) score += 2.0;
            } else if (water.contains("waterlog")) {
                if (v.getWaterloggingTolerance() != null && v.getWaterloggingTolerance().toLowerCase().contains("resistant")) score += 5.0;
            } else if (water.contains("irrigat")) {
                if (yMax != null && yMax >= 25.0) score += 3.0;
            }
        }

        // 5. Maturity Proximity & Priority (0–6 pts)
        if (maxMaturity > 0 && v.getMaturityDaysMax() != null) {
            int diff = maxMaturity - v.getMaturityDaysMax();
            if (diff >= 0 && diff <= 15) {
                score += 4.0; // Perfect fit
            } else if (diff > 15) {
                score += 3.0; // Earlier than max
            } else if (diff >= -10) {
                score += 2.0; // Slightly over tolerance
            }
        }
        if (priority.contains("early") || priority.contains("short")) {
            if (v.getMaturityDaysMax() != null && v.getMaturityDaysMax() <= 95) {
                score += 5.0; // Early maturity bonus (e.g. JS 93-05, JS 21-72)
            }
        }

        // 6. Biotic & Climate Stress Tolerance (0–8 pts)
        if (climateRisks != null && !climateRisks.isEmpty()) {
            for (String r : climateRisks) {
                String rl = r.toLowerCase();
                if (rl.contains("drought") && v.getDroughtTolerance() != null && v.getDroughtTolerance().toLowerCase().contains("resistant")) {
                    score += 3.0;
                }
                if (rl.contains("waterlog") && v.getWaterloggingTolerance() != null && v.getWaterloggingTolerance().toLowerCase().contains("resistant")) {
                    score += 3.0;
                }
                if (rl.contains("pest") && (
                        (v.getStemBorer() != null && v.getStemBorer().toLowerCase().contains("resistant")) ||
                        (v.getSpecialTraits() != null && v.getSpecialTraits().toLowerCase().contains("stem fly"))
                )) {
                    score += 3.5;
                }
                if (rl.contains("disease") && (
                        (v.getCharcoalRot() != null && v.getCharcoalRot().toLowerCase().contains("resistant")) ||
                        (v.getSpecialTraits() != null && (v.getSpecialTraits().toLowerCase().contains("ymv") || v.getSpecialTraits().toLowerCase().contains("rust")))
                )) {
                    score += 3.5;
                }
            }
        }
        if (priority.contains("disease")) {
            if (v.getCharcoalRot() != null && v.getCharcoalRot().toLowerCase().contains("resistant")) score += 3.0;
            if (v.getSpecialTraits() != null && (v.getSpecialTraits().toLowerCase().contains("ymv") || v.getSpecialTraits().toLowerCase().contains("rust"))) score += 3.0;
        }

        // 7. Release Year Recency (0–3 pts)
        if (v.getReleaseYear() != null && !v.getReleaseYear().isBlank()) {
            try {
                int year = Integer.parseInt(v.getReleaseYear().replaceAll("[^0-9]", ""));
                if (year >= 2010) {
                    score += Math.min(3.0, (year - 2005) * 0.2);
                }
            } catch (Exception ignored) {
            }
        }

        // 8. Farmer Query / Text Keyword Relevance (0–6 pts)
        if (!query.isBlank()) {
            String combinedText = ((v.getVarietyName() != null ? v.getVarietyName() : "") + " " +
                                  (v.getSpecialTraits() != null ? v.getSpecialTraits() : "") + " " +
                                  (v.getCategory() != null ? v.getCategory() : "") + " " +
                                  (v.getDeveloperOrCompany() != null ? v.getDeveloperOrCompany() : "")).toLowerCase();
            String[] tokens = query.split("\\s+");
            for (String t : tokens) {
                if (t.length() >= 3 && combinedText.contains(t)) {
                    score += 2.0;
                }
            }
        }

        return Math.round(score * 100.0) / 100.0;
    }

    /**
     * Multidimensional searching & ranking algorithm for cotton varieties.
     * Evaluates state relevance, yield potential, soil suitability, irrigation regime,
     * maturity alignment, fiber quality (staple/GOT), biotic/abiotic stress resilience, and farmer priorities.
     */
    public List<CottonVariety> searchAndRankCotton(com.anootations.farmerseedadvisory.dto.MaizeRecommendationRequest request, List<CottonVariety> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }

        String state = request.getState();
        String district = request.getDistrict();
        String priority = request.getPriority() != null ? request.getPriority().toLowerCase() : "";
        String soilType = request.getSoilType() != null ? request.getSoilType().toLowerCase() : "";
        String water = request.getWaterAvailability() != null ? request.getWaterAvailability().toLowerCase() :
                       (request.getIrrigation() != null ? request.getIrrigation().toLowerCase() : "");
        int maxMaturity = request.getMaximumMaturityDays();
        List<String> climateRisks = request.getClimateRisks();
        String query = request.getFarmerDescription() != null ? request.getFarmerDescription().toLowerCase() : "";

        return candidates.stream()
                .sorted((a, b) -> {
                    double scoreB = computeCottonAgronomicScore(b, state, district, priority, soilType, water, maxMaturity, climateRisks, query);
                    double scoreA = computeCottonAgronomicScore(a, state, district, priority, soilType, water, maxMaturity, climateRisks, query);
                    return Double.compare(scoreB, scoreA);
                })
                .collect(Collectors.toList());
    }

    public double computeCottonAgronomicScore(
            CottonVariety v,
            String state,
            String district,
            String priority,
            String soilType,
            String water,
            int maxMaturity,
            List<String> climateRisks,
            String query
    ) {
        double score = 0.0;

        // 1. Regional State & Agro-Climatic Match (0–10 pts)
        score += calculateStateRelevanceScore(state, district, v.getRecommendedRegions(), v.getAgroClimaticZone());

        // 2. Yield Potential Score (0–12 pts)
        Double yMax = v.getYieldNormalizedQHaMax();
        if (yMax != null && yMax > 12.0) {
            double yieldScore = (yMax - 12.0) * 0.65; // e.g. 22 q/ha = 6.5 pts, 28 q/ha = 10.4 pts
            if (priority.contains("yield")) {
                yieldScore *= 1.5;
            }
            score += yieldScore;
        }

        // 3. Soil Type Suitability (0–6 pts)
        if (!soilType.isBlank()) {
            if (soilType.contains("black") || soilType.contains("regur") || soilType.contains("vertisol")) {
                // Black Cotton Soil is the premier medium for cotton in India
                score += 3.0;
                if (v.getLodgingResistance() != null && v.getLodgingResistance().toLowerCase().contains("resistant")) score += 1.5;
                if (v.getDroughtTolerance() != null && v.getDroughtTolerance().toLowerCase().contains("resistant")) score += 1.5;
            } else if (soilType.contains("red")) {
                // Red soil requires drought hardiness and early-to-medium maturity
                if (v.getDroughtTolerance() != null && v.getDroughtTolerance().toLowerCase().contains("resistant")) score += 3.0;
                if (v.getMaturityDaysMax() != null && v.getMaturityDaysMax() <= 165) score += 2.0;
            } else if (soilType.contains("alluvial") || soilType.contains("loam")) {
                // High productivity in Northern zone
                if (yMax != null && yMax >= 20.0) score += 3.0;
            }
        }

        // 4. Water Regime & Moisture Management (0–6 pts)
        if (!water.isBlank()) {
            if (water.contains("rainfed")) {
                if (v.getDroughtTolerance() != null && v.getDroughtTolerance().toLowerCase().contains("resistant")) score += 4.0;
                if (v.getIrrigationCondition() != null && v.getIrrigationCondition().toLowerCase().contains("rainfed")) score += 2.0;
            } else if (water.contains("waterlog")) {
                if (v.getWaterloggingTolerance() != null && v.getWaterloggingTolerance().toLowerCase().contains("resistant")) score += 4.5;
            } else if (water.contains("irrigat")) {
                if (yMax != null && yMax >= 22.0) score += 3.0;
            }
        }

        // 5. Maturity Proximity & Priority (0–6 pts)
        if (maxMaturity > 0 && v.getMaturityDaysMax() != null) {
            int diff = maxMaturity - v.getMaturityDaysMax();
            if (diff >= 0 && diff <= 15) {
                score += 4.0; // Ideal maturity
            } else if (diff > 15) {
                score += 3.0; // Early
            } else if (diff >= -15) {
                score += 2.0; // Within tolerance
            }
        }
        if (priority.contains("early") || priority.contains("short")) {
            if (v.getMaturityDaysMax() != null && v.getMaturityDaysMax() <= 160) {
                score += 5.0; // Early maturing cotton bonus
            }
        }

        // 6. Biotic Pest & Disease Stress Tolerances (0–8 pts)
        if (climateRisks != null && !climateRisks.isEmpty()) {
            for (String r : climateRisks) {
                String rl = r.toLowerCase();
                if (rl.contains("drought") && v.getDroughtTolerance() != null && v.getDroughtTolerance().toLowerCase().contains("resistant")) {
                    score += 3.0;
                }
                if (rl.contains("waterlog") && v.getWaterloggingTolerance() != null && v.getWaterloggingTolerance().toLowerCase().contains("resistant")) {
                    score += 3.0;
                }
                if (rl.contains("pest")) {
                    // Bollworm / sucking pest resistance
                    if (v.getPinkStemBorer() != null && v.getPinkStemBorer().toLowerCase().contains("resistant")) score += 2.5;
                    if (v.getCategory() != null && v.getCategory().toLowerCase().contains("bt")) score += 2.5;
                    if (v.getSpecialTraits() != null && (v.getSpecialTraits().toLowerCase().contains("jassid") || v.getSpecialTraits().toLowerCase().contains("whitefly"))) score += 2.0;
                }
                if (rl.contains("disease")) {
                    // Bacterial blight, CLCuV, Root rot, Wilt
                    if (v.getSpecialTraits() != null && (v.getSpecialTraits().toLowerCase().contains("blight") || v.getSpecialTraits().toLowerCase().contains("clcuv") || v.getSpecialTraits().toLowerCase().contains("wilt"))) {
                        score += 3.5;
                    }
                }
            }
        }
        if (priority.contains("pest") || priority.contains("bollworm")) {
            if (v.getCategory() != null && v.getCategory().toLowerCase().contains("bt")) score += 3.0;
            if (v.getPinkStemBorer() != null && v.getPinkStemBorer().toLowerCase().contains("resistant")) score += 2.0;
        }

        // 7. Fiber Quality / Staple Traits (0–5 pts)
        if (v.getGrainType() != null && !v.getGrainType().isBlank()) {
            String staple = v.getGrainType().toLowerCase();
            if (staple.contains("long") || staple.contains("28") || staple.contains("29") || staple.contains("30")) {
                score += 2.5; // Long staple premium
            }
        }
        if (priority.contains("quality") || priority.contains("staple") || priority.contains("fiber")) {
            score += 3.0;
        }

        // 8. Release Year Recency (0–3 pts)
        if (v.getReleaseYear() != null && !v.getReleaseYear().isBlank()) {
            try {
                int year = Integer.parseInt(v.getReleaseYear().replaceAll("[^0-9]", ""));
                if (year >= 2010) {
                    score += Math.min(3.0, (year - 2005) * 0.2);
                }
            } catch (Exception ignored) {
            }
        }

        // 9. Query Keyword Relevance (0–6 pts)
        if (!query.isBlank()) {
            String combinedText = ((v.getVarietyName() != null ? v.getVarietyName() : "") + " " +
                                  (v.getSpecialTraits() != null ? v.getSpecialTraits() : "") + " " +
                                  (v.getCategory() != null ? v.getCategory() : "") + " " +
                                  (v.getDeveloperOrCompany() != null ? v.getDeveloperOrCompany() : "") + " " +
                                  (v.getGrainType() != null ? v.getGrainType() : "")).toLowerCase();
            String[] tokens = query.split("\\s+");
            for (String t : tokens) {
                if (t.length() >= 3 && combinedText.contains(t)) {
                    score += 2.0;
                }
            }
        }

        return Math.round(score * 100.0) / 100.0;
    }

    // ----------------------------------------------------------------
    // Region matching
    // ----------------------------------------------------------------

    /**
     * Returns true if the state appears in the recommendedRegions text,
     * or if no state filter was specified.
     * "All India" always matches.
     * Missing region data does NOT match.
     */
    private boolean regionMatches(String recommendedRegions, String requestedState) {
        if (requestedState == null || requestedState.isBlank()) {
            return true; // No state filter
        }
        if (recommendedRegions == null || recommendedRegions.isBlank()) {
            return false; // Missing region info — exclude
        }
        String regions = recommendedRegions.toLowerCase();
        String state = requestedState.toLowerCase().trim();
        return regions.contains(state) || regions.contains("all india");
    }

    /**
     * Returns true if state matches recommendedRegions OR agroClimaticZone text,
     * or if no state filter was specified.
     */
    private boolean regionOrZoneMatches(String recommendedRegions, String agroClimaticZone, String requestedState) {
        if (requestedState == null || requestedState.isBlank()) {
            return true;
        }
        String state = requestedState.toLowerCase().trim();
        if (recommendedRegions != null && !recommendedRegions.isBlank()) {
            String regions = recommendedRegions.toLowerCase();
            if (regions.contains(state) || regions.contains("all india")) {
                return true;
            }
        }
        if (agroClimaticZone != null && !agroClimaticZone.isBlank()) {
            String zone = agroClimaticZone.toLowerCase();
            if (zone.contains(state)) {
                return true;
            }
        }
        return false;
    }

    // ----------------------------------------------------------------
    // Season matching
    // ----------------------------------------------------------------

    /**
     * Returns true if the requested season appears in the variety's season field,
     * or if no season filter was specified.
     * Missing season data does NOT match when a season is requested.
     */
    private boolean seasonMatches(String varietySeason, String requestedSeason) {
        if (requestedSeason == null || requestedSeason.isBlank()) {
            return true; // No season filter
        }
        if (varietySeason == null || varietySeason.isBlank()) {
            return false; // Missing season info — exclude
        }
        return varietySeason.toLowerCase().contains(requestedSeason.toLowerCase().trim());
    }

    // ----------------------------------------------------------------
    // Irrigation matching
    // ----------------------------------------------------------------

    /**
     * Irrigation is a SOFT filter — varieties with missing/unknown
     * irrigation data are RETAINED (the LLM can evaluate them).
     * Only explicit contradictions are removed.
     *
     * MATCH     → retain
     * UNKNOWN   → retain (for LLM evaluation)
     * NO_MATCH  → exclude
     */
    private boolean irrigationDoesNotConflict(
            String irrigationCondition,
            String requestedIrrigation
    ) {
        if (requestedIrrigation == null || requestedIrrigation.isBlank()) {
            return true; // No irrigation filter
        }
        if (irrigationCondition == null || irrigationCondition.isBlank()) {
            return true; // Missing data — retain for LLM
        }

        String irrigation = irrigationCondition.toLowerCase().trim();
        String requested = requestedIrrigation.toLowerCase().trim();

        // ---- Rainfed request ----
        if (requested.equals("rainfed") || requested.contains("rain")) {
            if (irrigation.equals("irrigated")) return false; // Explicit contradiction
            return true; // "rainfed", "rainfed and irrigated", "dryland", unknown → retain
        }

        // ---- Irrigated request ----
        if (requested.equals("irrigated")) {
            if (irrigation.equals("rainfed") || irrigation.equals("dryland")) return false;
            return true; // "irrigated", "rainfed and irrigated", unknown → retain
        }

        // For other values — retain if text contains the requested condition
        return irrigation.contains(requested) || true; // Default: retain UNKNOWN
    }

    // ----------------------------------------------------------------
    // Maturity matching
    // ----------------------------------------------------------------

    /**
     * Returns true if the variety's maximum maturity is within the
     * farmer's required duration, or if no constraint was specified.
     * Varieties with missing maturity data are excluded when a constraint exists.
     */
    private boolean maturityMatches(Integer varietyMaxDays, int requestedMaxDays) {
        if (requestedMaxDays <= 0) {
            return true; // No maturity constraint
        }
        if (varietyMaxDays == null) {
            return false; // Missing data when constraint requested — exclude
        }
        return varietyMaxDays <= requestedMaxDays;
    }

    // ----------------------------------------------------------------
    // Convenience method: classify irrigation match type
    // (Used in RAG context building for LLM prompt)
    // ----------------------------------------------------------------

    public enum IrrigationMatchType { MATCH, UNKNOWN, NO_MATCH, NOT_REQUESTED }

    public IrrigationMatchType classifyIrrigationMatch(
            String irrigationCondition,
            String requestedIrrigation
    ) {
        if (requestedIrrigation == null || requestedIrrigation.isBlank()) {
            return IrrigationMatchType.NOT_REQUESTED;
        }
        if (irrigationCondition == null || irrigationCondition.isBlank()) {
            return IrrigationMatchType.UNKNOWN;
        }

        String irrigation = irrigationCondition.toLowerCase().trim();
        String requested = requestedIrrigation.toLowerCase().trim();

        if (requested.contains("rain")) {
            if (irrigation.equals("irrigated")) return IrrigationMatchType.NO_MATCH;
            if (irrigation.contains("rainfed")) return IrrigationMatchType.MATCH;
            if (irrigation.equals("dryland")) return IrrigationMatchType.MATCH;
            return IrrigationMatchType.UNKNOWN;
        }
        if (requested.equals("irrigated")) {
            if (irrigation.equals("irrigated")) return IrrigationMatchType.MATCH;
            if (irrigation.contains("rainfed") && irrigation.contains("irrigated")) return IrrigationMatchType.MATCH;
            if (irrigation.equals("rainfed") || irrigation.equals("dryland")) return IrrigationMatchType.NO_MATCH;
            return IrrigationMatchType.UNKNOWN;
        }
        if (irrigation.contains(requested)) return IrrigationMatchType.MATCH;
        return IrrigationMatchType.UNKNOWN;
    }
}
