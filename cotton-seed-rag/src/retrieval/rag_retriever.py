import json
from pathlib import Path
from typing import List, Dict, Optional, Any

import numpy as np

from src.retrieval.metadata_filter import (
    load_documents,
    filter_varieties,
)
from src.retrieval.query_parser import parse_farmer_query


PROJECT_ROOT = Path(__file__).resolve().parent.parent.parent
EMBEDDINGS_FILE = PROJECT_ROOT / "data" / "processed" / "embeddings.npy"
IDS_FILE = PROJECT_ROOT / "data" / "processed" / "embedding_ids.json"


class CottonRetriever:
    """
    Two-stage Hybrid Retriever for Cotton Variety Knowledge:
      Stage 1: Deterministic metadata filtering (State/Zone, Season, Irrigation, Maturity)
      Stage 2: Dense semantic ranking with BAAI/bge-m3 embeddings
    """

    def __init__(self, load_embeddings: bool = True):
        self.documents = load_documents()
        self.doc_map = {doc["id"]: doc for doc in self.documents}
        self.embeddings: Optional[np.ndarray] = None
        self.id_to_idx: Dict[str, int] = {}
        self.encoder = None

        if load_embeddings and EMBEDDINGS_FILE.exists() and IDS_FILE.exists():
            try:
                self.embeddings = np.load(EMBEDDINGS_FILE)
                with open(IDS_FILE, "r", encoding="utf-8") as f:
                    ids = json.load(f)
                self.id_to_idx = {doc_id: idx for idx, doc_id in enumerate(ids)}
            except Exception as e:
                print(f"Warning: Could not load vector embeddings: {e}")

    def _get_encoder(self):
        if self.encoder is None:
            from sentence_transformers import SentenceTransformer
            self.encoder = SentenceTransformer("BAAI/bge-m3")
        return self.encoder

    def retrieve(
        self,
        state: Optional[str] = None,
        season: Optional[str] = None,
        irrigation: Optional[str] = None,
        max_maturity_days: Optional[float] = None,
        min_maturity_days: Optional[float] = None,
    ) -> List[Dict[str, Any]]:
        """
        Deterministic hard & soft metadata filtering.
        """
        return filter_varieties(
            self.documents,
            state=state,
            season=season,
            irrigation=irrigation,
            max_maturity_days=max_maturity_days,
            min_maturity_days=min_maturity_days,
        )

    def retrieve_hybrid(
        self,
        query: str,
        state: Optional[str] = None,
        season: Optional[str] = None,
        irrigation: Optional[str] = None,
        max_maturity_days: Optional[float] = None,
        min_maturity_days: Optional[float] = None,
        top_k: int = 5,
    ) -> List[Dict[str, Any]]:
        """
        Hybrid retrieval: Filter candidates deterministically, then rank by
        dense vector similarity to the natural language query.
        """
        # If parameters not explicitly provided, extract from query
        if not any([state, season, irrigation, max_maturity_days, min_maturity_days]):
            parsed = parse_farmer_query(query)
            state = parsed.get("state")
            season = parsed.get("season")
            irrigation = parsed.get("irrigation")
            max_maturity_days = parsed.get("max_maturity_days")
            min_maturity_days = parsed.get("min_maturity_days")

        candidates = self.retrieve(
            state=state,
            season=season,
            irrigation=irrigation,
            max_maturity_days=max_maturity_days,
            min_maturity_days=min_maturity_days,
        )

        if not candidates or self.embeddings is None:
            return candidates[:top_k]

        try:
            encoder = self._get_encoder()
            query_embedding = encoder.encode(
                query,
                normalize_embeddings=True,
                convert_to_numpy=True
            )

            # Score candidates
            scored_candidates = []
            for doc in candidates:
                doc_id = doc.get("id")
                if doc_id in self.id_to_idx:
                    idx = self.id_to_idx[doc_id]
                    doc_emb = self.embeddings[idx]
                    similarity = float(np.dot(query_embedding, doc_emb))
                else:
                    similarity = 0.0

                candidate_copy = dict(doc)
                candidate_copy["similarity_score"] = round(similarity, 4)
                scored_candidates.append(candidate_copy)

            # Sort by descending similarity
            scored_candidates.sort(key=lambda x: x.get("similarity_score", 0.0), reverse=True)
            return scored_candidates[:top_k]

        except Exception as e:
            print(f"Semantic scoring fallback to deterministic slice: {e}")
            return candidates[:top_k]

    def retrieve_top(
        self,
        top_k: int = 5,
        state: Optional[str] = None,
        season: Optional[str] = None,
        irrigation: Optional[str] = None,
        max_maturity_days: Optional[float] = None,
        min_maturity_days: Optional[float] = None,
    ) -> List[Dict[str, Any]]:
        """Return at most top_k deterministic candidates."""
        candidates = self.retrieve(
            state=state,
            season=season,
            irrigation=irrigation,
            max_maturity_days=max_maturity_days,
            min_maturity_days=min_maturity_days,
        )
        return candidates[:top_k]
