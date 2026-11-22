package com.anootations.farmerseedadvisory.controller;

import com.anootations.farmerseedadvisory.dto.MaizeRecommendationRequest;
import com.anootations.farmerseedadvisory.entity.SoybeanVariety;
import com.anootations.farmerseedadvisory.repository.SoybeanVarietyRepository;
import com.anootations.farmerseedadvisory.service.MetadataFilterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Exposes soybean variety data and search to the Android application.
 *
 * GET /api/soybean/search                — state, trait, and multidimensional ranking search
 * GET /api/soybean/varieties/{varietyId} — variety details
 * GET /api/soybean/count                 — total varieties in DB (diagnostics)
 */
@RestController
@RequestMapping("/api/soybean")
public class SoybeanController {

    private final SoybeanVarietyRepository soybeanVarietyRepository;
    private final MetadataFilterService metadataFilterService;

    public SoybeanController(
            SoybeanVarietyRepository soybeanVarietyRepository,
            MetadataFilterService metadataFilterService
    ) {
        this.soybeanVarietyRepository = soybeanVarietyRepository;
        this.metadataFilterService = metadataFilterService;
    }

    /**
     * Search and filter soybean varieties using multidimensional agronomic ranking.
     * GET /api/soybean/search?state=Madhya+Pradesh&priority=High+Yield&limit=10
     */
    @GetMapping("/search")
    public ResponseEntity<List<SoybeanVariety>> search(
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

        List<SoybeanVariety> filtered = metadataFilterService.filterSoybean(state, season, irrigation, maxMaturityDays);
        List<SoybeanVariety> ranked = metadataFilterService.searchAndRankSoybean(req, filtered);

        List<SoybeanVariety> result = ranked.stream()
                .limit(limit > 0 ? limit : 20)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * Get complete details for a specific soybean variety by its variety ID.
     * Used by the Android "Variety Details" screen after a recommendation.
     */
    @GetMapping("/varieties/{varietyId}")
    public ResponseEntity<SoybeanVariety> getVariety(@PathVariable String varietyId) {
        return soybeanVarietyRepository.findByVarietyId(varietyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Diagnostic endpoint — returns the number of soybean varieties loaded.
     * GET /api/soybean/count
     */
    @GetMapping("/count")
    public ResponseEntity<String> count() {
        long count = soybeanVarietyRepository.count();
        return ResponseEntity.ok("Soybean varieties in database: " + count);
    }
}
