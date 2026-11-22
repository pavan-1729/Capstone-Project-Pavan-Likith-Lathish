"""
Setup configuration for Cotton Seed RAG System.
"""

from setuptools import setup, find_packages

with open("README.md", "r", encoding="utf-8") as fh:
    long_description = fh.read()

setup(
    name="cotton-seed-rag",
    version="1.0.0",
    author="Capstone Project Team",
    description="RAG system for cotton variety recommendations with metadata filtering, dense vector embeddings, and LLM reasoning",
    long_description=long_description,
    long_description_content_type="text/markdown",
    packages=find_packages(),
    classifiers=[
        "Development Status :: 4 - Beta",
        "Intended Audience :: Science/Research",
        "Topic :: Scientific/Engineering :: Artificial Intelligence",
        "Programming Language :: Python :: 3",
        "Programming Language :: Python :: 3.9",
        "Programming Language :: Python :: 3.10",
        "Programming Language :: Python :: 3.11",
        "Programming Language :: Python :: 3.12",
    ],
    python_requires=">=3.9",
    install_requires=[
        "python-dotenv>=1.0.0",
        "google-genai>=0.5.0",
        "pandas>=2.1.0",
        "numpy>=1.26.0",
        "sentence-transformers>=2.2.0",
        "torch>=2.1.0",
        "tqdm>=4.66.0",
    ],
    extras_require={
        "dev": [
            "pytest>=7.4.0",
            "jupyter>=1.0.0",
            "ipykernel>=6.27.0",
        ],
    },
)
