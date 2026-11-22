"""
Validation and Quality Assessment for Soybean RAG Documents
"""

import json
import os
import sys
from collections import Counter
from typing import Any, Dict, List

# Ensure root directory is in sys.path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))


def validate_soybean_documents(documents_path: str) -> None:
    print("=" * 60)
    print("VALIDATING SOYBEAN RAG DOCUMENTS")
    print("=" * 60)

    if not os.path.exists(documents_path):
        print(f"❌ Error: Documents file not found at {documents_path}")
        print("Run `python scripts/build_documents.py` first.")
        sys.exit(1)

    with open(documents_path, "r", encoding="utf-8") as f:
        docs = json.load(f)

    print(f"✓ Successfully loaded {len(docs)} documents\n")

    errors: List[str] = []
    warnings: List[str] = []

    required_doc_keys = ["id", "variety_name", "crop", "page_content", "metadata"]
    required_meta_keys = [
        "variety_id",
        "variety_name",
        "season",
        "maturity_days_min",
        "maturity_days_max",
        "irrigation_condition",
        "states",
    ]

    seasons_counter = Counter()
    irrigation_counter = Counter()
    maturity_class_counter = Counter()
    state_counter = Counter()
    yield_values = []
    oil_values = []
    protein_values = []

    for i, doc in enumerate(docs):
        doc_id = doc.get("id", f"Row-{i}")

        # Check document structure
        for key in required_doc_keys:
            if key not in doc or not doc[key]:
                errors.append(f"[{doc_id}] Missing top-level key: '{key}'")

        meta = doc.get("metadata", {})
        for mkey in required_meta_keys:
            if mkey not in meta or meta[mkey] is None or meta[mkey] == "":
                warnings.append(f"[{doc_id}] Missing metadata key or value: '{mkey}'")

        # Collect statistics
        for s in meta.get("season", []):
            seasons_counter[s] += 1
        
        irr = meta.get("irrigation_condition") or "UNKNOWN"
        irrigation_counter[irr] += 1

        mat_class = meta.get("maturity_class") or "UNKNOWN"
        maturity_class_counter[mat_class] += 1

        for st in meta.get("states", []):
            state_counter[st] += 1

        y_max = meta.get("yield_normalized_q_ha_max")
        if y_max is not None:
            yield_values.append(y_max)

        oil = meta.get("oil_content_percent")
        if oil is not None:
            oil_values.append(oil)

        prot = meta.get("protein_content_percent")
        if prot is not None:
            protein_values.append(prot)

    print("=" * 60)
    print("VALIDATION REPORT")
    print("=" * 60)
    print(f"Total documents: {len(docs)}")
    print(f"Errors: {len(errors)}")
    print(f"Warnings: {len(warnings)}")

    if errors:
        print("\n❌ ERRORS:")
        for err in errors[:10]:
            print(f"  - {err}")
        if len(errors) > 10:
            print(f"  ... and {len(errors) - 10} more errors.")

    if warnings:
        print("\n⚠ WARNINGS:")
        summary_warn = Counter(w.split("] ")[1] for w in warnings if "] " in w)
        for w_desc, count in summary_warn.most_common(5):
            print(f"  - {w_desc} in {count} documents")

    print("\n" + "=" * 60)
    print("STATISTICS")
    print("=" * 60)

    print("Seasons:")
    for s, c in seasons_counter.most_common():
        print(f"  {s}: {c}")

    print("\nIrrigation Conditions:")
    for irr, c in irrigation_counter.most_common():
        print(f"  {irr}: {c}")

    print("\nMaturity Classes:")
    for m, c in maturity_class_counter.most_common():
        print(f"  {m}: {c}")

    print("\nTop 8 Adapted States:")
    for st, c in state_counter.most_common(8):
        print(f"  {st}: {c} varieties")

    if yield_values:
        avg_yield = sum(yield_values) / len(yield_values)
        print(f"\nYield (Normalized Max q/ha):")
        print(f"  Min: {min(yield_values):.1f} | Max: {max(yield_values):.1f} | Average: {avg_yield:.1f}")

    if oil_values and protein_values:
        print(f"\nQuality Metrics:")
        print(f"  Avg Oil Content: {sum(oil_values)/len(oil_values):.2f}%")
        print(f"  Avg Protein Content: {sum(protein_values)/len(protein_values):.2f}%")

    print("=" * 60)


if __name__ == "__main__":
    docs_path = os.path.join("data", "processed", "soybean_rag_documents.json")
    validate_soybean_documents(docs_path)
