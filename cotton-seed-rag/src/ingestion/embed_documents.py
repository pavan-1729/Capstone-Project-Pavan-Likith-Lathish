import json
from pathlib import Path
from typing import List, Dict, Any

import numpy as np
from tqdm import tqdm
from sentence_transformers import SentenceTransformer


PROJECT_ROOT = Path(__file__).resolve().parent.parent.parent
INPUT_FILE = PROJECT_ROOT / "data" / "processed" / "cotton_rag_documents.json"
OUTPUT_EMBEDDINGS = PROJECT_ROOT / "data" / "processed" / "embeddings.npy"
OUTPUT_IDS = PROJECT_ROOT / "data" / "processed" / "embedding_ids.json"

MODEL_NAME = "BAAI/bge-m3"


def load_documents() -> List[Dict[str, Any]]:
    if not INPUT_FILE.exists():
        raise FileNotFoundError(
            f"Cotton processed documents not found at {INPUT_FILE}. "
            f"Please run src/ingestion/document_builder.py first."
        )
    with open(INPUT_FILE, "r", encoding="utf-8") as f:
        return json.load(f)


def generate_embeddings():
    print("=" * 60)
    print("COTTON RAG DENSE EMBEDDING GENERATION")
    print("=" * 60)

    documents = load_documents()
    print(f"\nLoaded {len(documents)} cotton documents.")
    print(f"Loading embedding model: {MODEL_NAME}")

    model = SentenceTransformer(MODEL_NAME)
    print("Model loaded successfully.")

    texts = [doc["page_content"] for doc in documents]
    document_ids = [doc["id"] for doc in documents]

    print(f"Generating dense vector embeddings for {len(texts)} varieties...")
    embeddings = model.encode(
        texts,
        batch_size=16,
        show_progress_bar=True,
        normalize_embeddings=True,
        convert_to_numpy=True,
    )

    print(f"\nEmbedding tensor shape: {embeddings.shape}")

    OUTPUT_EMBEDDINGS.parent.mkdir(parents=True, exist_ok=True)
    np.save(OUTPUT_EMBEDDINGS, embeddings)

    with open(OUTPUT_IDS, "w", encoding="utf-8") as f:
        json.dump(document_ids, f, indent=2, ensure_ascii=False)

    print("\nSaved artifacts:")
    print(f" - Dense Embeddings: {OUTPUT_EMBEDDINGS}")
    print(f" - Document IDs:     {OUTPUT_IDS}")
    print("Embedding generation completed successfully.")


if __name__ == "__main__":
    generate_embeddings()
