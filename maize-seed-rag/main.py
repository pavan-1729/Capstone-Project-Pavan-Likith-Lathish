"""
Main entry point for the Maize Seed RAG System.

Usage:
    python main.py --query "Your query here"
    python main.py --interactive
"""

import argparse
import json
import sys
from pathlib import Path

from src.retrieval import MaizeRetriever, parse_farmer_query
from src.recommender import recommend_varieties


def format_recommendation(recommendation, verbose=False):
    """Format recommendation for display."""
    
    lines = []
    lines.append("\n" + "=" * 70)
    lines.append("🌽 MAIZE VARIETY RECOMMENDATIONS")
    lines.append("=" * 70)
    
    if not recommendation["recommendations"]:
        lines.append("\n❌ No suitable varieties found.")
        return "\n".join(lines)
    
    # Display recommendations
    for rec in recommendation["recommendations"]:
        lines.append(f"\n{rec['rank']}. {rec['variety_name']} (ID: {rec['variety_id']})")
        lines.append(f"   Maturity: {rec['maturity']}")
        lines.append(f"   Irrigation: {rec['irrigation']}")
        lines.append(f"   Reason: {rec['reason']}")
    
    # Final note
    if recommendation.get("final_note"):
        lines.append("\n" + "-" * 70)
        lines.append(f"📝 Note: {recommendation['final_note']}")
    
    # Warnings
    if recommendation.get("warnings"):
        lines.append("\n⚠️  Warnings:")
        for warning in recommendation["warnings"]:
            lines.append(f"   - {warning}")
    
    # Model used
    if recommendation.get("model_used"):
        lines.append(f"\n🤖 Model: {recommendation['model_used']}")
    
    lines.append("=" * 70)
    
    return "\n".join(lines)


def run_query(query, verbose=False):
    """Execute a single query."""
    
    print(f"\n📝 Query: {query}\n")
    
    # Parse query
    print("🔍 Parsing query...")
    filters = parse_farmer_query(query)
    
    if verbose:
        print(f"   Filters extracted: {json.dumps(filters, indent=2)}")
    else:
        print(f"   Filters: {', '.join(f'{k}={v}' for k, v in filters.items() if v is not None)}")
    
    # Retrieve candidates
    print("\n🔎 Retrieving candidate varieties...")
    try:
        retriever = MaizeRetriever()
        candidates = retriever.retrieve(**filters)
        print(f"   Found {len(candidates)} candidate varieties")
    except FileNotFoundError as e:
        print(f"\n❌ Error: {e}")
        print("\n💡 Tip: Run 'python scripts/build_documents.py' first to build the document database.")
        return
    
    if not candidates:
        print("\n❌ No varieties match your criteria.")
        print("\n💡 Tip: Try relaxing some constraints.")
        return
    
    # Get recommendations
    print("\n🤖 Generating LLM recommendations...")
    try:
        recommendations = recommend_varieties(query, candidates)
        print(format_recommendation(recommendations, verbose=verbose))
        
    except Exception as e:
        print(f"\n❌ Error during recommendation: {e}")
        if verbose:
            import traceback
            traceback.print_exc()


def interactive_mode():
    """Run in interactive mode."""
    
    print("\n" + "=" * 70)
    print("🌽 MAIZE SEED RAG SYSTEM - INTERACTIVE MODE")
    print("=" * 70)
    print("\nEnter your queries below. Type 'quit' or 'exit' to stop.")
    print("Type 'help' for query examples.\n")
    
    while True:
        try:
            query = input("🌽 Query: ").strip()
            
            if not query:
                continue
            
            if query.lower() in ["quit", "exit", "q"]:
                print("\n👋 Goodbye!")
                break
            
            if query.lower() == "help":
                show_examples()
                continue
            
            run_query(query)
            
        except KeyboardInterrupt:
            print("\n\n👋 Goodbye!")
            break
        except Exception as e:
            print(f"\n❌ Error: {e}")


def show_examples():
    """Show example queries."""
    
    print("\n" + "=" * 70)
    print("📚 EXAMPLE QUERIES")
    print("=" * 70)
    
    examples = [
        "I need maize for Karnataka in Kharif season, rainfed, within 90 days",
        "Looking for irrigated maize varieties for Tamil Nadu, Rabi season",
        "Suggest maize for Maharashtra with maximum 100 days maturity",
        "Rainfed maize for Telangana during Kharif season",
        "I need early maturing maize for Bihar, maximum 85 days"
    ]
    
    for i, example in enumerate(examples, 1):
        print(f"\n{i}. {example}")
    
    print("\n" + "=" * 70)


def main():
    """Main entry point."""
    
    parser = argparse.ArgumentParser(
        description="Maize Seed RAG System - Get personalized maize variety recommendations",
        formatter_class=argparse.RawDescriptionHelpFormatter
    )
    
    parser.add_argument(
        "--query",
        "-q",
        type=str,
        help="Natural language query for maize variety recommendation"
    )
    
    parser.add_argument(
        "--interactive",
        "-i",
        action="store_true",
        help="Run in interactive mode"
    )
    
    parser.add_argument(
        "--examples",
        "-e",
        action="store_true",
        help="Show example queries"
    )
    
    parser.add_argument(
        "--verbose",
        "-v",
        action="store_true",
        help="Show detailed output"
    )
    
    args = parser.parse_args()
    
    # Show examples
    if args.examples:
        show_examples()
        return
    
    # Interactive mode
    if args.interactive:
        interactive_mode()
        return
    
    # Single query mode
    if args.query:
        run_query(args.query, verbose=args.verbose)
        return
    
    # No arguments - show help and enter interactive
    parser.print_help()
    print("\n💡 Starting interactive mode...\n")
    interactive_mode()


if __name__ == "__main__":
    main()
