"""Ingestion module for document building and embedding generation."""
from src.ingestion.document_builder import build_soybean_documents
from src.ingestion.embed_documents import generate_document_embeddings

__all__ = ["build_soybean_documents", "generate_document_embeddings"]
