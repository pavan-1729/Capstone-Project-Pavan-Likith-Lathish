package com.anootations.farmerseedadvisory.controller;

import com.anootations.farmerseedadvisory.dto.MaizeRecommendationRequest;
import com.anootations.farmerseedadvisory.entity.CottonVariety;
import com.anootations.farmerseedadvisory.repository.CottonVarietyRepository;
import com.anootations.farmerseedadvisory.service.MetadataFilterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Exposes cotton variety data and search to the Android application.
 *
 * GET /api/cotton/search                — state, trait, and multidimensional ranking search
 * GET /api/cotton/varieties/{varietyId} — variety details
 * GET /api/cotton/count                 — total varieties in DB (diagnostics)
 */
@RestController
@RequestMapping("/api/cotton")
public class CottonController {

    private final CottonVarietyRepository cottonVarietyRepository;
    private final MetadataFilterService metadataFilterService;

    public CottonController(
            CottonVarietyRepository cottonVarietyRepository,
            MetadataFilterService metadataFilterService
    ) {
        this.cottonVarietyRepository = cottonVarietyRepository;
        this.metadataFilterService = metadataFilterService;
    }

    /**
     * Search and filter cotton varieties using multidimensional agronomic ranking.
     * GET /api/cotton/search?state=Maharashtra&priority=High+Yield&limit=10
     */
    @GetMapping("/search")
    public ResponseEntity<List<CottonVariety>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String season,
            @RequestParam(required = false) String irrigation,
            @RequestParam(required = false) String soilType,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false, defaultValue = "0") int maxMaturityDays,
            @RequestParam(required = false, defaultValue = "20") int limit
    ) {
        MaizeRecommendationRequest req = new MaizeRecommendationRequest();
        req.setState(state);
        req.setDistrict(district);
        req.setSeason(season);
        req.setIrrigation(irrigation);
        req.setSoilType(soilType);
        req.setPriority(priority);
        req.setMaximumMaturityDays(maxMaturityDays);
        req.setFarmerDescription(query);

        List<CottonVariety> filtered = metadataFilterService.filterCotton(state, season, irrigation, maxMaturityDays);
        List<CottonVariety> ranked = metadataFilterService.searchAndRankCotton(req, filtered);

        List<CottonVariety> result = ranked.stream()
                .limit(limit > 0 ? limit : 20)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * Get complete details for a specific cotton variety by its variety ID.
     * Used by the Android "Variety Details" screen after a recommendation.
     */
    @GetMapping("/varieties/{varietyId}")
    public ResponseEntity<CottonVariety> getVariety(@PathVariable String varietyId) {
        return cottonVarietyRepository.findByVarietyId(varietyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Diagnostic endpoint — returns the number of cotton varieties loaded.
     * GET /api/cotton/count
     */
    @GetMapping("/count")
    public ResponseEntity<String> count() {
        long count = cottonVarietyRepository.count();
        return ResponseEntity.ok("Cotton varieties in database: " + count);
    }
}
