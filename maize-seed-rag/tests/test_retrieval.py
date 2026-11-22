"""
Unit tests for retrieval module.
"""

import pytest
import sys
from pathlib import Path

# Add project root to path
project_root = Path(__file__).parent.parent
sys.path.insert(0, str(project_root))

from src.retrieval.query_parser import (
    extract_state,
    extract_season,
    extract_irrigation,
    extract_max_maturity,
    parse_farmer_query
)
from src.retrieval.metadata_filter import (
    normalize_text,
    region_matches,
    season_matches,
    irrigation_matches,
    maturity_matches
)


class TestQueryParser:
    """Test query parsing functionality."""
    
    def test_extract_state(self):
        assert extract_state("I am from Karnataka") == "Karnataka"
        assert extract_state("Looking for varieties in Tamil Nadu") == "Tamil Nadu"
        assert extract_state("No state mentioned") is None
    
    def test_extract_season(self):
        assert extract_season("Kharif season crops") == "Kharif"
        assert extract_season("Rabi season planting") == "Rabi"
        assert extract_season("No season mentioned") is None
    
    def test_extract_irrigation(self):
        assert extract_irrigation("rainfed conditions") == "rainfed"
        assert extract_irrigation("irrigated land") == "irrigated"
        assert extract_irrigation("no irrigation info") is None
    
    def test_extract_max_maturity(self):
        assert extract_max_maturity("maturing within 90 days") == 90
        assert extract_max_maturity("maximum 100 days") == 100
        assert extract_max_maturity("no maturity info") is None
    
    def test_parse_farmer_query_complete(self):
        query = "I need maize for Karnataka in Kharif season, rainfed, maturing within 90 days"
        result = parse_farmer_query(query)
        
        assert result["state"] == "Karnataka"
        assert result["season"] == "Kharif"
        assert result["irrigation"] == "rainfed"
        assert result["max_maturity_days"] == 90


class TestMetadataFilter:
    """Test metadata filtering functionality."""
    
    def test_normalize_text(self):
        assert normalize_text("Karnataka") == "karnataka"
        assert normalize_text("  Tamil Nadu  ") == "tamil nadu"
        assert normalize_text(None) == ""
    
    def test_region_matches(self):
        assert region_matches("Karnataka, Tamil Nadu", "Karnataka") is True
        assert region_matches("Karnataka", "Tamil Nadu") is False
        assert region_matches("", "Karnataka") is False
        assert region_matches("Karnataka", None) is True
    
    def test_season_matches(self):
        assert season_matches("Kharif", "Kharif") is True
        assert season_matches("Kharif, Rabi", "Rabi") is True
        assert season_matches("Kharif", "Rabi") is False
        assert season_matches("", "Kharif") is False
    
    def test_irrigation_matches(self):
        # Match cases
        assert irrigation_matches("rainfed", "rainfed") is True
        assert irrigation_matches("irrigated", "irrigated") is True
        
        # Unknown cases (should return True to keep candidates)
        assert irrigation_matches(None, "rainfed") is True
        assert irrigation_matches("", "rainfed") is True
        
        # No match cases
        assert irrigation_matches("irrigated", "rainfed") is False
        assert irrigation_matches("rainfed", "irrigated") is False
    
    def test_maturity_matches(self):
        # Within range
        assert maturity_matches(80, 90, max_days=100) is True
        assert maturity_matches(90, 100, max_days=100) is True
        
        # Exceeds maximum
        assert maturity_matches(90, 110, max_days=100) is False
        
        # Below minimum
        assert maturity_matches(70, 80, min_days=85) is False
        
        # No constraints
        assert maturity_matches(80, 90) is True


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
