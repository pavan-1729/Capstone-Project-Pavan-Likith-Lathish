"""
Soybean RAG Candidate Retriever
===============================
Retrieves and ranks candidate soybean varieties using deterministic metadata filtering,
trait scoring, and optional semantic vector similarity.
"""

import json
import logging
import os
from pathlib import Path
from typing import Any, Dict, List, Optional

from src.ingestion.document_builder import build_soybean_documents
from src.retrieval.metadata_filter import IrrigationMatch, MetadataFilter
from src.retrieval.query_parser import parse_farmer_query

logger = logging.getLogger(__name__)

DEFAULT_DATA_PATH = os.path.join("data", "processed", "soybean_rag_documents.json")
DEFAULT_CSV_PATHS = [
    os.path.join("data", "soy.csv"),
    os.path.join("data", "raw", "soybean_varieties.csv"),
    os.path.join("data", "soybean_varieties_master_database.csv"),
]


class SoybeanRetriever:
    """Retriever for soybean varieties with metadata filtering and candidate scoring."""

    def __init__(
        self,
        documents_path: Optional[str] = None,
        csv_path: Optional[str] = None,
        auto_build: bool = True,
    ):
        """
        Initialize the retriever and load document database.

        Args:
            documents_path: Path to processed soybean_rag_documents.json.
            csv_path: Path to raw CSV if documents need to be generated.
            auto_build: If True, builds documents automatically if json does not exist.
        """
        self.documents_path = documents_path or DEFAULT_DATA_PATH
        
        if not csv_path:
            for p in DEFAULT_CSV_PATHS:
                if os.path.exists(p):
                    csv_path = p
                    break
        self.csv_path = csv_path or DEFAULT_CSV_PATHS[0]
        self.documents: List[Dict[str, Any]] = []

        self._load_documents(auto_build=auto_build)

    def _load_documents(self, auto_build: bool = True) -> None:
        """Load documents from JSON or build from CSV."""
        if os.path.exists(self.documents_path):
            with open(self.documents_path, "r", encoding="utf-8") as f:
                self.documents = json.load(f)
            logger.info(f"Loaded {len(self.documents)} documents from {self.documents_path}")
        elif auto_build and os.path.exists(self.csv_path):
            logger.info(f"Building documents from raw CSV {self.csv_path}...")
            self.documents = build_soybean_documents(self.csv_path, self.documents_path)
        else:
            raise FileNotFoundError(
                f"Documents not found at {self.documents_path}. Run scripts/build_documents.py first."
            )

    def retrieve(
        self,
        query: Optional[str] = None,
        state: Optional[str] = None,
        season: Optional[str] = None,
        irrigation: Optional[str] = None,
        max_maturity: Optional[int] = None,
        min_maturity: Optional[int] = None,
        traits: Optional[List[str]] = None,
        top_k: int = 30,
        strict_irrigation: bool = False,
        **kwargs: Any,
    ) -> List[Dict[str, Any]]:
        """
        Retrieve candidate soybean variety documents for a farmer's request.

        Args:
            query: Natural language query (parsed if provided).
            state: Target Indian state.
            season: Target growing season (Kharif, Spring).
            irrigation: Water condition (rainfed, irrigated).
            max_maturity: Maximum maturity days.
            min_maturity: Minimum maturity days.
            traits: List of desired agronomic traits.
            top_k: Maximum number of candidates to return.
            strict_irrigation: Whether to exclude unknown irrigation candidates.

        Returns:
            Ranked list of candidate documents.
        """
        parsed_filters = {}
        if query:
            parsed_filters = parse_farmer_query(query)

        # Merge query-parsed filters with explicit parameters (explicit overrides parsed)
        target_state = state or parsed_filters.get("state")
        target_season = season or parsed_filters.get("season")
        target_irrigation = irrigation or parsed_filters.get("irrigation")
        target_max_mat = max_maturity if max_maturity is not None else parsed_filters.get("max_maturity")
        target_min_mat = min_maturity if min_maturity is not None else parsed_filters.get("min_maturity")
        target_traits = list(set((traits or []) + parsed_filters.get("traits", [])))

        # Step 1: Deterministic Metadata Filtering
        filtered_docs = MetadataFilter.filter_documents(
            documents=self.documents,
            state=target_state,
            season=target_season,
            irrigation=target_irrigation,
            max_maturity=target_max_mat,
            min_maturity=target_min_mat,
            strict_irrigation=strict_irrigation,
        )

        # If filtering produced zero results (e.g. constraints too tight), relax maturity constraint slightly
        if not filtered_docs and target_max_mat is not None:
            logger.warning("No matches found with strict maturity. Relaxing maturity by +5 days...")
            filtered_docs = MetadataFilter.filter_documents(
                documents=self.documents,
                state=target_state,
                season=target_season,
                irrigation=target_irrigation,
                max_maturity=target_max_mat + 5,
                min_maturity=target_min_mat,
                strict_irrigation=False,
            )

        # Step 2: Trait & Agronomic Relevance Scoring
        scored_candidates = []
        for doc in filtered_docs:
            score = self._score_candidate(doc, target_traits, target_irrigation)
            scored_doc = dict(doc)
            scored_doc["_retrieval_score"] = score
            scored_candidates.append(scored_doc)

        # Step 3: Sort by relevance score descending
        scored_candidates.sort(key=lambda d: d.get("_retrieval_score", 0.0), reverse=True)

        return scored_candidates[:top_k]

    def _score_candidate(
        self,
        doc: Dict[str, Any],
        target_traits: List[str],
        target_irrigation: Optional[str],
    ) -> float:
        """Calculate heuristic candidate suitability score."""
        score = 10.0
        meta = doc.get("metadata", {})
        content = doc.get("page_content", "").lower()

        # 1. Irrigation Match Tier
        irr_match = doc.get("_irrigation_match")
        if irr_match == IrrigationMatch.MATCH.value:
            score += 5.0
        elif irr_match == IrrigationMatch.UNKNOWN.value:
            score += 1.0

        # 2. Yield Potential Score (0 to 5 points)
        yield_max = meta.get("yield_normalized_q_ha_max") or 25.0
        score += min(max((yield_max - 20.0) / 4.0, 0.0), 5.0)

        # 3. Trait Matches (3 points per matched trait)
        for trait in target_traits:
            trait_name = trait.replace("_", " ")
            if trait in ["high_protein", "biofortified"]:
                prot = meta.get("protein_content_percent")
                if (prot and prot >= 40.0) or "protein" in content or "biofortified" in content:
                    score += 4.0
            elif trait in ["high_oil"]:
                oil = meta.get("oil_content_percent")
                if (oil and oil >= 20.0) or "oil content" in content:
                    score += 4.0
            elif trait in ["drought_tolerant"]:
                drought_txt = meta.get("drought_tolerance", "").lower()
                if "tolerant" in drought_txt or "resilience" in drought_txt or "escaping" in drought_txt:
                    score += 4.0
            elif trait in ["waterlogging_tolerant"]:
                wl_txt = meta.get("waterlogging_tolerance", "").lower()
                if "tolerant" in wl_txt or "submergence" in wl_txt:
                    score += 4.0
            elif trait_name in content:
                score += 3.0

        # 4. Release Year recency bonus (0 to 2 points)
        rel_yr = meta.get("release_year")
        if rel_yr and rel_yr >= 2010:
            score += min((rel_yr - 2000) * 0.1, 2.5)

        return round(score, 2)
