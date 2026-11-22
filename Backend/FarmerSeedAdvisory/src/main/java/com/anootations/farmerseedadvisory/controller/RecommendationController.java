package com.anootations.farmerseedadvisory.controller;

import com.anootations.farmerseedadvisory.dto.MaizeRecommendationRequest;
import com.anootations.farmerseedadvisory.dto.RecommendationResponse;
import com.anootations.farmerseedadvisory.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Maize seed recommendation endpoint.
 *
 * POST /api/recommendations/maize
 *
 * The Android app sends the wizard inputs.
 * Spring Boot runs the RAG pipeline and returns structured recommendations.
 */
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * Run the maize RAG recommendation pipeline.
     *
     * Request body: MaizeRecommendationRequest
     * Response:     RecommendationResponse with top-K varieties
     */
    @PostMapping("/maize")
    public ResponseEntity<RecommendationResponse> recommendMaize(
            @RequestBody MaizeRecommendationRequest request,
            Authentication authentication
    ) {
        // Request validation — state is required for metadata filtering
        if (request.getState() == null || request.getState().isBlank()) {
            RecommendationResponse error = new RecommendationResponse();
            error.setWarnings(java.util.List.of("State is required to provide relevant recommendations."));
            error.setRecommendations(java.util.Collections.emptyList());
            return ResponseEntity.badRequest().body(error);
        }

        RecommendationResponse response = recommendationService.recommend(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Run the soybean RAG recommendation pipeline.
     *
     * Request body: MaizeRecommendationRequest
     * Response:     RecommendationResponse with top-K varieties
     */
    @PostMapping("/soybean")
    public ResponseEntity<RecommendationResponse> recommendSoybean(
            @RequestBody MaizeRecommendationRequest request,
            Authentication authentication
    ) {
        // Request validation — state is required for metadata filtering
        if (request.getState() == null || request.getState().isBlank()) {
            RecommendationResponse error = new RecommendationResponse();
            error.setWarnings(java.util.List.of("State is required to provide relevant recommendations."));
            error.setRecommendations(java.util.Collections.emptyList());
            return ResponseEntity.badRequest().body(error);
        }

        RecommendationResponse response = recommendationService.recommendSoybean(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Run the cotton RAG recommendation pipeline.
     *
     * Request body: MaizeRecommendationRequest
     * Response:     RecommendationResponse with top-K varieties
     */
    @PostMapping("/cotton")
    public ResponseEntity<RecommendationResponse> recommendCotton(
            @RequestBody MaizeRecommendationRequest request,
            Authentication authentication
    ) {
        // Request validation — state is required for metadata filtering
        if (request.getState() == null || request.getState().isBlank()) {
            RecommendationResponse error = new RecommendationResponse();
            error.setWarnings(java.util.List.of("State is required to provide relevant recommendations."));
            error.setRecommendations(java.util.Collections.emptyList());
            return ResponseEntity.badRequest().body(error);
        }

        RecommendationResponse response = recommendationService.recommendCotton(request);
        return ResponseEntity.ok(response);
    }
}
