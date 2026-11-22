package com.anootations.farmerseedadvisory.repository;

import com.anootations.farmerseedadvisory.entity.Farmer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FarmerRepository extends JpaRepository<Farmer, Long> {

    Optional<Farmer> findByFirebaseUid(String firebaseUid);

    boolean existsByFirebaseUid(String firebaseUid);
}