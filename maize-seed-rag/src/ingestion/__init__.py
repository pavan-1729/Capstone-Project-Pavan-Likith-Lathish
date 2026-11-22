"""
Data ingestion and document processing module.
"""

from src.ingestion.document_builder import build_documents
from src.ingestion.embed_documents import main as generate_embeddings

__all__ = ["build_documents", "generate_embeddings"]
