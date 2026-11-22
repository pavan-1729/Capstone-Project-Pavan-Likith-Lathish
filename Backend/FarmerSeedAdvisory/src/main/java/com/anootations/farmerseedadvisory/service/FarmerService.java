package com.anootations.farmerseedadvisory.service;

import com.anootations.farmerseedadvisory.dto.FarmerRequest;
import com.anootations.farmerseedadvisory.dto.FarmerResponse;
import com.anootations.farmerseedadvisory.entity.Farmer;
import com.anootations.farmerseedadvisory.exception.FarmerAlreadyExistsException;
import com.anootations.farmerseedadvisory.exception.FarmerNotFoundException;
import com.anootations.farmerseedadvisory.repository.FarmerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for farmer registration and profile management.
 * The Firebase UID is extracted from the verified token by the
 * security filter — it is never accepted from the request body.
 */
@Service
public class FarmerService {

    private static final Logger log = LoggerFactory.getLogger(FarmerService.class);

    private final FarmerRepository farmerRepository;

    public FarmerService(FarmerRepository farmerRepository) {
        this.farmerRepository = farmerRepository;
    }

    // ----------------------------------------------------------------
    // Register a new farmer
    // ----------------------------------------------------------------

    @Transactional
    public FarmerResponse register(String firebaseUid, FarmerRequest request) {

        if (farmerRepository.existsByFirebaseUid(firebaseUid)) {
            log.warn("Registration attempt for existing farmer. UID: {}", firebaseUid);
            throw new FarmerAlreadyExistsException(
                "A farmer profile already exists for this account. " +
                "Please use the profile update endpoint instead."
            );
        }

        Farmer farmer = new Farmer(
                firebaseUid,
                request.getName(),
                request.getMobile(),
                request.getEmail(),
                request.getState(),
                request.getDistrict(),
                request.getVillage(),
                request.getPreferredLanguage() != null ? request.getPreferredLanguage() : "en",
                request.getPrimaryCrop() != null ? request.getPrimaryCrop() : "Maize",
                request.getFarmSize()
        );

        Farmer saved = farmerRepository.save(farmer);
        log.info("New farmer registered. UID: {}", firebaseUid);
        return FarmerResponse.from(saved);
    }

    // ----------------------------------------------------------------
    // Get farmer profile
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public FarmerResponse getProfile(String firebaseUid) {

        Farmer farmer = farmerRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new FarmerNotFoundException(
                    "Farmer profile not found. Please complete your registration."
                ));

        return FarmerResponse.from(farmer);
    }

    // ----------------------------------------------------------------
    // Update farmer profile
    // ----------------------------------------------------------------

    @Transactional
    public FarmerResponse updateProfile(String firebaseUid, FarmerRequest request) {

        Farmer farmer = farmerRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new FarmerNotFoundException(
                    "Farmer profile not found. Please register first."
                ));

        // Update only the fields that are provided (non-null)
        if (request.getName() != null && !request.getName().isBlank()) {
            farmer.setName(request.getName());
        }
        if (request.getMobile() != null && !request.getMobile().isBlank()) {
            farmer.setMobile(request.getMobile());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            farmer.setEmail(request.getEmail());
        }
        if (request.getState() != null && !request.getState().isBlank()) {
            farmer.setState(request.getState());
        }
        if (request.getDistrict() != null) {
            farmer.setDistrict(request.getDistrict());
        }
        if (request.getVillage() != null) {
            farmer.setVillage(request.getVillage());
        }
        if (request.getPreferredLanguage() != null) {
            farmer.setPreferredLanguage(request.getPreferredLanguage());
        }
        if (request.getPrimaryCrop() != null) {
            farmer.setPrimaryCrop(request.getPrimaryCrop());
        }
        if (request.getFarmSize() != null) {
            farmer.setFarmSize(request.getFarmSize());
        }

        Farmer updated = farmerRepository.save(farmer);
        log.info("Farmer profile updated. UID: {}", firebaseUid);
        return FarmerResponse.from(updated);
    }
}
