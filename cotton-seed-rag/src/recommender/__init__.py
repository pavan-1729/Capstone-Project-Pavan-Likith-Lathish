"""Recommender package for ranking and generating cotton variety advice."""
from src.recommender.gemini_recommender import (
    build_recommendation_prompt,
    build_candidate_context,
    clean_json_response,
    recommend_varieties,
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
    "build_recommendation_prompt",
    "build_candidate_context",
    "clean_json_response",
    "FIREBASE_RECOMMENDER_AVAILABLE",
]
