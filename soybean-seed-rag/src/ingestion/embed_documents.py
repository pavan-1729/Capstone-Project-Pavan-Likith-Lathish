"""
Soybean Document Embeddings Generator
====================================
Generates dense or semantic vector embeddings for soybean variety documents.
Supports HuggingFace SentenceTransformers (e.g. BAAI/bge-m3 or all-MiniLM-L6-v2)
with fallback for lightweight semantic indexing.
"""

import json
import logging
import os
from pathlib import Path
from typing import List, Optional

import numpy as np

logger = logging.getLogger(__name__)


def generate_document_embeddings(
    documents_json_path: str,
    output_embeddings_path: str,
    output_ids_path: str,
    model_name: str = "BAAI/bge-m3",
) -> None:
    """
    Generate and save embeddings for processed soybean documents.

    Args:
        documents_json_path: Path to soybean_rag_documents.json
        output_embeddings_path: Path to save .npy embeddings
        output_ids_path: Path to save .json list of document IDs
        model_name: Embedding model identifier
    """
    if not os.path.exists(documents_json_path):
        raise FileNotFoundError(f"Documents file not found: {documents_json_path}")

    with open(documents_json_path, "r", encoding="utf-8") as f:
        docs = json.load(f)

    doc_ids = [d["id"] for d in docs]
    texts = [d["page_content"] for d in docs]

    print(f"Loaded {len(docs)} documents for embedding generation.")

    try:
        from sentence_transformers import SentenceTransformer
        print(f"Loading embedding model: {model_name}...")
        model = SentenceTransformer(model_name)
        embeddings = model.encode(texts, show_progress_bar=True, normalize_embeddings=True)
        embeddings = np.array(embeddings, dtype=np.float32)
    except ImportError:
        logger.warning(
            "sentence-transformers not installed. Generating normalized frequency-based embeddings as fallback."
        )
        print("Note: sentence-transformers not installed. Generating standard vector representations.")
        # Lightweight vocabulary representation as fallback
        vocab = {}
        for text in texts:
            for word in text.lower().split():
                if word.isalnum() and word not in vocab:
                    vocab[word] = len(vocab)
        
        vecs = np.zeros((len(texts), min(len(vocab), 1024)), dtype=np.float32)
        for i, text in enumerate(texts):
            for word in text.lower().split():
                if word in vocab and vocab[word] < 1024:
                    vecs[i, vocab[word]] += 1.0
            norm = np.linalg.norm(vecs[i])
            if norm > 0:
                vecs[i] /= norm
        embeddings = vecs

    os.makedirs(os.path.dirname(output_embeddings_path), exist_ok=True)
    os.makedirs(os.path.dirname(output_ids_path), exist_ok=True)

    np.save(output_embeddings_path, embeddings)
    with open(output_ids_path, "w", encoding="utf-8") as f:
        json.dump(doc_ids, f, indent=2)

    print(f"Saved embeddings matrix shape {embeddings.shape} to: {output_embeddings_path}")
    print(f"Saved document IDs to: {output_ids_path}")
