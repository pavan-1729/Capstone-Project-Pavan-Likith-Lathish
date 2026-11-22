"""
Main script for Soybean Seed RAG System using Firebase Local LLM.

This version uses a local LLM server (via Firebase Realtime Database queue)
instead of Google Gemini API.
"""

import os
import argparse
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

from src.retrieval import SoybeanRetriever, parse_farmer_query
from src.recommender import recommend_varieties_firebase, FIREBASE_RECOMMENDER_AVAILABLE
from src.client import FirebaseLLMClient, FIREBASE_AVAILABLE


def check_dependencies():
    """Check if Firebase dependencies are installed."""
    if not FIREBASE_AVAILABLE:
        print("❌ Firebase dependencies not installed!")
        print("\n📥 Install with:")
        print("   pip install firebase-admin")
        print("\n📖 See FIREBASE_LLM_SETUP.md for complete setup guide")
        return False
    
    if not FIREBASE_RECOMMENDER_AVAILABLE:
        print("❌ Firebase recommender not available!")
        return False
    
    return True


def check_configuration():
    """Check if Firebase is properly configured."""
    database_url = os.getenv('FIREBASE_DATABASE_URL')
    service_account = os.getenv('FIREBASE_SERVICE_ACCOUNT_PATH', 'serviceAccountKey.json')
    
    issues = []
    
    if not database_url:
        issues.append("FIREBASE_DATABASE_URL not set in .env")
    
    if not os.path.exists(service_account):
        issues.append(f"Service account key not found: {service_account}")
    
    if issues:
        print("❌ Configuration issues:")
        for issue in issues:
            print(f"   - {issue}")
        print("\n📖 See FIREBASE_LLM_SETUP.md for setup instructions")
        return False
    
    return True


def display_recommendations(recommendations: dict):
    """Display recommendations in a formatted way."""
    print("\n" + "="*70)
    print("🌱 SOYBEAN VARIETY RECOMMENDATIONS (Firebase LLM)")
    print("="*70)
    
    for rec in recommendations.get("recommendations", []):
        print(f"\n{rec['rank']}. {rec['variety_name']} (ID: {rec['variety_id']})")
        print(f"   Maturity: {rec.get('maturity', 'N/A')}")
        print(f"   Irrigation: {rec.get('irrigation', 'N/A')}")
        print(f"   Reason: {rec['reason']}")
    
    print("\n" + "-"*70)
    
    if recommendations.get('final_note'):
        print(f"📝 Note: {recommendations['final_note']}")
    
    if recommendations.get('warnings'):
        print(f"\n⚠️  Warnings:")
        for warning in recommendations['warnings']:
            print(f"   - {warning}")
    
    print(f"\n🤖 Model: {recommendations.get('model', 'firebase-llm')}")
    print("="*70)


def interactive_mode():
    """Run in interactive mode."""
    print("\n🌱 SOYBEAN SEED RAG SYSTEM - FIREBASE LLM MODE")
    print("="*70)
    print("\n💡 Using LOCAL LLM via Firebase Realtime Database")
    print("   Make sure the LLM server is running on the other laptop!\n")
    print("Enter your queries below. Type 'quit', 'exit', or 'q' to stop.")
    print("-"*70)
    
    # Initialize
    try:
        llm_client = FirebaseLLMClient()
        print("✓ Firebase LLM client initialized")
        
        # Check if server is responsive
        pending = llm_client.get_pending_jobs_count()
        print(f"✓ Firebase connection OK (pending jobs: {pending})")
        
    except Exception as e:
        print(f"\n❌ Failed to initialize Firebase LLM client: {e}")
        print("\n📖 See FIREBASE_LLM_SETUP.md for troubleshooting")
        return
    
    retriever = SoybeanRetriever()
    print("✓ Soybean document retriever loaded")
    print("\n" + "="*70)
    
    while True:
        try:
            # Get query
            query = input("\n🌱 Query: ").strip()
            
            if query.lower() in ['quit', 'exit', 'q']:
                print("\n👋 Goodbye!")
                break
            
            if not query:
                continue
            
            # Parse query
            print("\n🔍 Parsing query...")
            filters = parse_farmer_query(query)
            
            # Show extracted filters
            filter_str = ", ".join(
                f"{k}={v}" for k, v in filters.items() if v is not None
            )
            if filter_str:
                print(f"   Filters: {filter_str}")
            else:
                print("   No specific filters detected (showing candidate varieties)")
            
            # Retrieve candidates
            print("\n🔎 Retrieving candidate varieties...")
            candidates = retriever.retrieve(**filters)
            print(f"   Found {len(candidates)} candidate varieties")
            
            if not candidates:
                print("\n❌ No varieties found matching your criteria.")
                print("   Try broader search terms or different filters.")
                continue
            
            # Get recommendations
            print("\n🤖 Generating recommendations via Firebase LLM...")
            print("   (This may take 10-30 seconds depending on your LLM server)")
            
            recommendations = recommend_varieties_firebase(
                query,
                candidates,
                llm_client=llm_client
            )
            
            # Display results
            display_recommendations(recommendations)
        
        except KeyboardInterrupt:
            print("\n\n👋 Interrupted. Goodbye!")
            break
        
        except Exception as e:
            print(f"\n❌ Error: {e}")
            print("   Type 'quit' to exit or try another query.")


def single_query_mode(query: str):
    """Process a single query."""
    print("\n🌱 SOYBEAN SEED RAG SYSTEM - FIREBASE LLM MODE")
    print("="*70)
    print(f"\n📝 Query: {query}")
    
    try:
        # Initialize
        llm_client = FirebaseLLMClient()
        retriever = SoybeanRetriever()
        
        # Parse query
        print("\n🔍 Parsing query...")
        filters = parse_farmer_query(query)
        
        # Retrieve candidates
        print("🔎 Retrieving candidates...")
        candidates = retriever.retrieve(**filters)
        print(f"   Found {len(candidates)} varieties")
        
        if not candidates:
            print("\n❌ No varieties found matching your criteria.")
            return
        
        # Get recommendations
        print("\n🤖 Generating recommendations via Firebase LLM...")
        recommendations = recommend_varieties_firebase(
            query,
            candidates,
            llm_client=llm_client
        )
        
        # Display results
        display_recommendations(recommendations)
    
    except Exception as e:
        print(f"\n❌ Error: {e}")
        import traceback
        traceback.print_exc()


def main():
    """Main entry point."""
    parser = argparse.ArgumentParser(
        description="Soybean Seed RAG System with Firebase Local LLM"
    )
    parser.add_argument(
        "--query",
        type=str,
        help="Single query to process"
    )
    parser.add_argument(
        "--interactive",
        action="store_true",
        help="Run in interactive mode"
    )
    parser.add_argument(
        "--check",
        action="store_true",
        help="Check Firebase configuration and connection"
    )
    
    args = parser.parse_args()
    
    if not check_dependencies():
        return 1
    
    if not check_configuration():
        return 1
    
    if args.check:
        print("\n✓ All checks passed!")
        print("\n💡 Tips:")
        print("   - Make sure LLM server is running on other laptop")
        print("   - Run 'python server.py' on that laptop")
        print("   - See FIREBASE_LLM_SETUP.md for details")
        return 0
    
    elif args.query:
        single_query_mode(args.query)
        return 0
    
    elif args.interactive:
        interactive_mode()
        return 0
    
    else:
        interactive_mode()
        return 0


if __name__ == "__main__":
    exit(main())
