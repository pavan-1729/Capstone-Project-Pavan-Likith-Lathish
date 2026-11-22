import pytest
from src.retrieval.rag_retriever import CottonRetriever
from src.retrieval.query_parser import parse_farmer_query
from src.recommender.gemini_recommender import recommend_varieties


def test_end_to_end_pipeline():
    query = (
        "I am a farmer from Andhra Pradesh wanting to grow cotton in Kharif season "
        "under rainfed conditions with maturity within 165 days."
    )
    parsed = parse_farmer_query(query)
    assert parsed["state"] == "Andhra Pradesh"
    assert parsed["season"] == "Kharif"

    retriever = CottonRetriever(load_embeddings=False)
    candidates = retriever.retrieve(
        state=parsed["state"],
        season=parsed["season"],
        irrigation=parsed["irrigation"],
        max_maturity_days=parsed["max_maturity_days"],
    )
    assert len(candidates) > 0

    # Ensure all candidates are from Southern Zone and Kharif
    for c in candidates:
        meta = c["metadata"]
        assert "Southern Zone" in meta["recommended_regions"]
        assert meta["maturity_days_max"] <= 165

    # Run recommendation
    rec = recommend_varieties(query, candidates, top_k=3)
    assert "recommendations" in rec
    assert len(rec["recommendations"]) <= 3
