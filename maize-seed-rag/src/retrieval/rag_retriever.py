from typing import List, Dict, Optional

from src.retrieval.metadata_filter import (
    load_documents,
    filter_varieties
)


class MaizeRetriever:

    def __init__(self):
        self.documents = load_documents()

    def retrieve(
        self,
        state: Optional[str] = None,
        season: Optional[str] = None,
        irrigation: Optional[str] = None,
        max_maturity_days: Optional[int] = None,
        min_maturity_days: Optional[int] = None
    ) -> List[Dict]:
        """
        Retrieve maize varieties using deterministic
        metadata filtering.
        """

        candidates = filter_varieties(
            self.documents,
            state=state,
            season=season,
            irrigation=irrigation,
            max_maturity_days=max_maturity_days,
            min_maturity_days=min_maturity_days
        )

        return candidates

    def retrieve_top(
        self,
        top_k: int = 5,
        state: Optional[str] = None,
        season: Optional[str] = None,
        irrigation: Optional[str] = None,
        max_maturity_days: Optional[int] = None,
        min_maturity_days: Optional[int] = None
    ) -> List[Dict]:
        """
        Return at most top_k candidates.

        Ranking will be added later using semantic
        similarity / ML scoring.
        """

        candidates = self.retrieve(
            state=state,
            season=season,
            irrigation=irrigation,
            max_maturity_days=max_maturity_days,
            min_maturity_days=min_maturity_days
        )

        return candidates[:top_k]