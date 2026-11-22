"""
Integration tests for the complete Soybean Seed RAG pipeline.
"""

import os
import pytest
from src.ingestion.document_builder import build_soybean_documents
from src.recommender.gemini_recommender import recommend_varieties
from src.retrieval.query_parser import parse_farmer_query
from src.retrieval.rag_retriever import SoybeanRetriever


def test_document_builder_pipeline(tmp_path):
    csv_path = os.path.join("data", "raw", "soybean_varieties.csv")
    temp_json = tmp_path / "temp_soybean_docs.json"

    docs = build_soybean_documents(csv_path, str(temp_json))
    assert len(docs) == 184
    assert temp_json.exists()

    first_doc = docs[0]
    assert "page_content" in first_doc
    assert "Soybean Variety:" in first_doc["page_content"]
    assert first_doc["metadata"]["crop"] == "Soybean"


def test_end_to_end_query_maharashtra():
    query = "I need rainfed soybean for Maharashtra, Kharif season, within 95 days"

    # Step 1: Parse
    filters = parse_farmer_query(query)
    assert filters["state"] == "Maharashtra"
    assert filters["season"] == "Kharif"
    assert filters["max_maturity"] == 95

    # Step 2: Retrieve
    retriever = SoybeanRetriever()
    candidates = retriever.retrieve(**filters)
    assert len(candidates) > 0

    # Step 3: Recommend (mock / deterministic)
    recommendations = recommend_varieties(query, candidates, top_n=3, mock=True)
    assert len(recommendations["recommendations"]) > 0
    assert recommendations["model_used"] == "deterministic-fallback"

    top_rec = recommendations["recommendations"][0]
    assert top_rec["rank"] == 1
    assert "variety_name" in top_rec
    assert "reason" in top_rec


def test_end_to_end_query_high_protein():
    query = "Suggest high protein soybean for Madhya Pradesh under 100 days"
    retriever = SoybeanRetriever()
    candidates = retriever.retrieve(query=query)
    assert len(candidates) > 0

    recommendations = recommend_varieties(query, candidates, top_n=2, mock=True)
    assert len(recommendations["recommendations"]) == 2
    for r in recommendations["recommendations"]:
        assert r["variety_id"].startswith("SOY_VER_")
