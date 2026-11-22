package com.anootations.farmerseedadvisory.repository;

import com.anootations.farmerseedadvisory.entity.CottonVariety;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CottonVarietyRepository extends JpaRepository<CottonVariety, Long> {

    Optional<CottonVariety> findByVarietyId(String varietyId);

    boolean existsByVarietyId(String varietyId);

    /**
     * Find varieties that contain the given state name in their recommendedRegions.
     * Case-insensitive partial match.
     */
    @Query("SELECT cv FROM CottonVariety cv WHERE " +
           "LOWER(cv.recommendedRegions) LIKE LOWER(CONCAT('%', :state, '%'))")
    List<CottonVariety> findByRecommendedRegionsContainingIgnoreCase(@Param("state") String state);

    /**
     * Find varieties suitable for a given season (partial match, case-insensitive).
     */
    @Query("SELECT cv FROM CottonVariety cv WHERE " +
           "LOWER(cv.season) LIKE LOWER(CONCAT('%', :season, '%'))")
    List<CottonVariety> findBySeasonContainingIgnoreCase(@Param("season") String season);

    /**
     * Count all records — used for data import validation.
     */
    @Query("SELECT COUNT(cv) FROM CottonVariety cv")
    long countAll();
}
