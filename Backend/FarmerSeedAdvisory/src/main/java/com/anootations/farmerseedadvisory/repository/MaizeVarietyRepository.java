package com.anootations.farmerseedadvisory.repository;

import com.anootations.farmerseedadvisory.entity.MaizeVariety;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MaizeVarietyRepository extends JpaRepository<MaizeVariety, Long> {

    Optional<MaizeVariety> findByVarietyId(String varietyId);

    boolean existsByVarietyId(String varietyId);

    /**
     * Find varieties that contain the given state name in their recommendedRegions.
     * Case-insensitive partial match.
     */
    @Query("SELECT mv FROM MaizeVariety mv WHERE " +
           "LOWER(mv.recommendedRegions) LIKE LOWER(CONCAT('%', :state, '%'))")
    List<MaizeVariety> findByRecommendedRegionsContainingIgnoreCase(@Param("state") String state);

    /**
     * Find varieties suitable for a given season (partial match, case-insensitive).
     */
    @Query("SELECT mv FROM MaizeVariety mv WHERE " +
           "LOWER(mv.season) LIKE LOWER(CONCAT('%', :season, '%'))")
    List<MaizeVariety> findBySeasonContainingIgnoreCase(@Param("season") String season);

    /**
     * Count all records — used for data import validation.
     */
    @Query("SELECT COUNT(mv) FROM MaizeVariety mv")
    long countAll();
}
