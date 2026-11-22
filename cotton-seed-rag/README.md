# 🌿 Farmer-Aware: Climate-Adaptive Cotton Seed Advisory System
### *Retrieval-Augmented Agricultural Knowledge for Context-Aware Cotton Variety Selection & Agronomic Advisory*

---

## 📌 Executive Summary & Project Motivation

Cotton (*Gossypium spp.*)—popularly heralded as **"White Gold"**—is the backbone of rural agricultural economies in India, engaging over 6 million smallholder farmers and providing livelihood security across the **Central Zone** (Maharashtra, Gujarat, Madhya Pradesh), **Southern Zone** (Andhra Pradesh, Telangana, Karnataka, Tamil Nadu), and **Northern Zone** (Punjab, Haryana, Rajasthan). 

Despite India possessing the largest cotton cultivation area globally, smallholder cotton farmers endure acute climatic vulnerabilities and severe yield gaps:
- **Terminal Drought & Rainfall Deficit**: Rainfed cotton systems (over 60% of total acreage) suffer from monsoon dry spells during critical flowering and boll formation stages.
- **Heat & Moisture Stress**: Elevated temperatures during square initiation and unseasonal torrential rains during boll bursting degrade fiber quality and induce boll rot.
- **Biotic Pest Pressures**: Escalating infestations of **Pink Bollworm (*Pectinophora gossypiella*)**, **American Bollworm (*Helicoverpa armigera*)**, **Whitefly (*Bemisia tabaci*)**, and pathogens like **Cotton Leaf Curl Virus (CLCuD)**, **Charcoal Rot**, **Fusarium Wilt**, and **Bacterial Blight**.

Seed selection is the single most irreversible agronomic decision of the cropping season. Choosing a variety with inappropriate maturity, lack of drought tolerance, or mismatching agro-climatic adaptability leads to catastrophic crop failure and indebtedness.

Generic Large Language Models (LLMs) and chatbots are prone to **hallucination**, frequently inventing non-existent Bt traits, fabricated ginning outturn (GOT) percentages, inaccurate staple lengths, or recommending North Zone irrigated hybrids to rainfed Central Indian drylands.

**Farmer-Aware (Cotton Edition)** solves this crisis through a **Retrieval-Augmented Generation (RAG)** architecture strictly grounded in verified agricultural literature from the **Indian Council of Agricultural Research (ICAR)**, the **Central Institute for Cotton Research (ICAR-CICR, Nagpur)**, the **All India Coordinated Cotton Improvement Project (AICCIP)**, and State Agricultural Universities (PAU, MPKV, CCSHAU, ANGRAU, JNKVV). The system combines:
1. **Multi-lingual Native Android Application** designed specifically for Indian farmers (with English, Telugu, Tamil, and Hindi support).
2. **Enterprise Spring Boot Backend** with MySQL persistence, stateless Firebase authentication, OpenWeatherMap real-time agro-meteorology, and resilient fallback ranking.
3. **Advanced Python RAG Engine** with deterministic metadata filtering (Zone, State, Irrigation, Maturity, Biotic Stress), dense vector embeddings (**BAAI/bge-m3**), Google Gemini multi-model reasoning (`gemini-3-flash-preview`, `gemini-3.1-flash-lite-preview`, `gemini-3.8-flash`), and 100% grounded agricultural citation tracing.
4. **Authoritative Knowledge Base** covering 265 certified cotton varieties across 50 agronomic dimensions and verified ICAR-CICR gazette notifications.

---

## 🗂️ Project Repository Structure

```
Capstone Project/
│
├── 📱 Application_Android/              # Native Android Client Application
│   ├── app/
│   │   ├── src/main/java/com/farm/seedadviosory/
│   │   │   ├── config/                 # AppConfig (Base URL, Demo mode, timeouts)
│   │   │   ├── data/
│   │   │   │   ├── api/ & remote/      # RetrofitClient, ApiService, FirebaseTokenInterceptor
│   │   │   │   ├── mock/               # MockDataProvider (Offline & Demo mode dataset)
│   │   │   │   ├── model/              # Data models (CottonVariety, RecommendationResponse, etc.)
│   │   │   │   └── repository/         # AuthRepository, CropRepository, RecommendationRepository
│   │   │   ├── ui/
│   │   │   │   ├── about/              # AboutActivity, HowItWorksActivity (RAG explainability)
│   │   │   │   ├── advisory/           # CropAdvisoryActivity (Irrigation, Fertilizer, Pest)
│   │   │   │   ├── auth/               # Login, Register, ForgotPassword, LanguageSelection
│   │   │   │   ├── calendar/           # CropCalendarActivity (Stage-by-stage cotton timelines)
│   │   │   │   ├── crophealth/         # CropHealthFragment (Cotton disease diagnostics & pests)
│   │   │   │   ├── home/               # HomeFragment (Weather card, crop status, quick actions)
│   │   │   │   ├── main/               # MainActivity (Bottom navigation controller)
│   │   │   │   ├── notifications/      # NotificationsFragment (Alerts by category)
│   │   │   │   ├── profile/            # ProfileFragment (Farmer details, land size)
│   │   │   │   ├── recommendation/     # Wizard activities, results, variety details & evidence
│   │   │   │   │   ├── adapter/        # RecommendationAdapter (Custom card recycler)
│   │   │   │   │   └── wizard/         # 6 Wizard Step Fragments (Location, Season, Soil, etc.)
│   │   │   │   ├── seed/               # SeedAdvisorFragment (Cotton / Maize crop selector)
│   │   │   │   ├── settings/           # SettingsActivity (Language, Demo mode toggle)
│   │   │   │   └── splash/             # SplashActivity (Branding & initialization)
│   │   │   └── utils/                  # LocaleHelper (Multi-language engine), PreferenceManager
│   │   └── src/main/res/
│   │       ├── layout/                 # 25+ XML activity and fragment layouts
│   │       ├── values/                 # Base English strings, colors, dimensions, themes
│   │       ├── values-te/              # Telugu localization (తెలుగు)
│   │       ├── values-ta/              # Tamil localization (தமிழ்)
│   │       └── values-hi/              # Hindi localization (हिन्दी)
│   └── build.gradle.kts                # Android SDK 37, Java 11, Firebase BOM 34.19.0
│
├── ⚙️ Backend/FarmerSeedAdvisory/        # Enterprise Spring Boot REST API
│   ├── src/main/java/com/anootations/farmerseedadvisory/
│   │   ├── FarmerSeedAdvisoryApplication.java # Spring Boot entry point & CLI runner
│   │   ├── config/                     # FirebaseConfig, SecurityConfig, CORS
│   │   ├── controller/                 # RecommendationController, FarmerController,
│   │   │                               # CottonController, WeatherController, TestController
│   │   ├── dto/                        # Request/Response DTOs (CottonRecommendationRequest, etc.)
│   │   ├── entity/                     # JPA Entities (Farmer, CottonVariety)
│   │   ├── exception/                  # GlobalExceptionHandler, ValidationException, etc.
│   │   ├── repository/                 # FarmerRepository, CottonVarietyRepository
│   │   ├── security/                   # FirebaseAuthenticationFilter (Stateless token verification)
│   │   └── service/                    # RecommendationService, MetadataFilterService,
│   │                                   # CottonDataImportService, LlmService, WeatherService, FarmerService
│   ├── src/main/resources/
│   │   ├── application.properties      # MySQL, Firebase, Weather API configs
│   │   └── data/cotton_varieties.csv   # 265 certified ICAR-CICR cotton records
│   ├── firebase-service-account.json   # Google Firebase Admin SDK credentials
│   └── pom.xml                         # Spring Boot 3.3.5, Java 21, Hibernate, OkHttp, Gson, OpenCSV
│
├── 🧠 cotton-seed-rag/                 # Python RAG Knowledge & Evaluation Pipeline
│   ├── data/
│   │   ├── raw/cotton.csv              # Master dataset (265 varieties, 50 attributes)
│   │   └── processed/
│   │       ├── embeddings.npy          # 265 x 1024 dense vectors (BAAI/bge-m3)
│   │       ├── embedding_ids.json      # Variety ID to vector index mapping
│   │       ├── cotton_rag_documents.json # Clean structured natural language knowledge chunks
│   │       └── validation_report.json  # Comprehensive data quality validation audit
│   ├── src/
│   │   ├── __init__.py
│   │   ├── client/
│   │   │   ├── __init__.py
│   │   │   └── gemini_client.py        # Resilient Gemini API client with multi-model fallback ladder
│   │   ├── ingestion/
│   │   │   ├── __init__.py
│   │   │   ├── document_builder.py     # Converts CSV rows into natural language knowledge cards
│   │   │   └── embed_documents.py      # BAAI/bge-m3 dense vector embedding generator
│   │   ├── recommender/
│   │   │   ├── __init__.py
│   │   │   └── gemini_recommender.py   # Structured prompt builder, JSON parser & zero-hallucination validator
│   │   └── retrieval/
│   │       ├── __init__.py
│   │       ├── metadata_filter.py      # Exact hard & soft multi-attribute filtering (Zone, Season, Water, Maturity)
│   │       ├── query_parser.py         # Regex & NLP constraint extractor from cotton farmer prompts
│   │       └── rag_retriever.py        # Hybrid retriever combining filter + BGE-M3 cosine similarity
│   ├── scripts/
│   │   ├── build_documents.py          # Document building ingestion runner
│   │   ├── generate_embeddings.py      # Dense embedding generator runner
│   │   ├── run_evaluation_benchmark.py # 20 test queries benchmark runner (100% accuracy)
│   │   ├── test_full_metadata_pipeline.py # End-to-end filter & LLM integration test
│   │   ├── test_semantic_search.py     # Interactive vector similarity search tester
│   │   └── validate_dataset.py         # Automated dataset integrity validator
│   ├── notebooks/
│   │   └── evaluation.ipynb            # Interactive Jupyter notebook evaluation (20 Cotton Queries)
│   ├── results/evaluations/
│   │   ├── evaluation_report_20_queries.json # 20 test queries benchmark report (100% accuracy)
│   │   └── evaluation_results_20_queries.csv  # Benchmark summary table in CSV format
│   ├── tests/
│   │   ├── __init__.py
│   │   ├── test_retrieval.py           # Unit tests for filter & query parser (100% passing)
│   │   ├── test_recommender.py         # Unit tests for prompt & JSON parser
│   │   └── test_integration.py         # End-to-end pipeline integration test
│   ├── main.py                         # CLI entrypoint for single query / interactive mode
│   ├── requirements.txt                # Python dependencies
│   ├── setup.py                        # Package setup configuration
│   ├── .env                            # Environment variables & Gemini API keys
│   └── .env.example
│
└── 📚 Cotton Literature & Publications/ # Domain Literature & ICAR-CICR Gazette Bulletins
    ├── AICCIP-Cotton-Annual-Reports.pdf
    ├── CICR-Research-Monographs.pdf
    ├── ICAR-Cotton-Package-of-Practices.pdf
    └── Central-Sub-Committee-Crop-Standards-Varieties-Cotton.pdf
```

---

## 🏛️ System Architecture

The following diagram illustrates the complete end-to-end dataflow and component interaction across the Cotton Seed Advisory System:

```mermaid
flowchart TD
    subgraph Client["📱 Android Native Application (Java 11 / SDK 37)"]
        UI_LANG["Language Selection (EN / TE / TA / HI)"]
        UI_AUTH["Firebase Auth (Login / Register / Demo)"]
        UI_HOME["Home Dashboard (Weather & Status)"]
        UI_WIZARD["6-Step Cotton Recommendation Wizard"]
        UI_EVIDENCE["Evidence & Citation Viewer"]
        UI_CALENDAR["Cotton Phenology Calendar & Advisories"]
        UI_HEALTH["Cotton Health & Bollworm Diagnostic Info"]
    end

    subgraph Gateway["🔐 Security & Network Gateway"]
        FIREBASE_AUTH["Firebase Authentication Service"]
        RETROFIT["Retrofit2 + FirebaseTokenInterceptor"]
    end

    subgraph Backend["⚙️ Spring Boot Enterprise Service (Port 8080)"]
        SEC_FILTER["FirebaseAuthenticationFilter"]
        FARMER_CTRL["FarmerController (/api/farmer/*)"]
        WEATHER_CTRL["WeatherController (/api/weather)"]
        COTTON_CTRL["CottonController (/api/cotton/*)"]
        REC_CTRL["RecommendationController (/api/recommendations/cotton)"]
        
        CACHE["In-Memory Weather Cache (30 min TTL)"]
        FILTER_SRV["MetadataFilterService (Zone, State, Water, Maturity)"]
        DETERM_SRV["Deterministic Cotton Agronomic Scoring Engine (Fallback)"]
        LLM_GW["LlmService (Gemini REST Gateway / Ollama)"]
    end

    subgraph DataStore["💾 Storage & External APIs"]
        MYSQL[("MySQL Database: farmer_seed_advisory")]
        OPEN_WEATHER["OpenWeatherMap REST API"]
        CSV_DATA["cotton.csv (265 Records, 50 Columns)"]
    end

    subgraph PythonRAG["🧠 Python RAG Pipeline (cotton-seed-rag)"]
        DOC_BUILDER["Document Builder (Ingestion)"]
        EMBED_ENG["Sentence-Transformers (BAAI/bge-m3)"]
        VEC_STORE["Dense Vector Index (embeddings.npy: 265 x 1024)"]
        QUERY_PARSER["Query Parser (Constraint Extraction)"]
        HYBRID_RET["Hybrid Retriever (Filter + Cosine Similarity)"]
        GEMINI_CLIENT["Gemini Client (Model Fallback Ladder)"]
        GEMINI_API["Google Gemini LLM (3-flash-preview -> 3.1-flash-lite -> 3.8-flash)"]
    end

    %% Client Interactions
    UI_AUTH -->|Token Generation| FIREBASE_AUTH
    UI_WIZARD -->|POST /api/recommendations/cotton| RETROFIT
    UI_HOME -->|GET /api/weather| RETROFIT
    RETROFIT -->|Bearer Token Header| SEC_FILTER

    %% Backend Flow
    SEC_FILTER -->|Authenticated Principal| FARMER_CTRL
    SEC_FILTER --> REC_CTRL
    SEC_FILTER --> WEATHER_CTRL
    SEC_FILTER --> COTTON_CTRL

    WEATHER_CTRL --> CACHE
    CACHE -->|Cache Miss| OPEN_WEATHER
    FARMER_CTRL --> MYSQL
    COTTON_CTRL --> MYSQL
    CSV_DATA -->|Auto-Import at Startup| MYSQL

    REC_CTRL --> FILTER_SRV
    FILTER_SRV --> MYSQL
    FILTER_SRV -->|Candidate Subset (15-35)| LLM_GW
    LLM_GW -->|LLM Online| GEMINI_API
    LLM_GW -.->|LLM Offline / Timeout| DETERM_SRV
    DETERM_SRV -->|Ranked Fallback Response| REC_CTRL

    %% Python RAG Ingestion & Evaluation
    CSV_DATA --> DOC_BUILDER
    DOC_BUILDER --> EMBED_ENG
    EMBED_ENG --> VEC_STORE
    QUERY_PARSER --> HYBRID_RET
    VEC_STORE --> HYBRID_RET
    HYBRID_RET --> GEMINI_CLIENT
    GEMINI_CLIENT --> GEMINI_API
```

---

## 🔄 End-to-End System Workflows

### 1. Farmer Authentication & Multi-Lingual Onboarding
1. **Language Preference**: Farmer selects their preferred dialect (**English**, **తెలుగు**, **தமிழ்**, or **हिन्दी**). The choice is persisted in `PreferenceManager` and dynamically attached to the `BaseContext` via `LocaleHelper`.
2. **Registration / Login**: 
   - Farmers authenticate via Mobile Number and OTP/Password through Firebase Authentication.
   - For rural areas without connectivity during field trials, a one-tap **"Continue as Demo Farmer"** mode loads pre-indexed ICAR-CICR cotton mock data (`COT_VER_001` H 4, `COT_VER_011` MCU 5, `COT_VER_018` Bunny, etc.).
3. **Profile Synchronization**: The app contacts `POST /api/farmer/register` on Spring Boot. The server extracts the `Firebase UID` from the cryptographically verified JWT and initializes or updates the persistent `Farmer` entity in MySQL.

### 2. The 6-Step Climate-Adaptive Cotton Seed Wizard
Farmers navigate a step-by-step wizard (`RecommendationWizardActivity`) tailored to capture specific cotton cultivation constraints:
- **Step 1: Agro-Climatic Zone & Location**: Farmer selects State and District (prefilled from profile), automatically mapping to the official **ICAR Cotton Zone**:
  - *Northern Zone*: Punjab, Haryana, Rajasthan
  - *Central Zone*: Maharashtra, Gujarat, Madhya Pradesh
  - *Southern Zone*: Andhra Pradesh, Telangana, Karnataka, Tamil Nadu
- **Step 2: Cropping Season**: Cotton in India is primarily cultivated in the **Kharif** season (sowing April–June, harvesting October–February), with irrigated or rainfed designations.
- **Step 3: Soil Characteristics**: Selection of soil classification (Deep Black Cotton Soils / Vertisols, Medium Black Soil, Sandy Loam, Alluvial Soil) with optional pH and salinity metrics.
- **Step 4: Irrigation Capability**:
  - *Rainfed* (100% dependent on southwest/northeast monsoon precipitation; strictly requires drought-tolerant cultivars)
  - *Protective Irrigated* (1–2 critical supplemental irrigations during flowering/boll bursting)
  - *Assured Irrigated* (Canal or tube-well irrigation accessible throughout season)
- **Step 5: Climate & Biotic Risks**: Identification of farm-specific threats:
  - Climate risks: Terminal dry spells, heat stress during square formation, waterlogging in heavy soils, unseasonal rains during boll bursting.
  - Pest/Disease pressures: Pink Bollworm, American Bollworm, Whitefly, Jassids, CLCuD, Fusarium Wilt, Charcoal Rot, Leaf Blight.
- **Step 6: Fiber Traits & Agronomic Priorities**:
  - Maturity limit (e.g., `< 160 days` early maturing, `< 170 days` medium, `< 180 days` full season).
  - Cotton Category: *Bt Cotton Hybrid*, *Gossypium hirsutum*, *Gossypium arboreum* (Desi), *Gossypium herbaceum*, or *Gossypium barbadense* (Extra Long Staple).
  - Fiber Quality: Long staple length (>= 30 mm), high Ginning Outturn (GOT >= 37%), Micronaire (3.5–4.5).
  - Optimization Priority: Highest Seed Cotton Yield, Moisture Stress Tolerance, Fiber Premium, Balanced.

### 3. Two-Stage RAG Retrieval & Semantic Reasoning
When the farmer submits the wizard, the advisory request executes through a two-stage hybrid pipeline:

```
[265 Cotton Varieties in Catalog]
         │
         ▼  Stage 1: Hard & Soft Metadata Filtering
    • Zone & State Match (Northern, Central, or Southern Zone)
    • Season Match (Kharif compatibility)
    • Maturity Cutoff (MaturityMax <= farmer cutoff, 140–183 days)
    • Irrigation Compatibility (Exclude explicit incompatibilities)
         │
         ▼
[15 – 35 Candidate Varieties]
         │
         ▼  Stage 2: Generative Reasoning & Vector Grounding
    • Candidate context compiled (Yield, pedigree, GOT, staple, stress ratings)
    • Sent to LLM with strict negative constraint prompt
    • Grounding constraint: Use ONLY provided ICAR-CICR context
         │
         ▼
[Ranked Top-5 Recommendations with Citation Badges & Agronomic Warnings]
```

### 4. Zero-Downtime Deterministic Fallback Engine
To guarantee continuous service when cloud LLMs experience rate limits or latency spikes:
- If the LLM gateway (`LlmService` or `gemini_client`) times out or returns unparseable text, the system seamlessly triggers `buildDeterministicResponse()`.
- Calculates an objective cotton suitability score (0–10 points):
  - **Regional/Zone match**: +3.0 pts for direct state/zone match.
  - **Season match**: +2.0 pts for Kharif compatibility.
  - **Drought tolerance**: +1.5 pts for documented rainfed/moisture stress tolerance.
  - **Bollworm & pest resistance**: +1.5 pts for documented Pink Bollworm / Stem Borer resistance.
  - **High Ginning Outturn (GOT >= 37%)**: +1.0 pt.
  - **Verified ICAR-CICR publication citation**: +1.0 pt.
- Delivers a structured, ranked output with clear indicator badges notifying the farmer that deterministic scoring was engaged.

### 5. Citation Grounding & Evidence Verification (`EvidenceActivity`)
Every recommended variety includes cryptographic traceability to verified literature:
- **Source Organization**: e.g., *ICAR-CICR (Central Institute for Cotton Research, Nagpur)*, *MPKV Rahuri*, *PAU Ludhiana*, *CCS HAU Hisar*
- **Source Document**: e.g., *ICAR-CICR Technical Bulletin / AICCIP Annual Report / Central Variety Gazette Notification*
- **Exact Document Pages**: Official PDF and gazette notification reference
- **Verbatim Excerpt**: Verbatim text confirming release parameters, notification number (e.g., S.O. 4001 (E)), and morphological traits

### 6. Real-Time Agro-Meteorology Workflow
- Displays temperature, relative humidity, rainfall probability, and wind velocity.
- The mobile app queries `GET /api/weather?state={state}&district={district}`.
- Backend verifies an in-memory `ConcurrentHashMap` cache with a 30-minute TTL.
- On cache miss, it contacts OpenWeatherMap using server-side credentials, shielding API keys from reverse engineering.

### 7. Cotton Phenological Calendar & Agronomic Management Workflow
- **Crop Calendar**: Tracks cotton lifecycle across 6 key developmental stages:
  1. *Sowing & Germination* (Day 0–15)
  2. *Seedling & Early Vegetative* (Day 16–40)
  3. *Squaring (Floral Bud Formation)* (Day 41–60)
  4. *Flowering & Peak Boll Setting* (Day 61–100)
  5. *Boll Maturation & Fiber Elongation* (Day 101–140)
  6. *Boll Bursting & Picking / Harvest* (Day 140–180+)
- **Actionable Advisories**: Specific recommendations for *Life-saving Irrigation*, *Nutrient Application (NPK split & foliar KNO3)*, *Square/Boll Drop Management*, and *Integrated Pest Management (Pheromone traps for Pink Bollworm)*.

---

## 💻 Feature Implementation Details

### 1. Python RAG & Evaluation Engine (`cotton-seed-rag`)

| Component | Responsibility | Implementation Details |
| :--- | :--- | :--- |
| **Document Ingestion** | Transforms tabular CSV records into natural-language documents with source citations | [`document_builder.py`](file:///c:/Users/gangi/OneDrive/Desktop/Capstone%20Project/cotton-seed-rag/src/ingestion/document_builder.py): generates `cotton_rag_documents.json` |
| **Dense Embeddings** | Generates normalized dense vector embeddings using `BAAI/bge-m3` | [`embed_documents.py`](file:///c:/Users/gangi/OneDrive/Desktop/Capstone%20Project/cotton-seed-rag/src/ingestion/embed_documents.py): output shape `(265, 1024)`, stored in `embeddings.npy` |
| **Metadata Filter** | Deterministic multi-attribute filtering (Zone, State, Season, Irrigation, Maturity) | [`metadata_filter.py`](file:///c:/Users/gangi/OneDrive/Desktop/Capstone%20Project/cotton-seed-rag/src/retrieval/metadata_filter.py): handles NZ/CZ/SZ zone-to-state mapping |
| **Query Parser** | Extracts structured constraints from freeform natural language | [`query_parser.py`](file:///c:/Users/gangi/OneDrive/Desktop/Capstone%20Project/cotton-seed-rag/src/retrieval/query_parser.py): regex and abbreviation matching for Indian cotton zones |
| **Hybrid Retriever** | Combines deterministic metadata filtering with dense vector cosine similarity | [`rag_retriever.py`](file:///c:/Users/gangi/OneDrive/Desktop/Capstone%20Project/cotton-seed-rag/src/retrieval/rag_retriever.py): `CottonRetriever` class |
| **Gemini Client** | Connects to Google GenAI SDK with automatic multi-model failover ladder | [`gemini_client.py`](file:///c:/Users/gangi/OneDrive/Desktop/Capstone%20Project/cotton-seed-rag/src/client/gemini_client.py): fallback sequence: `gemini-3-flash-preview` → `gemini-3.1-flash-lite-preview` → `gemini-3.8-flash` → `gemini-flash-latest` |
| **JSON Cleaner & Validator** | Validates schema and enforces zero-hallucination constraint | [`gemini_recommender.py`](file:///c:/Users/gangi/OneDrive/Desktop/Capstone%20Project/cotton-seed-rag/src/recommender/gemini_recommender.py): checks `variety_id` candidate membership |
| **Evaluation Benchmark** | Comprehensive runner evaluating 20 multi-condition queries across India | [`run_evaluation_benchmark.py`](file:///c:/Users/gangi/OneDrive/Desktop/Capstone%20Project/cotton-seed-rag/scripts/run_evaluation_benchmark.py) & [`evaluation.ipynb`](file:///c:/Users/gangi/OneDrive/Desktop/Capstone%20Project/cotton-seed-rag/notebooks/evaluation.ipynb) |

### 2. Spring Boot Enterprise Backend (`Backend/FarmerSeedAdvisory`)

| Component | Responsibility | Key Classes / Files |
| :--- | :--- | :--- |
| **Stateless Security Filter** | Intercepts requests, validates Firebase JWT, sets authenticated principal in `SecurityContext` | `FirebaseAuthenticationFilter.java` |
| **Automated CSV Importer** | On startup, parses cotton variety records and populates MySQL table | `CottonDataImportService.java` |
| **Metadata Filtering Engine** | Java implementation of cotton zone matching, irrigation compatibility, and maturity cutoffs | `MetadataFilterService.java` |
| **Cotton Recommendation Controller** | Handles `POST /api/recommendations/cotton` and orchestrates RAG flow | `RecommendationController.java` & `RecommendationService.java` |
| **Agro-Meteorology Gateway** | Proxies OpenWeatherMap API with 30-minute concurrent cache | `WeatherService.java` |

### 3. Android Client Application (`Application_Android`)

| Feature | Technical Implementation | File Path |
| :--- | :--- | :--- |
| **Multi-Language Architecture** | Resource qualifiers (`values-te`, `values-ta`, `values-hi`) with `LocaleHelper` runtime context switching | `LocaleHelper.java` |
| **Crop Selector Screen** | Interactive crop selection card allowing farmers to choose **Cotton** or **Maize** | `SeedAdvisorFragment.java` |
| **6-Step Recommendation Wizard** | `ViewPager2` with dynamic step validation for Cotton agro-ecological parameters | `RecommendationWizardActivity.java` |
| **Variety Comparison & Evidence** | Displays Ginning Outturn (GOT), staple length, drought ratings, and ICAR-CICR gazette citations | `VarietyDetailActivity.java` & `EvidenceActivity.java` |
| **Cotton Phenology Calendar** | Stage-by-stage monitoring for Cotton (Sowing to Boll Bursting) with crop advisory cards | `CropCalendarActivity.java` |

---

## 📊 Dataset Specifications & Validation Audit

The Cotton variety catalog is constructed from official research bulletins published by ICAR-CICR (Nagpur) and AICCIP. The automated audit script (`scripts/validate_dataset.py`) verifies:

- **Total Catalog Size**: 265 certified cotton cultivars & hybrids
- **Agronomic Dimensions**: 50 verified columns per variety
- **Required Field Completeness**: 100% complete:
  - `variety_id`: 0 missing
  - `variety_name`: 0 missing
  - `crop`: 0 missing (100% Cotton)
  - `category`: 0 missing (Bt Cotton Hybrid, G. hirsutum, G. arboreum, G. herbaceum, G. barbadense)
  - `season`: 0 missing (100% Kharif)
  - `source_organization`: 0 missing
  - `source_document`: 0 missing
  - `source_pdf_page`: 0 missing
  - `source_text`: 0 missing
- **Duplicate Records**: 0 duplicate variety IDs, 0 duplicate names
- **Zone Distribution**:
  - Northern Zone (Punjab, Haryana, Rajasthan): **90 varieties**
  - Southern Zone (Andhra Pradesh, Telangana, Tamil Nadu, Karnataka): **88 varieties**
  - Central Zone (Gujarat, Maharashtra, Madhya Pradesh): **87 varieties**
- **Irrigation Breakdown**:
  - Protective Irrigated: **95 varieties**
  - Irrigated: **93 varieties**
  - Rainfed: **77 varieties**
- **Botanical Categories**:
  - *Gossypium hirsutum* (American Cotton): **61 varieties**
  - *Gossypium herbaceum* (Desi diploid): **61 varieties**
  - *Gossypium arboreum* (Desi diploid): **59 varieties**
  - *Bt Cotton Hybrids* (Transgenic): **46 varieties**
  - *Gossypium barbadense* (Egyptian / ELS): **38 varieties**
- **Validation Audit Status**: **PASSED** (see [`validation_report.json`](file:///c:/Users/gangi/OneDrive/Desktop/Capstone%20Project/cotton-seed-rag/data/processed/validation_report.json))

---

## 🧪 Evaluation Benchmark & Empirical Results

The Cotton Seed RAG pipeline underwent testing using a benchmark suite of **20 realistic farmer query profiles** representing diverse agricultural zones across India (Andhra Pradesh, Telangana, Karnataka, Tamil Nadu, Maharashtra, Gujarat, Madhya Pradesh, Punjab, Haryana, Rajasthan).

### Benchmark Summary Metrics (`results/evaluations/evaluation_report_20_queries.json`)

| Evaluation Dimension | Metric | Score / Result |
| :--- | :--- | :--- |
| **Dataset Scale** | Total Cotton Varieties Indexed | **265** |
| **Retrieval Scale** | Total Candidates Retrieved across 20 Queries | **687** |
| **Candidate Selectivity** | Average Candidates Retrieved per Query | **34.35** |
| **Metadata Constraint Accuracy** | Regional / Zone Matching Accuracy | **100.0% (1.0)** |
| **Metadata Constraint Accuracy** | Season Matching Accuracy | **100.0% (1.0)** |
| **Metadata Constraint Accuracy** | Maturity Constraint Accuracy | **100.0% (1.0)** |
| **Metadata Constraint Accuracy** | **Overall Metadata Constraint Accuracy** | **100.0% (1.0)** |
| **LLM Reasoning (Gemini)** | Gemini Generation Success Rate | **100.0% (1.0)** |
| **LLM Reasoning (Gemini)** | JSON Schema Compliance Rate | **100.0% (1.0)** |
| **Hallucination Prevention** | **Candidate Grounding Rate (Zero Hallucination)** | **100.0% (1.0)** |
| **Maturity Reporting** | Accurate Maturity Range Reporting | **100.0% (1.0)** |
| **Irrigation Constraint** | Irrigation Handling Accuracy | **100.0% (1.0)** |
| **End-to-End Pipeline** | **Overall Pipeline Success Rate** | **100.0% (1.0)** |
| **Performance** | Average End-to-End Latency per Query | **12.29 seconds** |

### Benchmark Results Table (All 20 Test Queries)

| Query ID | State / Agro-Climatic Zone | Season | Irrigation | Max Maturity | Candidates Found | Top 3 Recommendations | Model Used | Latency | Grounded | Pipeline Success |
| :---: | :--- | :--- | :--- | :---: | :---: | :--- | :--- | :---: | :---: | :---: |
| **Q01** | Andhra Pradesh | Kharif | Rainfed | 165 days | 32 | MCU 8, NCS 222, NH 1901 Bt | gemini-3-flash-preview | 13.2s | **True** | **100%** |
| **Q02** | Telangana | Kharif | Rainfed | 165 days | 32 | MCU 8, Bio 444, NCS 222 | gemini-3-flash-preview | 31.3s | **True** | **100%** |
| **Q03** | Karnataka | Kharif | Rainfed | 170 days | 41 | MCU 8, NH 1901 Bt, MCU 7 | gemini-3-flash-preview | 10.5s | **True** | **100%** |
| **Q04** | Tamil Nadu | Kharif | Irrigated | 175 days | 55 | MCU 3, Bheem, MCU 9 | gemini-3-flash-preview | 11.6s | **True** | **100%** |
| **Q05** | Andhra Pradesh | Kharif | Irrigated | 170 days | 45 | MCU 3, Bheem, Bio 644 | gemini-3-flash-preview | 14.7s | **True** | **100%** |
| **Q06** | Telangana | Kharif | Rainfed | 160 days | 20 | MCU 7, NH 1901 Bt, NCS 222 | gemini-3-flash-preview | 12.0s | **True** | **100%** |
| **Q07** | Maharashtra | Kharif | Rainfed | 165 days | 26 | MCU 5, Sujata, RCH 650 | gemini-3-flash-preview | 12.9s | **True** | **100%** |
| **Q08** | Gujarat | Kharif | Irrigated | 170 days | 36 | Ankur 3100, RCH 530, H 1098 | gemini-3-flash-preview | 12.1s | **True** | **100%** |
| **Q09** | Madhya Pradesh | Kharif | Rainfed | 165 days | 26 | MCU 5, Sujata, RCH 650 | gemini-3-flash-preview | 15.4s | **True** | **100%** |
| **Q10** | Maharashtra (Vidarbha) | Kharif | Rainfed | 160 days | 16 | Tulasi 111, RCH 650, Sujata | gemini-3-flash-preview | 12.0s | **True** | **100%** |
| **Q11** | Gujarat | Kharif | Protective Irri | 175 days | 73 | NCS 207, Surabhi, Virnar | gemini-3-flash-preview | 15.6s | **True** | **100%** |
| **Q12** | Madhya Pradesh | Kharif | Irrigated | 170 days | 36 | Ankur 3100, RCH 530, H 1098 | gemini-3-flash-preview | 10.2s | **True** | **100%** |
| **Q13** | Punjab | Kharif | Irrigated | 165 days | 28 | US 333, CICR 23 Bt, F 2383 | gemini-3-flash-preview | 11.4s | **True** | **100%** |
| **Q14** | Haryana | Kharif | Irrigated | 170 days | 46 | CSH 39 R, F 2383, CICR 23 Bt | gemini-3-flash-preview | 13.9s | **True** | **100%** |
| **Q15** | Rajasthan | Kharif | Irrigated | 165 days | 28 | US 333, F 2383, CICR 23 Bt | gemini-3-flash-preview | 9.9s | **True** | **100%** |
| **Q16** | Punjab | Kharif | Irrigated | 160 days | 15 | US 333, CICR 23 Bt, Srinandi | gemini-3-flash-preview | 11.1s | **True** | **100%** |
| **Q17** | Haryana | Kharif | Rainfed | 170 days | 39 | Nandyal Cotton 25, H 1236, Ankur 3028 | gemini-3.1-flash-lite | 4.3s | **True** | **100%** |
| **Q18** | Rajasthan | Kharif | Rainfed | 165 days | 25 | NCS 277, SP 1010, Bio 333 | gemini-3-flash-preview | 15.2s | **True** | **100%** |
| **Q19** | Southern Zone | Kharif | Rainfed | 165 days | 32 | MCU 7, Prabhat, Bio 444 | gemini-3.1-flash-lite | 4.2s | **True** | **100%** |
| **Q20** | Central Zone | Kharif | Irrigated | 170 days | 36 | H 777, Surabhi, F 414 | gemini-3.1-flash-lite | 4.5s | **True** | **100%** |

---

## 📡 API Specification & Data Contracts

All endpoints accept authentication via Firebase JWT:
```http
Authorization: Bearer <firebase-id-token>
```

### 1. Cotton Recommendation API
- **Endpoint**: `POST /api/recommendations/cotton`
- **Request Body**:
```json
{
  "state": "Maharashtra",
  "district": "Nagpur",
  "season": "Kharif",
  "soilType": "Black Cotton Soil",
  "irrigation": "Rainfed",
  "maximumMaturityDays": 165,
  "category": "Bt Cotton Hybrid",
  "priority": "Highest Yield",
  "climateRisks": ["Terminal Drought", "Pink Bollworm"],
  "farmerDescription": "Dry spell expected in September during boll development"
}
```
- **Response Body**:
```json
{
  "recommendations": [
    {
      "varietyId": "COT_VER_011",
      "varietyName": "MCU 5",
      "score": 0.94,
      "suitability": "HIGH",
      "category": "Gossypium Hirsutum",
      "season": "Kharif",
      "maturityDays": "155–165 days",
      "yieldRange": "22.2-29.0 q/ha",
      "irrigationCondition": "Rainfed",
      "recommendedRegions": "Central Zone (GJ, MH, MP)",
      "droughtTolerance": "Tolerant",
      "pinkBollwormResistance": "Highly Resistant",
      "specialTraits": "Ginning outturn 37%, Micronaire 4.2",
      "sourceOrganization": "ICAR-CICR Nagpur",
      "sourceDocument": "CICR Research Monograph",
      "reason": "MCU 5 is specifically suited for rainfed conditions in the Central Zone (Maharashtra). It matures within 165 days and possesses documented high drought tolerance and resistance to pink stem borer/bollworm.",
      "matchReasons": [
        "✓ Recommended for Central Zone (Maharashtra)",
        "✓ Ideal for Kharif rainfed cultivation",
        "✓ Matures within 165 days (meets <=165 day cutoff)",
        "✓ High ginning outturn (37%) and staple length 27mm"
      ],
      "warnings": [
        "Monitor for whitefly during vegetative phase to prevent CLCuD transmission.",
        "Maintain proper field drainage during initial vegetative growth."
      ]
    }
  ],
  "summary": "Retrieved 26 eligible candidate varieties for Maharashtra rainfed conditions.",
  "candidatesFound": 26,
  "modelUsed": "gemini-3-flash-preview",
  "llmAvailable": true,
  "generatedAt": "2026-11-22T15:57:00"
}
```

### 2. Cotton Variety Catalog API
- **Endpoint**: `GET /api/cotton/varieties/{varietyId}` — Detailed agronomic, botanical, and fiber traits for a variety.
- **Endpoint**: `GET /api/cotton/count` — Returns catalog record count (265 varieties).

---

## 📈 Feature Status Matrix

| Module | Feature | Status | Notes |
| :--- | :--- | :--- | :--- |
| **Android UI** | 6-Step Cotton Recommendation Wizard | ✅ **Complete** | Validated parameters for Indian cotton zones |
| **Android UI** | 4-Language Localization | ✅ **Complete** | English, Telugu, Tamil, Hindi strings & layouts |
| **Android UI** | Variety Detail & Evidence Screens | ✅ **Complete** | Renders GOT %, micronaire, staple length & ICAR citations |
| **Android UI** | Weather Card & Dashboard | ✅ **Complete** | Real-time weather proxy with 30-min TTL cache |
| **Android UI** | Cotton Phenology Calendar | ✅ **Complete** | 6-stage developmental calendar & bollworm advisories |
| **Android UI** | Offline / Demo Mode | ✅ **Complete** | Contains verified ICAR-CICR cotton mock data |
| **Backend** | Firebase JWT Authentication | ✅ **Complete** | Stateless `FirebaseAuthenticationFilter` |
| **Backend** | Cotton CSV Importer & MySQL DB | ✅ **Complete** | Startup loader for 265 certified cotton records |
| **Backend** | Metadata Filtering Service | ✅ **Complete** | Filters by Zone/State, Season, Maturity, and Water |
| **Backend** | Deterministic Scoring Fallback | ✅ **Complete** | Algorithmic scoring when LLMs are unavailable |
| **RAG Pipeline**| BGE-M3 Dense Vector Embeddings | ✅ **Complete** | `embeddings.npy` (265 x 1024 dense vectors) |
| **RAG Pipeline**| Multi-Model Gemini Fallback Ladder | ✅ **Complete** | Failover: `3-flash-preview` → `3.1-flash-lite` → `3.8-flash` |
| **RAG Pipeline**| Zero-Hallucination Grounding Prompt | ✅ **Complete** | 100% grounding across all 20 benchmark tests |
| **RAG Pipeline**| Evaluation Suite (20 Queries) | ✅ **Complete** | Benchmark reports generated in JSON and CSV |
| **Unit Tests**  | Automated PyTest Test Suite | ✅ **Complete** | 11/11 tests passing (`test_retrieval`, `test_recommender`, `test_integration`) |

---

## 🚀 Installation & Setup Guide

### 1. Prerequisites
- **Python**: Python 3.10+
- **Java**: JDK 21+
- **Database**: MySQL 8.0+
- **Android Studio**: Ladybug / Hedgehog or newer (Android SDK 37)

---

### 2. Setting Up the Cotton RAG Pipeline (`cotton-seed-rag`)
```powershell
# Navigate to the cotton RAG directory
cd "c:\Users\gangi\OneDrive\Desktop\Capstone Project\cotton-seed-rag"

# Create and activate virtual environment
python -m venv venv
.\venv\Scripts\Activate.ps1

# Install required dependencies
pip install -r requirements.txt

# Configure Gemini API key in .env
echo "GEMINI_API_KEY=your_actual_gemini_api_key_here" > .env

# Generate knowledge documents from CSV
python scripts/build_documents.py

# Generate dense vector embeddings (BAAI/bge-m3)
python scripts/generate_embeddings.py

# Run dataset validation audit
python scripts/validate_dataset.py

# Run test suite
pytest tests/ -v

# Run interactive semantic search test
python scripts/test_semantic_search.py

# Run end-to-end integration test
python scripts/test_full_metadata_pipeline.py

# Run full 20-query evaluation benchmark
python scripts/run_evaluation_benchmark.py
```

---

### 3. Using the CLI Interactive Seed Advisor (`main.py`)
```powershell
# Run a specific query
python main.py --query "I am a farmer from Maharashtra wanting to grow cotton in Kharif under rainfed conditions with maturity within 165 days"

# Run in interactive chat mode
python main.py --interactive
```

---

## ⚖️ Responsible AI & Agronomic Disclaimers

1. **Evidence-Grounded Recommendation**: This advisory engine provides research-backed recommendations strictly compiled from official ICAR-CICR and AICCIP bulletins. It does not replace local agronomic judgment.
2. **Micro-Climate & Soil Testing**: Local soil conditions (Vertisols vs. red sandy soils) and unseasonal weather shifts may alter crop duration and yield potential. Farmers should consult local **Krishi Vigyan Kendras (KVK)** or Agricultural Officers before final seed procurement.
3. **Pest & Chemical Disclaimers**: Chemical and biological pest controls (e.g. Bt refugia planting, pheromone traps, biopesticides) are advisory guidelines. Farmers must adhere strictly to Central Insecticides Board & Registration Committee (CIBRC) approved dosage rates.

---

## 👥 Contributors & Academic Context

- **Program**: B.Tech / M.Tech Capstone Project
- **Domain**: Artificial Intelligence in Agriculture & Climate-Adaptive Decision Support Systems
- **Author / Developer**: Capstone Project Team
- **Academic Year**: 2025–2026
