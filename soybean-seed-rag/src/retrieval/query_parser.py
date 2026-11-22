"""
Query Parser for Farmer and Agronomist Requests
===============================================
Extracts structured constraints (state, season, irrigation, maturity range, traits)
from natural language queries using rule-based pattern matching and synonym expansion.
"""

import re
from typing import Any, Dict, List, Optional

# Indian states and agricultural zones aliases
STATE_PATTERNS = {
    "Maharashtra": [r"\bmaharashtra\b", r"\bmaha\b", r"\bmh\b", r"\bvidarbha\b", r"\bmarathwada\b"],
    "Madhya Pradesh": [r"\bmadhya\s+pradesh\b", r"\bmp\b", r"\bmalwa\b", r"\bcentral\s+india\b"],
    "Rajasthan": [r"\brajasthan\b", r"\braj\b", r"\brj\b", r"\bkota\b"],
    "Karnataka": [r"\bkarnataka\b", r"\bkar\b", r"\bka\b", r"\bdharwad\b", r"\bbelagavi\b"],
    "Andhra Pradesh": [r"\bandhra\s+pradesh\b", r"\bandhra\b", r"\bap\b"],
    "Telangana": [r"\btelangana\b", r"\bts\b", r"\bhyderabad\b"],
    "Tamil Nadu": [r"\btamil\s+nadu\b", r"\btn\b"],
    "Uttar Pradesh": [r"\buttar\s+pradesh\b", r"\bup\b"],
    "Uttarakhand": [r"\buttarakhand\b", r"\buk\b", r"\bpantnagar\b"],
    "Punjab": [r"\bpunjab\b", r"\bpb\b", r"\bludhiana\b"],
    "Haryana": [r"\bharyana\b", r"\bhr\b"],
    "Bihar": [r"\bbihar\b"],
    "Jharkhand": [r"\bjharkhand\b"],
    "Chhattisgarh": [r"\bchhattisgarh\b", r"\bcg\b"],
    "Gujarat": [r"\bgujarat\b", r"\bgj\b"],
    "West Bengal": [r"\bwest\s+bengal\b", r"\bwb\b"],
}

# Season aliases
SEASON_PATTERNS = {
    "Kharif": [r"\bkharif\b", r"\bmonsoon\b", r"\brainy\s+season\b", r"\bsummer\s+crop\b"],
    "Spring": [r"\bspring\b", r"\bzaid\b", r"\bsummer\b"],
    "Rabi": [r"\brabi\b", r"\bwinter\b"],
}

# Irrigation condition patterns
IRRIGATION_PATTERNS = {
    "rainfed": [
        r"\brainfed\b",
        r"\bwithout\s+irrigation\b",
        r"\bno\s+irrigation\b",
        r"\bdryland\b",
        r"\bbarani\b",
        r"\brain\s+dependent\b",
        r"\bwater\s+stressed\b",
    ],
    "irrigated": [
        r"\birrigated\b",
        r"\bwith\s+irrigation\b",
        r"\bassured\s+irrigation\b",
        r"\bfull\s+irrigation\b",
        r"\bcanal\s+water\b",
        r"\btubewell\b",
    ],
    "protective": [
        r"\bprotective\s+irrigation\b",
        r"\bcritical\s+irrigation\b",
        r"\blimited\s+irrigation\b",
        r"\blife\s+saving\s+irrigation\b",
    ],
}

# Trait extraction patterns
TRAIT_PATTERNS = {
    "high_protein": [r"\bhigh\s+protein\b", r"\bbiofortified\b", r"\bprotein\s+rich\b"],
    "high_oil": [r"\bhigh\s+oil\b", r"\boil\s+content\b", r"\boilseed\b"],
    "drought_tolerant": [r"\bdrought\b", r"\bwater\s+deficit\b", r"\bdry\s+spell\b"],
    "waterlogging_tolerant": [r"\bwaterlogg(?:ing|ed)\b", r"\bsubmergence\b", r"\bflood\b", r"\bheavy\s+soil\b"],
    "lodging_resistant": [r"\blodging\b", r"\bsturdy\s+stem\b", r"\berect\b"],
    "early_maturity": [r"\bearly\s+matur(?:ing|ity)\b", r"\bshort\s+duration\b", r"\bquick\b"],
    "late_maturity": [r"\blate\s+matur(?:ing|ity)\b", r"\blong\s+duration\b"],
    "charcoal_rot_resistant": [r"\bcharcoal\s+rot\b", r"\bcollar\s+rot\b", r"\broot\s+rot\b"],
    "insect_resistant": [r"\bstem\s+borer\b", r"\bgirdle\s+beetle\b", r"\bsemilooper\b", r"\barmyworm\b", r"\bdefoliator\b"],
}


def parse_farmer_query(query: str) -> Dict[str, Any]:
    """
    Parse a farmer's natural language query into structured agricultural filters.

    Args:
        query: Free-text query string.

    Returns:
        Dictionary containing extracted filter parameters:
        {
            'state': str or None,
            'season': str or None,
            'irrigation': str or None,
            'max_maturity': int or None,
            'min_maturity': int or None,
            'traits': list of str,
            'raw_query': str
        }
    """
    clean_query = query.strip()
    result = {
        "state": None,
        "season": None,
        "irrigation": None,
        "max_maturity": None,
        "min_maturity": None,
        "traits": [],
        "raw_query": clean_query,
    }

    lower_query = clean_query.lower()

    # 1. State Extraction
    for state_name, patterns in STATE_PATTERNS.items():
        if any(re.search(pat, lower_query) for pat in patterns):
            result["state"] = state_name
            break

    # 2. Season Extraction
    for season_name, patterns in SEASON_PATTERNS.items():
        if any(re.search(pat, lower_query) for pat in patterns):
            result["season"] = season_name
            break

    # 3. Irrigation Extraction
    for irr_type, patterns in IRRIGATION_PATTERNS.items():
        if any(re.search(pat, lower_query) for pat in patterns):
            result["irrigation"] = irr_type
            break

    # 4. Maturity Days Extraction
    # Matches patterns like:
    # "within 90 days", "under 100 days", "less than 95 days", "<= 90 days", "max 90 days", "90 days or less"
    max_mat_matches = [
        re.search(r"(?:within|under|less\s+than|below|up\s+to|max(?:imum)?)\s*(\d{2,3})\s*(?:days)?", lower_query),
        re.search(r"(\d{2,3})\s*(?:days)?\s*(?:or\s+less|maximum|max)", lower_query),
        re.search(r"(?:<=|<)\s*(\d{2,3})", lower_query),
        re.search(r"in\s+(\d{2,3})\s+days", lower_query),
    ]
    for m in max_mat_matches:
        if m:
            val = int(m.group(1))
            if 70 <= val <= 160:  # Valid maturity range for soybean
                result["max_maturity"] = val
                break

    # Matches min maturity e.g.: "at least 90 days", "more than 100 days", ">= 95 days", "over 100 days"
    min_mat_matches = [
        re.search(r"(?:at\s+least|more\s+than|above|over|min(?:imum)?)\s*(\d{2,3})\s*(?:days)?", lower_query),
        re.search(r"(\d{2,3})\s*(?:days)?\s*(?:or\s+more|minimum|min)", lower_query),
        re.search(r"(?:>=|>)\s*(\d{2,3})", lower_query),
    ]
    for m in min_mat_matches:
        if m:
            val = int(m.group(1))
            if 70 <= val <= 160:
                result["min_maturity"] = val
                break

    # If user says "early maturing" without days, set default max maturity
    if "early_maturity" in [k for k, p in TRAIT_PATTERNS.items() if any(re.search(x, lower_query) for x in p)]:
        if result["max_maturity"] is None:
            result["max_maturity"] = 95  # Early soybean is <= 95 days

    # 5. Traits Extraction
    for trait_key, patterns in TRAIT_PATTERNS.items():
        if any(re.search(pat, lower_query) for pat in patterns):
            result["traits"].append(trait_key)

    return result
