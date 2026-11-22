import re
from typing import Optional, Dict, Any


# ---------------------------------------------------------
# Supported states
# ---------------------------------------------------------

STATES = [
    "Andhra Pradesh",
    "Telangana",
    "Tamil Nadu",
    "Karnataka",
    "Maharashtra",
    "Rajasthan",
    "Gujarat",
    "Madhya Pradesh",
    "Chhattisgarh",
    "Odisha",
    "Bihar",
    "Jharkhand",
    "West Bengal",
    "Uttar Pradesh",
]


# ---------------------------------------------------------
# Supported seasons
# ---------------------------------------------------------

SEASONS = [
    "Kharif",
    "Rabi"
]


# ---------------------------------------------------------
# Supported irrigation conditions
# ---------------------------------------------------------

IRRIGATION_TYPES = [
    "rainfed",
    "irrigated"
]


# ---------------------------------------------------------
# Extract state
# ---------------------------------------------------------

def extract_state(query: str) -> Optional[str]:

    query_lower = query.lower()

    for state in STATES:

        if state.lower() in query_lower:
            return state

    return None


# ---------------------------------------------------------
# Extract season
# ---------------------------------------------------------

def extract_season(query: str) -> Optional[str]:

    query_lower = query.lower()

    for season in SEASONS:

        if season.lower() in query_lower:
            return season

    return None


# ---------------------------------------------------------
# Extract irrigation
# ---------------------------------------------------------

def extract_irrigation(query: str) -> Optional[str]:

    query_lower = query.lower()

    # Check rainfed first because
    # "not irrigated" should not accidentally
    # become "irrigated".

    if "rainfed" in query_lower:
        return "rainfed"

    if "rain fed" in query_lower:
        return "rainfed"

    if "irrigated" in query_lower:
        return "irrigated"

    if "irrigation" in query_lower:
        return "irrigated"

    return None


# ---------------------------------------------------------
# Extract maximum maturity
# ---------------------------------------------------------

def extract_max_maturity(query: str) -> Optional[int]:

    patterns = [

        r"(?:within|under|below|less than|maximum|max|up to|not more than|no more than)"
        r"\s*(\d+)\s*(?:days|day)?",

        r"(\d+)\s*(?:days|day)"
        r"\s*(?:or less|maximum|max)"
    ]

    query_lower = query.lower()

    for pattern in patterns:

        match = re.search(
            pattern,
            query_lower
        )

        if match:
            return int(match.group(1))

    return None


# ---------------------------------------------------------
# Extract minimum maturity
# ---------------------------------------------------------

def extract_min_maturity(query: str) -> Optional[int]:

    patterns = [

        r"(?:at least|minimum|min)"
        r"\s*(\d+)\s*(?:days|day)?",

        r"(\d+)\s*(?:days|day)"
        r"\s*(?:or more|minimum|min)"
    ]

    query_lower = query.lower()

    for pattern in patterns:

        match = re.search(
            pattern,
            query_lower
        )

        if match:
            return int(match.group(1))

    return None


# ---------------------------------------------------------
# Parse farmer query
# ---------------------------------------------------------

def parse_farmer_query(
    query: str
) -> Dict[str, Any]:

    if not query or not query.strip():
        return {}

    return {
        "state": extract_state(query),

        "season": extract_season(query),

        "irrigation": extract_irrigation(query),

        "max_maturity_days":
            extract_max_maturity(query),

        "min_maturity_days":
            extract_min_maturity(query)
    }