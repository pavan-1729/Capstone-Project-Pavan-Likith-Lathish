"""
Integration tests for the full RAG pipeline.
"""

import pytest
import sys
from pathlib import Path

# Add project root to path
project_root = Path(__file__).parent.parent
sys.path.insert(0, str(project_root))

from src.retrieval import MaizeRetriever, parse_farmer_query
from src.retrieval.metadata_filter import load_documents


class TestDataLoading:
    """Test data loading functionality."""
    
    def test_load_documents(self):
        """Test that documents can be loaded."""
        try:
            documents = load_documents()
            assert len(documents) > 0
            assert "id" in documents[0]
            assert "metadata" in documents[0]
            assert "page_content" in documents[0]
        except FileNotFoundError:
            pytest.skip("Document file not found - run build_documents.py first")


class TestRetrieverIntegration:
    """Test the retriever with real data."""
    
    @pytest.fixture
    def retriever(self):
        """Create a retriever instance."""
        try:
            return MaizeRetriever()
        except FileNotFoundError:
            pytest.skip("Document file not found - run build_documents.py first")
    
    def test_retrieve_with_state(self, retriever):
        """Test retrieval with state filter."""
        results = retriever.retrieve(state="Karnataka")
        assert isinstance(results, list)
        # Verify Karnataka is in all results
        for doc in results:
            regions = doc.get("metadata", {}).get("recommended_regions", "")
            assert regions and "karnataka" in regions.lower()
    
    def test_retrieve_with_season(self, retriever):
        """Test retrieval with season filter."""
        results = retriever.retrieve(season="Kharif")
        assert isinstance(results, list)
        # Verify Kharif is in all results
        for doc in results:
            season = doc.get("metadata", {}).get("season", "")
            assert season and "kharif" in season.lower()
    
    def test_retrieve_with_multiple_filters(self, retriever):
        """Test retrieval with multiple filters."""
        results = retriever.retrieve(
            state="Karnataka",
            season="Kharif",
            irrigation="rainfed",
            max_maturity_days=100
        )
        assert isinstance(results, list)
    
    def test_retrieve_top_k(self, retriever):
        """Test top-k retrieval."""
        results = retriever.retrieve_top(top_k=5, state="Karnataka")
        assert len(results) <= 5


class TestEndToEnd:
    """Test complete query flow."""
    
    def test_query_to_retrieval(self):
        """Test parsing query and retrieving candidates."""
        query = "I need maize for Karnataka in Kharif season, rainfed, within 90 days"
        
        # Parse query
        filters = parse_farmer_query(query)
        assert filters["state"] == "Karnataka"
        assert filters["season"] == "Kharif"
        assert filters["irrigation"] == "rainfed"
        assert filters["max_maturity_days"] == 90
        
        # Retrieve candidates
        try:
            retriever = MaizeRetriever()
            candidates = retriever.retrieve(**filters)
            assert isinstance(candidates, list)
        except FileNotFoundError:
            pytest.skip("Document file not found - run build_documents.py first")


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
