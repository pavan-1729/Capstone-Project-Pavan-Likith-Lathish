"""
Unit tests for Query Parser, Metadata Filter, and Soybean Retriever.
"""

import pytest
from src.retrieval.metadata_filter import IrrigationMatch, MetadataFilter
from src.retrieval.query_parser import parse_farmer_query
from src.retrieval.rag_retriever import SoybeanRetriever


def test_query_parser_state_extraction():
    q1 = "I need soybean seeds for Maharashtra"
    f1 = parse_farmer_query(q1)
    assert f1["state"] == "Maharashtra"

    q2 = "Looking for varieties for MP and Central India"
    f2 = parse_farmer_query(q2)
    assert f2["state"] == "Madhya Pradesh"

    q3 = "Suggest varieties for Dharwad Karnataka region"
    f3 = parse_farmer_query(q3)
    assert f3["state"] == "Karnataka"

    q4 = "Need seeds for Rajasthan kota region"
    f4 = parse_farmer_query(q4)
    assert f4["state"] == "Rajasthan"


def test_query_parser_season_extraction():
    q1 = "Best Kharif soybean varieties"
    f1 = parse_farmer_query(q1)
    assert f1["season"] == "Kharif"

    q2 = "Can I grow soybean in spring season?"
    f2 = parse_farmer_query(q2)
    assert f2["season"] == "Spring"

    q3 = "Soybean for rabi season"
    f3 = parse_farmer_query(q3)
    assert f3["season"] == "Rabi"


def test_query_parser_irrigation_extraction():
    q1 = "I have rainfed land with no irrigation"
    f1 = parse_farmer_query(q1)
    assert f1["irrigation"] == "rainfed"

    q2 = "I have tubewell and assured irrigation"
    f2 = parse_farmer_query(q2)
    assert f2["irrigation"] == "irrigated"

    q3 = "Need varieties for protective irrigation"
    f3 = parse_farmer_query(q3)
    assert f3["irrigation"] == "protective"


def test_query_parser_maturity_extraction():
    q1 = "Soybean that matures within 90 days"
    f1 = parse_farmer_query(q1)
    assert f1["max_maturity"] == 90

    q2 = "Looking for early maturing crop under 95 days"
    f2 = parse_farmer_query(q2)
    assert f2["max_maturity"] == 95

    q3 = "Variety taking at least 100 days"
    f3 = parse_farmer_query(q3)
    assert f3["min_maturity"] == 100

    q4 = "Early maturing variety"
    f4 = parse_farmer_query(q4)
    assert f4["max_maturity"] == 95


def test_query_parser_combined():
    q = "I need rainfed soybean for Maharashtra, Kharif season, within 95 days with high protein"
    f = parse_farmer_query(q)
    assert f["state"] == "Maharashtra"
    assert f["season"] == "Kharif"
    assert f["irrigation"] == "rainfed"
    assert f["max_maturity"] == 95
    assert "high_protein" in f["traits"]


def test_metadata_filter_state():
    meta = {
        "states": ["Maharashtra", "Madhya Pradesh", "Rajasthan"],
        "recommended_regions": ["Central Zone (MP, Maha, Raj)"],
        "agro_climatic_zone": "Central Zone",
    }
    assert MetadataFilter.match_state(meta, "Maharashtra") is True
    assert MetadataFilter.match_state(meta, "Karnataka") is False
    assert MetadataFilter.match_state(meta, None) is True
    assert MetadataFilter.match_state(meta, "Central Zone") is True


def test_metadata_filter_season():
    meta1 = {"season": ["Kharif"], "season_raw": "Kharif"}
    meta2 = {"season": ["Kharif", "Spring"], "season_raw": "Kharif / Spring"}
    meta3 = {"season": ["Rabi"], "season_raw": "Rabi"}

    assert MetadataFilter.match_season(meta1, "Kharif") is True
    assert MetadataFilter.match_season(meta1, "Spring") is False
    assert MetadataFilter.match_season(meta2, "Kharif") is True
    assert MetadataFilter.match_season(meta2, "Spring") is True
    assert MetadataFilter.match_season(meta3, "Rabi") is True
    assert MetadataFilter.match_season(meta3, None) is True


def test_metadata_filter_maturity():
    meta = {"maturity_days_min": 85.0, "maturity_days_max": 95.0}

    assert MetadataFilter.match_maturity(meta, max_maturity=100) is True
    assert MetadataFilter.match_maturity(meta, max_maturity=90) is False
    assert MetadataFilter.match_maturity(meta, min_maturity=80) is True
    assert MetadataFilter.match_maturity(meta, min_maturity=90) is False
    assert MetadataFilter.match_maturity(meta) is True


def test_metadata_filter_irrigation():
    meta_rainfed = {"irrigation_condition": "Rainfed", "irrigation_type": "rainfed"}
    meta_irrigated = {"irrigation_condition": "Irrigated", "irrigation_type": "irrigated"}
    meta_both = {"irrigation_condition": "Rainfed / Irrigated", "irrigation_type": "both"}
    meta_protective = {"irrigation_condition": "Protective Irrigation Required", "irrigation_type": "protective"}
    meta_unknown = {"irrigation_condition": "Unknown", "irrigation_type": "unknown"}

    assert MetadataFilter.classify_irrigation(meta_rainfed, "rainfed") == IrrigationMatch.MATCH
    assert MetadataFilter.classify_irrigation(meta_irrigated, "rainfed") == IrrigationMatch.NO_MATCH
    assert MetadataFilter.classify_irrigation(meta_both, "rainfed") == IrrigationMatch.MATCH
    assert MetadataFilter.classify_irrigation(meta_both, "irrigated") == IrrigationMatch.MATCH
    assert MetadataFilter.classify_irrigation(meta_protective, "protective") == IrrigationMatch.MATCH
    assert MetadataFilter.classify_irrigation(meta_protective, "rainfed") == IrrigationMatch.UNKNOWN
    assert MetadataFilter.classify_irrigation(meta_unknown, "rainfed") == IrrigationMatch.UNKNOWN
    assert MetadataFilter.classify_irrigation(meta_rainfed, None) == IrrigationMatch.MATCH


def test_soybean_retriever_load_and_retrieve():
    retriever = SoybeanRetriever()
    assert len(retriever.documents) == 184

    # Query with state and season
    results = retriever.retrieve(state="Madhya Pradesh", season="Kharif", top_k=10)
    assert len(results) > 0
    for doc in results:
        states = doc["metadata"]["states"]
        assert "Madhya Pradesh" in states or "MP" in str(doc["metadata"]["recommended_regions"])


def test_soybean_retriever_trait_scoring():
    retriever = SoybeanRetriever()
    results = retriever.retrieve(query="High protein drought tolerant soybean for Maharashtra", top_k=5)
    assert len(results) > 0
    top_doc = results[0]
    assert "_retrieval_score" in top_doc
    assert top_doc["_retrieval_score"] > 10.0
