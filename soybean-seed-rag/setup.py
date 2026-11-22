from setuptools import setup, find_packages

setup(
    name="soybean-seed-rag",
    version="1.0.0",
    description="Intelligent Soybean Variety Recommendation System using RAG, Metadata Filtering, and Google Gemini LLM",
    author="Soybean Seed RAG Team",
    packages=find_packages(),
    python_requires=">=3.9",
    install_requires=[
        "pandas>=2.0.0",
        "numpy>=1.24.0",
        "python-dotenv>=1.0.0",
        "pydantic>=2.0.0",
        "google-generativeai>=0.8.0",
    ],
    extras_require={
        "dev": [
            "pytest>=7.0.0",
            "pytest-cov>=4.0.0",
        ],
        "embeddings": [
            "sentence-transformers>=2.2.0",
            "torch>=2.0.0",
        ],
    },
    entry_points={
        "console_scripts": [
            "soybean-rag=main:main",
        ],
    },
)
