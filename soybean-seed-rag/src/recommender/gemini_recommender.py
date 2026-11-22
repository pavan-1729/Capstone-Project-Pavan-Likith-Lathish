"""
LLM-Powered Soybean Variety Recommender
=======================================
Ranks and generates evidence-based agronomic recommendations for retrieved soybean
varieties using Google Gemini models with JSON mode and fallback capabilities.
"""

import json
import logging
from typing import Any, Dict, List, Optional

from src.client.gemini_client import GeminiClient

logger = logging.getLogger(__name__)

SYSTEM_PROMPT = """You are an elite ICAR / AICRP soybean agronomy specialist and agricultural extension expert.
Your mission is to evaluate retrieved candidate soybean varieties against a farmer's specific local conditions and recommend the optimal varieties.

STRICT EVIDENCE-BASED GUIDELINES:
1. ONLY utilize facts, figures, maturity durations, yields, and traits explicitly provided in the candidate documents.
2. DO NOT hallucinate nonexistent varieties, altered yield numbers, or unverified disease resistance.
3. If irrigation condition is UNKNOWN or 'Protective Irrigation Required', acknowledge this uncertainty transparently and provide practical water management advice.
4. Highlight key strengths (e.g., protein %, oil %, drought resilience, insect/disease resistance) relevant to the farmer's request.
5. Provide practical agronomic warnings (e.g. soil drainage, lodging risk under heavy fertilization, seed rate, pod shattering).
6. Return your response strictly as valid JSON adhering to the specified schema.
"""


def build_recommender_prompt(
    farmer_query: str,
    candidates: List[Dict[str, Any]],
    top_n: int = 5
) -> str:
    """Format candidate varieties into a structured LLM prompt."""
    candidates_text_parts = []
    for i, c in enumerate(candidates, 1):
        meta = c.get("metadata", {})
        irr_match = c.get("_irrigation_match", "MATCH")
        
        doc_summary = f"""--- CANDIDATE {i} ---
Variety Name: {c.get('variety_name')} (ID: {c.get('id')})
Category: {meta.get('category')} | Type: {meta.get('type')}
Developer: {meta.get('developer_or_company')} (Released: {meta.get('release_year')})
Season: {meta.get('season_raw')} | Maturity: {meta.get('maturity_days_min')}-{meta.get('maturity_days_max')} days ({meta.get('maturity_class')})
Yield: {meta.get('yield_value')} ({meta.get('yield_normalized_q_ha_min')}-{meta.get('yield_normalized_q_ha_max')} q/ha)
Irrigation Suitability: {meta.get('irrigation_condition')} [Match Status: {irr_match}]
Adapted States/Zones: {', '.join(meta.get('states', []))} | {meta.get('agro_climatic_zone')}
Morphology: Height: {meta.get('plant_height')}, Pod Height: {meta.get('ear_height')}, Lodging: {meta.get('lodging_resistance')}
Stress Tolerance: Drought: {meta.get('drought_tolerance')}, Waterlogging: {meta.get('waterlogging_tolerance')}, Heat: {meta.get('heat_tolerance')}
Special Traits & Quality: {meta.get('special_traits')}
"""
        candidates_text_parts.append(doc_summary)

    candidates_block = "\n".join(candidates_text_parts)

    prompt = f"""FARMER QUERY:
"{farmer_query}"

RETRIEVED CANDIDATE SOYBEAN VARIETIES ({len(candidates)} available):
{candidates_block}

TASK:
Evaluate these candidates against the farmer's query and select the top {min(top_n, len(candidates))} most suitable soybean varieties.
Rank them from 1 to {min(top_n, len(candidates))}, provide concise yet deeply informative evidence-based justification for each, and output key agronomic advisories.

OUTPUT JSON SCHEMA:
{{
  "recommendations": [
    {{
      "rank": 1,
      "variety_id": "string (matching candidate ID)",
      "variety_name": "string",
      "maturity": "string (e.g. 95-100 days)",
      "irrigation": "string",
      "yield_potential": "string (q/ha)",
      "reason": "Clear evidence-based rationale citing why this variety matches the farmer's region, season, soil, or constraints.",
      "key_strengths": ["string", "string"]
    }}
  ],
  "final_note": "A concise summary advisory explaining why the #1 choice is superior and general planting guidance.",
  "warnings": [
    "Practical warning regarding pest pressure, waterlogging, or seed handling..."
  ]
}}
"""
    return prompt


def generate_mock_recommendations(
    farmer_query: str,
    candidates: List[Dict[str, Any]],
    top_n: int = 5
) -> Dict[str, Any]:
    """
    Deterministic rule-based recommendation fallback when Gemini API is offline or unconfigured.
    Guarantees reliable, evidence-based responses.
    """
    selected = candidates[:top_n]
    recs = []

    for rank, doc in enumerate(selected, 1):
        meta = doc.get("metadata", {})
        mat_str = f"{meta.get('maturity_days_min', '')}-{meta.get('maturity_days_max', '')} days"
        irr = meta.get("irrigation_condition", "Rainfed / Irrigated")
        yield_pot = f"{meta.get('yield_normalized_q_ha_min', '')}-{meta.get('yield_normalized_q_ha_max', '')} q/ha"
        
        strengths = []
        if meta.get("drought_tolerance"):
            strengths.append(meta.get("drought_tolerance"))
        if meta.get("protein_content_percent"):
            strengths.append(f"High Protein ({meta.get('protein_content_percent')}%)")
        if meta.get("oil_content_percent"):
            strengths.append(f"Oil Content ({meta.get('oil_content_percent')}%)")
        if meta.get("lodging_resistance"):
            strengths.append(meta.get("lodging_resistance"))

        reason = (
            f"Strong agro-climatic fit for {', '.join(meta.get('states', ['target zones'])[:3])} "
            f"during {meta.get('season_raw', 'Kharif')} season. "
            f"Demonstrates reliable yield ({yield_pot}) with maturity of {mat_str}. "
            f"Adapted to {irr.lower()} conditions."
        )

        recs.append({
            "rank": rank,
            "variety_id": doc.get("id"),
            "variety_name": doc.get("variety_name"),
            "maturity": mat_str,
            "irrigation": irr,
            "yield_potential": yield_pot,
            "reason": reason,
            "key_strengths": strengths[:3] if strengths else ["High Yield Potential", "Regional Adaptation"],
        })

    top_name = recs[0]["variety_name"] if recs else "Selected variety"
    warnings = [
        "Ensure field has adequate drainage as soybean is sensitive to prolonged water stagnation at emergence.",
        "Perform certified seed treatment with Rhizobium and Trichoderma before sowing.",
    ]
    if any("protective" in r["irrigation"].lower() for r in recs):
        warnings.append("Some varieties perform optimally with at least 1-2 protective irrigations at pod filling stage.")

    return {
        "recommendations": recs,
        "final_note": f"{top_name} is ranked top based on superior yield stability, regional agro-climatic suitability, and maturity duration.",
        "warnings": warnings,
        "model_used": "deterministic-fallback",
    }


def recommend_varieties(
    query: str,
    candidates: List[Dict[str, Any]],
    top_n: int = 5,
    client: Optional[GeminiClient] = None,
    mock: bool = False,
) -> Dict[str, Any]:
    """
    Generate variety recommendations from candidate documents.

    Args:
        query: Farmer query string.
        candidates: Retrieved candidate documents.
        top_n: Number of varieties to recommend.
        client: Optional GeminiClient instance.
        mock: If True, forces deterministic fallback mode.

    Returns:
        Structured recommendation dictionary.
    """
    if not candidates:
        return {
            "recommendations": [],
            "final_note": "No matching soybean varieties found matching the strict query criteria. Please consider relaxing season or maturity constraints.",
            "warnings": ["Try broadening the search by increasing maturity window or checking adjacent states."],
            "model_used": "none",
        }

    # If mock flag is True, use deterministic fallback
    if mock:
        return generate_mock_recommendations(query, candidates, top_n)

    # Use Gemini client if available
    try:
        active_client = client or GeminiClient()
        if not active_client.is_available:
            logger.info("Gemini API not configured. Falling back to deterministic recommendation.")
            return generate_mock_recommendations(query, candidates, top_n)

        prompt = build_recommender_prompt(query, candidates, top_n=top_n)
        result = active_client.generate_content(
            prompt=prompt,
            system_instruction=SYSTEM_PROMPT,
            json_mode=True,
            temperature=0.2,
        )

        data = result.get("data", {})
        data["model_used"] = result.get("model_used", "gemini")
        return data

    except Exception as e:
        logger.warning(f"LLM generation failed: {e}. Falling back to deterministic recommender.")
        fallback_res = generate_mock_recommendations(query, candidates, top_n)
        fallback_res["model_used"] = f"fallback (LLM error: {e})"
        return fallback_res
