"""
Script to build entity-level RAG documents for soybean varieties.
"""

import os
import sys

# Ensure root directory is in sys.path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from src.ingestion.document_builder import build_soybean_documents

def main():
    csv_paths = [
        os.path.join("data", "soy.csv"),
        os.path.join("data", "raw", "soybean_varieties.csv"),
        os.path.join("data", "soybean_varieties_master_database.csv"),
    ]
    
    csv_path = None
    for p in csv_paths:
        if os.path.exists(p):
            csv_path = p
            break

    if not csv_path:
        print("ERROR: Could not find soybean varieties CSV in data/ or data/raw/")
        sys.exit(1)

    # If data/soy.csv is present, also sync it to data/raw/soybean_varieties.csv
    raw_path = os.path.join("data", "raw", "soybean_varieties.csv")
    if os.path.abspath(csv_path) != os.path.abspath(raw_path):
        import shutil
        os.makedirs(os.path.dirname(raw_path), exist_ok=True)
        shutil.copy2(csv_path, raw_path)

    output_path = os.path.join("data", "processed", "soybean_rag_documents.json")

    print("=" * 60)
    print("BUILDING SOYBEAN ENTITY-LEVEL RAG DOCUMENTS")
    print("=" * 60)

    docs = build_soybean_documents(csv_path=csv_path, output_path=output_path)

    print(f"Input file      : {csv_path}")
    print(f"Documents       : {len(docs)}")
    print(f"Output file     : {output_path}")
    print("Document generation completed.")
    print("=" * 60)

if __name__ == "__main__":
    main()
