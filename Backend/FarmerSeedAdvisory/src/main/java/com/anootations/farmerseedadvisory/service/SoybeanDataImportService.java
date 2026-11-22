package com.anootations.farmerseedadvisory.service;

import com.anootations.farmerseedadvisory.entity.SoybeanVariety;
import com.anootations.farmerseedadvisory.repository.SoybeanVarietyRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads the soybean varieties CSV into MySQL on application startup
 * if the database table is empty.
 *
 * The CSV must be placed at:
 *   src/main/resources/data/soybean_varieties.csv
 *
 * This is a one-time import — it skips if records already exist.
 */
@Service
public class SoybeanDataImportService {

    private static final Logger log = LoggerFactory.getLogger(SoybeanDataImportService.class);

    private final SoybeanVarietyRepository soybeanVarietyRepository;

    public SoybeanDataImportService(SoybeanVarietyRepository soybeanVarietyRepository) {
        this.soybeanVarietyRepository = soybeanVarietyRepository;
    }

    @PostConstruct
    @Transactional
    public void importIfEmpty() {
        long existingCount = soybeanVarietyRepository.count();
        if (existingCount > 0) {
            log.info("Soybean variety database already populated ({} records). Skipping CSV import.", existingCount);
            return;
        }

        log.info("Soybean variety database is empty. Starting CSV import...");

        try {
            ClassPathResource resource = new ClassPathResource("data/soybean_varieties.csv");

            if (!resource.exists()) {
                log.warn("soybean_varieties.csv not found at classpath:data/soybean_varieties.csv. " +
                         "Please ensure the CSV exists in src/main/resources/data/");
                return;
            }

            importFromCsv(resource);

        } catch (Exception e) {
            log.error("Failed to import soybean CSV: {}", e.getMessage(), e);
        }
    }

    private void importFromCsv(ClassPathResource resource) throws IOException, CsvValidationException {

        List<SoybeanVariety> varieties = new ArrayList<>();

        try (CSVReader reader = new CSVReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            // Read header row
            String[] headers = reader.readNext();
            if (headers == null) {
                log.warn("CSV file is empty");
                return;
            }

            // Build column index map for flexible column ordering
            java.util.Map<String, Integer> colIndex = new java.util.HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                colIndex.put(headers[i].trim().toLowerCase(), i);
            }

            String[] row;
            int lineNumber = 1;

            while ((row = reader.readNext()) != null) {
                lineNumber++;
                try {
                    SoybeanVariety variety = parseRow(row, colIndex);
                    if (variety != null) {
                        varieties.add(variety);
                    }
                } catch (Exception e) {
                    log.warn("Skipping CSV line {}: {}", lineNumber, e.getMessage());
                }
            }
        }

        if (!varieties.isEmpty()) {
            soybeanVarietyRepository.saveAll(varieties);
            log.info("Successfully imported {} soybean varieties into the database.", varieties.size());
        } else {
            log.warn("No valid soybean varieties found in CSV file.");
        }
    }

    private SoybeanVariety parseRow(String[] row, java.util.Map<String, Integer> colIndex) {
        String varietyId = get(row, colIndex, "variety_id");
        if (varietyId == null || varietyId.isBlank()) {
            return null; // Skip rows without variety_id
        }

        SoybeanVariety v = new SoybeanVariety();
        v.setVarietyId(varietyId);
        v.setVarietyName(get(row, colIndex, "variety_name"));
        v.setCrop(get(row, colIndex, "crop"));
        v.setCategory(get(row, colIndex, "category"));
        v.setType(get(row, colIndex, "type"));
        v.setDeveloperOrCompany(get(row, colIndex, "developer_or_company"));
        v.setParents(get(row, colIndex, "parents"));
        v.setReleaseYear(get(row, colIndex, "release_year"));
        v.setGazetteNotification(get(row, colIndex, "gazette_notification"));
        v.setSeason(get(row, colIndex, "season"));
        v.setMaturityClass(get(row, colIndex, "maturity_class"));
        v.setMaturityDaysMin(getInt(row, colIndex, "maturity_days_min"));
        v.setMaturityDaysMax(getInt(row, colIndex, "maturity_days_max"));
        v.setYieldValue(get(row, colIndex, "yield_value"));
        v.setYieldUnit(get(row, colIndex, "yield_unit"));
        v.setYieldNormalizedQHaMin(getDouble(row, colIndex, "yield_normalized_q_ha_min"));
        v.setYieldNormalizedQHaMax(getDouble(row, colIndex, "yield_normalized_q_ha_max"));
        v.setRecommendedRegions(get(row, colIndex, "recommended_regions"));
        v.setAgroClimaticZone(get(row, colIndex, "agro_climatic_zone"));
        v.setIrrigationCondition(get(row, colIndex, "irrigation_condition"));
        v.setPlantHeight(get(row, colIndex, "plant_height"));
        v.setEarHeight(get(row, colIndex, "ear_height"));
        v.setLodgingResistance(get(row, colIndex, "lodging_resistance"));
        v.setLeafOrientation(get(row, colIndex, "leaf_orientation"));
        v.setStayGreen(get(row, colIndex, "stay_green"));
        v.setDroughtTolerance(get(row, colIndex, "drought_tolerance"));
        v.setWaterloggingTolerance(get(row, colIndex, "waterlogging_tolerance"));
        v.setHeatTolerance(get(row, colIndex, "heat_tolerance"));
        v.setColdStressTolerance(get(row, colIndex, "cold_stress_tolerance"));
        v.setFallArmyworm(get(row, colIndex, "fall_armyworm"));
        v.setStemBorer(get(row, colIndex, "stem_borer"));
        v.setPinkStemBorer(get(row, colIndex, "pink_stem_borer"));
        v.setTurcicumLeafBlight(get(row, colIndex, "turcicum_leaf_blight"));
        v.setMaydisLeafBlight(get(row, colIndex, "maydis_leaf_blight"));
        v.setCurvulariaLeafSpot(get(row, colIndex, "curvularia_leaf_spot"));
        v.setBandedLeafSheathBlight(get(row, colIndex, "banded_leaf_sheath_blight"));
        v.setCharcoalRot(get(row, colIndex, "charcoal_rot"));
        v.setFusariumStalkRot(get(row, colIndex, "fusarium_stalk_rot"));
        v.setDownyMildew(get(row, colIndex, "downy_mildew"));
        v.setGrainType(get(row, colIndex, "grain_type"));
        v.setGrainColour(get(row, colIndex, "grain_colour"));
        v.setSpecialTraits(get(row, colIndex, "special_traits"));
        v.setSourceDocument(get(row, colIndex, "source_document"));
        v.setSourcePdfPage(get(row, colIndex, "source_pdf_page"));
        v.setSourcePrintedPage(get(row, colIndex, "source_printed_page"));
        v.setSourceOrganization(get(row, colIndex, "source_organization"));
        v.setSourceType(get(row, colIndex, "source_type"));
        v.setSourceUrl(get(row, colIndex, "source_url"));
        v.setSourceText(get(row, colIndex, "source_text"));
        v.setDataQualityNotes(get(row, colIndex, "data_quality_notes"));

        return v;
    }

    private String get(String[] row, java.util.Map<String, Integer> colIndex, String column) {
        Integer idx = colIndex.get(column);
        if (idx == null || idx >= row.length) return null;
        String value = row[idx].trim();
        return value.isEmpty() ? null : value;
    }

    private Integer getInt(String[] row, java.util.Map<String, Integer> colIndex, String column) {
        String value = get(row, colIndex, column);
        if (value == null) return null;
        try {
            return (int) Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double getDouble(String[] row, java.util.Map<String, Integer> colIndex, String column) {
        String value = get(row, colIndex, column);
        if (value == null) return null;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
