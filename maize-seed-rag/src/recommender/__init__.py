"""
Recommendation engine for maize variety selection with Gemini and Firebase LLM support.
"""

from src.recommender.gemini_recommender import (
    recommend_varieties,
    build_candidate_context,
    clean_json_response,
    validate_recommendation
)

try:
    from src.recommender.firebase_recommender import recommend_varieties_firebase
    FIREBASE_RECOMMENDER_AVAILABLE = True
except ImportError:
    FIREBASE_RECOMMENDER_AVAILABLE = False
    recommend_varieties_firebase = None

__all__ = [
    "recommend_varieties",
    "recommend_varieties_firebase",
    "build_candidate_context",
    "clean_json_response",
    "validate_recommendation",
    "FIREBASE_RECOMMENDER_AVAILABLE"
]
