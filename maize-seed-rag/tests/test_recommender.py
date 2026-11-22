"""
Unit tests for recommendation module.
"""

import pytest
import json
import sys
from pathlib import Path

# Add project root to path
project_root = Path(__file__).parent.parent
sys.path.insert(0, str(project_root))

from src.recommender.gemini_recommender import (
    clean_json_response,
    validate_recommendation,
    build_candidate_context
)


class TestJSONProcessing:
    """Test JSON response processing."""
    
    def test_clean_json_response_simple(self):
        response = '{"key": "value"}'
        result = clean_json_response(response)
        assert result == {"key": "value"}
    
    def test_clean_json_response_with_markdown(self):
        response = '```json\n{"key": "value"}\n```'
        result = clean_json_response(response)
        assert result == {"key": "value"}
    
    def test_clean_json_response_with_text(self):
        response = 'Some text before {"key": "value"} some text after'
        result = clean_json_response(response)
        assert result == {"key": "value"}
    
    def test_clean_json_response_invalid(self):
        response = 'No JSON here'
        result = clean_json_response(response)
        assert result is None


class TestRecommendationValidation:
    """Test recommendation structure validation."""
    
    def test_validate_recommendation_valid(self):
        recommendation = {
            "recommendations": [
                {
                    "rank": 1,
                    "variety_id": "MAI-001",
                    "variety_name": "Test Variety",
                    "maturity": "85-95 days",
                    "reason": "Good match",
                    "irrigation": "rainfed"
                }
            ],
            "final_note": "Best variety selected",
            "warnings": []
        }
        assert validate_recommendation(recommendation) is True
    
    def test_validate_recommendation_missing_recommendations(self):
        recommendation = {
            "final_note": "Note",
            "warnings": []
        }
        assert validate_recommendation(recommendation) is False
    
    def test_validate_recommendation_missing_fields(self):
        recommendation = {
            "recommendations": [
                {
                    "rank": 1,
                    "variety_name": "Test Variety"
                    # Missing required fields
                }
            ],
            "final_note": "Note",
            "warnings": []
        }
        assert validate_recommendation(recommendation) is False
    
    def test_validate_recommendation_wrong_types(self):
        recommendation = {
            "recommendations": "not a list",
            "final_note": "Note",
            "warnings": []
        }
        assert validate_recommendation(recommendation) is False


class TestCandidateContext:
    """Test candidate context building."""
    
    def test_build_candidate_context(self):
        candidates = [
            {
                "metadata": {
                    "variety_id": "MAI-001",
                    "variety_name": "Test Maize",
                    "crop": "Maize",
                    "season": "Kharif",
                    "maturity_days_min": 85,
                    "maturity_days_max": 95,
                    "irrigation_condition": "rainfed"
                },
                "page_content": "Test variety description"
            }
        ]
        
        context = build_candidate_context(candidates)
        
        assert "CANDIDATE 1" in context
        assert "MAI-001" in context
        assert "Test Maize" in context
        assert "Kharif" in context
        assert "rainfed" in context


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
