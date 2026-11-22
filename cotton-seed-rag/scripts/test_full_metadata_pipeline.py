#!/usr/bin/env python
"""
End-to-End Filter & LLM Integration Test for Cotton Seed Advisory.
"""
import sys
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent
if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))

from src.retrieval.rag_retriever import CottonRetriever
from src.retrieval.query_parser import parse_farmer_query
from src.recommender.gemini_recommender import recommend_varieties


def run_pipeline_test():
    print("=" * 70)
    print("🧪 COTTON SEED RAG - END-TO-END PIPELINE INTEGRATION TEST")
    print("=" * 70)

    test_query = (
        "I am a farmer from Maharashtra. I want to cultivate cotton during Kharif season "
        "under rainfed conditions and I need a variety that matures within 165 days with drought tolerance."
    )
    print(f"\nFarmer Query:\n\"{test_query}\"")

    # Step 1: Query Parsing
    parsed = parse_farmer_query(test_query)
    print("\n1. Extracted Constraints:")
    for k, v in parsed.items():
        print(f"   - {k}: {v}")

    # Step 2: Metadata Filtering
    retriever = CottonRetriever(load_embeddings=False)
    candidates = retriever.retrieve(
        state=parsed["state"],
        season=parsed["season"],
        irrigation=parsed["irrigation"],
        max_maturity_days=parsed["max_maturity_days"],
    )
    print(f"\n2. Retrieved Candidates: {len(candidates)} varieties")
    for idx, c in enumerate(candidates[:5], start=1):
        m = c["metadata"]
        print(f"   [{idx}] {m['variety_name']} ({m['variety_id']}) - {m['maturity_days_min']}-{m['maturity_days_max']} days | {m['recommended_regions']}")

    # Step 3: LLM Recommender & Citation Grounding
    print("\n3. Calling Grounded Gemini Recommender...")
    response = recommend_varieties(test_query, candidates, top_k=3)

    print("\n" + "=" * 70)
    print("RECOMMENDATION RESULTS")
    print("=" * 70)
    print(f"Model Used: {response.get('model_used')}")
    print(f"Candidates Evaluated: {response.get('candidates_count')}")

    for rec in response.get("recommendations", []):
        print(f"\nRank {rec['rank']}: {rec['variety_name']} ({rec['variety_id']})")
        print(f"  Maturity:   {rec['maturity']}")
        print(f"  Irrigation: {rec['irrigation']}")
        print(f"  Reason:     {rec['reason']}")

    if response.get("final_note"):
        print(f"\nNote: {response['final_note']}")
    if response.get("warnings"):
        print("\nWarnings:")
        for w in response["warnings"]:
            print(f"  - {w}")

    print("\n✅ End-to-end integration test completed successfully.")


if __name__ == "__main__":
    run_pipeline_test()
