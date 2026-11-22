import json
import re
from typing import List, Dict, Any, Tuple, Optional

from src.client.gemini_client import generate_with_fallback


def build_candidate_context(candidates: List[Dict[str, Any]]) -> str:
    """Build detailed textual context for retrieved cotton candidate varieties."""
    sections = []

    for index, document in enumerate(candidates, start=1):
        metadata = document.get("metadata", {})
        page_content = document.get("page_content", "")

        section = f"""
CANDIDATE {index}
Variety ID: {metadata.get("variety_id")}
Variety Name: {metadata.get("variety_name")}
Crop: {metadata.get("crop", "Cotton")}
Category: {metadata.get("category")}
Type: {metadata.get("type")}
Developer / Company: {metadata.get("developer_or_company")}
Season: {metadata.get("season")}
Maturity Class: {metadata.get("maturity_class")}
Maturity: {metadata.get("maturity_days_min")} - {metadata.get("maturity_days_max")} days
Recommended Regions: {metadata.get("recommended_regions")}
Agro-climatic Zone: {metadata.get("agro_climatic_zone")}
Irrigation Condition: {metadata.get("irrigation_condition")}
Yield: {metadata.get("yield_normalized_q_ha_min")} - {metadata.get("yield_normalized_q_ha_max")} q/ha ({metadata.get("yield_value")})
Special Traits: {metadata.get("special_traits")}
Staple Length / Fiber: {metadata.get("grain_type")}
Lint Colour: {metadata.get("grain_colour")}

Stress & Disease Resistances:
- Drought Tolerance: {metadata.get("drought_tolerance")}
- Heat Tolerance: {metadata.get("heat_tolerance")}
- Waterlogging Tolerance: {metadata.get("waterlogging_tolerance")}
- Fall Armyworm: {metadata.get("fall_armyworm")}
- Stem Borer: {metadata.get("stem_borer")}
- Pink Stem Borer / Bollworm: {metadata.get("pink_stem_borer")}
- Turcicum Leaf Blight: {metadata.get("turcicum_leaf_blight")}
- Curvularia Leaf Spot: {metadata.get("curvularia_leaf_spot")}
- Charcoal Rot: {metadata.get("charcoal_rot")}
- Fusarium Stalk Rot: {metadata.get("fusarium_stalk_rot")}

VERIFIED SOURCE CITATION:
Source Document: {metadata.get("source_document")} (Page {metadata.get("source_pdf_page")})
Source Organization: {metadata.get("source_organization")}
Source Text: {metadata.get("source_text")}

FULL DOCUMENT CONTENT:
{page_content}
"""
        sections.append(section)

    return "\n".join(sections)


def clean_json_response(raw_text: str) -> Dict[str, Any]:
    """Clean and parse JSON from LLM response, stripping markdown fences if present."""
    text = raw_text.strip()
    if text.startswith("```json"):
        text = text[len("```json"):].strip()
    elif text.startswith("```"):
        text = text[len("```"):].strip()

    if text.endswith("```"):
        text = text[:-3].strip()

    # Find outermost json object if there is extraneous prose
    json_match = re.search(r"\{.*\}", text, re.DOTALL)
    if json_match:
        text = json_match.group(0)

    return json.loads(text)


def build_recommendation_prompt(query: str, candidates: List[Dict[str, Any]]) -> str:
    """Construct prompt with strict negative grounding constraints."""
    context = build_candidate_context(candidates)

    prompt = f"""You are an authoritative Cotton Agronomy Seed Advisory Expert specializing in Indian agro-climatic zones, ICAR-CICR (Central Institute for Cotton Research), and AICCIP recommendations.

A farmer has submitted the following query:
"{query}"

The following verified candidate cotton varieties have been deterministically retrieved from official agricultural research publications:

{context}

STRICT GROUNDING INSTRUCTIONS (ZERO-TOLERANCE FOR HALLUCINATION):
1. Recommend ONLY varieties from the candidate list above. NEVER invent, hallucinate, or recommend any variety not in this candidate list.
2. If no candidate varieties are suitable, return an empty recommendations list.
3. For each recommended variety, accurately report the EXACT maturity range (e.g., "150-165 days") as documented in its candidate profile.
4. Verify irrigation compatibility and explain why each recommended variety is ideal for the farmer's state, season, and constraints.
5. Highlight fiber traits (staple length, ginning outturn, micronaire) and pest/disease tolerances where relevant.
6. Rank the top recommendations (at most 5).

You MUST respond strictly with valid JSON conforming to this schema:
{{
  "recommendations": [
    {{
      "rank": 1,
      "variety_id": "COT_VER_XXX",
      "variety_name": "Exact Name",
      "maturity": "X - Y days",
      "irrigation": "Irrigated / Rainfed / Protective Irrigated",
      "reason": "Detailed evidence-grounded agronomic justification citing specific traits and region match."
    }}
  ],
  "final_note": "A summary agronomic advisory note for the farmer.",
  "warnings": [
    "Practical crop management warnings (e.g. bollworm monitoring, spacing, drainage)."
  ]
}}
"""
    return prompt


def recommend_varieties(
    query: str,
    candidates: List[Dict[str, Any]],
    top_k: int = 5
) -> Dict[str, Any]:
    """
    Generate evidence-grounded cotton seed recommendations using Gemini reasoning.
    """
    if not candidates:
        return {
            "recommendations": [],
            "final_note": "No cotton varieties met all requested metadata constraints.",
            "warnings": ["Please check state, season, or relax maturity constraints."],
            "model_used": None,
            "candidates_count": 0,
        }

    # Slice candidates to max 25 to fit LLM context efficiently
    candidate_subset = candidates[:25]
    prompt = build_recommendation_prompt(query, candidate_subset)

    try:
        response_text, model_used = generate_with_fallback(
            prompt=prompt,
            temperature=0.2,
            json_mode=True
        )

        parsed_json = clean_json_response(response_text)

        # Validate that all returned variety_ids are strictly among candidates (hallucination check)
        valid_ids = {doc.get("id") for doc in candidate_subset}
        valid_ids.update({doc.get("metadata", {}).get("variety_id") for doc in candidate_subset})

        validated_recs = []
        for rec in parsed_json.get("recommendations", []):
            v_id = rec.get("variety_id")
            if v_id in valid_ids:
                validated_recs.append(rec)
            else:
                print(f"Warning: Hallucinated variety ID '{v_id}' was rejected.")

        # Limit to top_k
        parsed_json["recommendations"] = validated_recs[:top_k]
        parsed_json["model_used"] = model_used
        parsed_json["candidates_count"] = len(candidates)
        return parsed_json

    except Exception as e:
        print(f"LLM generation failed or returned invalid response ({e}). Falling back to deterministic ranking.")
        # Deterministic fallback response
        fallback_recs = []
        for rank, doc in enumerate(candidate_subset[:top_k], start=1):
            meta = doc.get("metadata", {})
            v_id = meta.get("variety_id", doc.get("id"))
            name = meta.get("variety_name", "Unknown")
            mat = f"{meta.get('maturity_days_min')} - {meta.get('maturity_days_max')} days"
            irri = meta.get("irrigation_condition", "Not specified")
            reason = (
                f"{name} is recommended for {meta.get('recommended_regions')} during Kharif season. "
                f"Documented yield: {meta.get('yield_value', 'N/A')}. "
                f"Traits: {meta.get('special_traits', 'Verified ICAR record')}."
            )
            fallback_recs.append({
                "rank": rank,
                "variety_id": v_id,
                "variety_name": name,
                "maturity": mat,
                "irrigation": irri,
                "reason": reason
            })

        return {
            "recommendations": fallback_recs,
            "final_note": "Recommendations ranked deterministically from ICAR-CICR verified variety dataset.",
            "warnings": ["Generated via deterministic fallback mode."],
            "model_used": "deterministic-fallback",
            "candidates_count": len(candidates),
        }
