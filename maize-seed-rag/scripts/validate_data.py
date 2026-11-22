"""
Validate the processed RAG documents.

Usage:
    python scripts/validate_data.py
"""

import sys
import json
from pathlib import Path

# Add project root to path
project_root = Path(__file__).parent.parent
sys.path.insert(0, str(project_root))

from src.retrieval.metadata_filter import load_documents


def validate_documents():
    """Validate document structure and metadata."""
    
    print("=" * 60)
    print("VALIDATING MAIZE RAG DOCUMENTS")
    print("=" * 60)
    
    try:
        documents = load_documents()
        print(f"\n✓ Successfully loaded {len(documents)} documents")
    except Exception as e:
        print(f"\n✗ Failed to load documents: {e}")
        return
    
    # Validate structure
    required_keys = ["id", "page_content", "metadata"]
    metadata_keys = ["variety_id", "variety_name", "season", "irrigation_condition"]
    
    errors = []
    warnings = []
    
    for idx, doc in enumerate(documents):
        doc_id = doc.get("id", f"document_{idx}")
        
        # Check required keys
        for key in required_keys:
            if key not in doc:
                errors.append(f"Document {doc_id}: Missing '{key}'")
        
        # Check metadata
        metadata = doc.get("metadata", {})
        for key in metadata_keys:
            if key not in metadata or metadata[key] is None:
                warnings.append(f"Document {doc_id}: Missing metadata '{key}'")
    
    # Report
    print(f"\n{'='*60}")
    print("VALIDATION REPORT")
    print(f"{'='*60}")
    print(f"Total documents: {len(documents)}")
    print(f"Errors: {len(errors)}")
    print(f"Warnings: {len(warnings)}")
    
    if errors:
        print("\n⚠ ERRORS:")
        for error in errors[:10]:  # Show first 10
            print(f"  - {error}")
        if len(errors) > 10:
            print(f"  ... and {len(errors) - 10} more")
    
    if warnings:
        print("\n⚠ WARNINGS:")
        for warning in warnings[:10]:  # Show first 10
            print(f"  - {warning}")
        if len(warnings) > 10:
            print(f"  ... and {len(warnings) - 10} more")
    
    if not errors and not warnings:
        print("\n✓ All validations passed!")
    
    # Statistics
    print(f"\n{'='*60}")
    print("STATISTICS")
    print(f"{'='*60}")
    
    seasons = {}
    irrigation = {}
    
    for doc in documents:
        metadata = doc.get("metadata", {})
        
        season = metadata.get("season")
        if season is None:
            season = "UNKNOWN"
        seasons[season] = seasons.get(season, 0) + 1
        
        irrig = metadata.get("irrigation_condition")
        if irrig is None:
            irrig = "UNKNOWN"
        irrigation[irrig] = irrigation.get(irrig, 0) + 1
    
    print("\nSeasons:")
    # Sort with None-safe key function
    for season, count in sorted(seasons.items(), key=lambda x: (x[0] is None, x[0])):
        print(f"  {season}: {count}")
    
    print("\nIrrigation:")
    # Sort with None-safe key function
    for irrig, count in sorted(irrigation.items(), key=lambda x: (x[0] is None, x[0])):
        print(f"  {irrig}: {count}")


if __name__ == "__main__":
    validate_documents()
