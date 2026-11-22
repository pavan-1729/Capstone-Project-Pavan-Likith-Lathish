import pytest
from pathlib import Path

from src.retrieval.metadata_filter import (
    load_documents,
    region_matches,
    season_matches,
    get_irrigation_match_type,
    maturity_matches,
    filter_varieties,
)
from src.retrieval.query_parser import (
    extract_state,
    extract_season,
    extract_irrigation,
    extract_maturity,
    parse_farmer_query,
)


def test_load_documents():
    docs = load_documents()
    assert len(docs) == 265
    sample = docs[0]
    assert "id" in sample
    assert "page_content" in sample
    assert "metadata" in sample
    assert sample["metadata"]["crop"] == "Cotton"


def test_region_matching():
    # Southern Zone
    assert region_matches("Southern Zone (AP, TS, TN)", "Telangana")
    assert region_matches("Southern Zone (AP, TS, TN)", "Andhra Pradesh")
    assert region_matches("Southern Zone (AP, TS, TN)", "Tamil Nadu")
    assert not region_matches("Southern Zone (AP, TS, TN)", "Punjab")

    # Central Zone
    assert region_matches("Central Zone (GJ, MH, MP)", "Maharashtra")
    assert region_matches("Central Zone (GJ, MH, MP)", "Gujarat")
    assert region_matches("Central Zone (GJ, MH, MP)", "Madhya Pradesh")
    assert not region_matches("Central Zone (GJ, MH, MP)", "Haryana")

    # Northern Zone
    assert region_matches("Northern Zone (PB, HR, RJ)", "Punjab")
    assert region_matches("Northern Zone (PB, HR, RJ)", "Haryana")
    assert region_matches("Northern Zone (PB, HR, RJ)", "Rajasthan")
    assert not region_matches("Northern Zone (PB, HR, RJ)", "Karnataka")


def test_season_matching():
    assert season_matches("Kharif (Irri/Rainfed)", "Kharif")
    assert season_matches("Kharif", "Kharif")
    assert not season_matches("Kharif", "Rabi")


def test_irrigation_matching():
    assert get_irrigation_match_type("Rainfed", "rainfed") == "MATCH"
    assert get_irrigation_match_type("Irrigated", "rainfed") == "NO_MATCH"
    assert get_irrigation_match_type("Irrigated", "irrigated") == "MATCH"
    assert get_irrigation_match_type("Rainfed", "irrigated") == "NO_MATCH"


def test_maturity_matching():
    assert maturity_matches(150, 160, max_days=165)
    assert not maturity_matches(150, 175, max_days=165)
    assert maturity_matches(160, 175, min_days=155)
    assert not maturity_matches(140, 150, min_days=155)


def test_query_parser():
    q = (
        "I am a farmer from Telangana. I want to grow cotton in Kharif season "
        "under rainfed conditions and need a variety maturing within 160 days."
    )
    parsed = parse_farmer_query(q)
    assert parsed["state"] == "Telangana"
    assert parsed["season"] == "Kharif"
    assert parsed["irrigation"] == "rainfed"
    assert parsed["max_maturity_days"] == 160


def test_filter_varieties():
    docs = load_documents()
    # Filter for Southern Zone, Kharif, Rainfed, <= 165 days
    filtered = filter_varieties(
        docs,
        state="Telangana",
        season="Kharif",
        irrigation="rainfed",
        max_maturity_days=165
    )
    assert len(filtered) > 0
    for doc in filtered:
        meta = doc["metadata"]
        assert region_matches(meta["recommended_regions"], "Telangana")
        assert meta["maturity_days_max"] <= 165
