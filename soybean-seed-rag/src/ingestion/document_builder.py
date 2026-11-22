"""
Soybean Document Builder
========================
Converts raw soybean variety CSV data into rich, structured entity-level documents
for RAG retrieval and LLM context augmentation.
"""

import csv
import json
import logging
import os
import re
from pathlib import Path
from typing import Any, Dict, List, Optional

logger = logging.getLogger(__name__)

# Agricultural zone to state mapping for Indian agro-climatic zones
ZONE_STATE_MAP = {
    "Northern Plain Zone": ["Uttar Pradesh", "Uttarakhand", "Punjab", "Haryana"],
    "North Plain Zone": ["Uttar Pradesh", "Uttarakhand"],
    "Southern Zone": ["Karnataka", "Andhra Pradesh", "Tamil Nadu", "Telangana"],
    "Central Zone": ["Madhya Pradesh", "Maharashtra", "Rajasthan", "Gujarat"],
    "North Eastern Zone": ["Bihar", "Jharkhand", "West Bengal", "Assam", "Odisha", "Chhattisgarh"],
    "Eastern Plateau": ["Jharkhand", "Bihar", "West Bengal", "Odisha"],
    "North Western Zone": ["Punjab", "Haryana"],
    "Central & North Eastern Zone": ["Madhya Pradesh", "Chhattisgarh", "Uttar Pradesh", "Bihar", "Assam", "West Bengal"],
    "CZ": ["Madhya Pradesh", "Maharashtra", "Rajasthan", "Gujarat"],
    "SZ": ["Karnataka", "Andhra Pradesh", "Tamil Nadu", "Telangana"],
    "NPZ": ["Uttar Pradesh", "Uttarakhand", "Punjab", "Haryana"],
    "NEZ": ["Bihar", "Jharkhand", "West Bengal", "Assam", "Odisha", "Chhattisgarh"],
}

# State abbreviation mapping
STATE_ABBR_MAP = {
    "UP": "Uttar Pradesh",
    "UK": "Uttarakhand",
    "Kar": "Karnataka",
    "KA": "Karnataka",
    "AP": "Andhra Pradesh",
    "TN": "Tamil Nadu",
    "MP": "Madhya Pradesh",
    "Maha": "Maharashtra",
    "MH": "Maharashtra",
    "Raj": "Rajasthan",
    "RJ": "Rajasthan",
    "PB": "Punjab",
    "HR": "Haryana",
}


def extract_states_from_text(text: str) -> List[str]:
    """Extract full state names from region/zone string."""
    states = set()
    if not text:
        return []

    # Check known zone maps
    for zone, zone_states in ZONE_STATE_MAP.items():
        if zone.lower() in text.lower():
            states.update(zone_states)

    # Check abbreviation patterns inside parentheses e.g. (MP, Maha, Raj)
    abbr_match = re.findall(r"\b([A-Za-z]+)\b", text)
    for token in abbr_match:
        if token in STATE_ABBR_MAP:
            states.add(STATE_ABBR_MAP[token])

    # Direct name checks
    all_known_states = [
        "Madhya Pradesh", "Maharashtra", "Rajasthan", "Karnataka",
        "Andhra Pradesh", "Tamil Nadu", "Telangana", "Uttar Pradesh",
        "Uttarakhand", "Punjab", "Haryana", "Bihar", "Jharkhand",
        "Gujarat", "Chhattisgarh", "West Bengal", "Odisha", "Assam",
    ]
    for s in all_known_states:
        if s.lower() in text.lower():
            states.add(s)

    return sorted(list(states))


def extract_percentages(text: str) -> Dict[str, Optional[float]]:
    """Extract oil and protein percentages from traits text."""
    res = {"oil_content_percent": None, "protein_content_percent": None}
    if not text:
        return res

    oil_m = re.search(r"Oil\s+content\s+([0-9.]+)\s*%", text, re.IGNORECASE)
    if oil_m:
        try:
            res["oil_content_percent"] = float(oil_m.group(1))
        except ValueError:
            pass

    protein_m = re.search(r"Protein\s+([0-9.]+)\s*%", text, re.IGNORECASE)
    if protein_m:
        try:
            res["protein_content_percent"] = float(protein_m.group(1))
        except ValueError:
            pass

    return res


def build_page_content(row: Dict[str, str], metadata: Dict[str, Any]) -> str:
    """
    Construct rich narrative text for the entity document.
    Ensures complete, unfragmented context for LLM reasoning.
    """
    variety_name = row.get("variety_name", "Unknown").strip()
    variety_id = row.get("variety_id", "").strip()
    category = row.get("category", "").strip()
    breeding_type = row.get("type", "").strip()
    developer = row.get("developer_or_company", "").strip()
    parents = row.get("parents", "").strip()
    release_year = row.get("release_year", "").strip()
    notification = row.get("gazette_notification", "").strip()
    
    season = row.get("season", "").strip()
    maturity_class = row.get("maturity_class", "").strip()
    mat_min = row.get("maturity_days_min", "").strip()
    mat_max = row.get("maturity_days_max", "").strip()
    
    yield_val = row.get("yield_value", "").strip()
    yield_norm_min = row.get("yield_normalized_q_ha_min", "").strip()
    yield_norm_max = row.get("yield_normalized_q_ha_max", "").strip()
    
    rec_regions = row.get("recommended_regions", "").strip()
    zone = row.get("agro_climatic_zone", "").strip()
    irrigation = row.get("irrigation_condition", "").strip()
    
    plant_h = row.get("plant_height", "").strip()
    ear_h = row.get("ear_height", "").strip()
    lodging = row.get("lodging_resistance", "").strip()
    canopy = row.get("leaf_orientation", "").strip()
    stay_green = row.get("stay_green", "").strip()
    
    drought = row.get("drought_tolerance", "").strip()
    waterlog = row.get("waterlogging_tolerance", "").strip()
    heat = row.get("heat_tolerance", "").strip()
    cold = row.get("cold_stress_tolerance", "").strip()
    
    insects = [
        f"Defoliator/Armyworm: {row.get('fall_armyworm', '').strip()}",
        f"Stem Borer / Fly: {row.get('stem_borer', '').strip()}",
        f"Girdle Beetle: {row.get('pink_stem_borer', '').strip()}",
    ]
    insect_str = "; ".join([i for i in insects if not i.endswith(": ")])
    
    diseases = [
        f"Leaf Blight/Spot: {row.get('turcicum_leaf_blight', '').strip()}",
        f"Anthracnose/Myrothecium: {row.get('maydis_leaf_blight', '').strip()}",
        f"Powdery Mildew/Cercospora: {row.get('curvularia_leaf_spot', '').strip()}",
        f"Aerial Blight: {row.get('banded_leaf_sheath_blight', '').strip()}",
        f"Charcoal/Collar Rot: {row.get('charcoal_rot', '').strip()}",
        f"Fusarium Wilt/Root Rot: {row.get('fusarium_stalk_rot', '').strip()}",
        f"Downy Mildew: {row.get('downy_mildew', '').strip()}",
    ]
    disease_str = "; ".join([d for d in diseases if not d.endswith(": ")])
    
    grain_type = row.get("grain_type", "").strip()
    grain_colour = row.get("grain_colour", "").strip()
    special_traits = row.get("special_traits", "").strip()
    source_doc = row.get("source_document", "").strip()
    source_org = row.get("source_organization", "").strip()

    states_str = ", ".join(metadata.get("states", [])) or "Specific agricultural zones"

    content = f"""Soybean Variety: {variety_name} (ID: {variety_id})
Crop: Soybean (Glycine max)
Category: {category} | Breeding Type: {breeding_type} | Developer: {developer}
Release Year: {release_year} | Gazette Notification: {notification}
Pedigree / Parentage: {parents}

AGRONOMIC SPECIFICATIONS:
- Suitable Season: {season}
- Maturity Class: {maturity_class} (Takes {mat_min}-{mat_max} days from sowing to harvest)
- Potential Yield: {yield_val} ({yield_norm_min} to {yield_norm_max} quintals per hectare normalized)
- Irrigation Condition: {irrigation}
- Recommended Regions: {rec_regions}
- Agro-Climatic Zone: {zone}
- Adapted States: {states_str}

MORPHOLOGY & GROWTH HABIT:
- Plant Height: {plant_h} | Pod Insertion: {ear_h}
- Lodging Resistance: {lodging}
- Canopy / Foliage: {canopy}
- Stay-Green Trait: {stay_green}

STRESS TOLERANCE:
- Drought Tolerance: {drought}
- Waterlogging Tolerance: {waterlog}
- Heat Tolerance: {heat}
- Cold / Spring Stress: {cold}

PEST & DISEASE RESISTANCE:
- Insect Tolerance: {insect_str}
- Disease Resistance: {disease_str}

GRAIN & QUALITY ATTRIBUTES:
- Seed Characteristics: {grain_type}, {grain_colour}
- Nutritional & Special Traits: {special_traits}

DOCUMENTATION & SOURCE:
- Source: {source_doc} ({source_org})
"""
    return content.strip()


def build_soybean_documents(
    csv_path: str,
    output_path: Optional[str] = None
) -> List[Dict[str, Any]]:
    """
    Build entity-level RAG documents from the soybean varieties master CSV.

    Args:
        csv_path: Path to raw CSV file.
        output_path: Optional path to save processed JSON file.

    Returns:
        List of document dictionaries.
    """
    if not os.path.exists(csv_path):
        raise FileNotFoundError(f"Source CSV file not found at: {csv_path}")

    documents = []
    with open(csv_path, mode="r", encoding="utf-8", errors="replace") as f:
        reader = csv.DictReader(f)
        for row in reader:
            variety_id = row.get("variety_id", "").strip()
            variety_name = row.get("variety_name", "").strip()
            if not variety_id or not variety_name:
                continue

            rec_regions = row.get("recommended_regions", "").strip()
            zone = row.get("agro_climatic_zone", "").strip()
            combined_region_text = f"{rec_regions} {zone}"
            states = extract_states_from_text(combined_region_text)

            season_raw = row.get("season", "").strip()
            seasons = [s.strip() for s in re.split(r"[/,&]", season_raw) if s.strip()]

            # Numeric parsers
            def parse_float(val: str) -> Optional[float]:
                try:
                    return float(val.strip())
                except (ValueError, TypeError):
                    return None

            def parse_int(val: str) -> Optional[int]:
                try:
                    return int(float(val.strip()))
                except (ValueError, TypeError):
                    return None

            mat_min = parse_float(row.get("maturity_days_min"))
            mat_max = parse_float(row.get("maturity_days_max"))
            yield_min = parse_float(row.get("yield_normalized_q_ha_min"))
            yield_max = parse_float(row.get("yield_normalized_q_ha_max"))
            release_yr = parse_int(row.get("release_year"))

            irrigation_condition = row.get("irrigation_condition", "").strip()
            irrigation_lower = irrigation_condition.lower()
            if "rainfed" in irrigation_lower and "irrigated" in irrigation_lower:
                irrigation_type = "both"
            elif "rainfed" in irrigation_lower:
                irrigation_type = "rainfed"
            elif "protective" in irrigation_lower or "limited" in irrigation_lower:
                irrigation_type = "protective"
            elif "irrigated" in irrigation_lower:
                irrigation_type = "irrigated"
            else:
                irrigation_type = "unknown"

            traits_text = row.get("special_traits", "")
            percentages = extract_percentages(traits_text)

            metadata = {
                "variety_id": variety_id,
                "variety_name": variety_name,
                "crop": "Soybean",
                "category": row.get("category", "").strip(),
                "type": row.get("type", "").strip(),
                "developer_or_company": row.get("developer_or_company", "").strip(),
                "release_year": release_yr,
                "gazette_notification": row.get("gazette_notification", "").strip(),
                "season": seasons,
                "season_raw": season_raw,
                "maturity_class": row.get("maturity_class", "").strip(),
                "maturity_days_min": mat_min,
                "maturity_days_max": mat_max,
                "yield_normalized_q_ha_min": yield_min,
                "yield_normalized_q_ha_max": yield_max,
                "yield_value": row.get("yield_value", "").strip(),
                "recommended_regions": [r.strip() for r in rec_regions.split(";") if r.strip()] if ";" in rec_regions else [rec_regions],
                "agro_climatic_zone": zone,
                "states": states,
                "irrigation_condition": irrigation_condition,
                "irrigation_type": irrigation_type,
                "plant_height": row.get("plant_height", "").strip(),
                "ear_height": row.get("ear_height", "").strip(),
                "lodging_resistance": row.get("lodging_resistance", "").strip(),
                "drought_tolerance": row.get("drought_tolerance", "").strip(),
                "waterlogging_tolerance": row.get("waterlogging_tolerance", "").strip(),
                "heat_tolerance": row.get("heat_tolerance", "").strip(),
                "cold_stress_tolerance": row.get("cold_stress_tolerance", "").strip(),
                "oil_content_percent": percentages["oil_content_percent"],
                "protein_content_percent": percentages["protein_content_percent"],
                "grain_type": row.get("grain_type", "").strip(),
                "grain_colour": row.get("grain_colour", "").strip(),
                "special_traits": traits_text.strip(),
                "source_document": row.get("source_document", "").strip(),
                "source_organization": row.get("source_organization", "").strip(),
                "source_url": row.get("source_url", "").strip(),
            }

            page_content = build_page_content(row, metadata)

            doc = {
                "id": variety_id,
                "variety_name": variety_name,
                "crop": "Soybean",
                "page_content": page_content,
                "metadata": metadata,
            }
            documents.append(doc)

    if output_path:
        os.makedirs(os.path.dirname(output_path), exist_ok=True)
        with open(output_path, "w", encoding="utf-8") as f:
            json.dump(documents, f, indent=2, ensure_ascii=False)
        logger.info(f"Saved {len(documents)} soybean documents to {output_path}")

    return documents
