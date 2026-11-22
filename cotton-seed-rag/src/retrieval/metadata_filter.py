import json
from pathlib import Path
from typing import Optional, List, Dict, Any


PROJECT_ROOT = Path(__file__).resolve().parent.parent.parent
DOCUMENT_FILE = PROJECT_ROOT / "data" / "processed" / "cotton_rag_documents.json"


# Indian Cotton Zone to State Mapping (ICAR-CICR / AICCIP)
STATE_TO_ZONE_MAP = {
    # Northern Zone
    "punjab": ["northern zone", "pb", "nz"],
    "pb": ["northern zone", "pb", "nz"],
    "haryana": ["northern zone", "hr", "nz"],
    "hr": ["northern zone", "hr", "nz"],
    "rajasthan": ["northern zone", "rj", "nz"],
    "rj": ["northern zone", "rj", "nz"],
    "north zone": ["northern zone", "pb", "hr", "rj", "nz"],
    "northern zone": ["northern zone", "pb", "hr", "rj", "nz"],
    "nz": ["northern zone", "pb", "hr", "rj", "nz"],

    # Southern Zone
    "andhra pradesh": ["southern zone", "ap", "sz"],
    "andhra": ["southern zone", "ap", "sz"],
    "ap": ["southern zone", "ap", "sz"],
    "telangana": ["southern zone", "ts", "tg", "sz"],
    "ts": ["southern zone", "ts", "tg", "sz"],
    "tamil nadu": ["southern zone", "tn", "sz"],
    "tamilnadu": ["southern zone", "tn", "sz"],
    "tn": ["southern zone", "tn", "sz"],
    "karnataka": ["southern zone", "ka", "sz"],
    "south zone": ["southern zone", "ap", "ts", "tn", "sz"],
    "southern zone": ["southern zone", "ap", "ts", "tn", "sz"],
    "sz": ["southern zone", "ap", "ts", "tn", "sz"],

    # Central Zone
    "gujarat": ["central zone", "gj", "cz"],
    "gj": ["central zone", "gj", "cz"],
    "maharashtra": ["central zone", "mh", "cz"],
    "mh": ["central zone", "mh", "cz"],
    "madhya pradesh": ["central zone", "mp", "cz"],
    "mp": ["central zone", "mp", "cz"],
    "central zone": ["central zone", "gj", "mh", "mp", "cz"],
    "cz": ["central zone", "gj", "mh", "mp", "cz"],
}


def load_documents() -> List[Dict[str, Any]]:
    """Load all cotton RAG documents from the processed JSON file."""
    if not DOCUMENT_FILE.exists():
        raise FileNotFoundError(
            f"Cotton document file not found at: {DOCUMENT_FILE}. "
            f"Please run scripts/build_documents.py first."
        )

    with open(DOCUMENT_FILE, "r", encoding="utf-8") as file:
        return json.load(file)


def normalize_text(value: Any) -> str:
    """Convert any value into normalized lowercase text."""
    if value is None:
        return ""
    return str(value).strip().lower()


def region_matches(recommended_regions: Optional[str], requested_state: Optional[str]) -> bool:
    """
    Check whether the requested state / agro-climatic zone is compatible
    with the recommended regions of the cotton variety.
    """
    if not requested_state:
        return True

    regions = normalize_text(recommended_regions)
    state = normalize_text(requested_state)

    if not regions:
        return False

    # Check for All India release
    if "all india" in regions:
        return True

    # Direct substring match
    if state in regions:
        return True

    # Zone mapping match
    if state in STATE_TO_ZONE_MAP:
        aliases = STATE_TO_ZONE_MAP[state]
        for alias in aliases:
            if alias in regions:
                return True

    return False


def season_matches(season: Optional[str], requested_season: Optional[str]) -> bool:
    """
    Check whether the requested growing season is supported by the variety.
    """
    if not requested_season:
        return True

    available_seasons = normalize_text(season)
    requested = normalize_text(requested_season)

    if not available_seasons:
        return False

    return requested in available_seasons


def get_irrigation_match_type(
    irrigation_condition: Optional[str],
    requested_irrigation: Optional[str]
) -> str:
    """
    Classify irrigation compatibility:
    - MATCH: Metadata explicitly supports the requested condition.
    - UNKNOWN: Missing or partial compatibility (e.g. protective irrigation under rainfed).
    - NO_MATCH: Explicitly contradicts the requested condition.
    - NOT_REQUESTED: No irrigation constraint specified.
    """
    if not requested_irrigation:
        return "NOT_REQUESTED"

    irrigation = normalize_text(irrigation_condition)
    requested = normalize_text(requested_irrigation)

    if not irrigation:
        return "UNKNOWN"

    # Rainfed request
    if requested == "rainfed":
        if irrigation == "rainfed" or "rainfed" in irrigation:
            return "MATCH"
        if "irrigated" in irrigation and "rainfed" not in irrigation and "protective" not in irrigation:
            return "NO_MATCH"
        if "protective" in irrigation:
            # Protective irrigation can be given to rainfed crops if available
            return "UNKNOWN"
        return "UNKNOWN"

    # Irrigated request
    if requested == "irrigated":
        if "irrigated" in irrigation or "protective" in irrigation:
            return "MATCH"
        if irrigation == "rainfed":
            return "NO_MATCH"
        return "UNKNOWN"

    # Protective irrigated request
    if "protective" in requested:
        if "protective" in irrigation or "irrigated" in irrigation:
            return "MATCH"
        return "UNKNOWN"

    return "UNKNOWN"


def maturity_matches(
    maturity_days_min: Any,
    maturity_days_max: Any,
    max_days: Optional[float] = None,
    min_days: Optional[float] = None
) -> bool:
    """
    Check if the variety maturity range falls within the farmer's cutoffs.
    """
    if max_days is None and min_days is None:
        return True

    if maturity_days_min is None and maturity_days_max is None:
        return False

    try:
        minimum = float(maturity_days_min) if maturity_days_min is not None else float(maturity_days_max)
        maximum = float(maturity_days_max) if maturity_days_max is not None else float(maturity_days_min)
    except (ValueError, TypeError):
        return False

    if max_days is not None:
        if maximum > float(max_days):
            return False

    if min_days is not None:
        if minimum < float(min_days):
            return False

    return True


def filter_varieties(
    documents: List[Dict[str, Any]],
    state: Optional[str] = None,
    season: Optional[str] = None,
    irrigation: Optional[str] = None,
    max_maturity_days: Optional[float] = None,
    min_maturity_days: Optional[float] = None
) -> List[Dict[str, Any]]:
    """
    Apply deterministic metadata filtering for cotton varieties.
    Hard filters: State/Zone, Season, Maturity Cutoffs.
    Soft filter: Irrigation compatibility.
    """
    results = []

    for document in documents:
        metadata = document.get("metadata", {})

        # 1. State / Regional zone filter
        if not region_matches(metadata.get("recommended_regions"), state):
            continue

        # 2. Season filter
        if not season_matches(metadata.get("season"), season):
            continue

        # 3. Irrigation filter
        irrigation_match_type = get_irrigation_match_type(
            metadata.get("irrigation_condition"),
            irrigation
        )
        if irrigation_match_type == "NO_MATCH":
            continue

        # 4. Maturity constraints filter
        if not maturity_matches(
            metadata.get("maturity_days_min"),
            metadata.get("maturity_days_max"),
            max_days=max_maturity_days,
            min_days=min_maturity_days
        ):
            continue

        candidate = dict(document)
        candidate_metadata = dict(metadata)
        candidate_metadata["irrigation_match_type"] = irrigation_match_type
        candidate["metadata"] = candidate_metadata
        results.append(candidate)

    return results
