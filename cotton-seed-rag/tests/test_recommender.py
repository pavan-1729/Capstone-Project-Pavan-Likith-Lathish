import pytest
from src.recommender.gemini_recommender import (
    build_candidate_context,
    clean_json_response,
    recommend_varieties,
)


def test_clean_json_response():
    raw_md = '```json\n{"recommendations": [{"variety_id": "COT_VER_001", "variety_name": "H 4"}]}\n```'
    parsed = clean_json_response(raw_md)
    assert "recommendations" in parsed
    assert parsed["recommendations"][0]["variety_id"] == "COT_VER_001"


def test_build_candidate_context():
    sample_candidate = [{
        "id": "COT_VER_001",
        "page_content": "Certified cotton record for H 4.",
        "metadata": {
            "variety_id": "COT_VER_001",
            "variety_name": "H 4",
            "crop": "Cotton",
            "season": "Kharif",
            "maturity_days_min": 160,
            "maturity_days_max": 177,
            "recommended_regions": "Southern Zone (AP, TS, TN)",
            "irrigation_condition": "Irrigated",
            "special_traits": "Ginning outturn 39%",
            "source_document": "ICAR-CICR Bulletin",
            "source_pdf_page": 91,
            "source_organization": "MPKV Rahuri"
        }
    }]
    context = build_candidate_context(sample_candidate)
    assert "COT_VER_001" in context
    assert "H 4" in context
    assert "Ginning outturn 39%" in context


def test_recommend_varieties_empty():
    res = recommend_varieties("Cotton in winter", [])
    assert len(res["recommendations"]) == 0
    assert "No cotton varieties met" in res["final_note"]
