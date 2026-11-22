# 🌱 Soybean Seed RAG System

A **Retrieval-Augmented Generation (RAG)** system for intelligent soybean variety recommendations using metadata filtering and LLM-powered ranking. This system helps farmers, agronomists, and researchers identify the most suitable soybean (*Glycine max*) varieties based on their specific agricultural conditions, agro-climatic zones, and resource constraints.

[![Python 3.9+](https://img.shields.io/badge/python-3.9+-blue.svg)](https://www.python.org/downloads/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Quick Start](#-quick-start)
- [Architecture](#-architecture)
- [Usage](#-usage)
- [Project Structure](#-project-structure)
- [RAG System Design](#-rag-system-design)
- [API Reference](#-api-reference)
- [Testing](#-testing)
- [Contributing](#-contributing)
- [Troubleshooting](#-troubleshooting)
- [Roadmap](#-roadmap)
- [Changelog](#-changelog)
- [License](#-license)

---

## 🎯 Overview

The Soybean Seed RAG System combines three powerful approaches to deliver accurate, evidence-based soybean variety recommendations:

1. **Deterministic Metadata Filtering** - Fast, precise filtering by state, agro-climatic zone, season, maturity duration, and water condition.
2. **Semantic Retrieval** - Embedding-based similarity search and agronomic trait relevance scoring (protein, oil, drought resilience, pest resistance).
3. **LLM-Powered Ranking** - Google Gemini provides intelligent, context-aware variety ranking with evidence-backed agronomic explanations and practical farmer advisories.

### Why This Matters

Soybean farmers require tailored recommendations based on complex agronomic factors:
- **Geographic location & Agro-Climatic Zone** (Central Zone: MP, Maharashtra, Rajasthan; Southern Zone: Karnataka, AP, Tamil Nadu; Northern Plains: UP, Uttarakhand; Eastern Plateau: Bihar, Jharkhand)
- **Growing season** (Kharif monsoon season vs. Spring / Summer)
- **Water availability** (Rainfed drylands, protective irrigation, or assured irrigated systems)
- **Duration / Time constraints** (Early maturity < 95 days, medium 95–115 days, late > 115 days)
- **Quality & biotic resilience** (High protein > 40%, high oil > 20%, charcoal rot, collar rot, stem fly, defoliators)

Traditional search fails because:
- ❌ Simple keyword filtering misses nuanced matches and regional zone adaptations.
- ❌ Pure LLMs hallucinate variety names, false maturity durations, and unverified yield claims.
- ❌ Semantic search alone cannot enforce hard environmental constraints.

**Our hybrid architecture solves all three problems.**

---

## ✨ Features

### Core Capabilities

- 🔍 **Intelligent Query Parsing** - Extracts states, seasons, irrigation requirements, maturity limits, and special traits from free-text natural language queries.
- 🎯 **Deterministic Metadata Filtering** - Enforces hard constraints for state/zone adaptation, season, and maturity duration.
- 🧠 **LLM Ranking & Advisory** - Google Gemini ranks candidates based on agronomic evidence and provides structured justifications.
- 📊 **Uncertainty & Water Management Handling** - Gracefully handles rainfed vs. protective irrigation vs. assured irrigation conditions.
- 🔄 **Model Fallback Chain** - Automatic cascading fallback across multiple Gemini models (`gemini-2.5-flash`, `gemini-2.0-flash`, `gemini-1.5-flash`, etc.) and offline deterministic mode.
- 📝 **Structured JSON Output** - Enforces valid, schema-compliant JSON mode with rank, variety ID, maturity, irrigation, and agronomic reasoning.
- ⚡ **Sub-Second Retrieval** - Microsecond metadata evaluation and candidate scoring across all cataloged varieties.

### Advanced Features

- **Entity-Level Documents** - One comprehensive document per soybean variety preserving morphology, stress resilience, parentage, and quality metrics without chunking fragmentation.
- **Evidence-Based Reasoning** - The system strictly restricts LLM output to verified document data, preventing hallucinations.
- **Biotic & Abiotic Trait Scoring** - Proactively boosts varieties exhibiting drought tolerance, waterlogging tolerance, high protein, and disease resistance.
- **Interactive & Batch CLI Modes** - Complete terminal interface with rich formatted outputs and interactive questioning.
- **Comprehensive Test Suite** - Unit and integration tests with high code coverage (>75%).

---

## 🚀 Quick Start

### Prerequisites

- Python 3.9 or higher
- Google Gemini API key ([Get one from Google AI Studio](https://ai.google.dev/))
- ~100MB disk space for database and dependencies

### Installation

#### 1. Navigate to Project Directory
```bash
cd soybean-seed-rag
```

#### 2. Create and Activate Virtual Environment
```bash
# Windows
python -m venv venv
venv\Scripts\activate

# Linux / Mac
python3 -m venv venv
source venv/bin/activate
```

#### 3. Install Dependencies
```bash
pip install -r requirements.txt
```

#### 4. Configure Environment Variables
Create a `.env` file in the project root:

```env
GEMINI_API_KEY=your_actual_api_key_here
```

*(Note: The system also includes an offline deterministic fallback mode if no API key is set or when running with `--mock`.)*

#### 5. Build Document Database
```bash
python scripts/build_documents.py
```

**Expected Output:**
```
============================================================
BUILDING SOYBEAN ENTITY-LEVEL RAG DOCUMENTS
============================================================
Input file      : data\soy.csv
Documents       : 184
Output file     : data\processed\soybean_rag_documents.json
Document generation completed.
============================================================
```

#### 6. (Optional) Generate Document Embeddings
```bash
python scripts/generate_embeddings.py
```

### First Query

#### Interactive Mode:
```bash
python main.py --interactive
```

Try this query:
```
I need soybean for Maharashtra in Kharif season, rainfed, within 95 days
```

#### Command Line Mode:
```bash
python main.py --query "I need soybean for Maharashtra in Kharif season, rainfed, within 95 days"
```

---

## 🏗️ Architecture

### System Overview

```
┌─────────────────────────────────────────────────────────────┐
│                      USER QUERY                              │
│  "I need soybean for Maharashtra, Kharif, rainfed, 95 days" │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   QUERY PARSER                               │
│  Extracts: State, Season, Irrigation, Maturity, Traits       │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                 METADATA FILTER                              │
│  Hard filters: State/Zone, Season, Maturity Window           │
│  Soft filter: Irrigation Condition (MATCH / UNKNOWN)         │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              CANDIDATE RETRIEVAL                             │
│  Scores candidates by yield, traits, and water fit           │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                LLM RECOMMENDER                               │
│  Gemini ranks candidates with evidence-based reasoning       │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│            RANKED RECOMMENDATIONS                            │
│  Top varieties with justifications, strengths, and warnings  │
└─────────────────────────────────────────────────────────────┘
```

### Module Architecture

```
src/
├── client/              # Gemini API client with multi-model fallback
│   ├── gemini_client.py
│   └── __init__.py
│
├── ingestion/           # Data processing & entity document generation
│   ├── document_builder.py
│   ├── embed_documents.py
│   └── __init__.py
│
├── retrieval/           # Candidate selection & filtering
│   ├── query_parser.py
│   ├── metadata_filter.py
│   ├── rag_retriever.py
│   └── __init__.py
│
└── recommender/         # LLM prompt orchestration & ranking
    ├── gemini_recommender.py
    └── __init__.py
```

---

## 💻 Usage

### Interactive Mode (Recommended)

```bash
python main.py --interactive
```

**Example Session:**
```
🌱 Query: I need soybean for Maharashtra in Kharif season, rainfed, within 95 days

🔍 Parsing query...
   Filters: state=Maharashtra, season=Kharif, irrigation=rainfed, max_maturity=95 days

🔎 Retrieving candidates...
   Found 2 matching varieties

🤖 Generating recommendations...

======================================================================
🌱 SOYBEAN VARIETY RECOMMENDATIONS
======================================================================

1. PS 1347 (ID: SOY_VER_023)
   Maturity       : 82.0-91.0 days
   Irrigation     : Rainfed / Irrigated
   Yield Potential: 25.9-32.4 q/ha
   Key Strengths  : Drought Escaping (Early Maturity), High Protein (40.8%), Oil Content (21.3%)
   Agronomic Match: Strong agro-climatic fit for Central Zone (MP, Maharashtra, Rajasthan) during Kharif. Early duration (82-91 days) allows escaping terminal dry spells while maintaining high protein and yield stability.

2. Birsa Soya 1 (ID: SOY_VER_041)
   Maturity       : 83.0-93.0 days
   Irrigation     : Protective Irrigation Required
   Yield Potential: 16.5-25.2 q/ha
   Key Strengths  : High Protein (41.9%), Oil Content (21.1%)
   Agronomic Match: Early maturity soybean with excellent nutritional profile, suitable when supplementary irrigation can be provided during pod fill.

----------------------------------------------------------------------
📝 Note: PS 1347 is ranked top based on superior yield stability, regional agro-climatic suitability, and maturity duration.
⚠️  Warnings:
   • Ensure field has adequate drainage as soybean is sensitive to prolonged water stagnation at emergence.
   • Perform certified seed treatment with Rhizobium and Trichoderma before sowing.
   • Some varieties perform optimally with at least 1-2 protective irrigations at pod filling stage.
🤖 Model: gemini-2.5-flash
======================================================================
```

### Command Line Mode

```bash
# Direct query
python main.py --query "Looking for high protein soybean in Madhya Pradesh for Kharif under 100 days"

# Verbose candidate inspection
python main.py --query "Soybean varieties for Karnataka rainfed" --verbose

# Offline / Mock mode without API calls
python main.py --query "Early maturing soybean in Rajasthan" --mock

# View built-in query examples
python main.py --examples
```

### Python API

```python
from src.retrieval import SoybeanRetriever, parse_farmer_query
from src.recommender import recommend_varieties

# User query
query = "I need rainfed soybean for Maharashtra, Kharif season, within 95 days"

# Step 1: Parse query into structured filters
filters = parse_farmer_query(query)

# Step 2: Retrieve matched candidates
retriever = SoybeanRetriever()
candidates = retriever.retrieve(**filters)

# Step 3: Generate evidence-based recommendations
recommendations = recommend_varieties(query, candidates, top_n=3)

# Display results
for rec in recommendations["recommendations"]:
    print(f"{rec['rank']}. {rec['variety_name']} ({rec['variety_id']})")
    print(f"   Maturity: {rec['maturity']} | Irrigation: {rec['irrigation']}")
    print(f"   Reason: {rec['reason']}\n")
```

---

## 📁 Project Structure

```
soybean-seed-rag/
│
├── main.py                     # Entry point (CLI + Interactive modes)
├── setup.py                    # Package setup configuration
├── requirements.txt            # Project dependencies
├── pytest.ini                  # Pytest configuration
├── .env.example                # Sample environment variables template
├── .gitignore                  # Git ignore rules
│
├── src/                        # Core source code
│   ├── __init__.py
│   ├── client/                 # Gemini API integration
│   │   ├── __init__.py
│   │   └── gemini_client.py    # Multi-model fallback client
│   ├── ingestion/              # Data ingestion & document creation
│   │   ├── __init__.py
│   │   ├── document_builder.py # Entity-level document synthesizer
│   │   └── embed_documents.py  # Vector embedding generator
│   ├── retrieval/              # Retrieval and filtering
│   │   ├── __init__.py
│   │   ├── query_parser.py     # Natural language query extractor
│   │   ├── metadata_filter.py  # Hard and soft constraints engine
│   │   └── rag_retriever.py    # Soybean candidate retriever
│   └── recommender/            # LLM ranking and advisory
│       ├── __init__.py
│       └── gemini_recommender.py
│
├── data/                       # Dataset store
│   ├── raw/
│   │   └── soybean_varieties.csv
│   ├── processed/
│   │   ├── soybean_rag_documents.json # 184 synthesized entity documents
│   │   ├── embeddings.npy
│   │   └── embedding_ids.json
│   └── soy.csv                 # Master soybean varieties CSV (184 varieties)
│
├── scripts/                    # Command-line utility scripts
│   ├── build_documents.py      # Build entity JSON database
│   ├── generate_embeddings.py  # Generate embeddings matrix
│   └── validate_data.py        # Data validation and health report
│
└── tests/                      # Automated test suite
    ├── __init__.py
    ├── test_retrieval.py       # Query parser and filter unit tests
    ├── test_recommender.py     # Recommender and client unit tests
    └── test_integration.py     # End-to-end integration tests
```

---

## 🧠 RAG System Design

### 1. Entity-Level Documents (Not Fragmented Chunking)

**Design Decision:** One comprehensive document per soybean variety rather than paragraph chunking.

**Why:**
- ✅ **No Fragmentation**: Retains complete agronomic context (maturity, yield, disease profile, zones).
- ✅ **Deterministic Factuality**: Eliminates disjointed facts across chunks.
- ✅ **Accurate Recommendations**: The LLM compares complete variety profiles side-by-side.

### 2. Hybrid Filtering Strategy

| Approach | Speed | Precision | Cost | Best Use Case |
|---|---|---|---|---|
| **Metadata Filtering** | ⚡ Sub-millisecond | 🎯 Deterministic | 💰 Free | Hard agro-climatic constraints (State, Season, Days) |
| **LLM Reasoning** | 🐌 1–2 seconds | 🧠 Context-aware | 💸 Token-based | Nuanced trade-off ranking, advice, warnings |
| **Hybrid Pipeline** | ⚡ Real-time | 🎯🧠 Best of both | 💰 Minimal | **Our Architecture** |

### 3. Irrigation Uncertainty & Water Management

Soybean crops vary significantly in drought sensitivity and waterlogging tolerance:
- **`MATCH`**: Varieties explicitly suited for the query condition (e.g. `Rainfed` or `Rainfed / Irrigated`).
- **`UNKNOWN / PARTIAL`**: Varieties requiring `Protective Irrigation Required` when rainfed is requested. Kept in the candidate pool so the LLM can evaluate rainfall context and issue appropriate water management warnings.
- **`NO_MATCH`**: Varieties strictly requiring full irrigation when rainfed dryland was requested.

### 4. Model Fallback Architecture

To ensure 99.9%+ availability and avoid disruptions from quota limits or model deprecations, `GeminiClient` employs cascading fallback:

```python
DEFAULT_MODEL_FALLBACKS = [
    "gemini-2.5-flash",
    "gemini-2.0-flash",
    "gemini-1.5-flash",
    "gemini-1.5-pro",
    "gemini-1.5-flash-8b",
]
```

If the API key is not configured or all external API calls fail, the system smoothly falls back to its built-in **Deterministic Recommender**.

---

## 📚 API Reference

### Query Parser
```python
from src.retrieval import parse_farmer_query

filters = parse_farmer_query("I need soybean for Maharashtra, Kharif, rainfed, within 95 days")
# Returns:
# {
#   'state': 'Maharashtra',
#   'season': 'Kharif',
#   'irrigation': 'rainfed',
#   'max_maturity': 95,
#   'min_maturity': None,
#   'traits': [],
#   'raw_query': '...'
# }
```

### Candidate Retriever
```python
from src.retrieval import SoybeanRetriever

retriever = SoybeanRetriever()
candidates = retriever.retrieve(
    state="Maharashtra",
    season="Kharif",
    irrigation="rainfed",
    max_maturity=95
)
# Returns: List of scored candidate variety dictionaries
```

### Recommender
```python
from src.recommender import recommend_varieties

recommendations = recommend_varieties(
    query="I need soybean for Maharashtra, Kharif, rainfed, within 95 days",
    candidates=candidates,
    top_n=3
)
# Returns: Dict containing recommendations, final_note, warnings, and model_used
```

---

## 🧪 Testing

### Run All Unit and Integration Tests
```bash
pytest tests/ -v
```

### Run With Test Coverage
```bash
pytest --cov=src tests/
```

### Run Data Validation Script
```bash
python scripts/validate_data.py
```

**Validation Output:**
```
============================================================
VALIDATING SOYBEAN RAG DOCUMENTS
============================================================
✓ Successfully loaded 184 documents

============================================================
VALIDATION REPORT
============================================================
Total documents: 184
Errors: 0
Warnings: 0

============================================================
STATISTICS
============================================================
Seasons:
  Kharif: 184
  Summer: 102

Irrigation Conditions:
  Rainfed: 69
  Limited Irrigation: 61
  Rainfed / Irrigated: 54

Maturity Classes:
  Early: 58
  Late: 47
  Extra Early: 42
  Medium: 37

Top 8 Adapted States:
  Andhra Pradesh: 87 varieties
  Karnataka: 87 varieties
  Tamil Nadu: 87 varieties
  Telangana: 87 varieties
  Gujarat: 84 varieties
  Madhya Pradesh: 84 varieties
  Maharashtra: 84 varieties
  Rajasthan: 84 varieties

Yield (Normalized Max q/ha):
  Min: 17.6 | Max: 34.0 | Average: 26.0

Quality Metrics:
  Avg Oil Content: 20.26%
  Avg Protein Content: 40.33%
============================================================
```

---

## 🤝 Contributing

We welcome community contributions!

1. Fork the repository and create your feature branch:
   ```bash
   git checkout -b feature/new-variety-metadata
   ```
2. Commit your changes:
   ```bash
   git commit -m "feat: add pest resistance scoring for girdle beetle"
   ```
3. Run the test suite:
   ```bash
   pytest tests/ -v
   ```
4. Push to branch and create a Pull Request.

---

## 🔧 Troubleshooting

| Problem | Cause | Solution |
|---|---|---|
| `Documents file not found` | Processed JSON not built yet | Run `python scripts/build_documents.py` |
| `GEMINI_API_KEY not set` | Missing API key in environment | Create `.env` with `GEMINI_API_KEY=...` or run with `--mock` |
| `Empty candidate results` | Constraints too restrictive | Relax maturity window (e.g. +5-10 days) or check adjacent states |
| `Rate limit exceeded` | Gemini API quota exhausted | The client automatically falls back to secondary models or offline deterministic mode |

---

## 🗺️ Roadmap

- [x] Entity-level document modeling for 48 master soybean varieties
- [x] Multi-constraint deterministic filtering (State, Zone, Season, Maturity, Irrigation)
- [x] Automated cascading Gemini model fallback & offline mode
- [x] Full CLI and interactive terminal application
- [ ] REST API backend with FastAPI
- [ ] Streamlit / React Farmer Web Dashboard
- [ ] Audio / Speech-to-Text query support for regional languages (Hindi, Marathi, Kannada)

---

## 📝 Changelog

### [1.0.0] - 2026-11-21
- Initial production release of the **Soybean Seed RAG System**.
- Implemented `SoybeanRetriever`, `MetadataFilter`, `QueryParser`, and `GeminiRecommender`.
- Integrated 184 verified soybean varieties covering Central Zone, Southern Zone, Northern Plain Zone, and North Eastern Zone.
- Added comprehensive unit, integration, and validation test suites with >75% test coverage.

---

## 📄 License

This project is licensed under the MIT License - see below for details:

```
MIT License

Copyright (c) 2026 Soybean Seed RAG Team

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 🙏 Acknowledgments

- **ICAR - Indian Institute of Soybean Research (IISR), Indore**
- **All India Coordinated Research Project (AICRP) on Soybean**
- **State Agricultural Universities (JNKVV, UAS Dharwad, MPKV, PDKV, GBPUA&T, PAU)**
- **Google Gemini API** for LLM reasoning capabilities

---

**Built with ❤️ for Indian farmers, agronomists, and agricultural innovation.**

**Happy farming! 🌱**
