"""
Firebase LLM-based recommendation engine for Cotton.

Uses a local LLM server (via Firebase queue) to rank and explain cotton variety recommendations.
"""

import json
import re
from typing import List, Dict, Any, Optional

from src.client.firebase_llm_client import FirebaseLLMClient


def recommend_varieties_firebase(
    query: str,
    candidates: List[Dict[str, Any]],
    llm_client: Optional[FirebaseLLMClient] = None,
    top_k: int = 5
) -> Dict[str, Any]:
    """
    Use Firebase LLM to rank candidate cotton varieties and provide explanations.

    Args:
        query: Original farmer query
        candidates: List of candidate variety documents
        llm_client: FirebaseLLMClient instance (created if not provided)
        top_k: Number of top recommendations to return

    Returns:
        Dictionary with ranked recommendations, notes, and warnings
    """
    if not candidates:
        return {
            "recommendations": [],
            "final_note": "No cotton varieties matched the specified criteria.",
            "warnings": ["Try broadening your search criteria or adjusting maturity constraints."],
            "model": "firebase-llm"
        }

    # Create client if not provided
    if llm_client is None:
        llm_client = FirebaseLLMClient()

    # Limit candidates to prevent context overflow
    max_candidates = 25
    if len(candidates) > max_candidates:
        candidates = candidates[:max_candidates]
        print(f"⚠️  Limited to {max_candidates} candidates for LLM processing")

    # Build context
    context = _build_candidate_context(candidates)

    # Build prompt
    prompt = _build_ranking_prompt(query, context, top_k)

    # Call LLM via Firebase
    try:
        print(f"🔮 Generating cotton recommendations using Firebase LLM...")
        response = llm_client.generate_content(prompt, json_mode=True)

        # Parse and validate response
        result = _parse_json_response(response)
        _validate_recommendation(result, top_k)

        # Hallucination check against candidate IDs
        valid_ids = {doc.get("id") for doc in candidates}
        valid_ids.update({doc.get("metadata", {}).get("variety_id") for doc in candidates})

        validated_recs = []
        for rec in result.get("recommendations", []):
            v_id = rec.get("variety_id")
            if v_id in valid_ids:
                validated_recs.append(rec)
            else:
                print(f"⚠️  Warning: Filtered hallucinated variety ID '{v_id}'.")

        result["recommendations"] = validated_recs[:top_k] if validated_recs else result.get("recommendations", [])[:top_k]
        result["model"] = "firebase-llm"

        return result

    except Exception as e:
        print(f"❌ Firebase LLM error: {e}")
        # Fallback to simple filtering-based ranking
        return _fallback_ranking(query, candidates, top_k)


def _build_candidate_context(candidates: List[Dict[str, Any]]) -> str:
    """Build formatted context string from candidate cotton documents."""
    context_lines = []

    for i, doc in enumerate(candidates, 1):
        meta = doc.get('metadata', {})

        variety_name = meta.get('variety_name', 'Unknown')
        variety_id = doc.get('id', meta.get('variety_id', 'unknown'))
        context_lines.append(f"\n{i}. {variety_name} (ID: {variety_id})")

        category = meta.get('category', 'Certified Hybrid/Variety')
        context_lines.append(f"   Category: {category}")

        maturity_min = meta.get('maturity_days_min', 'N/A')
        maturity_max = meta.get('maturity_days_max', 'N/A')
        if maturity_min != 'N/A' and maturity_max != 'N/A':
            context_lines.append(f"   Maturity: {maturity_min}-{maturity_max} days")
        else:
            context_lines.append(f"   Maturity: Unknown")

        irrigation = meta.get('irrigation_condition', 'UNKNOWN')
        context_lines.append(f"   Irrigation: {irrigation}")

        regions = meta.get('recommended_regions')
        if regions:
            context_lines.append(f"   Regions: {regions}")

        zone = meta.get('agro_climatic_zone')
        if zone:
            context_lines.append(f"   Zone: {zone}")

        yield_val = meta.get('yield_value')
        if yield_val:
            context_lines.append(f"   Yield Potential: {yield_val}")

        staple = meta.get('grain_type')
        if staple:
            context_lines.append(f"   Staple/Fiber: {staple}")

        traits = meta.get('special_traits')
        if traits:
            context_lines.append(f"   Traits: {traits}")

        resistances = []
        for r_name in ['drought_tolerance', 'heat_tolerance', 'waterlogging_tolerance', 'pink_stem_borer', 'stem_borer']:
            r_val = meta.get(r_name)
            if r_val and r_val.lower() not in ['none', 'susceptible', 'unknown', 'n/a']:
                resistances.append(f"{r_name.replace('_', ' ').title()}: {r_val}")
        if resistances:
            context_lines.append(f"   Tolerances: {', '.join(resistances)}")

    return '\n'.join(context_lines)


def _build_ranking_prompt(query: str, context: str, top_k: int) -> str:
    prompt = f"""You are an agricultural expert specializing in Indian cotton agronomy, ICAR-CICR standards, and farmer seed advisory.

FARMER'S QUERY:
{query}

CANDIDATE COTTON VARIETIES (Pre-filtered from verified ICAR-CICR catalog):
{context}

TASK:
Rank these cotton varieties from most suitable to least suitable based on the farmer's query and farm conditions.
Provide your top {top_k} recommendations.

STRICT GROUNDING RULES:
1. Recommend ONLY varieties from the candidate list above.
2. Accurately report documented maturity days and irrigation compatibility.
3. Highlight fiber qualities (staple length, ginning outturn) and pest/disease resilience (bollworm, sucking pests, blight).
4. Provide actionable agronomic reasons citing why the variety fits the state/zone and soil.
5. Provide practical management warnings (e.g. bollworm monitoring, spacing, drainage).

OUTPUT FORMAT (JSON):
{{
  "recommendations": [
    {{
      "rank": 1,
      "variety_id": "COT_VER_XXX",
      "variety_name": "Variety Name",
      "maturity": "XX-XX days",
      "irrigation": "rainfed/irrigated/UNKNOWN",
      "reason": "Brief agronomic explanation of why this variety is suitable"
    }}
  ],
  "final_note": "Brief summary agronomic advisory note for the farmer",
  "warnings": ["Key crop management alerts"]
}}

Respond with ONLY the JSON object. No other text."""
    return prompt


def _parse_json_response(response: str) -> Dict[str, Any]:
    text = response.strip()

    if text.startswith("```"):
        text = re.sub(r'^```(?:json)?\s*\n', '', text)
        text = re.sub(r'\n```\s*$', '', text)

    json_match = re.search(r'\{.*\}', text, re.DOTALL)
    if json_match:
        text = json_match.group(0)

    try:
        return json.loads(text)
    except json.JSONDecodeError as e:
        print(f"⚠️  JSON parsing failed: {e}")
        print(f"Response: {text[:200]}...")
        raise ValueError(f"Failed to parse LLM response as JSON: {e}")


def _validate_recommendation(result: Dict[str, Any], expected_top_k: int):
    if 'recommendations' not in result:
        raise ValueError("Missing 'recommendations' field")

    if not isinstance(result['recommendations'], list):
        raise ValueError("'recommendations' must be a list")

    if len(result['recommendations']) == 0:
        raise ValueError("'recommendations' list is empty")

    for i, rec in enumerate(result['recommendations'], 1):
        required_fields = ['rank', 'variety_id', 'variety_name', 'reason']
        for field in required_fields:
            if field not in rec:
                raise ValueError(f"Recommendation {i} missing field: {field}")


def _fallback_ranking(
    query: str,
    candidates: List[Dict[str, Any]],
    top_k: int
) -> Dict[str, Any]:
    recommendations = []

    for i, doc in enumerate(candidates[:top_k], 1):
        meta = doc.get('metadata', {})

        maturity_min = meta.get('maturity_days_min', 'N/A')
        maturity_max = meta.get('maturity_days_max', 'N/A')
        maturity = f"{maturity_min}-{maturity_max} days" if maturity_min != 'N/A' else "Unknown"

        name = meta.get('variety_name', 'Unknown')
        regions = meta.get('recommended_regions', 'all India')
        yield_val = meta.get('yield_value', 'N/A')

        recommendations.append({
            "rank": i,
            "variety_id": doc.get('id', meta.get('variety_id', 'unknown')),
            "variety_name": name,
            "maturity": maturity,
            "irrigation": meta.get('irrigation_condition', 'UNKNOWN'),
            "reason": f"Adapted for {regions} with potential yield of {yield_val}. Matched your criteria from ICAR-CICR cotton catalog."
        })

    return {
        "recommendations": recommendations,
        "final_note": "Recommendations based on deterministic metadata matching (LLM ranking unavailable).",
        "warnings": ["LLM ranking unavailable — using deterministic agronomic ordering."],
        "model": "fallback"
    }
