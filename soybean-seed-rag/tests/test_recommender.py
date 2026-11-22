"""
Unit tests for Recommender and Gemini Client.
"""

import json
import pytest
from src.client.gemini_client import GeminiClient
from src.recommender.gemini_recommender import (
    build_recommender_prompt,
    generate_mock_recommendations,
    recommend_varieties,
)


def test_gemini_client_json_cleaning():
    raw_json_md = """```json
    {
        "recommendations": [
            {"rank": 1, "variety_name": "JS 335"}
        ],
        "final_note": "Great variety"
    }
    ```"""
    parsed = GeminiClient._clean_and_parse_json(raw_json_md)
    assert "recommendations" in parsed
    assert parsed["recommendations"][0]["variety_name"] == "JS 335"


def test_gemini_client_fallback_list():
    client = GeminiClient(api_key="dummy_key")
    assert len(client.models) >= 3
    assert "gemini-2.5-flash" in client.models or "gemini-2.0-flash" in client.models


def test_gemini_client_unconfigured_error():
    client = GeminiClient(api_key="")
    assert not client.is_available
    with pytest.raises(RuntimeError):
        client.generate_content("test prompt")


def test_build_recommender_prompt():
    sample_candidate = {
        "id": "SOY_VER_001",
        "variety_name": "JS 335",
        "metadata": {
            "category": "Biofortified High Protein",
            "type": "Mutation Breeding Line",
            "developer_or_company": "PAU Ludhiana",
            "release_year": 1998,
            "season_raw": "Kharif",
            "maturity_days_min": 106,
            "maturity_days_max": 113,
            "maturity_class": "Medium",
            "yield_value": "22.6-29.0 q/ha",
            "irrigation_condition": "Rainfed",
            "states": ["Karnataka", "Maharashtra"],
        },
    }
    prompt = build_recommender_prompt("Need rainfed soybean for Karnataka", [sample_candidate], top_n=1)
    assert "JS 335" in prompt
    assert "SOY_VER_001" in prompt
    assert "Need rainfed soybean for Karnataka" in prompt


def test_generate_mock_recommendations():
    candidates = [
        {
            "id": "SOY_VER_001",
            "variety_name": "JS 335",
            "metadata": {
                "maturity_days_min": 106,
                "maturity_days_max": 113,
                "irrigation_condition": "Rainfed",
                "yield_normalized_q_ha_min": 22.6,
                "yield_normalized_q_ha_max": 29.0,
                "states": ["Karnataka"],
                "protein_content_percent": 39.1,
                "oil_content_percent": 18.5,
            },
        }
    ]
    res = generate_mock_recommendations("Query", candidates, top_n=1)
    assert len(res["recommendations"]) == 1
    assert res["recommendations"][0]["variety_name"] == "JS 335"
    assert "warnings" in res
    assert "final_note" in res


def test_recommend_varieties_empty():
    res = recommend_varieties("Some query", [])
    assert len(res["recommendations"]) == 0
    assert "No matching" in res["final_note"]
