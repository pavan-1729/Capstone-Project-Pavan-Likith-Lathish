package com.anootations.farmerseedadvisory.repository;

import com.anootations.farmerseedadvisory.entity.SoybeanVariety;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SoybeanVarietyRepository extends JpaRepository<SoybeanVariety, Long> {

    Optional<SoybeanVariety> findByVarietyId(String varietyId);

    boolean existsByVarietyId(String varietyId);

    /**
     * Find varieties that contain the given state name in their recommendedRegions.
     * Case-insensitive partial match.
     */
    @Query("SELECT sv FROM SoybeanVariety sv WHERE " +
           "LOWER(sv.recommendedRegions) LIKE LOWER(CONCAT('%', :state, '%'))")
    List<SoybeanVariety> findByRecommendedRegionsContainingIgnoreCase(@Param("state") String state);

    /**
     * Find varieties suitable for a given season (partial match, case-insensitive).
     */
    @Query("SELECT sv FROM SoybeanVariety sv WHERE " +
           "LOWER(sv.season) LIKE LOWER(CONCAT('%', :season, '%'))")
    List<SoybeanVariety> findBySeasonContainingIgnoreCase(@Param("season") String season);

    /**
     * Count all records — used for data import validation.
     */
    @Query("SELECT COUNT(sv) FROM SoybeanVariety sv")
    long countAll();
}
