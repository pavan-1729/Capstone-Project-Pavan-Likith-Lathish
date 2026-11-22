#!/usr/bin/env python
"""
Interactive / script tester for dense vector semantic similarity search on Cotton Varieties.
"""
import sys
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent
if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))

from src.retrieval.rag_retriever import CottonRetriever


def test_semantic_queries():
    print("=" * 70)
    print("🌱 COTTON SEED RAG - SEMANTIC SEARCH TESTER")
    print("=" * 70)

    retriever = CottonRetriever(load_embeddings=True)

    test_queries = [
        "High ginning outturn Bt cotton hybrid with drought tolerance for Telangana",
        "Extra long staple cotton for irrigated conditions in Northern Zone Punjab",
        "Early maturing cotton variety resistant to bollworm and moisture stress in Gujarat",
    ]

    for idx, query in enumerate(test_queries, start=1):
        print(f"\n[{idx}] Query: {query}")
        results = retriever.retrieve_hybrid(query=query, top_k=3)
        for r_idx, res in enumerate(results, start=1):
            meta = res.get("metadata", {})
            score = res.get("similarity_score", "N/A")
            print(f"    {r_idx}. {meta.get('variety_name')} (ID: {meta.get('variety_id')})")
            print(f"       Category: {meta.get('category')} | Zone: {meta.get('recommended_regions')}")
            print(f"       Maturity: {meta.get('maturity_days_min')}-{meta.get('maturity_days_max')} days | Yield: {meta.get('yield_value')}")
            print(f"       Traits: {meta.get('special_traits')} | Similarity: {score}")


if __name__ == "__main__":
    test_semantic_queries()
