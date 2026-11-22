package com.anootations.farmerseedadvisory.controller;

import com.anootations.farmerseedadvisory.entity.MaizeVariety;
import com.anootations.farmerseedadvisory.exception.ValidationException;
import com.anootations.farmerseedadvisory.repository.MaizeVarietyRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes maize variety data to the Android application.
 *
 * GET /api/maize/varieties/{varietyId}  — variety details
 * GET /api/maize/count                  — total varieties in DB (diagnostics)
 */
@RestController
@RequestMapping("/api/maize")
public class MaizeController {

    private final MaizeVarietyRepository maizeVarietyRepository;

    public MaizeController(MaizeVarietyRepository maizeVarietyRepository) {
        this.maizeVarietyRepository = maizeVarietyRepository;
    }

    /**
     * Get complete details for a specific maize variety by its variety ID.
     * Used by the Android "Variety Details" screen after a recommendation.
     */
    @GetMapping("/varieties/{varietyId}")
    public ResponseEntity<MaizeVariety> getVariety(@PathVariable String varietyId) {
        return maizeVarietyRepository.findByVarietyId(varietyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Diagnostic endpoint — returns the number of varieties loaded.
     * GET /api/maize/count
     */
    @GetMapping("/count")
    public ResponseEntity<String> count() {
        long count = maizeVarietyRepository.count();
        return ResponseEntity.ok("Maize varieties in database: " + count);
    }
}
