import json

from src.client.gemini_client import generate_with_fallback


# =========================================================
# Build candidate information
# =========================================================

def build_candidate_context(candidates):

    sections = []

    for index, document in enumerate(
        candidates,
        start=1
    ):

        metadata = document.get(
            "metadata",
            {}
        )

        page_content = document.get(
            "page_content",
            ""
        )

        section = f"""
CANDIDATE {index}

Variety ID:
{metadata.get("variety_id")}

Variety Name:
{metadata.get("variety_name")}

Crop:
{metadata.get("crop")}

Category:
{metadata.get("category")}

Type:
{metadata.get("type")}

Developer / Company:
{metadata.get("developer_or_company")}

Season:
{metadata.get("season")}

Maturity Class:
{metadata.get("maturity_class")}

Maturity:
{metadata.get("maturity_days_min")} - {metadata.get("maturity_days_max")} days

Recommended Regions:
{metadata.get("recommended_regions")}

Irrigation Condition:
{metadata.get("irrigation_condition")}

Agro-climatic Zone:
{metadata.get("agro_climatic_zone")}

Yield:
{metadata.get("yield_normalized_q_ha_min")} - {metadata.get("yield_normalized_q_ha_max")} q/ha

Special Traits:
{metadata.get("special_traits")}

Disease / Stress Information:

Turcicum leaf blight:
{metadata.get("turcicum_leaf_blight")}

Charcoal rot:
{metadata.get("charcoal_rot")}

Drought tolerance:
{metadata.get("drought_tolerance")}

Heat tolerance:
{metadata.get("heat_tolerance")}

Waterlogging tolerance:
{metadata.get("waterlogging_tolerance")}

Fall armyworm:
{metadata.get("fall_armyworm")}

Stem borer:
{metadata.get("stem_borer")}

SOURCE DOCUMENT CONTENT:
{page_content}
"""

        sections.append(section)

    return "\n".join(sections)


# =========================================================
# Clean Gemini JSON response
# =========================================================

def clean_json_response(response):

    if not response:
        return None

    response = response.strip()

    # -----------------------------------------------------
    # Remove markdown code fences
    # -----------------------------------------------------

    if response.startswith("```json"):

        response = response[
            len("```json"):
        ]

    elif response.startswith("```"):

        response = response[
            len("```"):
        ]

    if response.endswith("```"):

        response = response[
            :-len("```")
        ]

    response = response.strip()

    # -----------------------------------------------------
    # First attempt
    # -----------------------------------------------------

    try:

        return json.loads(response)

    except json.JSONDecodeError:

        pass

    # -----------------------------------------------------
    # Try to locate JSON object
    # -----------------------------------------------------

    start = response.find("{")
    end = response.rfind("}")

    if (
        start != -1
        and end != -1
        and end > start
    ):

        json_text = response[
            start:end + 1
        ]

        try:

            return json.loads(json_text)

        except json.JSONDecodeError:

            return None

    return None


# =========================================================
# Validate recommendation structure
# =========================================================

def validate_recommendation(result):

    if not isinstance(
        result,
        dict
    ):
        return False

    # -----------------------------------------------------
    # recommendations
    # -----------------------------------------------------

    if "recommendations" not in result:

        return False

    if not isinstance(
        result["recommendations"],
        list
    ):

        return False

    # -----------------------------------------------------
    # final_note
    # -----------------------------------------------------

    if "final_note" not in result:

        return False

    if not isinstance(
        result["final_note"],
        str
    ):

        return False

    # -----------------------------------------------------
    # warnings
    # -----------------------------------------------------

    if "warnings" not in result:

        return False

    if not isinstance(
        result["warnings"],
        list
    ):

        return False

    # -----------------------------------------------------
    # Recommendation fields
    # -----------------------------------------------------

    required_fields = [
        "rank",
        "variety_id",
        "variety_name",
        "maturity",
        "reason",
        "irrigation"
    ]

    for recommendation in result["recommendations"]:

        if not isinstance(
            recommendation,
            dict
        ):

            return False

        for field in required_fields:

            if field not in recommendation:

                return False

    return True


# =========================================================
# Recommendation
# =========================================================

def recommend_varieties(
    farmer_query,
    candidates
):

    # =====================================================
    # No candidates
    # =====================================================

    if not candidates:

        return {
            "recommendations": [],
            "final_note": (
                "No candidate varieties were found."
            ),
            "warnings": [],
            "model_used": None
        }

    # =====================================================
    # Build candidate context
    # =====================================================

    candidate_context = build_candidate_context(
        candidates
    )

    # =====================================================
    # Prompt
    # =====================================================

    prompt = f"""
You are an agricultural recommendation assistant
for a maize seed RAG system.

Your job is to rank the most suitable maize varieties
from the retrieved candidate documents.

IMPORTANT RULES:

1. Use ONLY the information provided in the candidate
   documents.

2. Do NOT use outside knowledge.

3. Do NOT invent information.

4. Do NOT introduce varieties that are not present
   in the candidate documents.

5. Metadata filtering has already been performed for
   hard constraints such as:

   - State / region
   - Growing season
   - Maximum maturity period
   - Minimum maturity period

6. The farmer's request is:

{farmer_query}

7. Rank the retrieved candidates from most suitable
   to least suitable.

8. Give higher priority to candidates whose documented
   characteristics better satisfy the farmer's request.

9. Irrigation information may be NULL or UNKNOWN.

10. Do NOT assume that missing irrigation information
    means the variety is rainfed.

11. If irrigation information is missing, use exactly:

    "Irrigation suitability is not specified in the available source."

12. Do not fabricate:

    - Yield
    - Disease resistance
    - Drought tolerance
    - Heat tolerance
    - Irrigation suitability
    - Maturity
    - Region
    - Any other characteristic

13. If a candidate is a different maize category such as
    baby corn or popcorn, mention that in the reason if
    it is relevant to ranking.

14. If important information is unavailable from the
    retrieved documents, mention it in the warnings list.

15. Do not reject a candidate solely because irrigation
    information is missing.

16. The recommendation must be based only on the retrieved
    candidate documents.

17. The farmer specifically requested rainfed cultivation.
    Since irrigation information is UNKNOWN for some
    candidates, do not claim that they are suitable for
    rainfed cultivation unless the source explicitly
    supports that conclusion.

18. Prefer candidates whose documented information gives
    stronger evidence for the farmer's request.

CANDIDATE DOCUMENTS
===================

{candidate_context}

RETURN ONLY VALID JSON.

Do NOT use markdown.

Do NOT wrap the JSON in code fences.

Use exactly this structure:

{{
    "recommendations": [
        {{
            "rank": 1,
            "variety_id": "string",
            "variety_name": "string",
            "maturity": "string",
            "reason": "string",
            "irrigation": "string"
        }}
    ],
    "final_note": "string",
    "warnings": [
        "string"
    ]
}}

JSON RULES:

- rank must be an integer.

- variety_id must come directly from the candidate.

- variety_name must come directly from the candidate.

- maturity must contain the documented maturity range.

- reason must briefly explain why the candidate received
  its ranking.

- irrigation must contain documented irrigation information
  or exactly:

  "Irrigation suitability is not specified in the available source."

- final_note should briefly explain why the top-ranked
  variety was preferred and mention important uncertainty.

- warnings must contain important limitations or missing
  information from the retrieved documents.

- If there are no important warnings, return:

  "warnings": []
"""

    # =====================================================
    # Call Gemini
    # =====================================================

    response, model = generate_with_fallback(
        prompt,
        temperature=0.2,
        json_mode=True
    )

    # =====================================================
    # Parse JSON
    # =====================================================

    result = clean_json_response(
        response
    )

    if result is None:

        raise ValueError(
            "Gemini returned an invalid JSON response.\n\n"
            f"Raw response:\n{response}"
        )

    # =====================================================
    # Validate
    # =====================================================

    if not validate_recommendation(
        result
    ):

        raise ValueError(
            "Gemini returned JSON with an "
            "invalid recommendation structure.\n\n"
            f"Raw response:\n{response}"
        )

    # =====================================================
    # Add model information
    # =====================================================

    result["model_used"] = model

    return result