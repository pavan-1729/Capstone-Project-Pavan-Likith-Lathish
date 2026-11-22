import json
from pathlib import Path

import pandas as pd


INPUT_FILE = Path("data/raw/maize_varieties.csv")
OUTPUT_FILE = Path("data/processed/maize_rag_documents.json")


def clean_value(value):
    """
    Convert pandas missing values to None
    and clean string whitespace.
    """

    if pd.isna(value):
        return None

    if isinstance(value, str):
        value = value.strip()

        if not value:
            return None

    return value


def add_field(lines, label, value):
    """
    Add a field to the natural-language document
    only when verified data exists.
    """

    value = clean_value(value)

    if value is not None:
        lines.append(f"{label}: {value}")


def build_page_content(row):
    """
    Build one natural-language knowledge document
    for exactly one maize variety.

    IMPORTANT:
    No information is inferred.
    Only information present in the dataset is included.
    """

    lines = []

    add_field(
        lines,
        "Variety name",
        row.get("variety_name")
    )

    add_field(
        lines,
        "Crop",
        row.get("crop")
    )

    add_field(
        lines,
        "Category",
        row.get("category")
    )

    add_field(
        lines,
        "Type",
        row.get("type")
    )

    add_field(
        lines,
        "Developer or company",
        row.get("developer_or_company")
    )

    add_field(
        lines,
        "Parents",
        row.get("parents")
    )

    add_field(
        lines,
        "Release year",
        row.get("release_year")
    )

    add_field(
        lines,
        "Gazette notification",
        row.get("gazette_notification")
    )

    add_field(
        lines,
        "Growing season",
        row.get("season")
    )

    add_field(
        lines,
        "Maturity class",
        row.get("maturity_class")
    )

    add_field(
        lines,
        "Minimum maturity days",
        row.get("maturity_days_min")
    )

    add_field(
        lines,
        "Maximum maturity days",
        row.get("maturity_days_max")
    )

    add_field(
        lines,
        "Yield",
        row.get("yield_value")
    )

    add_field(
        lines,
        "Yield unit",
        row.get("yield_unit")
    )

    add_field(
        lines,
        "Normalized yield minimum",
        row.get("yield_normalized_q_ha_min")
    )

    add_field(
        lines,
        "Normalized yield maximum",
        row.get("yield_normalized_q_ha_max")
    )

    add_field(
        lines,
        "Recommended regions",
        row.get("recommended_regions")
    )

    add_field(
        lines,
        "Agro-climatic zone",
        row.get("agro_climatic_zone")
    )

    add_field(
        lines,
        "Irrigation condition",
        row.get("irrigation_condition")
    )

    # Morphological traits

    add_field(
        lines,
        "Plant height",
        row.get("plant_height")
    )

    add_field(
        lines,
        "Ear height",
        row.get("ear_height")
    )

    add_field(
        lines,
        "Lodging resistance",
        row.get("lodging_resistance")
    )

    add_field(
        lines,
        "Leaf orientation",
        row.get("leaf_orientation")
    )

    add_field(
        lines,
        "Stay green characteristic",
        row.get("stay_green")
    )

    # Stress tolerance

    add_field(
        lines,
        "Drought tolerance",
        row.get("drought_tolerance")
    )

    add_field(
        lines,
        "Waterlogging tolerance",
        row.get("waterlogging_tolerance")
    )

    add_field(
        lines,
        "Heat tolerance",
        row.get("heat_tolerance")
    )

    add_field(
        lines,
        "Cold stress tolerance",
        row.get("cold_stress_tolerance")
    )

    # Pest and disease information

    add_field(
        lines,
        "Fall Armyworm response",
        row.get("fall_armyworm")
    )

    add_field(
        lines,
        "Stem borer response",
        row.get("stem_borer")
    )

    add_field(
        lines,
        "Pink stem borer response",
        row.get("pink_stem_borer")
    )

    add_field(
        lines,
        "Turcicum leaf blight response",
        row.get("turcicum_leaf_blight")
    )

    add_field(
        lines,
        "Maydis leaf blight response",
        row.get("maydis_leaf_blight")
    )

    add_field(
        lines,
        "Curvularia leaf spot response",
        row.get("curvularia_leaf_spot")
    )

    add_field(
        lines,
        "Banded leaf sheath blight response",
        row.get("banded_leaf_sheath_blight")
    )

    add_field(
        lines,
        "Charcoal rot response",
        row.get("charcoal_rot")
    )

    add_field(
        lines,
        "Fusarium stalk rot response",
        row.get("fusarium_stalk_rot")
    )

    add_field(
        lines,
        "Downy mildew response",
        row.get("downy_mildew")
    )

    # Grain characteristics

    add_field(
        lines,
        "Grain type",
        row.get("grain_type")
    )

    add_field(
        lines,
        "Grain colour",
        row.get("grain_colour")
    )

    add_field(
        lines,
        "Special traits",
        row.get("special_traits")
    )

    # Original source text

    add_field(
        lines,
        "Source text",
        row.get("source_text")
    )

    return "\n".join(lines)


def build_metadata(row):
    """
    Build structured metadata for deterministic filtering.
    """

    return {
        "variety_id": clean_value(row.get("variety_id")),
        "variety_name": clean_value(row.get("variety_name")),
        "crop": clean_value(row.get("crop")),
        "category": clean_value(row.get("category")),
        "type": clean_value(row.get("type")),
        "developer_or_company": clean_value(
            row.get("developer_or_company")
        ),

        "season": clean_value(row.get("season")),

        "maturity_class": clean_value(
            row.get("maturity_class")
        ),

        "maturity_days_min": clean_value(
            row.get("maturity_days_min")
        ),

        "maturity_days_max": clean_value(
            row.get("maturity_days_max")
        ),

        "yield_normalized_q_ha_min": clean_value(
            row.get("yield_normalized_q_ha_min")
        ),

        "yield_normalized_q_ha_max": clean_value(
            row.get("yield_normalized_q_ha_max")
        ),

        "recommended_regions": clean_value(
            row.get("recommended_regions")
        ),

        "agro_climatic_zone": clean_value(
            row.get("agro_climatic_zone")
        ),

        "irrigation_condition": clean_value(
            row.get("irrigation_condition")
        ),

        "plant_height": clean_value(
            row.get("plant_height")
        ),

        "ear_height": clean_value(
            row.get("ear_height")
        ),

        "lodging_resistance": clean_value(
            row.get("lodging_resistance")
        ),

        "leaf_orientation": clean_value(
            row.get("leaf_orientation")
        ),

        "stay_green": clean_value(
            row.get("stay_green")
        ),

        "drought_tolerance": clean_value(
            row.get("drought_tolerance")
        ),

        "waterlogging_tolerance": clean_value(
            row.get("waterlogging_tolerance")
        ),

        "heat_tolerance": clean_value(
            row.get("heat_tolerance")
        ),

        "cold_stress_tolerance": clean_value(
            row.get("cold_stress_tolerance")
        ),

        "fall_armyworm": clean_value(
            row.get("fall_armyworm")
        ),

        "stem_borer": clean_value(
            row.get("stem_borer")
        ),

        "pink_stem_borer": clean_value(
            row.get("pink_stem_borer")
        ),

        "turcicum_leaf_blight": clean_value(
            row.get("turcicum_leaf_blight")
        ),

        "maydis_leaf_blight": clean_value(
            row.get("maydis_leaf_blight")
        ),

        "curvularia_leaf_spot": clean_value(
            row.get("curvularia_leaf_spot")
        ),

        "banded_leaf_sheath_blight": clean_value(
            row.get("banded_leaf_sheath_blight")
        ),

        "charcoal_rot": clean_value(
            row.get("charcoal_rot")
        ),

        "fusarium_stalk_rot": clean_value(
            row.get("fusarium_stalk_rot")
        ),

        "downy_mildew": clean_value(
            row.get("downy_mildew")
        ),

        "grain_type": clean_value(
            row.get("grain_type")
        ),

        "grain_colour": clean_value(
            row.get("grain_colour")
        ),

        "special_traits": clean_value(
            row.get("special_traits")
        ),

        "source_document": clean_value(
            row.get("source_document")
        ),

        "source_pdf_page": clean_value(
            row.get("source_pdf_page")
        ),

        "source_printed_page": clean_value(
            row.get("source_printed_page")
        ),

        "source_organization": clean_value(
            row.get("source_organization")
        ),

        "source_type": clean_value(
            row.get("source_type")
        ),

        "source_url": clean_value(
            row.get("source_url")
        ),

        "data_quality_notes": clean_value(
            row.get("data_quality_notes")
        )
    }


def build_documents():

    print("=" * 60)
    print("BUILDING MAIZE ENTITY-LEVEL RAG DOCUMENTS")
    print("=" * 60)

    df = pd.read_csv(INPUT_FILE)

    documents = []

    for _, row in df.iterrows():

        variety_id = clean_value(
            row.get("variety_id")
        )

        variety_name = clean_value(
            row.get("variety_name")
        )

        if variety_id is None:
            print(
                "WARNING: Skipping row without variety_id"
            )
            continue

        page_content = build_page_content(row)

        metadata = build_metadata(row)

        document = {
            "id": variety_id,
            "page_content": page_content,
            "metadata": metadata
        }

        documents.append(document)

    OUTPUT_FILE.parent.mkdir(
        parents=True,
        exist_ok=True
    )

    with open(
        OUTPUT_FILE,
        "w",
        encoding="utf-8"
    ) as file:

        json.dump(
            documents,
            file,
            indent=2,
            ensure_ascii=False
        )

    print(f"\nInput rows      : {len(df)}")
    print(f"Documents       : {len(documents)}")
    print(f"Output file     : {OUTPUT_FILE}")

    print("\nDocument generation completed.")


if __name__ == "__main__":
    build_documents()