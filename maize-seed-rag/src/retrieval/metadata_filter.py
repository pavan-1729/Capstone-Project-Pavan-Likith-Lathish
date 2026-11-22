import json
from pathlib import Path
from typing import Optional


# =========================================================
# Project paths
# =========================================================

PROJECT_ROOT = Path(__file__).resolve().parent.parent.parent

DOCUMENT_FILE = (
    PROJECT_ROOT
    / "data"
    / "processed"
    / "maize_rag_documents.json"
)


# =========================================================
# Load documents
# =========================================================

def load_documents():
    """
    Load all maize RAG documents from the processed JSON file.
    """

    if not DOCUMENT_FILE.exists():
        raise FileNotFoundError(
            f"Document file not found:\n{DOCUMENT_FILE}"
        )

    with open(
        DOCUMENT_FILE,
        "r",
        encoding="utf-8"
    ) as file:
        return json.load(file)


# =========================================================
# Text normalization
# =========================================================

def normalize_text(value) -> str:
    """
    Convert any value into normalized lowercase text.
    """

    if value is None:
        return ""

    return str(value).strip().lower()


# =========================================================
# Region matching
# =========================================================

def region_matches(
    recommended_regions,
    requested_state
) -> bool:
    """
    Check whether the requested state is present
    in the recommended regions.

    Missing region information does NOT match.
    """

    if not requested_state:
        return True

    regions = normalize_text(
        recommended_regions
    )

    state = normalize_text(
        requested_state
    )

    if not regions:
        return False

    return state in regions


# =========================================================
# Season matching
# =========================================================

def season_matches(
    season,
    requested_season
) -> bool:
    """
    Check whether the requested growing season
    is supported by the variety.

    Missing season information does NOT match
    when a season is explicitly requested.
    """

    if not requested_season:
        return True

    available_seasons = normalize_text(
        season
    )

    requested = normalize_text(
        requested_season
    )

    if not available_seasons:
        return False

    return requested in available_seasons


# =========================================================
# Irrigation match classification
# =========================================================

def get_irrigation_match_type(
    irrigation_condition,
    requested_irrigation
) -> str:
    """
    Classify irrigation compatibility.

    Returns
    -------
    MATCH
        Metadata explicitly supports the requested
        irrigation condition.

    UNKNOWN
        Irrigation metadata is missing or unclear.
        Candidate should be retained and evaluated
        later by the LLM.

    NO_MATCH
        Metadata explicitly contradicts the requested
        irrigation condition.

    NOT_REQUESTED
        No irrigation condition was supplied.
    """

    if not requested_irrigation:
        return "NOT_REQUESTED"

    irrigation = normalize_text(
        irrigation_condition
    )

    requested = normalize_text(
        requested_irrigation
    )

    # -----------------------------------------------------
    # Missing irrigation metadata
    # -----------------------------------------------------

    if not irrigation:
        return "UNKNOWN"

    # -----------------------------------------------------
    # Rainfed request
    # -----------------------------------------------------

    if requested == "rainfed":

        # Explicit rainfed support
        if irrigation == "rainfed":
            return "MATCH"

        # Rainfed + irrigated
        if (
            "rainfed" in irrigation
            and "irrigated" in irrigation
        ):
            return "MATCH"

        # Dryland is compatible with rainfed
        if irrigation == "dryland":
            return "MATCH"

        # Explicitly irrigated only
        if irrigation == "irrigated":
            return "NO_MATCH"

        # Unknown wording
        return "UNKNOWN"

    # -----------------------------------------------------
    # Irrigated request
    # -----------------------------------------------------

    if requested == "irrigated":

        # Explicitly irrigated
        if irrigation == "irrigated":
            return "MATCH"

        # Rainfed + irrigated
        if (
            "rainfed" in irrigation
            and "irrigated" in irrigation
        ):
            return "MATCH"

        # Rainfed only
        if irrigation == "rainfed":
            return "NO_MATCH"

        # Dryland only
        if irrigation == "dryland":
            return "NO_MATCH"

        # Unknown wording
        return "UNKNOWN"

    # -----------------------------------------------------
    # Other irrigation condition
    # -----------------------------------------------------

    if requested in irrigation:
        return "MATCH"

    return "UNKNOWN"


# =========================================================
# Irrigation matching
# =========================================================

def irrigation_matches(
    irrigation_condition,
    requested_irrigation
) -> bool:
    """
    Determine whether a document should remain
    in the candidate set.

    Important:
        UNKNOWN irrigation information is retained.

    Therefore:

        MATCH     -> True
        UNKNOWN   -> True
        NO_MATCH  -> False
    """

    match_type = get_irrigation_match_type(
        irrigation_condition,
        requested_irrigation
    )

    return match_type != "NO_MATCH"


# =========================================================
# Maturity matching
# =========================================================

def maturity_matches(
    maturity_days_min,
    maturity_days_max,
    max_days: Optional[float] = None,
    min_days: Optional[float] = None
) -> bool:
    """
    Check maturity constraints.

    Rules
    -----

    max_days:
        The variety's maximum maturity must be <= max_days.

        Example:
            80-90 days with max_days=100 -> MATCH
            90-100 days with max_days=100 -> MATCH
            90-110 days with max_days=100 -> NO MATCH

    min_days:
        The variety's minimum maturity must be >= min_days.

        Example:
            90-100 days with min_days=80 -> MATCH
            70-90 days with min_days=80 -> NO MATCH

    If a maturity constraint is requested and the document
    has no maturity information, the variety is excluded.
    """

    # -----------------------------------------------------
    # No maturity filtering requested
    # -----------------------------------------------------

    if max_days is None and min_days is None:
        return True

    # -----------------------------------------------------
    # Missing maturity information
    # -----------------------------------------------------

    if (
        maturity_days_min is None
        and maturity_days_max is None
    ):
        return False

    try:

        minimum = (
            float(maturity_days_min)
            if maturity_days_min is not None
            else float(maturity_days_max)
        )

        maximum = (
            float(maturity_days_max)
            if maturity_days_max is not None
            else float(maturity_days_min)
        )

    except (ValueError, TypeError):

        return False

    # -----------------------------------------------------
    # Maximum maturity constraint
    # -----------------------------------------------------

    if max_days is not None:

        if maximum > float(max_days):
            return False

    # -----------------------------------------------------
    # Minimum maturity constraint
    # -----------------------------------------------------

    if min_days is not None:

        if minimum < float(min_days):
            return False

    return True


# =========================================================
# Main metadata filter
# =========================================================

def filter_varieties(
    documents,
    state=None,
    season=None,
    irrigation=None,
    max_maturity_days=None,
    min_maturity_days=None
):
    """
    Apply deterministic metadata filters.

    HARD FILTERS
    ------------
    State
    Season
    Maturity

    SOFT / EVIDENCE FILTER
    ----------------------
    Irrigation

    Irrigation candidates are handled as:

        MATCH
            Explicitly compatible.

        UNKNOWN
            Missing irrigation metadata.
            Candidate is retained for LLM evaluation.

        NO_MATCH
            Explicitly incompatible.
            Candidate is removed.

    Returns
    -------
    list
        Filtered documents.

    Each returned document receives:

        metadata["irrigation_match_type"]

    with one of:

        MATCH
        UNKNOWN
        NOT_REQUESTED
    """

    results = []

    for document in documents:

        metadata = document.get(
            "metadata",
            {}
        )

        # -------------------------------------------------
        # State
        # -------------------------------------------------

        if not region_matches(
            metadata.get(
                "recommended_regions"
            ),
            state
        ):
            continue

        # -------------------------------------------------
        # Season
        # -------------------------------------------------

        if not season_matches(
            metadata.get(
                "season"
            ),
            season
        ):
            continue

        # -------------------------------------------------
        # Irrigation
        # -------------------------------------------------

        irrigation_match_type = (
            get_irrigation_match_type(
                metadata.get(
                    "irrigation_condition"
                ),
                irrigation
            )
        )

        # Explicitly incompatible varieties
        # are removed.
        if irrigation_match_type == "NO_MATCH":
            continue

        # -------------------------------------------------
        # Maturity
        # -------------------------------------------------

        if not maturity_matches(
            metadata.get(
                "maturity_days_min"
            ),
            metadata.get(
                "maturity_days_max"
            ),
            max_days=max_maturity_days,
            min_days=min_maturity_days
        ):
            continue

        # -------------------------------------------------
        # Create a copy so the original loaded document
        # is not modified.
        # -------------------------------------------------

        candidate = dict(document)

        candidate_metadata = dict(
            metadata
        )

        candidate_metadata[
            "irrigation_match_type"
        ] = irrigation_match_type

        candidate["metadata"] = (
            candidate_metadata
        )

        # -------------------------------------------------
        # Add candidate
        # -------------------------------------------------

        results.append(
            candidate
        )

    return results