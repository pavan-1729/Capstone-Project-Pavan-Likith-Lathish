import json
from pathlib import Path
from typing import Dict, Any

import pandas as pd


PROJECT_ROOT = Path(__file__).resolve().parent.parent
DATA_PATH = PROJECT_ROOT / "data" / "raw" / "cotton.csv"
FALLBACK_DATA_PATH = PROJECT_ROOT / "data" / "cotton.csv"
REPORT_PATH = PROJECT_ROOT / "data" / "processed" / "validation_report.json"

REQUIRED_COLUMNS = [
    "variety_id",
    "variety_name",
    "crop",
    "category",
    "season",
    "maturity_days_min",
    "maturity_days_max",
    "recommended_regions",
    "irrigation_condition",
    "source_document",
    "source_pdf_page",
    "source_printed_page",
    "source_organization",
    "source_type",
    "source_text",
]


def validate_dataset() -> Dict[str, Any]:
    print("=" * 60)
    print("COTTON DATASET VALIDATION AUDIT")
    print("=" * 60)

    target_path = DATA_PATH if DATA_PATH.exists() else FALLBACK_DATA_PATH
    if not target_path.exists():
        raise FileNotFoundError(f"Dataset not found at {DATA_PATH} or {FALLBACK_DATA_PATH}")

    df = pd.read_csv(target_path)
    total_records = len(df)
    total_columns = len(df.columns)

    print(f"\nTotal Records : {total_records}")
    print(f"Total Columns : {total_columns}")

    # Check required columns
    missing_columns = [col for col in REQUIRED_COLUMNS if col not in df.columns]
    duplicate_ids = int(df["variety_id"].duplicated().sum())
    duplicate_names = int(df["variety_name"].duplicated().sum())

    required_empty = {col: int(df[col].isna().sum()) for col in REQUIRED_COLUMNS if col in df.columns}
    missing_values = {col: int(df[col].isna().sum()) for col in df.columns}

    numeric_columns = [
        "maturity_days_min",
        "maturity_days_max",
        "yield_normalized_q_ha_min",
        "yield_normalized_q_ha_max",
    ]
    numeric_issues = {}
    for col in numeric_columns:
        if col in df.columns:
            invalid_count = pd.to_numeric(df[col], errors="coerce").isna().sum()
            numeric_issues[col] = int(invalid_count)

    season_distribution = df["season"].value_counts().to_dict() if "season" in df.columns else {}
    region_distribution = df["recommended_regions"].value_counts().to_dict() if "recommended_regions" in df.columns else {}
    irrigation_distribution = df["irrigation_condition"].value_counts().to_dict() if "irrigation_condition" in df.columns else {}
    category_distribution = df["category"].value_counts().to_dict() if "category" in df.columns else {}
    organization_distribution = df["source_organization"].value_counts().to_dict() if "source_organization" in df.columns else {}

    report = {
        "dataset_name": "Cotton Seed Catalog (ICAR-CICR & AICCIP)",
        "file_path": str(target_path),
        "total_records": total_records,
        "total_columns": total_columns,
        "missing_required_columns": missing_columns,
        "duplicate_variety_ids": duplicate_ids,
        "duplicate_variety_names": duplicate_names,
        "required_empty_counts": required_empty,
        "numeric_issues": numeric_issues,
        "total_missing_values_across_all_columns": sum(missing_values.values()),
        "season_distribution": season_distribution,
        "region_distribution": region_distribution,
        "irrigation_distribution": irrigation_distribution,
        "category_distribution": category_distribution,
        "source_organizations": organization_distribution,
        "status": "PASSED" if duplicate_ids == 0 and len(missing_columns) == 0 and sum(required_empty.values()) == 0 else "FAILED",
    }

    REPORT_PATH.parent.mkdir(parents=True, exist_ok=True)
    with open(REPORT_PATH, "w", encoding="utf-8") as f:
        json.dump(report, f, indent=2)

    print(f"\nValidation Status : {report['status']}")
    print(f"Duplicate IDs     : {duplicate_ids}")
    print(f"Missing Values    : {report['total_missing_values_across_all_columns']}")
    print(f"Report saved to   : {REPORT_PATH}")
    return report


if __name__ == "__main__":
    validate_dataset()
