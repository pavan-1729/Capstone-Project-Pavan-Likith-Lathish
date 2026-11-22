"""Recommender package for ranking and generating variety advice."""
from src.recommender.gemini_recommender import (
    build_recommender_prompt,
    generate_mock_recommendations,
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
    "build_recommender_prompt",
    "generate_mock_recommendations",
    "FIREBASE_RECOMMENDER_AVAILABLE",
]
