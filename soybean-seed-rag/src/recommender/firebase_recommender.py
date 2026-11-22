"""
Firebase LLM-based recommendation engine for Soybean.

Uses a local LLM server (via Firebase queue) to rank and explain soybean variety recommendations.
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
    Use Firebase LLM to rank candidate soybean varieties and provide explanations.
    
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
            "final_note": "No soybean varieties matched the specified criteria.",
            "warnings": ["Try broadening your search criteria."],
            "model": "firebase-llm"
        }
    
    # Create client if not provided
    if llm_client is None:
        llm_client = FirebaseLLMClient()
    
    # Limit candidates to prevent context overflow
    max_candidates = 30
    if len(candidates) > max_candidates:
        candidates = candidates[:max_candidates]
        print(f"⚠️  Limited to {max_candidates} candidates for LLM processing")
    
    # Build context
    context = _build_candidate_context(candidates)
    
    # Build prompt
    prompt = _build_ranking_prompt(query, context, top_k)
    
    # Call LLM via Firebase
    try:
        print(f"🔮 Generating soybean recommendations using Firebase LLM...")
        response = llm_client.generate_content(prompt, json_mode=True)
        
        # Parse and validate response
        result = _parse_json_response(response)
        _validate_recommendation(result, top_k)
        
        # Add model info
        result["model"] = "firebase-llm"
        
        return result
    
    except Exception as e:
        print(f"❌ Firebase LLM error: {e}")
        # Fallback to simple filtering-based ranking
        return _fallback_ranking(query, candidates, top_k)


def _build_candidate_context(candidates: List[Dict[str, Any]]) -> str:
    """
    Build formatted context string from candidate soybean documents.
    """
    context_lines = []
    
    for i, doc in enumerate(candidates, 1):
        meta = doc.get('metadata', {})
        
        variety_name = meta.get('variety_name', 'Unknown')
        variety_id = doc.get('id', meta.get('variety_id', 'unknown'))
        context_lines.append(f"\n{i}. {variety_name} (ID: {variety_id})")
        
        maturity_min = meta.get('maturity_days_min', 'N/A')
        maturity_max = meta.get('maturity_days_max', 'N/A')
        if maturity_min != 'N/A' and maturity_max != 'N/A':
            context_lines.append(f"   Maturity: {maturity_min}-{maturity_max} days")
        else:
            context_lines.append(f"   Maturity: Unknown")
        
        irrigation = meta.get('irrigation_condition', 'UNKNOWN')
        context_lines.append(f"   Irrigation: {irrigation}")
        
        regions = meta.get('recommended_regions', [])
        if isinstance(regions, list) and regions:
            regions_str = ', '.join(regions[:3])
            if len(regions) > 3:
                regions_str += f" (and {len(regions)-3} more)"
            context_lines.append(f"   Regions: {regions_str}")
        elif isinstance(regions, str) and regions:
            context_lines.append(f"   Regions: {regions}")
            
        zone = meta.get('agro_climatic_zone')
        if zone:
            context_lines.append(f"   Zone: {zone}")
        
        season = meta.get('season_raw', meta.get('season', 'Unknown'))
        context_lines.append(f"   Season: {season}")
        
        traits = meta.get('special_traits')
        if traits:
            context_lines.append(f"   Traits: {traits}")
        
        page_content = doc.get('page_content', '')
        if page_content:
            brief = page_content[:200].replace('\n', ' ').strip()
            context_lines.append(f"   Info: {brief}...")
    
    return '\n'.join(context_lines)


def _build_ranking_prompt(query: str, context: str, top_k: int) -> str:
    prompt = f"""You are an agricultural expert helping farmers choose soybean varieties in India.

FARMER'S QUERY:
{query}

CANDIDATE VARIETIES (Pre-filtered from ICAR/ICAR-IISR data):
{context}

TASK:
Rank these soybean varieties from most suitable to least suitable based on the farmer's query.
Provide your top {top_k} recommendations.

STRICT RULES:
1. Base rankings ONLY on the information provided above
2. If data is missing (UNKNOWN), acknowledge uncertainty
3. Prioritize exact matches over partial matches
4. Explain reasoning for each recommendation citing region, season, maturity, traits
5. Warn about data gaps, pest pressures, or seed viability
6. Consider: region match, season match, maturity days, irrigation suitability, drought/disease resilience

OUTPUT FORMAT (JSON):
{{
  "recommendations": [
    {{
      "rank": 1,
      "variety_id": "SOY_VER_XXX",
      "variety_name": "Variety Name",
      "maturity": "XX-XX days",
      "irrigation": "rainfed/irrigated/UNKNOWN",
      "reason": "Brief explanation of why this variety is suitable"
    }}
  ],
  "final_note": "Brief summary of recommendations (1-2 sentences)",
  "warnings": ["Any data limitations or caveats"]
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
        
        recommendations.append({
            "rank": i,
            "variety_id": doc.get('id', meta.get('variety_id', 'unknown')),
            "variety_name": meta.get('variety_name', 'Unknown'),
            "maturity": maturity,
            "irrigation": meta.get('irrigation_condition', 'UNKNOWN'),
            "reason": "Matched your filtering criteria from ICAR soybean catalog"
        })
    
    return {
        "recommendations": recommendations,
        "final_note": "Recommendations based on filter matching (LLM ranking unavailable)",
        "warnings": ["LLM ranking failed - using filter-based ordering"],
        "model": "fallback"
    }
