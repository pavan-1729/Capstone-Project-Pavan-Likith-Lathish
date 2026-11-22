# ⏱️ Firebase LLM Timeout Configuration Guide

## Overview

This guide explains how to configure timeout settings for the Firebase LLM client, including how to set unlimited waiting time for slow LLM responses.

---

## 🔧 Configuration Methods

### Method 1: Environment Variables (.env) - Recommended

Edit your `.env` file:

```env
# Firebase LLM Client Settings
FIREBASE_MAX_WAIT_TIME=86400  # 24 hours (effectively unlimited for most use cases)
FIREBASE_POLL_INTERVAL=3.0     # Check every 3 seconds
```

**Common Values:**
```env
# Short timeout (for fast models)
FIREBASE_MAX_WAIT_TIME=300     # 5 minutes

# Medium timeout (for average models)
FIREBASE_MAX_WAIT_TIME=1800    # 30 minutes

# Long timeout (for slow models)
FIREBASE_MAX_WAIT_TIME=7200    # 2 hours

# Very long timeout (effectively unlimited)
FIREBASE_MAX_WAIT_TIME=86400   # 24 hours

# True unlimited (wait forever)
FIREBASE_MAX_WAIT_TIME=0       # 0 = unlimited (wait forever)
```

### Method 2: Python Code

```python
from src.client import FirebaseLLMClient

# Specific timeout
client = FirebaseLLMClient(max_wait_time=3600)  # 1 hour

# Unlimited waiting
client = FirebaseLLMClient(max_wait_time=0)     # 0 = unlimited
# OR
client = FirebaseLLMClient(max_wait_time=-1)    # Negative = unlimited
```

---

## 📊 Timeout Values Guide

| Time | Seconds | Use Case |
|------|---------|----------|
| 5 minutes | 300 | Fast models (qwen3:4b on GPU) |
| 10 minutes | 600 | Medium models (qwen3:4b on CPU) |
| 30 minutes | 1800 | Large models (llama3.2 on CPU) |
| 1 hour | 3600 | Very large models (13b+ on CPU) |
| 2 hours | 7200 | Extremely large models |
| 6 hours | 21600 | Batch processing |
| 24 hours | 86400 | Effectively unlimited |
| Unlimited | 0 | Wait forever (not recommended) |

---

## ⚙️ Poll Interval

The poll interval controls how often the client checks Firebase for updates.

```env
# Check every 2 seconds (more responsive, more reads)
FIREBASE_POLL_INTERVAL=2.0

# Check every 3 seconds (balanced - recommended)
FIREBASE_POLL_INTERVAL=3.0

# Check every 5 seconds (less responsive, fewer reads)
FIREBASE_POLL_INTERVAL=5.0

# Check every 10 seconds (minimal reads, use for very slow models)
FIREBASE_POLL_INTERVAL=10.0
```

**Recommendation:**
- Fast models (GPU): 2-3 seconds
- Slow models (CPU): 5-10 seconds
- Very slow models: 10-15 seconds

**Firebase Costs:**
- Each poll = 1 database read
- 3-second interval = 20 reads/minute = 1,200 reads/hour
- Firebase free tier: 10GB downloaded/month (millions of reads)
- Cost is negligible for normal use

---

## 🚀 Current Configuration

Your current `.env` settings:

```env
FIREBASE_MAX_WAIT_TIME=86400  # 24 hours
FIREBASE_POLL_INTERVAL=3.0     # 3 seconds
```

**This means:**
- ✅ The client will wait up to **24 hours** for a response
- ✅ It checks Firebase every **3 seconds**
- ✅ You'll see progress updates every **30 seconds**
- ✅ This is effectively unlimited for all practical purposes

---

## 📝 Progress Indicators

The client shows helpful progress messages:

```
📤 Submitting job: job_1794544180_f4343a19
⏳ Waiting for response (max 86400s)...
⏳ Pending.... (15s)
⏳ Processing... (45s)
⏳ Still processing... (1m 30s elapsed)
⏳ Still processing... (2m 0s elapsed)
✓ Response received (264.4s)
```

**Progress updates:**
- Every 30 seconds, you'll see elapsed time
- Shows current status (pending/processing)
- Final time when response is received

---

## 🔍 Troubleshooting

### Issue: Timeout too short

**Symptom:**
```
TimeoutError: LLM server did not respond within 300 seconds
```

**Solution:**
```env
# Increase timeout in .env
FIREBASE_MAX_WAIT_TIME=7200  # 2 hours
```

### Issue: Want true unlimited waiting

**Solution:**
```env
# Set to 0 for unlimited
FIREBASE_MAX_WAIT_TIME=0
```

**OR in code:**
```python
client = FirebaseLLMClient(max_wait_time=0)
```

### Issue: Too many Firebase reads

**Symptom:** High Firebase database usage

**Solution:**
```env
# Increase poll interval
FIREBASE_POLL_INTERVAL=10.0  # Check every 10 seconds
```

### Issue: Not seeing any progress

**Solution:**
- Check that server is running on other laptop
- Verify Firebase connection
- Look at Firebase console to see if job exists

---

## 💡 Best Practices

### 1. Set Realistic Timeouts

```env
# For qwen3:4b on decent hardware
FIREBASE_MAX_WAIT_TIME=1800  # 30 minutes is reasonable

# For larger models or slow hardware
FIREBASE_MAX_WAIT_TIME=7200  # 2 hours gives plenty of buffer

# For production with unknown model
FIREBASE_MAX_WAIT_TIME=86400 # 24 hours catches everything
```

### 2. Balance Polling vs Responsiveness

```env
# Fast responses needed (web app)
FIREBASE_POLL_INTERVAL=2.0

# Batch processing (don't care about latency)
FIREBASE_POLL_INTERVAL=10.0
```

### 3. Monitor Actual Response Times

```python
import time
from src.client import FirebaseLLMClient

client = FirebaseLLMClient()

start = time.time()
response = client.generate_content("Your prompt")
elapsed = time.time() - start

print(f"Response time: {elapsed:.1f}s")

# Adjust timeout based on typical times
# Set timeout to 3x your average response time
```

### 4. Set Per-Query Timeouts

```python
# For quick queries
quick_client = FirebaseLLMClient(max_wait_time=300)  # 5 min

# For complex queries
complex_client = FirebaseLLMClient(max_wait_time=3600)  # 1 hour
```

---

## 🎯 Recommended Settings by Use Case

### Development/Testing
```env
FIREBASE_MAX_WAIT_TIME=1800   # 30 minutes
FIREBASE_POLL_INTERVAL=3.0     # 3 seconds
```

### Production (Web App)
```env
FIREBASE_MAX_WAIT_TIME=7200   # 2 hours
FIREBASE_POLL_INTERVAL=2.0     # 2 seconds (responsive)
```

### Production (Batch Processing)
```env
FIREBASE_MAX_WAIT_TIME=86400  # 24 hours
FIREBASE_POLL_INTERVAL=10.0    # 10 seconds (efficient)
```

### Research/Experimentation
```env
FIREBASE_MAX_WAIT_TIME=0      # Unlimited
FIREBASE_POLL_INTERVAL=5.0     # 5 seconds
```

---

## 📈 Performance Impact

### Timeout Setting
- **No CPU/memory impact** - just controls when to give up
- **Higher = more reliable** for slow models
- **Lower = fail faster** if server is down

### Poll Interval
- **Lower = more responsive** but more Firebase reads
- **Higher = more efficient** but slower to notice completion
- **Typical cost:** 1 read per poll (negligible in Firebase free tier)

---

## 🔒 Safety Considerations

### Avoid Infinite Loops

While unlimited timeout (0) is supported, consider:

```python
# Add manual timeout check in your application
import time

start = time.time()
client = FirebaseLLMClient(max_wait_time=0)  # Unlimited

try:
    response = client.generate_content(prompt)
except Exception as e:
    elapsed = time.time() - start
    if elapsed > 3600:  # Your app-level limit
        print(f"Gave up after {elapsed:.0f}s")
    raise
```

### Monitor Queue

```python
from src.client import FirebaseLLMClient

client = FirebaseLLMClient()

# Check if jobs are stuck
pending = client.get_pending_jobs_count()
if pending > 10:
    print("⚠️  Warning: Many pending jobs. Check LLM server.")
```

---

## ✅ Quick Test

After changing timeout settings:

```bash
# Test with a simple query
python -c "from src.client.firebase_llm_client import FirebaseLLMClient; client = FirebaseLLMClient(); print(client.generate_content('Count to 5'))"
```

Expected output:
```
✓ Firebase initialized successfully
📤 Submitting job: job_xxxxx
⏳ Waiting for response (max 86400s)...
⏳ Processing.... (10s)
✓ Response received (XX.Xs)
1, 2, 3, 4, 5
```

---

## 📞 Summary

Your current setup:
- ✅ **24-hour timeout** - Effectively unlimited for all practical use
- ✅ **3-second polling** - Good balance of responsiveness and efficiency
- ✅ **Progress updates** - Every 30 seconds
- ✅ **Works with any model** - No matter how slow

**You're all set!** The system will now wait as long as needed (up to 24 hours) for responses from your local LLM server.

---

**For more information, see:**
- `FIREBASE_LLM_SETUP.md` - Complete Firebase setup guide
- `.env.example` - Configuration template
- `src/client/firebase_llm_client.py` - Implementation details
