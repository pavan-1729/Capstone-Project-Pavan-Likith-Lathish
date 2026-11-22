#!/usr/bin/env python
"""
Embedding generation script runner for Cotton Seed RAG.
"""
import sys
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent
if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))

from src.ingestion.embed_documents import generate_embeddings

if __name__ == "__main__":
    generate_embeddings()
