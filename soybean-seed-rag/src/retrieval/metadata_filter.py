"""
Deterministic Metadata Filtering & Hard/Soft Constraint Verification
====================================================================
Filters and scores candidate soybean variety documents based on hard constraints
(state, season, maturity) and soft constraints (irrigation conditions, traits).
"""

from enum import Enum
from typing import Any, Dict, List, Optional, Tuple


class IrrigationMatch(str, Enum):
    MATCH = "MATCH"
    UNKNOWN = "UNKNOWN"
    NO_MATCH = "NO_MATCH"


class MetadataFilter:
    """Applies deterministic agricultural constraints to soybean variety documents."""

    @staticmethod
    def match_state(doc_metadata: Dict[str, Any], query_state: Optional[str]) -> bool:
        """Check if document matches the queried state or region."""
        if not query_state:
            return True

        q_state_lower = query_state.lower()
        adapted_states = [s.lower() for s in doc_metadata.get("states", [])]
        if q_state_lower in adapted_states:
            return True

        # Check recommended regions and agro climatic zones text
        rec_regions = " ".join(doc_metadata.get("recommended_regions", [])).lower()
        zone = doc_metadata.get("agro_climatic_zone", "").lower()

        if q_state_lower in rec_regions or q_state_lower in zone:
            return True

        zone_comb = (rec_regions + " " + zone).lower()
        if "all india" in zone_comb:
            return True

        zone_state_map = {
            "madhya pradesh": ["cz", "central", "mp"],
            "mp": ["cz", "central", "mp"],
            "maharashtra": ["cz", "central", "mh", "maha"],
            "mh": ["cz", "central", "mh", "maha"],
            "rajasthan": ["cz", "central", "rj", "raj"],
            "rj": ["cz", "central", "rj", "raj"],
            "gujarat": ["cz", "central", "gj"],
            "chhattisgarh": ["cz", "central", "nez", "cg"],
            "karnataka": ["sz", "southern", "ka", "kar"],
            "ka": ["sz", "southern", "ka", "kar"],
            "andhra pradesh": ["sz", "southern", "ap"],
            "ap": ["sz", "southern", "ap"],
            "telangana": ["sz", "southern", "ap", "ts", "tg"],
            "ts": ["sz", "southern", "ap", "ts", "tg"],
            "tamil nadu": ["sz", "southern", "tn"],
            "tn": ["sz", "southern", "tn"],
            "punjab": ["npz", "northern plain", "pb"],
            "pb": ["npz", "northern plain", "pb"],
            "haryana": ["npz", "northern plain", "hr"],
            "hr": ["npz", "northern plain", "hr"],
            "uttar pradesh": ["npz", "northern plain", "up", "central"],
            "up": ["npz", "northern plain", "up", "central"],
            "uttarakhand": ["npz", "northern plain", "uk"],
            "bihar": ["nez", "north eastern", "bihar"],
            "jharkhand": ["nez", "north eastern", "jh"],
            "west bengal": ["nez", "north eastern", "wb"],
            "assam": ["nez", "north eastern", "assam"],
            "odisha": ["nez", "north eastern", "od"],
        }

        if q_state_lower in zone_state_map:
            if any(k in zone_comb for k in zone_state_map[q_state_lower]):
                return True

        return False

    @staticmethod
    def match_season(doc_metadata: Dict[str, Any], query_season: Optional[str]) -> bool:
        """Check if document is suitable for the queried growing season."""
        if not query_season:
            return True

        q_season_lower = query_season.lower()
        doc_seasons = [s.lower() for s in doc_metadata.get("season", [])]
        raw_season = doc_metadata.get("season_raw", "").lower()

        # Kharif queries match Kharif or Kharif / Spring
        if "kharif" in q_season_lower:
            return "kharif" in raw_season or any("kharif" in s for s in doc_seasons)

        # Spring / Summer queries match Spring, Summer, or Kharif & Summer
        if "spring" in q_season_lower or "zaid" in q_season_lower or "summer" in q_season_lower:
            return (
                "spring" in raw_season
                or "summer" in raw_season
                or any("spring" in s or "summer" in s for s in doc_seasons)
            )

        # Rabi queries
        if "rabi" in q_season_lower:
            return "rabi" in raw_season or any("rabi" in s for s in doc_seasons)

        return True

    @staticmethod
    def match_maturity(
        doc_metadata: Dict[str, Any],
        max_maturity: Optional[int] = None,
        min_maturity: Optional[int] = None,
    ) -> bool:
        """Check if document fits maturity duration requirements."""
        mat_min = doc_metadata.get("maturity_days_min")
        mat_max = doc_metadata.get("maturity_days_max")

        if max_maturity is not None:
            if mat_max is not None:
                # Primary condition: maximum maturity days within user limit
                if mat_max > max_maturity:
                    return False
            elif mat_min is not None and mat_min > max_maturity:
                return False

        if min_maturity is not None:
            if mat_min is not None:
                if mat_min < min_maturity:
                    return False
            elif mat_max is not None and mat_max < min_maturity:
                return False

        return True

    @staticmethod
    def classify_irrigation(
        doc_metadata: Dict[str, Any],
        query_irrigation: Optional[str]
    ) -> IrrigationMatch:
        """
        Classify irrigation match status (MATCH, UNKNOWN, NO_MATCH).
        """
        if not query_irrigation:
            return IrrigationMatch.MATCH

        q_irr = query_irrigation.lower()
        irr_cond = doc_metadata.get("irrigation_condition", "").lower()
        irr_type = doc_metadata.get("irrigation_type", "unknown").lower()

        if not irr_cond or irr_cond == "unknown":
            return IrrigationMatch.UNKNOWN

        if "rainfed" in q_irr:
            if irr_type in ["rainfed", "both"]:
                return IrrigationMatch.MATCH
            if "protective" in irr_type or "protective" in irr_cond or "limited" in irr_cond:
                return IrrigationMatch.UNKNOWN
            if irr_type == "irrigated":
                return IrrigationMatch.NO_MATCH

        if "irrigated" in q_irr:
            if irr_type in ["irrigated", "both", "protective"] or "limited" in irr_cond:
                return IrrigationMatch.MATCH
            if irr_type == "rainfed":
                return IrrigationMatch.NO_MATCH

        if "protective" in q_irr or "limited" in q_irr:
            if "protective" in irr_cond or "limited" in irr_cond or irr_type in ["both", "irrigated"]:
                return IrrigationMatch.MATCH
            return IrrigationMatch.UNKNOWN

        return IrrigationMatch.UNKNOWN

    @classmethod
    def filter_documents(
        cls,
        documents: List[Dict[str, Any]],
        state: Optional[str] = None,
        season: Optional[str] = None,
        irrigation: Optional[str] = None,
        max_maturity: Optional[int] = None,
        min_maturity: Optional[int] = None,
        strict_irrigation: bool = False,
    ) -> List[Dict[str, Any]]:
        """
        Filter documents against agricultural constraints.

        Args:
            documents: List of candidate variety documents.
            state: State name or None.
            season: Season name or None.
            irrigation: Irrigation requirement or None.
            max_maturity: Maximum days to maturity.
            min_maturity: Minimum days to maturity.
            strict_irrigation: If True, exclude UNKNOWN irrigation matches.

        Returns:
            Filtered list of documents with attached filter_status in metadata.
        """
        filtered = []

        for doc in documents:
            meta = doc.get("metadata", {})

            # Hard Constraint 1: State / Region
            if not cls.match_state(meta, state):
                continue

            # Hard Constraint 2: Season
            if not cls.match_season(meta, season):
                continue

            # Hard Constraint 3: Maturity duration
            if not cls.match_maturity(meta, max_maturity, min_maturity):
                continue

            # Soft Constraint: Irrigation
            irr_status = cls.classify_irrigation(meta, irrigation)
            if irr_status == IrrigationMatch.NO_MATCH:
                continue
            if strict_irrigation and irr_status == IrrigationMatch.UNKNOWN:
                continue

            # Attach match classification
            doc_copy = dict(doc)
            doc_copy["_irrigation_match"] = irr_status.value
            filtered.append(doc_copy)

        return filtered
