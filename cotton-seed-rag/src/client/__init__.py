"""Client package for API communication."""
from src.client.gemini_client import generate_with_fallback

try:
    from src.client.firebase_llm_client import FirebaseLLMClient, create_firebase_llm_client
    FIREBASE_AVAILABLE = True
except ImportError:
    FIREBASE_AVAILABLE = False
    FirebaseLLMClient = None
    create_firebase_llm_client = None

__all__ = [
    "generate_with_fallback",
    "FirebaseLLMClient",
    "create_firebase_llm_client",
    "FIREBASE_AVAILABLE"
]
