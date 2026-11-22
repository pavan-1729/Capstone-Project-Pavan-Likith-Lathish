#!/usr/bin/env python
"""
Ingestion script runner for building Cotton RAG documents.
"""
import sys
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent
if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))

from src.ingestion.document_builder import build_documents

if __name__ == "__main__":
    build_documents()
