import re
from typing import Optional, Dict, Any


COTTON_STATES = [
    # Southern Zone
    "Andhra Pradesh",
    "Telangana",
    "Tamil Nadu",
    "Karnataka",
    # Central Zone
    "Maharashtra",
    "Gujarat",
    "Madhya Pradesh",
    # Northern Zone
    "Punjab",
    "Haryana",
    "Rajasthan",
]

COTTON_ZONES = [
    "Northern Zone",
    "Central Zone",
    "Southern Zone",
    "North Zone",
    "South Zone",
]

COTTON_SEASONS = [
    "Kharif",
    "Rabi"
]


def extract_state(query: str) -> Optional[str]:
    """Extract Indian state or agro-climatic zone from query string."""
    query_lower = query.lower()

    # Direct state check
    for state in COTTON_STATES:
        if state.lower() in query_lower:
            return state

    # Abbreviation check
    abbrevs = {
        r"\bap\b": "Andhra Pradesh",
        r"\bts\b": "Telangana",
        r"\btg\b": "Telangana",
        r"\btn\b": "Tamil Nadu",
        r"\bka\b": "Karnataka",
        r"\bmh\b": "Maharashtra",
        r"\bgj\b": "Gujarat",
        r"\bmp\b": "Madhya Pradesh",
        r"\bpb\b": "Punjab",
        r"\bhr\b": "Haryana",
        r"\brj\b": "Rajasthan",
    }
    for pattern, state_name in abbrevs.items():
        if re.search(pattern, query_lower):
            return state_name

    # Zone check
    for zone in COTTON_ZONES:
        if zone.lower() in query_lower:
            return zone

    return None


def extract_season(query: str) -> Optional[str]:
    """Extract growing season from query string."""
    query_lower = query.lower()
    for season in COTTON_SEASONS:
        if season.lower() in query_lower:
            return season
    return None


def extract_irrigation(query: str) -> Optional[str]:
    """Extract irrigation condition constraint."""
    query_lower = query.lower()

    if "protective irrigate" in query_lower or "protective irrigation" in query_lower:
        return "protective irrigated"

    if "rainfed" in query_lower or "rain-fed" in query_lower or "rain fed" in query_lower or "dryland" in query_lower:
        return "rainfed"

    if "irrigated" in query_lower or "canal" in query_lower or "borewell" in query_lower:
        return "irrigated"

    return None


def extract_maturity(query: str) -> Dict[str, Optional[int]]:
    """
    Extract maximum and minimum maturity day constraints from natural language.
    Examples:
      - 'matures within 160 days' -> max=160
      - 'below 165 days' -> max=165
      - 'under 170 days' -> max=170
      - 'less than 160 days' -> max=160
      - 'between 150 and 165 days' -> min=150, max=165
    """
    query_lower = query.lower()
    max_days = None
    min_days = None

    # Range pattern: between X and Y days
    range_match = re.search(r"between\s+(\d+)\s*(?:and|to|-)\s*(\d+)\s*days?", query_lower)
    if range_match:
        min_days = int(range_match.group(1))
        max_days = int(range_match.group(2))
        return {"max_days": max_days, "min_days": min_days}

    # Max patterns
    max_patterns = [
        r"within\s+(\d+)\s*days?",
        r"below\s+(\d+)\s*days?",
        r"under\s+(\d+)\s*days?",
        r"less\s+than\s+(\d+)\s*days?",
        r"<=\s*(\d+)",
        r"<\s*(\d+)",
        r"maximum\s+(?:of\s+)?(\d+)\s*days?",
        r"matures?\s+in\s+(\d+)\s*days?",
    ]

    for pat in max_patterns:
        m = re.search(pat, query_lower)
        if m:
            max_days = int(m.group(1))
            break

    # Min patterns
    min_patterns = [
        r"more\s+than\s+(\d+)\s*days?",
        r"above\s+(\d+)\s*days?",
        r">=\s*(\d+)",
        r">\s*(\d+)",
        r"minimum\s+(?:of\s+)?(\d+)\s*days?",
    ]

    for pat in min_patterns:
        m = re.search(pat, query_lower)
        if m:
            min_days = int(m.group(1))
            break

    return {"max_days": max_days, "min_days": min_days}


def parse_farmer_query(query: str) -> Dict[str, Any]:
    """Parse raw farmer natural language query into structured metadata constraints."""
    state = extract_state(query)
    season = extract_season(query)
    irrigation = extract_irrigation(query)
    maturity = extract_maturity(query)

    return {
        "state": state,
        "season": season,
        "irrigation": irrigation,
        "max_maturity_days": maturity["max_days"],
        "min_maturity_days": maturity["min_days"],
    }
