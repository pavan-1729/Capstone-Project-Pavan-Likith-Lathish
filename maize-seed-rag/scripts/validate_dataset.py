import pandas as pd
import json
from pathlib import Path


DATA_PATH = Path("Data/raw/maize_varieties.csv")
REPORT_PATH = Path("Data/processed/validation_report.json")


REQUIRED_COLUMNS = [
    "variety_id",
    "variety_name",
    "crop",
    "source_document",
    "source_pdf_page",
    "source_printed_page",
    "source_organization",
    "source_type",
    "source_text",
]


def validate_dataset():

    print("=" * 60)
    print("MAIZE DATASET VALIDATION")
    print("=" * 60)

    if not DATA_PATH.exists():
        raise FileNotFoundError(
            f"Dataset not found: {DATA_PATH}"
        )

    df = pd.read_csv(DATA_PATH)

    print(f"\nRows    : {len(df)}")
    print(f"Columns : {len(df.columns)}")

    # --------------------------------------------------
    # Required columns
    # --------------------------------------------------

    missing_columns = [
        column
        for column in REQUIRED_COLUMNS
        if column not in df.columns
    ]

    # --------------------------------------------------
    # Duplicate checks
    # --------------------------------------------------

    duplicate_ids = int(
        df["variety_id"].duplicated().sum()
    )

    duplicate_names = int(
        df["variety_name"].duplicated().sum()
    )

    # --------------------------------------------------
    # Empty required values
    # --------------------------------------------------

    required_empty = {}

    for column in REQUIRED_COLUMNS:
        required_empty[column] = int(
            df[column].isna().sum()
        )

    # --------------------------------------------------
    # Missing values
    # --------------------------------------------------

    missing_values = {
        column: int(df[column].isna().sum())
        for column in df.columns
    }

    # --------------------------------------------------
    # Numeric validation
    # --------------------------------------------------

    numeric_columns = [
        "maturity_days_min",
        "maturity_days_max",
        "yield_value",
        "yield_normalized_q_ha_min",
        "yield_normalized_q_ha_max",
    ]

    numeric_errors = {}

    for column in numeric_columns:

        if column not in df.columns:
            continue

        converted = pd.to_numeric(
            df[column],
            errors="coerce"
        )

        original_non_null = df[column].notna()

        invalid_count = int(
            (original_non_null & converted.isna()).sum()
        )

        numeric_errors[column] = invalid_count

    # --------------------------------------------------
    # Maturity validation
    # --------------------------------------------------

    maturity_invalid = 0

    if (
        "maturity_days_min" in df.columns
        and "maturity_days_max" in df.columns
    ):

        min_days = pd.to_numeric(
            df["maturity_days_min"],
            errors="coerce"
        )

        max_days = pd.to_numeric(
            df["maturity_days_max"],
            errors="coerce"
        )

        maturity_invalid = int(
            (
                min_days.notna()
                & max_days.notna()
                & (min_days > max_days)
            ).sum()
        )

    # --------------------------------------------------
    # Crop values
    # --------------------------------------------------

    crop_values = (
        df["crop"]
        .dropna()
        .astype(str)
        .str.strip()
        .value_counts()
        .to_dict()
    )

    # --------------------------------------------------
    # Season values
    # --------------------------------------------------

    season_values = (
        df["season"]
        .dropna()
        .astype(str)
        .str.strip()
        .value_counts()
        .to_dict()
    )

    # --------------------------------------------------
    # Report
    # --------------------------------------------------

    report = {
        "dataset": str(DATA_PATH),
        "rows": len(df),
        "columns": len(df.columns),

        "missing_required_columns": missing_columns,

        "duplicate_variety_ids": duplicate_ids,
        "duplicate_variety_names": duplicate_names,

        "required_field_missing_values": required_empty,

        "missing_values": missing_values,

        "numeric_validation": numeric_errors,

        "invalid_maturity_ranges": maturity_invalid,

        "crop_distribution": crop_values,

        "season_distribution": season_values,

        "validation_passed": (
            len(missing_columns) == 0
            and duplicate_ids == 0
            and duplicate_names == 0
            and maturity_invalid == 0
        )
    }

    # --------------------------------------------------
    # Save report
    # --------------------------------------------------

    REPORT_PATH.parent.mkdir(
        parents=True,
        exist_ok=True
    )

    with open(
        REPORT_PATH,
        "w",
        encoding="utf-8"
    ) as file:

        json.dump(
            report,
            file,
            indent=4,
            ensure_ascii=False
        )

    print("\nValidation complete.")

    print(
        f"Duplicate IDs    : {duplicate_ids}"
    )

    print(
        f"Duplicate names  : {duplicate_names}"
    )

    print(
        f"Invalid maturity : {maturity_invalid}"
    )

    print(
        f"Validation passed: {report['validation_passed']}"
    )

    print(
        f"\nReport saved to: {REPORT_PATH}"
    )


if __name__ == "__main__":
    validate_dataset()