import json
from pathlib import Path

import numpy as np
from tqdm import tqdm
from sentence_transformers import SentenceTransformer


INPUT_FILE = Path(
    "data/processed/maize_rag_documents.json"
)

OUTPUT_EMBEDDINGS = Path(
    "data/processed/embeddings.npy"
)

OUTPUT_IDS = Path(
    "data/processed/embedding_ids.json"
)

MODEL_NAME = "BAAI/bge-m3"


def load_documents():

    with open(
        INPUT_FILE,
        "r",
        encoding="utf-8"
    ) as file:

        return json.load(file)


def main():

    print("=" * 60)
    print("MAIZE RAG EMBEDDING GENERATION")
    print("=" * 60)

    documents = load_documents()

    print(
        f"\nDocuments loaded: {len(documents)}"
    )

    print(
        f"Loading model: {MODEL_NAME}"
    )

    model = SentenceTransformer(
        MODEL_NAME
    )

    print("Model loaded.")

    texts = []

    document_ids = []

    for document in documents:

        document_id = document["id"]

        page_content = document[
            "page_content"
        ]

        document_ids.append(
            document_id
        )

        texts.append(
            page_content
        )

    print(
        f"\nGenerating embeddings for "
        f"{len(texts)} documents..."
    )

    embeddings = model.encode(
        texts,
        batch_size=16,
        show_progress_bar=True,
        normalize_embeddings=True,
        convert_to_numpy=True
    )

    print(
        f"\nEmbedding shape: "
        f"{embeddings.shape}"
    )

    OUTPUT_EMBEDDINGS.parent.mkdir(
        parents=True,
        exist_ok=True
    )

    np.save(
        OUTPUT_EMBEDDINGS,
        embeddings
    )

    with open(
        OUTPUT_IDS,
        "w",
        encoding="utf-8"
    ) as file:

        json.dump(
            document_ids,
            file,
            indent=2,
            ensure_ascii=False
        )

    print("\nFiles created:")

    print(
        f"Embeddings: "
        f"{OUTPUT_EMBEDDINGS}"
    )

    print(
        f"IDs: "
        f"{OUTPUT_IDS}"
    )

    print("\nEmbedding generation completed.")


if __name__ == "__main__":
    main()