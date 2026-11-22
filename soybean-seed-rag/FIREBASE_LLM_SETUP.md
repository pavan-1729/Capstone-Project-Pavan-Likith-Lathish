# 🔥 Firebase Local LLM Integration Guide

This guide explains how to use a **local LLM server** (running on another laptop) with your Maize Seed RAG system via Firebase Realtime Database.

---

## 📌 Architecture Overview

```
┌─────────────────────┐         ┌──────────────────────┐         ┌──────────────────────┐
│                     │ Writes  │                      │ Pulls   │                      │
│ Maize RAG System    ├────────►│ Firebase Realtime DB ├────────►│ Local LLM Server     │
│ (Your Laptop)       │◄────────┤ (Cloud Queue)        │◄────────┤ (Another Laptop)     │
└─────────────────────┘ Reads   └──────────────────────┘ Updates └──────────┬───────────┘
                                                                            │
                                                              HTTP POST     │
                                                                            ▼
                                                                 ┌──────────────────────┐
                                                                 │ Ollama (qwen3:4b)    │
                                                                 │ Local Inference      │
                                                                 └──────────────────────┘
```

### Benefits
- ✅ **Zero Cloud GPU Costs** - Use your own hardware
- ✅ **Privacy** - Data stays in your control
- ✅ **Customization** - Use any Ollama model
- ✅ **Asynchronous** - Non-blocking requests via queue

---

## 🛠️ Setup Instructions

### Prerequisites

1. **Two Laptops:**
   - **Laptop A**: Your laptop (Maize RAG System)
   - **Laptop B**: Another laptop (Local LLM Server)

2. **Firebase Project:**
   - Create project at [Firebase Console](https://console.firebase.google.com/)
   - Enable **Realtime Database**
   - Download service account key

3. **Software:**
   - Python 3.8+ on both laptops
   - Ollama installed on Laptop B

---

## 📥 Step 1: Setup Firebase Project

### 1.1 Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **"Add project"**
3. Follow the wizard to create your project

### 1.2 Enable Realtime Database
1. In Firebase Console, go to **Build** > **Realtime Database**
2. Click **"Create Database"**
3. Choose location (closest to you)
4. Start in **Test Mode** (or configure rules later)

### 1.3 Download Service Account Key
1. Go to **Project Settings** (gear icon)
2. Navigate to **Service accounts** tab
3. Click **"Generate new private key"**
4. Save as `serviceAccountKey.json`

### 1.4 Get Database URL
- Copy your database URL (looks like: `https://your-project-id-default-rtdb.firebaseio.com/`)

---

## 💻 Step 2: Setup Laptop B (LLM Server)

### 2.1 Install Ollama
Follow instructions at [ollama.com](https://ollama.com/)

**Windows:**
```powershell
# Download installer from ollama.com/download
# Run installer
```

**macOS/Linux:**
```bash
curl -fsSL https://ollama.com/install.sh | sh
```

### 2.2 Pull Model
```bash
# Pull the model (qwen3:4b is recommended for speed)
ollama pull qwen3:4b

# Alternative models:
# ollama pull llama3.2
# ollama pull mistral
# ollama pull deepseek-r1:7b
```

### 2.3 Clone LLM Server Repository
```bash
cd /path/to/your/projects
git clone <your-llm-server-repo-url>
cd local-llm-server
```

### 2.4 Setup Python Environment
```bash
# Create virtual environment
python -m venv venv

# Activate
# Windows:
venv\Scripts\activate
# macOS/Linux:
source venv/bin/activate

# Install dependencies
pip install firebase-admin python-dotenv requests
```

### 2.5 Configure LLM Server
1. Copy `serviceAccountKey.json` to `local-llm-server/` directory

2. Create `.env` file:
```env
FIREBASE_DATABASE_URL=https://your-project-id-default-rtdb.firebaseio.com/
```

3. Test Ollama connection:
```bash
python test_llm.py
```

4. Start the server:
```bash
python server.py
```

**Expected output:**
```
Firebase connected successfully!

LOCAL LLM SERVER STARTING...

Listening for Firebase jobs...
Ollama model: qwen3:4b
```

**Keep this running!** Leave this terminal window open.

---

## 🌽 Step 3: Setup Laptop A (Maize RAG System)

### 3.1 Install Firebase Dependencies
```bash
cd maize-seed-rag
pip install firebase-admin
```

### 3.2 Add Service Account Key
1. Copy the same `serviceAccountKey.json` to your `maize-seed-rag/` directory

### 3.3 Update .env File
Add to your `.env`:
```env
# Existing Gemini key
GEMINI_API_KEY=your_gemini_key_here

# New Firebase configuration
FIREBASE_DATABASE_URL=https://your-project-id-default-rtdb.firebaseio.com/
FIREBASE_SERVICE_ACCOUNT_PATH=serviceAccountKey.json

# Choose which LLM to use (gemini or firebase)
LLM_BACKEND=firebase
```

### 3.4 Test Firebase LLM Client
```bash
python -c "from src.client.firebase_llm_client import FirebaseLLMClient; client = FirebaseLLMClient(); print(client.generate_content('Hello, test!'))"
```

---

## 🚀 Step 4: Run Maize RAG with Firebase LLM

### Option 1: Using Python API
```python
from src.retrieval import MaizeRetriever, parse_farmer_query
from src.recommender import recommend_varieties_firebase
from src.client import FirebaseLLMClient

# Create Firebase LLM client
llm_client = FirebaseLLMClient()

# Your query
query = "I need rainfed maize for Karnataka, Kharif, within 90 days"

# Parse and retrieve
filters = parse_farmer_query(query)
retriever = MaizeRetriever()
candidates = retriever.retrieve(**filters)

# Get recommendations using Firebase LLM
recommendations = recommend_varieties_firebase(
    query,
    candidates,
    llm_client=llm_client
)

# Display
for rec in recommendations["recommendations"]:
    print(f"{rec['rank']}. {rec['variety_name']}")
    print(f"   Reason: {rec['reason']}\n")
```

### Option 2: Create Unified Script

Create `main_firebase.py`:
```python
import os
from dotenv import load_dotenv
from src.retrieval import MaizeRetriever, parse_farmer_query
from src.recommender import recommend_varieties_firebase
from src.client import FirebaseLLMClient

load_dotenv()

def main():
    """Main function using Firebase LLM."""
    
    # Create LLM client
    llm_client = FirebaseLLMClient()
    
    # Create retriever
    retriever = MaizeRetriever()
    
    print("🌽 MAIZE SEED RAG SYSTEM - FIREBASE LLM MODE")
    print("="*60)
    
    while True:
        # Get query
        query = input("\n🌽 Query (or 'quit' to exit): ").strip()
        
        if query.lower() in ['quit', 'exit', 'q']:
            break
        
        if not query:
            continue
        
        try:
            # Parse query
            print("\n🔍 Parsing query...")
            filters = parse_farmer_query(query)
            print(f"   Filters: {filters}")
            
            # Retrieve candidates
            print("\n🔎 Retrieving candidates...")
            candidates = retriever.retrieve(**filters)
            print(f"   Found {len(candidates)} varieties")
            
            if not candidates:
                print("❌ No varieties found. Try different criteria.")
                continue
            
            # Get recommendations
            print("\n🤖 Generating recommendations via Firebase LLM...")
            recommendations = recommend_varieties_firebase(
                query,
                candidates,
                llm_client=llm_client
            )
            
            # Display results
            print("\n" + "="*60)
            print("🌽 MAIZE VARIETY RECOMMENDATIONS")
            print("="*60)
            
            for rec in recommendations["recommendations"]:
                print(f"\n{rec['rank']}. {rec['variety_name']} (ID: {rec['variety_id']})")
                print(f"   Maturity: {rec.get('maturity', 'N/A')}")
                print(f"   Irrigation: {rec.get('irrigation', 'N/A')}")
                print(f"   Reason: {rec['reason']}")
            
            if recommendations.get('final_note'):
                print(f"\n📝 Note: {recommendations['final_note']}")
            
            if recommendations.get('warnings'):
                print(f"\n⚠️  Warnings:")
                for warning in recommendations['warnings']:
                    print(f"   - {warning}")
            
            print(f"\n🤖 Model: {recommendations.get('model', 'unknown')}")
            print("="*60)
        
        except Exception as e:
            print(f"\n❌ Error: {e}")
            import traceback
            traceback.print_exc()

if __name__ == "__main__":
    main()
```

Run it:
```bash
python main_firebase.py
```

---

## 🔧 Configuration Options

### Change LLM Model (on Laptop B)

Edit `local-llm-server/server.py`:
```python
MODEL = "qwen3:4b"  # Change to your preferred model
```

Available models:
- `qwen3:4b` - Fast, good quality (recommended)
- `llama3.2` - Meta's latest
- `mistral` - Strong reasoning
- `deepseek-r1:7b` - Excellent for technical tasks
- `gemma:7b` - Google's model

### Adjust Timeout Settings

In your `.env`:
```env
FIREBASE_MAX_WAIT_TIME=300  # Maximum seconds to wait for response
FIREBASE_POLL_INTERVAL=2.0   # Check interval in seconds
```

Or in code:
```python
client = FirebaseLLMClient(
    max_wait_time=300,  # 5 minutes
    poll_interval=2.0    # Check every 2 seconds
)
```

---

## 🔍 Troubleshooting

### Problem: "Firebase connection failed"
**Solution:**
1. Check `FIREBASE_DATABASE_URL` in `.env`
2. Verify `serviceAccountKey.json` exists
3. Check Firebase console for database status

### Problem: "Timeout waiting for response"
**Solution:**
1. Ensure Laptop B server is running (`python server.py`)
2. Check Laptop B has internet connection
3. Verify Ollama is running (`ollama list`)
4. Increase timeout in `.env`

### Problem: "Job failed to process"
**Solutions:**
1. Check Laptop B server logs
2. Test Ollama directly: `ollama run qwen3:4b "Hello"`
3. Restart Ollama service
4. Check model is pulled: `ollama list`

### Problem: "Module firebase_admin not found"
**Solution:**
```bash
pip install firebase-admin
```

### Problem: Slow responses
**Solutions:**
1. Use smaller model (qwen3:4b instead of 13b)
2. Upgrade Laptop B hardware
3. Use GPU on Laptop B if available
4. Reduce context size in prompts

---

## 📊 Monitoring & Debugging

### Check Queue Status
```python
from src.client import FirebaseLLMClient

client = FirebaseLLMClient()
pending = client.get_pending_jobs_count()
print(f"Pending jobs: {pending}")
```

### Clean Old Jobs
```python
client.cleanup_old_jobs(max_age_seconds=3600)  # Remove jobs older than 1 hour
```

### View Firebase Console
1. Go to Firebase Console
2. Navigate to Realtime Database
3. Browse `/llm_jobs` to see queue status

---

## 🔒 Security Best Practices

1. **Never commit credentials:**
   ```bash
   # Add to .gitignore
   serviceAccountKey.json
   .env
   ```

2. **Use Firebase rules** (in Firebase Console):
   ```json
   {
     "rules": {
       "llm_jobs": {
         ".read": "auth != null",
         ".write": "auth != null"
       }
     }
   }
   ```

3. **Limit access:**
   - Only authorized users should have database access
   - Keep service account key secure
   - Don't share `.env` file

---

## 🎯 Performance Tips

1. **Batch Processing:**
   - Process multiple queries together when possible
   - Reduces Firebase read/write operations

2. **Model Selection:**
   - Smaller models (4b) → Faster, less memory
   - Larger models (13b+) → Better quality, slower

3. **Hardware:**
   - GPU recommended on Laptop B
   - At least 8GB RAM for 4b models
   - 16GB+ RAM for larger models

4. **Network:**
   - Stable internet on both laptops
   - Firebase has fast global CDN
   - Latency typically <2s

---

## 🚀 Advanced: Multiple Workers

Run multiple LLM servers for load balancing:

**Laptop B:**
```bash
python server.py
```

**Laptop C (optional):**
```bash
python server.py  # Same configuration
```

Both will process jobs from the same queue. Firebase transactions prevent conflicts.

---

## 📝 Comparison: Gemini vs Firebase LLM

| Feature | Gemini (Cloud) | Firebase LLM (Local) |
|---------|----------------|----------------------|
| **Cost** | $$$ (paid API) | Free (your hardware) |
| **Speed** | Fast (1-3s) | Medium (2-10s) |
| **Privacy** | Data sent to Google | Stays local |
| **Setup** | Easy (API key) | Medium (2 laptops) |
| **Quality** | Excellent | Good (model-dependent) |
| **Customization** | Limited | Full control |
| **Availability** | 99.9%+ | Depends on your setup |

**Recommendation:**
- **Development/Testing:** Use Gemini (easy setup)
- **Production/Privacy:** Use Firebase LLM (cost-effective, private)
- **Hybrid:** Use both with automatic fallback

---

## 🤝 Support

**Issues:**
- Laptop B server not responding → Check Ollama and server logs
- Firebase connection issues → Verify credentials and database URL
- Slow inference → Try smaller model or upgrade hardware

**Resources:**
- [Ollama Documentation](https://ollama.com/docs)
- [Firebase Documentation](https://firebase.google.com/docs/database)
- [Project Issues](https://github.com/yourrepo/issues)

---

**Happy farming with local LLMs! 🌽🔥**
