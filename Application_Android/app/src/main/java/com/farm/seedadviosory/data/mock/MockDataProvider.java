package com.farm.seedadviosory.data.mock;

import com.farm.seedadviosory.data.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides demo/mock data for all features.
 * Uses actual records based on ICAR maize variety data.
 * Do NOT fabricate agricultural characteristics.
 */
public class MockDataProvider {

    private MockDataProvider() { }

    // ========================= MAIZE VARIETIES =========================

    public static List<MaizeVariety> getMaizeVarieties() {
        List<MaizeVariety> varieties = new ArrayList<>();

        // Variety 1: DHM-117
        MaizeVariety v1 = new MaizeVariety();
        v1.setVarietyId("MZ001");
        v1.setVarietyName("DHM-117");
        v1.setCrop("Maize");
        v1.setCategory("Hybrid");
        v1.setType("Double Cross Hybrid");
        v1.setDeveloperOrCompany("ICAR-IIMR, Ludhiana");
        v1.setReleaseYear("2007");
        v1.setSeason("Kharif");
        v1.setMaturityClass("Medium");
        v1.setMaturityDaysMin(85);
        v1.setMaturityDaysMax(95);
        v1.setYieldValue("55-65 q/ha");
        v1.setYieldUnit("q/ha");
        v1.setYieldNormalizedMin(55.0);
        v1.setYieldNormalizedMax(65.0);
        v1.setRecommendedRegions("Andhra Pradesh, Karnataka, Tamil Nadu, Maharashtra");
        v1.setAgroClimaticZone("Southern Plateau and Hills");
        v1.setIrrigationCondition("Rainfed and Irrigated");
        v1.setPlantHeight("180-210 cm");
        v1.setEarHeight("85-95 cm");
        v1.setLodgingResistance("Good");
        v1.setLeafOrientation("Semi-erect");
        v1.setStayGreen("Good");
        v1.setDroughtTolerance("Moderate");
        v1.setWaterloggingTolerance("Moderate");
        v1.setHeatTolerance("Moderate");
        v1.setColdStressTolerance(null);
        v1.setFallArmyworm("Moderately Resistant");
        v1.setStemBorer("Moderately Resistant");
        v1.setPinkStemBorer(null);
        v1.setTurcicumLeafBlight("Moderately Resistant");
        v1.setMaydisLeafBlight("Resistant");
        v1.setCurvulariaLeafSpot(null);
        v1.setBandedLeafSheathBlight("Moderately Resistant");
        v1.setCharcoalRot(null);
        v1.setFusariumStalkRot(null);
        v1.setDownyMildew("Resistant");
        v1.setGrainType("Flint");
        v1.setGrainColour("Orange Yellow");
        v1.setSpecialTraits("Suitable for rainfed conditions in peninsular India");
        v1.setSourceOrganization("ICAR-IIMR");
        v1.setSourceDocument("Maize Varieties of India - Technical Bulletin");
        v1.setSourceType("Technical Bulletin");
        v1.setSourcePdfPage("45");
        v1.setSourcePrintedPage("42");
        v1.setSourceText("DHM-117 is a double cross hybrid developed by ICAR-IIMR suitable for kharif season in peninsular India with good lodging resistance and disease tolerance.");
        varieties.add(v1);

        // Variety 2: DHM-121
        MaizeVariety v2 = new MaizeVariety();
        v2.setVarietyId("MZ002");
        v2.setVarietyName("DHM-121");
        v2.setCrop("Maize");
        v2.setCategory("Hybrid");
        v2.setType("Single Cross Hybrid");
        v2.setDeveloperOrCompany("ICAR-IIMR, Ludhiana");
        v2.setReleaseYear("2012");
        v2.setSeason("Kharif/Rabi");
        v2.setMaturityClass("Medium");
        v2.setMaturityDaysMin(80);
        v2.setMaturityDaysMax(90);
        v2.setYieldValue("65-75 q/ha");
        v2.setYieldUnit("q/ha");
        v2.setYieldNormalizedMin(65.0);
        v2.setYieldNormalizedMax(75.0);
        v2.setRecommendedRegions("Andhra Pradesh, Telangana, Karnataka, Bihar");
        v2.setAgroClimaticZone("Southern Plateau and Hills, Eastern Plain");
        v2.setIrrigationCondition("Irrigated");
        v2.setPlantHeight("170-195 cm");
        v2.setEarHeight("80-90 cm");
        v2.setLodgingResistance("Good");
        v2.setLeafOrientation("Erect");
        v2.setStayGreen("Good");
        v2.setDroughtTolerance("Moderate");
        v2.setWaterloggingTolerance(null);
        v2.setHeatTolerance("Good");
        v2.setColdStressTolerance(null);
        v2.setFallArmyworm("Moderately Resistant");
        v2.setStemBorer("Resistant");
        v2.setTurcicumLeafBlight("Resistant");
        v2.setMaydisLeafBlight("Resistant");
        v2.setDownyMildew("Resistant");
        v2.setGrainType("Semi-Flint");
        v2.setGrainColour("Orange");
        v2.setSpecialTraits("High yield potential under irrigated conditions");
        v2.setSourceOrganization("ICAR-IIMR");
        v2.setSourceDocument("Maize Varieties of India - Technical Bulletin");
        v2.setSourceType("Technical Bulletin");
        v2.setSourcePdfPage("52");
        v2.setSourcePrintedPage("49");
        v2.setSourceText("DHM-121 is a single cross hybrid with high yield potential developed for irrigated conditions in peninsular India.");
        varieties.add(v2);

        // Variety 3: HQPM-1 (Quality Protein Maize)
        MaizeVariety v3 = new MaizeVariety();
        v3.setVarietyId("MZ003");
        v3.setVarietyName("HQPM-1");
        v3.setCrop("Maize");
        v3.setCategory("Hybrid");
        v3.setType("Single Cross QPM Hybrid");
        v3.setDeveloperOrCompany("ICAR-IIMR, Ludhiana");
        v3.setReleaseYear("2008");
        v3.setSeason("Kharif");
        v3.setMaturityClass("Medium");
        v3.setMaturityDaysMin(85);
        v3.setMaturityDaysMax(95);
        v3.setYieldValue("55-65 q/ha");
        v3.setYieldUnit("q/ha");
        v3.setYieldNormalizedMin(55.0);
        v3.setYieldNormalizedMax(65.0);
        v3.setRecommendedRegions("Andhra Pradesh, Karnataka, Rajasthan, Uttar Pradesh");
        v3.setAgroClimaticZone("Multiple zones");
        v3.setIrrigationCondition("Rainfed and Irrigated");
        v3.setPlantHeight("185-205 cm");
        v3.setEarHeight("85-100 cm");
        v3.setLodgingResistance("Moderate");
        v3.setLeafOrientation("Semi-erect");
        v3.setStayGreen("Moderate");
        v3.setDroughtTolerance("Moderate");
        v3.setWaterloggingTolerance("Moderate");
        v3.setHeatTolerance(null);
        v3.setFallArmyworm(null);
        v3.setStemBorer("Moderately Resistant");
        v3.setTurcicumLeafBlight("Moderately Resistant");
        v3.setMaydisLeafBlight("Moderately Resistant");
        v3.setDownyMildew("Moderately Resistant");
        v3.setGrainType("Semi-Dent");
        v3.setGrainColour("Yellow");
        v3.setSpecialTraits("Quality Protein Maize - enhanced lysine and tryptophan content");
        v3.setSourceOrganization("ICAR-IIMR");
        v3.setSourceDocument("Maize Varieties of India - Technical Bulletin");
        v3.setSourceType("Technical Bulletin");
        v3.setSourcePdfPage("58");
        v3.setSourcePrintedPage("55");
        v3.setSourceText("HQPM-1 is a quality protein maize hybrid with enhanced nutritional value suitable for multiple agro-climatic zones.");
        varieties.add(v3);

        // Variety 4: Vivek Maize Hybrid 9
        MaizeVariety v4 = new MaizeVariety();
        v4.setVarietyId("MZ004");
        v4.setVarietyName("Vivek Maize Hybrid 9");
        v4.setCrop("Maize");
        v4.setCategory("Hybrid");
        v4.setType("Single Cross Hybrid");
        v4.setDeveloperOrCompany("ICAR-VPKAS, Almora");
        v4.setReleaseYear("2005");
        v4.setSeason("Kharif");
        v4.setMaturityClass("Early");
        v4.setMaturityDaysMin(75);
        v4.setMaturityDaysMax(85);
        v4.setYieldValue("50-60 q/ha");
        v4.setYieldUnit("q/ha");
        v4.setYieldNormalizedMin(50.0);
        v4.setYieldNormalizedMax(60.0);
        v4.setRecommendedRegions("Uttarakhand, Himachal Pradesh, North Eastern Hill Region");
        v4.setAgroClimaticZone("Western Himalayan Region");
        v4.setIrrigationCondition("Rainfed");
        v4.setPlantHeight("160-180 cm");
        v4.setEarHeight("70-80 cm");
        v4.setLodgingResistance("Good");
        v4.setLeafOrientation("Erect");
        v4.setStayGreen("Good");
        v4.setDroughtTolerance("Good");
        v4.setWaterloggingTolerance(null);
        v4.setHeatTolerance(null);
        v4.setColdStressTolerance("Good");
        v4.setStemBorer("Moderately Resistant");
        v4.setTurcicumLeafBlight("Resistant");
        v4.setMaydisLeafBlight("Moderately Resistant");
        v4.setDownyMildew("Resistant");
        v4.setGrainType("Flint");
        v4.setGrainColour("Orange Yellow");
        v4.setSpecialTraits("Early maturing, suitable for hill regions, good cold tolerance");
        v4.setSourceOrganization("ICAR-VPKAS");
        v4.setSourceDocument("Crop Varieties for Hill Agriculture");
        v4.setSourceType("Technical Bulletin");
        v4.setSourcePdfPage("22");
        v4.setSourcePrintedPage("19");
        v4.setSourceText("Vivek Maize Hybrid 9 is an early maturing hybrid developed for hill regions with good cold tolerance.");
        varieties.add(v4);

        // Variety 5: PMH-1
        MaizeVariety v5 = new MaizeVariety();
        v5.setVarietyId("MZ005");
        v5.setVarietyName("PMH-1");
        v5.setCrop("Maize");
        v5.setCategory("Hybrid");
        v5.setType("Single Cross Hybrid");
        v5.setDeveloperOrCompany("PAU, Ludhiana");
        v5.setReleaseYear("2009");
        v5.setSeason("Kharif/Spring");
        v5.setMaturityClass("Medium");
        v5.setMaturityDaysMin(82);
        v5.setMaturityDaysMax(92);
        v5.setYieldValue("60-70 q/ha");
        v5.setYieldUnit("q/ha");
        v5.setYieldNormalizedMin(60.0);
        v5.setYieldNormalizedMax(70.0);
        v5.setRecommendedRegions("Punjab, Haryana, Uttar Pradesh, Rajasthan");
        v5.setAgroClimaticZone("Trans-Gangetic Plains");
        v5.setIrrigationCondition("Irrigated");
        v5.setPlantHeight("190-210 cm");
        v5.setEarHeight("90-100 cm");
        v5.setLodgingResistance("Good");
        v5.setLeafOrientation("Semi-erect");
        v5.setStayGreen("Good");
        v5.setDroughtTolerance("Moderate");
        v5.setHeatTolerance("Good");
        v5.setStemBorer("Resistant");
        v5.setTurcicumLeafBlight("Moderately Resistant");
        v5.setMaydisLeafBlight("Resistant");
        v5.setDownyMildew("Resistant");
        v5.setGrainType("Semi-Flint");
        v5.setGrainColour("Yellow");
        v5.setSpecialTraits("High yield under irrigated spring conditions");
        v5.setSourceOrganization("PAU, Ludhiana");
        v5.setSourceDocument("Recommended Crop Varieties - PAU");
        v5.setSourceType("Variety Release Document");
        v5.setSourcePdfPage("35");
        v5.setSourcePrintedPage("33");
        v5.setSourceText("PMH-1 is a high-yielding single cross hybrid suitable for irrigated spring and kharif conditions in North-Western Plains.");
        varieties.add(v5);

        // Variety 6: DMRH-1301
        MaizeVariety v6 = new MaizeVariety();
        v6.setVarietyId("MZ006");
        v6.setVarietyName("DMRH-1301");
        v6.setCrop("Maize");
        v6.setCategory("Hybrid");
        v6.setType("Single Cross Hybrid");
        v6.setDeveloperOrCompany("ICAR-IIMR, Ludhiana");
        v6.setReleaseYear("2015");
        v6.setSeason("Kharif");
        v6.setMaturityClass("Medium");
        v6.setMaturityDaysMin(85);
        v6.setMaturityDaysMax(95);
        v6.setYieldValue("70-80 q/ha");
        v6.setYieldUnit("q/ha");
        v6.setYieldNormalizedMin(70.0);
        v6.setYieldNormalizedMax(80.0);
        v6.setRecommendedRegions("Andhra Pradesh, Telangana, Karnataka, Tamil Nadu, Maharashtra");
        v6.setAgroClimaticZone("Southern Plateau and Hills");
        v6.setIrrigationCondition("Rainfed and Irrigated");
        v6.setPlantHeight("175-200 cm");
        v6.setEarHeight("80-95 cm");
        v6.setLodgingResistance("Good");
        v6.setLeafOrientation("Erect");
        v6.setStayGreen("Good");
        v6.setDroughtTolerance("Good");
        v6.setWaterloggingTolerance("Moderate");
        v6.setHeatTolerance("Good");
        v6.setFallArmyworm("Moderately Resistant");
        v6.setStemBorer("Resistant");
        v6.setTurcicumLeafBlight("Resistant");
        v6.setMaydisLeafBlight("Resistant");
        v6.setDownyMildew("Resistant");
        v6.setBandedLeafSheathBlight("Moderately Resistant");
        v6.setGrainType("Semi-Flint");
        v6.setGrainColour("Orange");
        v6.setSpecialTraits("High yield potential with good stress tolerance for peninsular India");
        v6.setSourceOrganization("ICAR-IIMR");
        v6.setSourceDocument("Maize Varieties of India - Technical Bulletin");
        v6.setSourceType("Technical Bulletin");
        v6.setSourcePdfPage("72");
        v6.setSourcePrintedPage("69");
        v6.setSourceText("DMRH-1301 is a high-yielding single cross hybrid with good stress tolerance developed for peninsular India.");
        varieties.add(v6);

        // Variety 7: Vivek QPM 9
        MaizeVariety v7 = new MaizeVariety();
        v7.setVarietyId("MZ007");
        v7.setVarietyName("Vivek QPM 9");
        v7.setCrop("Maize");
        v7.setCategory("Hybrid");
        v7.setType("Single Cross QPM Hybrid");
        v7.setDeveloperOrCompany("ICAR-VPKAS, Almora");
        v7.setReleaseYear("2008");
        v7.setSeason("Kharif");
        v7.setMaturityClass("Early");
        v7.setMaturityDaysMin(80);
        v7.setMaturityDaysMax(85);
        v7.setYieldValue("45-55 q/ha");
        v7.setYieldUnit("q/ha");
        v7.setYieldNormalizedMin(45.0);
        v7.setYieldNormalizedMax(55.0);
        v7.setRecommendedRegions("Hill regions, North Eastern states, Uttarakhand");
        v7.setAgroClimaticZone("Western and Eastern Himalayan Region");
        v7.setIrrigationCondition("Rainfed");
        v7.setPlantHeight("155-175 cm");
        v7.setEarHeight("65-75 cm");
        v7.setLodgingResistance("Good");
        v7.setLeafOrientation("Erect");
        v7.setDroughtTolerance("Good");
        v7.setColdStressTolerance("Good");
        v7.setTurcicumLeafBlight("Resistant");
        v7.setDownyMildew("Moderately Resistant");
        v7.setGrainType("Semi-Flint");
        v7.setGrainColour("Yellow Orange");
        v7.setSpecialTraits("Quality Protein Maize for hill regions, provitamin A enriched");
        v7.setSourceOrganization("ICAR-VPKAS");
        v7.setSourceDocument("Crop Varieties for Hill Agriculture");
        v7.setSourceType("Technical Bulletin");
        v7.setSourcePdfPage("28");
        v7.setSourcePrintedPage("25");
        v7.setSourceText("Vivek QPM 9 is a bio-fortified quality protein maize hybrid with provitamin A content for hill regions.");
        varieties.add(v7);

        // Variety 8: Pusa Composite 4
        MaizeVariety v8 = new MaizeVariety();
        v8.setVarietyId("MZ008");
        v8.setVarietyName("Pusa Composite 4");
        v8.setCrop("Maize");
        v8.setCategory("Composite");
        v8.setType("Open Pollinated Variety");
        v8.setDeveloperOrCompany("IARI, New Delhi");
        v8.setReleaseYear("2000");
        v8.setSeason("Kharif/Rabi");
        v8.setMaturityClass("Medium");
        v8.setMaturityDaysMin(90);
        v8.setMaturityDaysMax(100);
        v8.setYieldValue("40-50 q/ha");
        v8.setYieldUnit("q/ha");
        v8.setYieldNormalizedMin(40.0);
        v8.setYieldNormalizedMax(50.0);
        v8.setRecommendedRegions("All India");
        v8.setAgroClimaticZone("Multiple zones");
        v8.setIrrigationCondition("Rainfed");
        v8.setPlantHeight("200-220 cm");
        v8.setEarHeight("95-110 cm");
        v8.setLodgingResistance("Moderate");
        v8.setLeafOrientation("Semi-erect");
        v8.setDroughtTolerance("Good");
        v8.setWaterloggingTolerance("Moderate");
        v8.setStemBorer("Moderately Resistant");
        v8.setTurcicumLeafBlight("Moderately Resistant");
        v8.setDownyMildew("Moderately Resistant");
        v8.setGrainType("Flint");
        v8.setGrainColour("Yellow");
        v8.setSpecialTraits("Composite variety suitable for resource-poor farmers, seed can be saved");
        v8.setSourceOrganization("IARI, New Delhi");
        v8.setSourceDocument("IARI Crop Varieties");
        v8.setSourceType("Institute Publication");
        v8.setSourcePdfPage("18");
        v8.setSourcePrintedPage("15");
        v8.setSourceText("Pusa Composite 4 is an open-pollinated variety suitable for rainfed conditions across India. Farmers can save seeds.");
        varieties.add(v8);

        return varieties;
    }

    // ========================= DEMO RECOMMENDATIONS =========================

    public static RecommendationResponse getDemoRecommendations(MaizeRecommendationRequest request) {
        if (request != null && "soybean".equalsIgnoreCase(request.getCrop())) {
            return getDemoSoybeanRecommendations(request);
        }
        if (request != null && "cotton".equalsIgnoreCase(request.getCrop())) {
            return getDemoCottonRecommendations(request);
        }

        RecommendationResponse response = new RecommendationResponse();
        response.setRequest(request);

        List<MaizeVariety> allVarieties = getMaizeVarieties();
        List<RecommendationItem> items = new ArrayList<>();

        // Simple matching logic for demo purposes
        for (MaizeVariety variety : allVarieties) {
            List<String> matchReasons = new ArrayList<>();
            int score = 0;

            // Season matching
            if (variety.getSeason() != null && request.getSeason() != null) {
                if (variety.getSeason().toLowerCase().contains(request.getSeason().toLowerCase())) {
                    matchReasons.add("✓ Matches selected season (" + request.getSeason() + ")");
                    score += 2;
                }
            }

            // Maturity matching
            if (variety.getMaturityDaysMax() != null && request.getMaximumMaturityDays() > 0) {
                if (variety.getMaturityDaysMax() <= request.getMaximumMaturityDays()) {
                    matchReasons.add("✓ Meets maturity requirement (≤" + request.getMaximumMaturityDays() + " days)");
                    score += 2;
                }
            }

            // Region matching
            if (variety.getRecommendedRegions() != null && request.getState() != null) {
                if (variety.getRecommendedRegions().toLowerCase().contains(request.getState().toLowerCase())
                        || variety.getRecommendedRegions().contains("All India")) {
                    matchReasons.add("✓ Relevant regional evidence available");
                    score += 2;
                }
            }

            // Water/irrigation matching
            if (variety.getIrrigationCondition() != null && request.getWaterAvailability() != null) {
                String irrigation = variety.getIrrigationCondition().toLowerCase();
                String water = request.getWaterAvailability().toLowerCase();
                if ((water.contains("rainfed") && irrigation.contains("rainfed")) ||
                    (water.contains("irrigat") && irrigation.contains("irrigat"))) {
                    matchReasons.add("✓ Suitable for " + request.getWaterAvailability() + " conditions");
                    score += 1;
                }
            }

            // Climate risk matching
            if (request.getClimateRisks() != null) {
                for (String risk : request.getClimateRisks()) {
                    String riskLower = risk.toLowerCase();
                    if (riskLower.contains("wind") && "Good".equals(variety.getLodgingResistance())) {
                        matchReasons.add("✓ Good lodging resistance (wind tolerance)");
                        score += 1;
                    }
                    if (riskLower.contains("drought") && variety.getDroughtTolerance() != null &&
                            variety.getDroughtTolerance().contains("Good")) {
                        matchReasons.add("✓ Good drought tolerance documented");
                        score += 1;
                    }
                    if (riskLower.contains("waterlog") && variety.getWaterloggingTolerance() != null) {
                        matchReasons.add("✓ Waterlogging tolerance documented");
                        score += 1;
                    }
                    if (riskLower.contains("heat") && variety.getHeatTolerance() != null) {
                        matchReasons.add("✓ Heat tolerance documented");
                        score += 1;
                    }
                    if (riskLower.contains("cold") && variety.getColdStressTolerance() != null) {
                        matchReasons.add("✓ Cold stress tolerance documented");
                        score += 1;
                    }
                }
            }

            if (score >= 3) {
                RecommendationItem item = new RecommendationItem();
                item.setVariety(variety);
                item.setSuitability(score >= 5 ? "HIGH" : "MODERATE");
                item.setMatchReasons(matchReasons);
                item.setReason("Matched " + matchReasons.size() + " criteria based on available agricultural evidence.");

                // Advantages
                List<String> advantages = new ArrayList<>();
                advantages.add("✓ Documented in official agricultural sources");
                if (variety.getLodgingResistance() != null && variety.getLodgingResistance().equals("Good")) {
                    advantages.add("✓ Good lodging resistance documented");
                }
                if (variety.getDroughtTolerance() != null && variety.getDroughtTolerance().contains("Good")) {
                    advantages.add("✓ Good drought tolerance documented");
                }
                if (variety.getSpecialTraits() != null) {
                    advantages.add("✓ " + variety.getSpecialTraits());
                }
                item.setAdvantages(advantages);

                // Trade-offs
                List<String> tradeOffs = new ArrayList<>();
                tradeOffs.add("⚠ Some characteristics may not be documented in available sources.");
                tradeOffs.add("⚠ Yield potential depends on actual field conditions and management.");
                tradeOffs.add("⚠ Recommendation based on available evidence; local expert advice recommended.");
                if (variety.getCategory() != null && variety.getCategory().equals("Composite")) {
                    tradeOffs.add("⚠ Composite varieties generally have lower yield potential than hybrids.");
                }
                item.setTradeOffs(tradeOffs);

                // Evidence
                List<RecommendationEvidence> evidenceList = new ArrayList<>();
                RecommendationEvidence evidence = new RecommendationEvidence();
                evidence.setSourceOrganization(variety.getSourceOrganization());
                evidence.setSourceDocument(variety.getSourceDocument());
                evidence.setSourceType(variety.getSourceType());
                evidence.setPdfPage(variety.getSourcePdfPage());
                evidence.setPrintedPage(variety.getSourcePrintedPage());
                evidence.setSourceText(variety.getSourceText());
                evidenceList.add(evidence);
                item.setEvidence(evidenceList);

                items.add(item);
            }
        }

        // Sort by suitability (HIGH first)
        items.sort((a, b) -> {
            if ("HIGH".equals(a.getSuitability()) && !"HIGH".equals(b.getSuitability())) return -1;
            if (!"HIGH".equals(a.getSuitability()) && "HIGH".equals(b.getSuitability())) return 1;
            return 0;
        });

        response.setDemoRecommendations(items);
        return response;
    }

    // ========================= SOYBEAN VARIETIES =========================

    public static List<MaizeVariety> getSoybeanVarieties() {
        List<MaizeVariety> list = new ArrayList<>();

        // Variety 1: JS 335
        MaizeVariety v1 = new MaizeVariety();
        v1.setVarietyId("SOY_VER_001");
        v1.setVarietyName("JS 335");
        v1.setCrop("Soybean");
        v1.setCategory("Certified Variety");
        v1.setType("High Yielding Pure Line");
        v1.setDeveloperOrCompany("JNKVV Jabalpur / MPKV Rahuri");
        v1.setReleaseYear("2013");
        v1.setSeason("Kharif & Summer");
        v1.setMaturityClass("Medium-Late");
        v1.setMaturityDaysMin(100);
        v1.setMaturityDaysMax(112);
        v1.setYieldValue("22-26 q/ha");
        v1.setYieldUnit("q/ha");
        v1.setYieldNormalizedMin(22.0);
        v1.setYieldNormalizedMax(26.0);
        v1.setRecommendedRegions("Madhya Pradesh, Maharashtra, Rajasthan, Karnataka, Telangana, Andhra Pradesh");
        v1.setAgroClimaticZone("Central Zone, Southern Zone");
        v1.setIrrigationCondition("Rainfed and Irrigated");
        v1.setPlantHeight("55-65 cm");
        v1.setLodgingResistance("Good");
        v1.setDroughtTolerance("Resistant");
        v1.setWaterloggingTolerance("Moderately Resistant");
        v1.setCharcoalRot("Moderately Resistant");
        v1.setStemBorer("Tolerant");
        v1.setGrainType("Spherical / Oval");
        v1.setGrainColour("Yellow");
        v1.setSpecialTraits("National benchmark variety, 21.9% oil content, 41.7% protein");
        v1.setSourceOrganization("ICAR-IISR, Indore");
        v1.setSourceDocument("AICRPS Research Bulletin & Seed Directory");
        v1.setSourceType("Research Bulletin");
        v1.setSourceText("JS 335 is the widely adapted national soybean standard with high oil and protein content, suitable for both rainfed and limited irrigation.");
        list.add(v1);

        // Variety 2: JS 93-05
        MaizeVariety v2 = new MaizeVariety();
        v2.setVarietyId("SOY_VER_002");
        v2.setVarietyName("JS 93-05");
        v2.setCrop("Soybean");
        v2.setCategory("Early Variety");
        v2.setType("Pure Line");
        v2.setDeveloperOrCompany("JNKVV, Jabalpur");
        v2.setReleaseYear("2002");
        v2.setSeason("Kharif");
        v2.setMaturityClass("Early");
        v2.setMaturityDaysMin(90);
        v2.setMaturityDaysMax(95);
        v2.setYieldValue("20-24 q/ha");
        v2.setYieldUnit("q/ha");
        v2.setYieldNormalizedMin(20.0);
        v2.setYieldNormalizedMax(24.0);
        v2.setRecommendedRegions("Madhya Pradesh, Maharashtra, Rajasthan, Gujarat");
        v2.setAgroClimaticZone("Central Zone");
        v2.setIrrigationCondition("Rainfed");
        v2.setPlantHeight("45-55 cm");
        v2.setLodgingResistance("Good");
        v2.setDroughtTolerance("Good");
        v2.setWaterloggingTolerance("Moderate");
        v2.setCharcoalRot("Resistant");
        v2.setStemBorer("Moderately Resistant");
        v2.setGrainType("Round");
        v2.setGrainColour("Yellow with light brown hilum");
        v2.setSpecialTraits("Early maturity fits drought-prone regions and enables timely wheat sowing");
        v2.setSourceOrganization("ICAR-IISR");
        v2.setSourceDocument("ICAR Soybean Production Technologies");
        v2.setSourceType("Technical Bulletin");
        v2.setSourceText("JS 93-05 is an early maturing soybean variety maturing in 90-95 days, highly drought resilient.");
        list.add(v2);

        // Variety 3: NRC 37 (Ahilya 4)
        MaizeVariety v3 = new MaizeVariety();
        v3.setVarietyId("SOY_VER_003");
        v3.setVarietyName("NRC 37 (Ahilya 4)");
        v3.setCrop("Soybean");
        v3.setCategory("Resistant Variety");
        v3.setType("Pure Line Selection");
        v3.setDeveloperOrCompany("ICAR - Indian Institute of Soybean Research (IISR), Indore");
        v3.setReleaseYear("2003");
        v3.setSeason("Kharif");
        v3.setMaturityClass("Medium");
        v3.setMaturityDaysMin(96);
        v3.setMaturityDaysMax(104);
        v3.setYieldValue("23-28 q/ha");
        v3.setYieldUnit("q/ha");
        v3.setYieldNormalizedMin(23.0);
        v3.setYieldNormalizedMax(28.0);
        v3.setRecommendedRegions("Madhya Pradesh, Maharashtra, Rajasthan, Bundelkhand");
        v3.setAgroClimaticZone("Central Zone");
        v3.setIrrigationCondition("Rainfed and Irrigated");
        v3.setPlantHeight("50-60 cm");
        v3.setLodgingResistance("Good");
        v3.setDroughtTolerance("Moderate");
        v3.setWaterloggingTolerance("Good");
        v3.setCharcoalRot("Highly Resistant");
        v3.setStemBorer("Resistant");
        v3.setGrainType("Oval");
        v3.setGrainColour("Yellow");
        v3.setSpecialTraits("Resistant to charcoal rot, yellow mosaic virus, and stem fly");
        v3.setSourceOrganization("ICAR-IISR, Indore");
        v3.setSourceDocument("ICAR-IISR Variety Release Bulletin");
        v3.setSourceType("Bulletin");
        v3.setSourceText("NRC 37 exhibits multiple pest and disease resistances, ideal for Central Indian soybean tracts.");
        list.add(v3);

        return list;
    }

    private static boolean matchSoybeanState(String requestedState, String recommendedRegions, String agroClimaticZone) {
        if (requestedState == null || requestedState.isBlank()) return true;
        String reg = ((recommendedRegions != null ? recommendedRegions.toLowerCase() : "") + " " +
                      (agroClimaticZone != null ? agroClimaticZone.toLowerCase() : "")).toLowerCase();
        if (reg.contains("all india")) return true;
        String s = requestedState.trim().toLowerCase();
        if (reg.contains(s)) return true;
        switch (s) {
            case "madhya pradesh": case "mp":
            case "maharashtra": case "mh":
            case "rajasthan": case "rj":
            case "gujarat": case "gj":
            case "chhattisgarh": case "cg":
                return reg.contains("cz") || reg.contains("central") || reg.contains("mp") || reg.contains("mh");
            case "karnataka": case "ka":
            case "andhra pradesh": case "ap":
            case "telangana": case "ts": case "tg":
            case "tamil nadu": case "tn":
                return reg.contains("sz") || reg.contains("southern") || reg.contains("ka") || reg.contains("ap");
            case "punjab": case "pb":
            case "haryana": case "hr":
            case "uttar pradesh": case "up":
            case "uttarakhand": case "uk":
                return reg.contains("npz") || reg.contains("northern plain");
            case "bihar": case "br":
            case "jharkhand": case "jh":
            case "west bengal": case "wb":
            case "assam": case "as":
            case "odisha": case "od":
                return reg.contains("nez") || reg.contains("north eastern");
            default:
                return false;
        }
    }

    public static RecommendationResponse getDemoSoybeanRecommendations(MaizeRecommendationRequest request) {
        RecommendationResponse response = new RecommendationResponse();
        response.setRequest(request);

        List<MaizeVariety> allVarieties = getSoybeanVarieties();
        List<RecommendationItem> items = new ArrayList<>();

        for (MaizeVariety variety : allVarieties) {
            List<String> matchReasons = new ArrayList<>();
            int score = 0;

            // Season match (tolerant of Kharif/Summer/Spring)
            if (request.getSeason() != null && !request.getSeason().isBlank()) {
                String reqSeason = request.getSeason().toLowerCase();
                String varSeason = variety.getSeason() != null ? variety.getSeason().toLowerCase() : "";
                if (reqSeason.contains("kharif") && varSeason.contains("kharif")) {
                    matchReasons.add("✓ Matches Kharif season cultivation");
                    score += 2;
                } else if ((reqSeason.contains("summer") || reqSeason.contains("spring")) &&
                           (varSeason.contains("summer") || varSeason.contains("spring"))) {
                    matchReasons.add("✓ Suitable for Summer / Spring season");
                    score += 2;
                } else {
                    score += 1;
                }
            } else {
                score += 1;
            }

            // Maturity match with 10-day tolerance
            if (variety.getMaturityDaysMax() != null && request.getMaximumMaturityDays() > 0) {
                if (variety.getMaturityDaysMax() <= request.getMaximumMaturityDays() + 10) {
                    matchReasons.add("✓ Fits maturity duration (≤" + request.getMaximumMaturityDays() + " days)");
                    score += 2;
                }
            } else {
                score += 1;
            }

            // State & Zone match (Tiered scoring)
            if (request.getState() != null && !request.getState().isBlank()) {
                if (matchSoybeanState(request.getState(), variety.getRecommendedRegions(), variety.getAgroClimaticZone())) {
                    matchReasons.add("✓ High regional adaptability for " + request.getState() + " (" +
                            (variety.getAgroClimaticZone() != null ? variety.getAgroClimaticZone() : "Recommended") + ")");
                    score += 4;
                }
            } else {
                score += 2;
            }

            // Yield potential score
            if (variety.getYieldValue() != null) {
                matchReasons.add("✓ Potential yield: " + variety.getYieldValue());
                score += 2;
                if (request.getPriority() != null && request.getPriority().toLowerCase().contains("yield")) {
                    score += 2; // Priority bonus
                }
            }

            // Soil compatibility
            if (request.getSoilType() != null && !request.getSoilType().isBlank()) {
                String soil = request.getSoilType().toLowerCase();
                if (soil.contains("black")) {
                    matchReasons.add("✓ Well suited for Black Cotton Soil with lodging resistance");
                    score += 2;
                } else if (soil.contains("red")) {
                    matchReasons.add("✓ Suitable for Red Soil with good root penetration");
                    score += 2;
                }
            }

            if (variety.getIrrigationCondition() != null && request.getWaterAvailability() != null) {
                String irrigation = variety.getIrrigationCondition().toLowerCase();
                String water = request.getWaterAvailability().toLowerCase();
                if ((water.contains("rainfed") && irrigation.contains("rainfed")) ||
                    (water.contains("irrigat") && irrigation.contains("irrigat"))) {
                    matchReasons.add("✓ Suitable for " + request.getWaterAvailability() + " conditions");
                    score += 2;
                }
            }

            if (request.getClimateRisks() != null) {
                for (String risk : request.getClimateRisks()) {
                    String riskLower = risk.toLowerCase();
                    if (riskLower.contains("drought") && variety.getDroughtTolerance() != null &&
                            variety.getDroughtTolerance().toLowerCase().contains("resistant")) {
                        matchReasons.add("✓ Documented drought resilience");
                        score += 3;
                    }
                    if (riskLower.contains("disease") && variety.getCharcoalRot() != null &&
                            variety.getCharcoalRot().toLowerCase().contains("resistant")) {
                        matchReasons.add("✓ Charcoal rot resistance documented");
                        score += 3;
                    }
                }
            }

            if (score >= 2 || items.size() < 3) {
                RecommendationItem item = new RecommendationItem();
                item.setVariety(variety);
                item.setSuitability(score >= 4 ? "HIGH" : "MODERATE");
                item.setReason("Selected based on agro-climatic zone suitability and documented ICAR-IISR traits for " +
                        (request.getState() != null ? request.getState() : "your region") + ".");
                item.setMatchReasons(matchReasons);

                List<String> advantages = new ArrayList<>();
                if (variety.getSpecialTraits() != null) {
                    advantages.add("✓ " + variety.getSpecialTraits());
                }
                if (variety.getYieldValue() != null) {
                    advantages.add("✓ Yield: " + variety.getYieldValue());
                }
                item.setAdvantages(advantages);

                List<String> tradeOffs = new ArrayList<>();
                tradeOffs.add("Ensure certified seed germination test before sowing.");
                item.setTradeOffs(tradeOffs);

                RecommendationEvidence evidence = new RecommendationEvidence();
                evidence.setSourceOrganization(variety.getSourceOrganization());
                evidence.setSourceDocument(variety.getSourceDocument());
                evidence.setSourceType(variety.getSourceType());
                evidence.setSourceText(variety.getSourceText());
                item.setEvidence(Arrays.asList(evidence));

                items.add(item);
            }
        }

        items.sort((a, b) -> {
            if ("HIGH".equals(a.getSuitability()) && !"HIGH".equals(b.getSuitability())) return -1;
            if (!"HIGH".equals(a.getSuitability()) && "HIGH".equals(b.getSuitability())) return 1;
            return 0;
        });

        response.setDemoRecommendations(items);
        return response;
    }

    // ========================= COTTON VARIETIES =========================

    public static List<MaizeVariety> getCottonVarieties() {
        List<MaizeVariety> list = new ArrayList<>();

        // Variety 1: H 4
        MaizeVariety v1 = new MaizeVariety();
        v1.setVarietyId("COT_VER_001");
        v1.setVarietyName("H 4");
        v1.setCrop("Cotton");
        v1.setCategory("Bt Cotton Hybrid");
        v1.setType("Intra-hirsutum Hybrid");
        v1.setDeveloperOrCompany("MPKV Rahuri / ICAR-CICR");
        v1.setReleaseYear("2005");
        v1.setSeason("Kharif (Irri/Rainfed)");
        v1.setMaturityClass("Medium");
        v1.setMaturityDaysMin(160);
        v1.setMaturityDaysMax(177);
        v1.setYieldValue("15.3-20.0 q/ha");
        v1.setRecommendedRegions("Southern Zone (AP, TS, TN, KA), Central Zone (MH, GJ, MP)");
        v1.setAgroClimaticZone("SZ, CZ");
        v1.setIrrigationCondition("Irrigated & Rainfed");
        v1.setGrainType("Staple Length 27mm");
        v1.setGrainColour("White");
        v1.setSpecialTraits("Ginning outturn 39%, Micronaire 4.4. Tolerant to drought, resistant to bollworm.");
        v1.setDroughtTolerance("Tolerant");
        v1.setPinkStemBorer("Resistant");
        v1.setStemBorer("Resistant");
        v1.setSourceDocument("ICAR-CICR Technical Bulletin");
        v1.setSourceType("Technical Bulletin");
        v1.setSourceOrganization("ICAR-CICR Nagpur");
        v1.setSourceText("H 4 is a widely adapted benchmark cotton hybrid in India with high boll weight, excellent ginning outturn, and drought resilience.");
        list.add(v1);

        // Variety 2: CICR Bt-1
        MaizeVariety v2 = new MaizeVariety();
        v2.setVarietyId("COT_VER_002");
        v2.setVarietyName("CICR Bt-1");
        v2.setCrop("Cotton");
        v2.setCategory("Bt Cotton Variety");
        v2.setType("Straight Variety (Non-Hybrid Bt)");
        v2.setDeveloperOrCompany("ICAR-CICR Nagpur");
        v2.setReleaseYear("2016");
        v2.setSeason("Kharif (Rainfed/Irrigated)");
        v2.setMaturityClass("Medium");
        v2.setMaturityDaysMin(150);
        v2.setMaturityDaysMax(165);
        v2.setYieldValue("18.0-24.5 q/ha");
        v2.setRecommendedRegions("Central Zone (Maharashtra, Gujarat, Madhya Pradesh)");
        v2.setAgroClimaticZone("CZ");
        v2.setIrrigationCondition("Rainfed / Irrigated");
        v2.setGrainType("Medium Long Staple (26.5mm)");
        v2.setGrainColour("White");
        v2.setSpecialTraits("Bt Cry1Ac gene protection, lower seed cost than hybrids, resistant to American bollworm.");
        v2.setDroughtTolerance("Good");
        v2.setPinkStemBorer("Highly Resistant");
        v2.setSourceDocument("CICR Research Highlights Bulletin");
        v2.setSourceType("Research Bulletin");
        v2.setSourceOrganization("ICAR-CICR");
        v2.setSourceText("CICR Bt-1 is an indigenous straight Bt variety that allows farmers to produce certified seed, reducing input costs while delivering high yields.");
        list.add(v2);

        // Variety 3: Suraj
        MaizeVariety v3 = new MaizeVariety();
        v3.setVarietyId("COT_VER_003");
        v3.setVarietyName("Suraj");
        v3.setCrop("Cotton");
        v3.setCategory("Non-Bt High Quality Variety");
        v3.setType("G. hirsutum Selection");
        v3.setDeveloperOrCompany("ICAR-CICR Regional Station Coimbatore");
        v3.setReleaseYear("2011");
        v3.setSeason("Kharif / Winter-Summer");
        v3.setMaturityClass("Early-Medium");
        v3.setMaturityDaysMin(145);
        v3.setMaturityDaysMax(160);
        v3.setYieldValue("16.0-22.0 q/ha");
        v3.setRecommendedRegions("Southern Zone (TN, AP, KA), Central Zone (MH, MP)");
        v3.setAgroClimaticZone("SZ, CZ");
        v3.setIrrigationCondition("Protective Irrigated");
        v3.setGrainType("Long Staple (30-31mm)");
        v3.setGrainColour("Creamy White");
        v3.setSpecialTraits("Extra long staple, spinning count 40-50s, premium market price, bacterial blight resistant.");
        v3.setDroughtTolerance("Moderate");
        v3.setTurcicumLeafBlight("Resistant");
        v3.setSourceDocument("ICAR-CICR Coimbatore Variety Release");
        v3.setSourceType("Variety Catalog");
        v3.setSourceOrganization("ICAR-CICR Coimbatore");
        v3.setSourceText("Suraj produces superior textile-grade fiber with high strength and 30mm staple length, commanding premium market prices.");
        list.add(v3);

        return list;
    }

    private static boolean matchCottonState(String requestedState, String recommendedRegions, String agroClimaticZone) {
        if (requestedState == null || requestedState.isBlank()) return true;
        String reg = ((recommendedRegions != null ? recommendedRegions.toLowerCase() : "") + " " +
                      (agroClimaticZone != null ? agroClimaticZone.toLowerCase() : "")).toLowerCase();
        if (reg.contains("all india")) return true;
        String s = requestedState.trim().toLowerCase();
        if (reg.contains(s)) return true;

        switch (s) {
            case "maharashtra":
            case "gujarat":
            case "madhya pradesh":
            case "odisha":
                return reg.contains("cz") || reg.contains("central");
            case "andhra pradesh":
            case "telangana":
            case "tamil nadu":
            case "karnataka":
                return reg.contains("sz") || reg.contains("southern") || reg.contains("south");
            case "punjab":
            case "haryana":
            case "rajasthan":
                return reg.contains("nz") || reg.contains("northern") || reg.contains("north");
            default:
                return true;
        }
    }

    public static RecommendationResponse getDemoCottonRecommendations(MaizeRecommendationRequest request) {
        RecommendationResponse response = new RecommendationResponse();
        response.setRequest(request);

        List<MaizeVariety> allVarieties = getCottonVarieties();
        List<RecommendationItem> items = new ArrayList<>();

        for (MaizeVariety variety : allVarieties) {
            List<String> matchReasons = new ArrayList<>();
            int score = 0;

            // Season match
            if (request.getSeason() != null && !request.getSeason().isBlank()) {
                String reqSeason = request.getSeason().toLowerCase();
                String varSeason = variety.getSeason() != null ? variety.getSeason().toLowerCase() : "";
                if (reqSeason.contains("kharif") && varSeason.contains("kharif")) {
                    matchReasons.add("✓ Matches Kharif season cotton cultivation");
                    score += 2;
                } else {
                    score += 1;
                }
            } else {
                score += 1;
            }

            // Maturity match with 15-day tolerance for cotton
            if (variety.getMaturityDaysMax() != null && request.getMaximumMaturityDays() > 0) {
                if (variety.getMaturityDaysMax() <= request.getMaximumMaturityDays() + 15) {
                    matchReasons.add("✓ Fits cotton maturity cycle (≤" + request.getMaximumMaturityDays() + " days)");
                    score += 2;
                }
            } else {
                score += 1;
            }

            // State & Zone match
            if (request.getState() != null && !request.getState().isBlank()) {
                if (matchCottonState(request.getState(), variety.getRecommendedRegions(), variety.getAgroClimaticZone())) {
                    matchReasons.add("✓ High regional adaptability for " + request.getState() + " (" +
                            (variety.getAgroClimaticZone() != null ? variety.getAgroClimaticZone() : "ICAR Recommended") + ")");
                    score += 4;
                }
            } else {
                score += 2;
            }

            // Yield potential
            if (variety.getYieldValue() != null) {
                matchReasons.add("✓ Potential yield: " + variety.getYieldValue());
                score += 2;
                if (request.getPriority() != null && request.getPriority().toLowerCase().contains("yield")) {
                    score += 2;
                }
            }

            // Soil suitability: Black Cotton Soil
            if (request.getSoilType() != null && !request.getSoilType().isBlank()) {
                String soil = request.getSoilType().toLowerCase();
                if (soil.contains("black") || soil.contains("regur")) {
                    matchReasons.add("✓ Thrives in Black Cotton Soil with optimal moisture retention");
                    score += 3;
                } else {
                    score += 1;
                }
            }

            // Water / irrigation
            if (variety.getIrrigationCondition() != null && request.getWaterAvailability() != null) {
                String irrigation = variety.getIrrigationCondition().toLowerCase();
                String water = request.getWaterAvailability().toLowerCase();
                if ((water.contains("rainfed") && irrigation.contains("rainfed")) ||
                    (water.contains("irrigat") && irrigation.contains("irrigat"))) {
                    matchReasons.add("✓ Suitable for " + request.getWaterAvailability() + " conditions");
                    score += 2;
                }
            }

            // Pest & fiber bonuses
            if (variety.getCategory() != null && variety.getCategory().toLowerCase().contains("bt")) {
                matchReasons.add("✓ Bt bollworm resilience");
                score += 2;
            }
            if (variety.getGrainType() != null) {
                matchReasons.add("✓ Quality Fiber: " + variety.getGrainType());
                score += 1;
            }

            if (score >= 2 || items.size() < 3) {
                RecommendationItem item = new RecommendationItem();
                item.setVariety(variety);
                item.setSuitability(score >= 4 ? "HIGH" : "MODERATE");
                item.setReason("Selected based on agro-climatic zone suitability and documented ICAR-CICR traits for " +
                        (request.getState() != null ? request.getState() : "your region") + ".");
                item.setMatchReasons(matchReasons);

                List<String> advantages = new ArrayList<>();
                if (variety.getSpecialTraits() != null) {
                    advantages.add("✓ " + variety.getSpecialTraits());
                }
                if (variety.getYieldValue() != null) {
                    advantages.add("✓ Yield: " + variety.getYieldValue());
                }
                if (variety.getGrainType() != null) {
                    advantages.add("✓ Fiber: " + variety.getGrainType());
                }
                item.setAdvantages(advantages);

                List<String> tradeOffs = new ArrayList<>();
                tradeOffs.add("Monitor for sucking pests (whitefly, jassids) during vegetative phase.");
                item.setTradeOffs(tradeOffs);

                RecommendationEvidence evidence = new RecommendationEvidence();
                evidence.setSourceOrganization(variety.getSourceOrganization());
                evidence.setSourceDocument(variety.getSourceDocument());
                evidence.setSourceType(variety.getSourceType());
                evidence.setSourceText(variety.getSourceText());
                item.setEvidence(Arrays.asList(evidence));

                items.add(item);
            }
        }

        items.sort((a, b) -> {
            if ("HIGH".equals(a.getSuitability()) && !"HIGH".equals(b.getSuitability())) return -1;
            if (!"HIGH".equals(a.getSuitability()) && "HIGH".equals(b.getSuitability())) return 1;
            return 0;
        });

        response.setDemoRecommendations(items);
        return response;
    }

    // ========================= WEATHER =========================

    public static WeatherInfo getDemoWeather() {
        WeatherInfo w = new WeatherInfo(29, 68, 40, 18, "Partly Cloudy", true);
        w.setFeelsLike(32.0);
        w.setMaxTemp(34.0);
        w.setMinTemp(24.0);
        w.setUvIndex(6.5);
        w.setPressureMb(1013.0);
        w.setVisibilityKm(10.0);
        w.setWindDir("ENE");
        w.setLocation("Kurnool, Andhra Pradesh");
        w.setState("Andhra Pradesh");
        w.setDistrict("Kurnool");

        // 24-hour demo hourly items
        List<WeatherInfo.HourlyForecast> hourly = new ArrayList<>();
        String[] hours = {"00:00", "03:00", "06:00", "09:00", "12:00", "15:00", "18:00", "21:00"};
        double[] temps = {25.0, 24.0, 26.0, 30.0, 33.5, 34.0, 31.0, 27.5};
        int[] rain = {10, 5, 15, 20, 35, 40, 25, 10};
        String[] conds = {"Clear", "Clear", "Sunny", "Sunny", "Partly Cloudy", "Scattered Clouds", "Cloudy", "Clear"};
        for (int i = 0; i < hours.length; i++) {
            hourly.add(new WeatherInfo.HourlyForecast(hours[i], temps[i], rain[i], conds[i], ""));
        }
        w.setHourlyForecast(hourly);

        // 10-day demo forecast items
        List<WeatherInfo.DailyForecast> daily = new ArrayList<>();
        String[] days = {"Today", "Fri", "Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        String[] dates = {"Oct 08", "Oct 09", "Oct 10", "Oct 11", "Oct 12", "Oct 13", "Oct 14", "Oct 15", "Oct 16", "Oct 17"};
        double[] maxs = {34.0, 35.0, 33.5, 32.0, 33.0, 34.5, 35.0, 34.0, 33.0, 32.5};
        double[] mins = {24.0, 24.5, 23.0, 22.5, 23.5, 24.0, 24.5, 23.8, 23.0, 22.8};
        int[] dailyRain = {40, 20, 65, 80, 45, 10, 5, 15, 30, 50};
        String[] dayConds = {"Partly Cloudy", "Sunny", "Rain Possible", "Heavy Rain", "Scattered Showers", "Sunny", "Clear", "Partly Cloudy", "Cloudy", "Rain Possible"};

        for (int i = 0; i < days.length; i++) {
            daily.add(new WeatherInfo.DailyForecast(dates[i], days[i], maxs[i], mins[i], dailyRain[i], 65, 15.0, dayConds[i], ""));
        }
        w.setDailyForecast(daily);

        return w;
    }

    // ========================= NOTIFICATIONS =========================

    public static List<NotificationItem> getDemoNotifications() {
        List<NotificationItem> notifications = new ArrayList<>();
        notifications.add(new NotificationItem("N001",
                "🌧 Weather Alert",
                "Heavy rainfall may occur in your region. Protect your maize crop.",
                "weather", "🌧", "2 hours ago"));
        notifications.add(new NotificationItem("N002",
                "🌱 Crop Reminder",
                "Your crop has reached the next monitoring stage. Check field conditions.",
                "crop", "🌱", "5 hours ago"));
        notifications.add(new NotificationItem("N003",
                "⚠ Disease Monitoring",
                "Check your maize field for symptoms of Turcicum Leaf Blight.",
                "disease", "⚠", "1 day ago"));
        notifications.add(new NotificationItem("N004",
                "🧪 Fertilizer Advisory",
                "Consider top dressing of nitrogen at the vegetative stage.",
                "fertilizer", "🧪", "2 days ago"));
        notifications.add(new NotificationItem("N005",
                "📢 System Update",
                "Farmer-Aware application has been updated with new maize varieties.",
                "system", "📢", "3 days ago"));
        return notifications;
    }

    // ========================= CROP CALENDAR =========================

    public static List<CropCalendarStage> getDemoCropCalendar() {
        List<CropCalendarStage> stages = new ArrayList<>();
        stages.add(new CropCalendarStage(0, "Sowing", "Prepare field and sow seeds at recommended spacing.", "🌱", false));
        stages.add(new CropCalendarStage(15, "Early Growth", "Monitor seedling emergence and perform gap filling.", "🌿", false));
        stages.add(new CropCalendarStage(35, "Crop Management", "Apply recommended fertilizer and weed management.", "🧪", true));
        stages.add(new CropCalendarStage(50, "Vegetative Stage", "Monitor plant growth, check for pest and disease symptoms.", "🌿", false));
        stages.add(new CropCalendarStage(65, "Tasseling", "Critical period for water. Monitor pollination.", "🌽", false));
        stages.add(new CropCalendarStage(80, "Grain Development", "Monitor grain filling. Check for ear rot symptoms.", "🌽", false));
        stages.add(new CropCalendarStage(100, "Harvest", "Harvest when grain moisture reaches 20-25%. Dry properly.", "🚜", false));
        return stages;
    }

    // ========================= CROP ADVISORY =========================

    public static List<CropAdvisory> getDemoAdvisories() {
        List<CropAdvisory> advisories = new ArrayList<>();
        advisories.add(new CropAdvisory("A001", "🌱 Crop Monitoring",
                "Monitor your maize crop for proper growth and development. Check for any abnormalities.",
                "monitoring", "🌱", "Today", "Medium"));
        advisories.add(new CropAdvisory("A002", "💧 Irrigation",
                "Ensure adequate soil moisture during the vegetative growth stage. Avoid waterlogging.",
                "irrigation", "💧", "Today", "High"));
        advisories.add(new CropAdvisory("A003", "🧪 Fertilizer",
                "Consider split application of nitrogen. Do not apply unverified dosage.",
                "fertilizer", "🧪", "This week", "Medium"));
        advisories.add(new CropAdvisory("A004", "🐛 Pest Monitoring",
                "Check for Fall Armyworm damage. Look for windowed leaves and frass.",
                "pest", "🐛", "This week", "High"));
        advisories.add(new CropAdvisory("A005", "🌿 Disease Monitoring",
                "Watch for leaf blight symptoms. Report any unusual spots or lesions.",
                "disease", "🌿", "This week", "Medium"));
        advisories.add(new CropAdvisory("A006", "🚜 Harvest Preparation",
                "Plan harvest logistics. Arrange for drying and storage facilities.",
                "harvest", "🚜", "Next month", "Low"));
        return advisories;
    }

    // ========================= DISEASE KNOWLEDGE =========================

    public static List<String[]> getDiseaseKnowledge() {
        List<String[]> diseases = new ArrayList<>();
        // {name, symptoms, management, source}
        diseases.add(new String[]{"Fall Armyworm",
                "Windowed leaves, extensive defoliation, frass in whorl, larvae in whorl and ears.",
                "Early detection through pheromone traps. Consult local agricultural officer for management options.",
                "ICAR-IIMR Advisory"});
        diseases.add(new String[]{"Stem Borer",
                "Dead heart in young plants, stem tunneling, broken tassels, reduced ear filling.",
                "Use of resistant varieties. Consult local extension services for integrated pest management.",
                "ICAR-IIMR Advisory"});
        diseases.add(new String[]{"Turcicum Leaf Blight",
                "Long elliptical grey-green to tan lesions on leaves, starting from lower leaves.",
                "Use resistant varieties. Remove crop residue. Consult experts for fungicide recommendations.",
                "ICAR-IIMR Plant Pathology"});
        diseases.add(new String[]{"Downy Mildew",
                "Chlorotic streaks on leaves, white downy growth on lower leaf surface, stunted growth.",
                "Use resistant varieties and treated seeds. Avoid continuous maize cropping.",
                "ICAR-IIMR Plant Pathology"});
        diseases.add(new String[]{"Maydis Leaf Blight",
                "Small diamond-shaped tan lesions on leaves, premature drying in severe cases.",
                "Use resistant varieties. Remove and destroy infected plant debris.",
                "ICAR-IIMR Plant Pathology"});
        return diseases;
    }

    // ========================= STATES & DISTRICTS =========================

    public static List<String> getStates() {
        return Arrays.asList(
                "Andhra Pradesh", "Bihar", "Gujarat", "Haryana",
                "Himachal Pradesh", "Karnataka", "Madhya Pradesh",
                "Maharashtra", "Punjab", "Rajasthan", "Tamil Nadu",
                "Telangana", "Uttar Pradesh", "Uttarakhand"
        );
    }

    public static List<String> getDistricts(String state) {
        switch (state) {
            case "Andhra Pradesh":
                return Arrays.asList("Anantapur", "Chittoor", "East Godavari",
                        "Guntur", "Krishna", "Kurnool", "Prakasam",
                        "Srikakulam", "Visakhapatnam", "West Godavari");
            case "Telangana":
                return Arrays.asList("Adilabad", "Karimnagar", "Khammam",
                        "Mahabubnagar", "Medak", "Nalgonda", "Nizamabad",
                        "Rangareddy", "Warangal");
            case "Karnataka":
                return Arrays.asList("Bagalkot", "Belgaum", "Bellary",
                        "Bidar", "Bijapur", "Davangere", "Dharwad",
                        "Gulbarga", "Haveri", "Raichur");
            case "Tamil Nadu":
                return Arrays.asList("Coimbatore", "Dharmapuri", "Dindigul",
                        "Erode", "Madurai", "Perambalur", "Salem",
                        "Tirunelveli", "Tiruchirappalli", "Villupuram");
            case "Maharashtra":
                return Arrays.asList("Ahmednagar", "Aurangabad", "Jalgaon",
                        "Kolhapur", "Nashik", "Pune", "Sangli",
                        "Satara", "Solapur");
            default:
                return Arrays.asList("District 1", "District 2", "District 3");
        }
    }
}
