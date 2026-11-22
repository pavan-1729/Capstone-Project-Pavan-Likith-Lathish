"""
Script to generate document embeddings for soybean varieties.
"""

import os
import sys

# Ensure root directory is in sys.path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from src.ingestion.embed_documents import generate_document_embeddings


def main():
    docs_path = os.path.join("data", "processed", "soybean_rag_documents.json")
    emb_path = os.path.join("data", "processed", "embeddings.npy")
    ids_path = os.path.join("data", "processed", "embedding_ids.json")

    print("=" * 60)
    print("GENERATING SOYBEAN DOCUMENT EMBEDDINGS")
    print("=" * 60)

    generate_document_embeddings(
        documents_json_path=docs_path,
        output_embeddings_path=emb_path,
        output_ids_path=ids_path,
    )
    print("=" * 60)


if __name__ == "__main__":
    main()
