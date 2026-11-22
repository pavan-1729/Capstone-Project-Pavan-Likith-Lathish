"""
Main entry point for the Cotton Seed RAG Advisory System.

Usage:
    python main.py --query "I am from Maharashtra and want to grow cotton in Kharif under rainfed conditions maturing within 165 days"
    python main.py --interactive
"""

import argparse
import json
import sys
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent
if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))

from src.retrieval.rag_retriever import CottonRetriever
from src.retrieval.query_parser import parse_farmer_query
from src.recommender.gemini_recommender import recommend_varieties


def format_recommendation(recommendation, verbose=False):
    """Format cotton recommendations for terminal display."""
    lines = []
    lines.append("\n" + "=" * 75)
    lines.append("🌱 COTTON SEED ADVISORY RECOMMENDATIONS (ICAR-CICR GROUNDED)")
    lines.append("=" * 75)

    recs = recommendation.get("recommendations", [])
    if not recs:
        lines.append("\n❌ No suitable cotton varieties matched your exact constraints.")
        if recommendation.get("final_note"):
            lines.append(f"\nNote: {recommendation['final_note']}")
        lines.append("=" * 75)
        return "\n".join(lines)

    lines.append(f"\nTop {len(recs)} Recommended Varieties (from {recommendation.get('candidates_count', 0)} eligible candidates):\n")

    for rec in recs:
        lines.append(f"🏆 Rank #{rec['rank']}: {rec['variety_name']} [ID: {rec['variety_id']}]")
        lines.append(f"   ⏱️  Maturity:   {rec['maturity']}")
        lines.append(f"   💧 Irrigation: {rec['irrigation']}")
        lines.append(f"   📋 Reason:     {rec['reason']}\n")

    if recommendation.get("final_note"):
        lines.append("-" * 75)
        lines.append(f"📝 Agronomic Advisory Note: {recommendation['final_note']}")

    if recommendation.get("warnings"):
        lines.append("\n⚠️  Agronomic Management Alerts:")
        for w in recommendation["warnings"]:
            lines.append(f"   • {w}")

    if recommendation.get("model_used"):
        lines.append(f"\n🤖 Reasoning Engine: {recommendation['model_used']}")

    lines.append("=" * 75)
    return "\n".join(lines)


def run_query(query: str, top_k: int = 5, verbose: bool = False):
    """Execute end-to-end query processing."""
    print(f"\n👨‍🌾 Farmer Query: \"{query}\"")

    print("\n🔍 Step 1: Parsing agro-climatic constraints...")
    parsed = parse_farmer_query(query)
    if verbose:
        print(f"   Parsed constraints: {json.dumps(parsed, indent=2)}")
    else:
        filters_str = ", ".join(f"{k}={v}" for k, v in parsed.items() if v is not None)
        print(f"   Detected: {filters_str or 'None'}")

    print("\n📚 Step 2: Retrieving certified varieties from ICAR catalog...")
    retriever = CottonRetriever(load_embeddings=True)
    candidates = retriever.retrieve(
        state=parsed.get("state"),
        season=parsed.get("season"),
        irrigation=parsed.get("irrigation"),
        max_maturity_days=parsed.get("max_maturity_days"),
        min_maturity_days=parsed.get("min_maturity_days"),
    )
    print(f"   Found {len(candidates)} candidate varieties meeting constraints.")

    print("\n🧠 Step 3: LLM multi-model reasoning and grounded ranking...")
    recommendation = recommend_varieties(query, candidates, top_k=top_k)

    print(format_recommendation(recommendation, verbose=verbose))


def interactive_mode(top_k: int = 5):
    """Run interactive question-answering session."""
    print("\n" + "=" * 75)
    print("🌱 COTTON SEED ADVISOR - INTERACTIVE CONSOLE")
    print("=" * 75)
    print("Ask seed advisory questions (e.g., 'I am from Gujarat, Kharif season, rainfed, <=160 days')")
    print("Type 'exit', 'quit', or 'q' to stop.\n")

    while True:
        try:
            query = input("Farmer Query > ").strip()
            if not query:
                continue
            if query.lower() in ("exit", "quit", "q"):
                print("\nExiting Cotton Seed Advisor. Happy farming!")
                break
            run_query(query, top_k=top_k)
            print()
        except KeyboardInterrupt:
            print("\nExiting.")
            break


def main():
    parser = argparse.ArgumentParser(description="Cotton Seed RAG Advisory System")
    parser.add_argument("--query", "-q", type=str, help="Single farmer query to process")
    parser.add_argument("--interactive", "-i", action="store_true", help="Launch interactive chat mode")
    parser.add_argument("--top-k", "-k", type=int, default=5, help="Number of varieties to recommend (default: 5)")
    parser.add_argument("--verbose", "-v", action="store_true", help="Show verbose internal steps")

    args = parser.parse_args()

    if args.interactive:
        interactive_mode(top_k=args.top_k)
    elif args.query:
        run_query(args.query, top_k=args.top_k, verbose=args.verbose)
    else:
        # Default demo query
        default_q = (
            "I am a farmer from Andhra Pradesh. I want to cultivate cotton during Kharif season "
            "under rainfed conditions and need a variety maturing within 165 days."
        )
        run_query(default_q, top_k=args.top_k, verbose=args.verbose)


if __name__ == "__main__":
    main()
