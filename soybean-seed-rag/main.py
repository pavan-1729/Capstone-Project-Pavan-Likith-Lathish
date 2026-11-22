"""
Soybean Seed RAG System - Main Entry Point
=========================================
CLI and Interactive interface for soybean variety retrieval and recommendations.
"""

import argparse
import os
import sys
from typing import Any, Dict, List

from dotenv import load_dotenv

load_dotenv()

from src.client.gemini_client import GeminiClient
from src.retrieval.query_parser import parse_farmer_query
from src.retrieval.rag_retriever import SoybeanRetriever
from src.recommender.gemini_recommender import recommend_varieties

EXAMPLE_QUERIES = [
    # Regional Queries
    "I need soybean varieties for Maharashtra",
    "Looking for varieties suitable for Madhya Pradesh",
    "Suggest soybean for Karnataka region",
    "Soybean varieties adapted to Rajasthan",
    
    # Season & Irrigation Queries
    "Kharif season soybean varieties for rainfed land",
    "What soybean can I grow under irrigated conditions?",
    "Need soybean for rainfed conditions within 95 days",
    
    # Trait Queries
    "High protein soybean varieties for Central India",
    "Looking for drought tolerant early maturing soybean under 95 days",
    "Suggest charcoal rot resistant soybean for Maharashtra",
    
    # Combined Farmer Queries
    "I need soybean for Maharashtra in Kharif season, rainfed, within 95 days",
    "Looking for high protein soybean for Madhya Pradesh, rainfed, under 100 days",
    "Suggest irrigated soybean for Karnataka during Kharif, max 105 days",
    "I have rainfed land in Rajasthan, suggest early maturing soybean within 90 days",
]


def print_banner():
    banner = r"""
======================================================================
 🌱  SOYBEAN SEED RAG SYSTEM
     Intelligent Variety Recommendations via Metadata Filtering & LLM
======================================================================
"""
    print(banner)


def print_examples():
    print("\n" + "=" * 60)
    print("EXAMPLE QUERIES")
    print("=" * 60)
    for i, ex in enumerate(EXAMPLE_QUERIES, 1):
        print(f" {i:2d}. {ex}")
    print("=" * 60 + "\n")


def display_results(results: Dict[str, Any], query: str, verbose: bool = False):
    recs = results.get("recommendations", [])
    model_used = results.get("model_used", "unknown")
    final_note = results.get("final_note", "")
    warnings = results.get("warnings", [])

    print("\n" + "=" * 70)
    print("🌱 SOYBEAN VARIETY RECOMMENDATIONS")
    print("=" * 70)

    if not recs:
        print("\n❌ No varieties matched all the specified constraints.")
        if final_note:
            print(f"\n📝 {final_note}")
    else:
        for rec in recs:
            rank = rec.get("rank", "-")
            name = rec.get("variety_name", "Unknown")
            vid = rec.get("variety_id", "")
            mat = rec.get("maturity", "N/A")
            irr = rec.get("irrigation", "N/A")
            yd = rec.get("yield_potential", "N/A")
            reason = rec.get("reason", "")
            strengths = rec.get("key_strengths", [])

            print(f"\n{rank}. {name} (ID: {vid})")
            print(f"   Maturity       : {mat}")
            print(f"   Irrigation     : {irr}")
            if yd != "N/A":
                print(f"   Yield Potential: {yd}")
            if strengths:
                print(f"   Key Strengths  : {', '.join(strengths)}")
            print(f"   Agronomic Match: {reason}")

    print("\n" + "-" * 70)
    if final_note:
        print(f"📝 Note: {final_note}")
    if warnings:
        print("⚠️  Warnings:")
        for w in warnings:
            print(f"   • {w}")
    print(f"🤖 Model: {model_used}")
    print("=" * 70 + "\n")


def run_pipeline(
    query: str,
    retriever: SoybeanRetriever,
    client: GeminiClient,
    top_k: int = 5,
    mock: bool = False,
    verbose: bool = False,
):
    print(f"\n🌱 Query: {query}\n")

    # 1. Query Parsing
    print("🔍 Parsing query...")
    filters = parse_farmer_query(query)
    filter_desc = []
    if filters.get("state"):
        filter_desc.append(f"state={filters['state']}")
    if filters.get("season"):
        filter_desc.append(f"season={filters['season']}")
    if filters.get("irrigation"):
        filter_desc.append(f"irrigation={filters['irrigation']}")
    if filters.get("max_maturity"):
        filter_desc.append(f"max_maturity={filters['max_maturity']} days")
    if filters.get("min_maturity"):
        filter_desc.append(f"min_maturity={filters['min_maturity']} days")
    if filters.get("traits"):
        filter_desc.append(f"traits={', '.join(filters['traits'])}")

    print(f"   Filters: {', '.join(filter_desc) if filter_desc else 'Broad search (no strict filters)'}")

    # 2. Candidate Retrieval
    print("\n🔎 Retrieving candidates...")
    candidates = retriever.retrieve(query=query, top_k=top_k * 3)
    print(f"   Found {len(candidates)} matching varieties")

    if verbose:
        print("\n[VERBOSE] Top candidate pool:")
        for i, c in enumerate(candidates[:6], 1):
            m = c.get("metadata", {})
            print(
                f"   {i}. {c.get('variety_name')} | Mat: {m.get('maturity_days_min')}-{m.get('maturity_days_max')}d | "
                f"Irr: {m.get('irrigation_condition')} | Score: {c.get('_retrieval_score', 0)}"
            )

    # 3. Recommendation Generation
    print("\n🤖 Generating recommendations...")
    recommendations = recommend_varieties(
        query=query,
        candidates=candidates,
        top_n=top_k,
        client=client,
        mock=mock,
    )

    # 4. Display Results
    display_results(recommendations, query, verbose=verbose)


def interactive_mode(retriever: SoybeanRetriever, client: GeminiClient, mock: bool = False, verbose: bool = False):
    print_banner()
    print("Type your farming query below (or type 'help', 'examples', or 'exit'/'quit'):\n")

    while True:
        try:
            user_input = input("🌱 Query > ").strip()
            if not user_input:
                continue

            if user_input.lower() in ["exit", "quit", "q"]:
                print("\nHappy farming! Exiting Soybean Seed RAG System. 🌱\n")
                break
            elif user_input.lower() == "examples":
                print_examples()
                continue
            elif user_input.lower() == "help":
                print("\nAsk natural questions like:")
                print("  'I need soybean for Maharashtra, Kharif, rainfed, under 95 days'")
                print("  'Suggest high oil varieties for Madhya Pradesh'")
                print("  'Type examples for more suggestions.'\n")
                continue

            run_pipeline(
                query=user_input,
                retriever=retriever,
                client=client,
                top_k=5,
                mock=mock,
                verbose=verbose,
            )

        except (KeyboardInterrupt, EOFError):
            print("\nExiting. Happy farming! 🌱\n")
            break


def main():
    parser = argparse.ArgumentParser(
        description="🌱 Soybean Seed RAG System: Intelligent soybean variety recommendations"
    )
    parser.add_argument("--query", "-q", type=str, help="Single query mode")
    parser.add_argument("--interactive", "-i", action="store_true", help="Launch interactive chat session")
    parser.add_argument("--examples", action="store_true", help="Show example queries and exit")
    parser.add_argument("--top-k", "-k", type=int, default=5, help="Number of recommendations to return (default: 5)")
    parser.add_argument("--mock", action="store_true", help="Use deterministic mock recommendation without API calls")
    parser.add_argument("--verbose", "-v", action="store_true", help="Display verbose retrieval logs")

    args = parser.parse_args()

    if args.examples:
        print_examples()
        return

    # Initialize retriever and client
    retriever = SoybeanRetriever()
    client = GeminiClient()

    if args.interactive or (not args.query and not args.interactive):
        if not args.query:
            interactive_mode(retriever, client, mock=args.mock, verbose=args.verbose)
            return

    if args.query:
        run_pipeline(
            query=args.query,
            retriever=retriever,
            client=client,
            top_k=args.top_k,
            mock=args.mock,
            verbose=args.verbose,
        )


if __name__ == "__main__":
    main()
