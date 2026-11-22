package com.anootations.farmerseedadvisory.controller;

import com.anootations.farmerseedadvisory.dto.FarmerRequest;
import com.anootations.farmerseedadvisory.dto.FarmerResponse;
import com.anootations.farmerseedadvisory.service.FarmerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for farmer registration and profile management.
 *
 * All endpoints require a valid Firebase ID token in the
 * Authorization header:
 *
 *   Authorization: Bearer <firebase-id-token>
 *
 * The Firebase UID is always extracted from the verified token —
 * it is NEVER accepted from the request body.
 */
@RestController
@RequestMapping("/api/farmer")
public class FarmerController {

    private final FarmerService farmerService;

    public FarmerController(FarmerService farmerService) {
        this.farmerService = farmerService;
    }

    // ----------------------------------------------------------------
    // POST /api/farmer/register
    // Register a new farmer profile linked to the Firebase UID.
    // Returns 201 on success, 409 if profile already exists.
    // ----------------------------------------------------------------

    @PostMapping("/register")
    public ResponseEntity<FarmerResponse> registerFarmer(
            @Valid @RequestBody FarmerRequest request,
            Authentication authentication
    ) {
        String firebaseUid = authentication.getName();
        FarmerResponse response = farmerService.register(firebaseUid, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ----------------------------------------------------------------
    // GET /api/farmer/profile
    // Get the authenticated farmer's profile.
    // Returns 200 with FarmerResponse, or 404 if not registered.
    // ----------------------------------------------------------------

    @GetMapping("/profile")
    public ResponseEntity<FarmerResponse> getProfile(
            Authentication authentication
    ) {
        String firebaseUid = authentication.getName();
        FarmerResponse response = farmerService.getProfile(firebaseUid);
        return ResponseEntity.ok(response);
    }

    // ----------------------------------------------------------------
    // PUT /api/farmer/profile
    // Update the authenticated farmer's profile.
    // Only non-null fields in the request body are updated.
    // Returns 200 with updated FarmerResponse.
    // ----------------------------------------------------------------

    @PutMapping("/profile")
    public ResponseEntity<FarmerResponse> updateProfile(
            @RequestBody FarmerRequest request,
            Authentication authentication
    ) {
        String firebaseUid = authentication.getName();
        FarmerResponse response = farmerService.updateProfile(firebaseUid, request);
        return ResponseEntity.ok(response);
    }
}
