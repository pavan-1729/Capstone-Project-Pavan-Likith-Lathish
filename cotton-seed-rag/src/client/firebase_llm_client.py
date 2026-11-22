"""
Firebase LLM Client for Local LLM Server Integration.

Connects to a Firebase Realtime Database queue to submit prompts
and receive responses from a local LLM server running on another machine.
"""

import os
import time
import json
import uuid
from typing import Optional, Dict, Any
from dotenv import load_dotenv

try:
    import firebase_admin
    from firebase_admin import credentials, db
    FIREBASE_AVAILABLE = True
except ImportError:
    FIREBASE_AVAILABLE = False
    print("⚠️  Warning: firebase-admin not installed. Run: pip install firebase-admin")


class FirebaseLLMClient:
    """
    Client for submitting prompts to a local LLM server via Firebase Realtime Database.
    
    Architecture:
        Client (this) -> Firebase DB -> Local LLM Server (on another laptop) -> Ollama
    
    The client writes a job with status="pending", then polls until the server
    updates it to status="completed" or "failed".
    """
    
    def __init__(
        self,
        service_account_path: Optional[str] = None,
        database_url: Optional[str] = None,
        max_wait_time: Optional[int] = None,
        poll_interval: Optional[float] = None
    ):
        """
        Initialize Firebase LLM Client.
        
        Args:
            service_account_path: Path to Firebase service account key JSON
            database_url: Firebase Realtime Database URL
            max_wait_time: Maximum seconds to wait for response (default: from env or 86400)
                          Set to None or 0 for unlimited waiting
            poll_interval: Seconds between status checks (default: from env or 3.0)
        """
        if not FIREBASE_AVAILABLE:
            raise ImportError(
                "firebase-admin is required. Install with: pip install firebase-admin"
            )
        
        load_dotenv()
        
        # Get configuration
        self.service_account_path = service_account_path or os.getenv(
            'FIREBASE_SERVICE_ACCOUNT_PATH',
            'serviceAccountKey.json'
        )
        self.database_url = database_url or os.getenv(
            'FIREBASE_DATABASE_URL',
            ''
        )
        
        if not self.database_url:
            raise ValueError(
                "Firebase Database URL not provided. Set FIREBASE_DATABASE_URL "
                "in .env or pass as parameter."
            )
        
        # Get timeout settings from environment or use defaults
        if max_wait_time is None:
            env_timeout = os.getenv('FIREBASE_MAX_WAIT_TIME')
            if env_timeout:
                max_wait_time = int(env_timeout)
            else:
                max_wait_time = 86400  # Default: 24 hours
        
        # Support unlimited waiting (0 or negative means unlimited)
        self.max_wait_time = max_wait_time if max_wait_time > 0 else float('inf')
        
        if poll_interval is None:
            env_interval = os.getenv('FIREBASE_POLL_INTERVAL')
            if env_interval:
                poll_interval = float(env_interval)
            else:
                poll_interval = 3.0  # Default: 3 seconds
        
        self.poll_interval = poll_interval
        self.jobs_ref = None
        
        # Initialize Firebase
        self._initialize_firebase()
    
    def _initialize_firebase(self):
        """Initialize Firebase Admin SDK."""
        try:
            # Check if already initialized
            firebase_admin.get_app()
            print("✓ Firebase already initialized")
        except ValueError:
            # Initialize new app
            if not os.path.exists(self.service_account_path):
                raise FileNotFoundError(
                    f"Service account key not found: {self.service_account_path}\n"
                    "Download from Firebase Console > Project Settings > Service accounts"
                )
            
            cred = credentials.Certificate(self.service_account_path)
            firebase_admin.initialize_app(cred, {
                'databaseURL': self.database_url
            })
            print("✓ Firebase initialized successfully")
        
        # Get reference to jobs node
        self.jobs_ref = db.reference('llm_jobs')
    
    def generate_content(
        self,
        prompt: str,
        json_mode: bool = False,
        **kwargs
    ) -> str:
        """
        Submit a prompt to the local LLM server and wait for response.
        
        Args:
            prompt: The prompt to send to the LLM
            json_mode: If True, instructs LLM to return JSON (appended to prompt)
            **kwargs: Additional parameters (currently unused, for compatibility)
        
        Returns:
            Generated text response from the LLM
        
        Raises:
            TimeoutError: If response not received within max_wait_time
            Exception: If job fails or other errors occur
        """
        if not prompt or not prompt.strip():
            raise ValueError("Prompt cannot be empty")
        
        # Add JSON instruction if requested
        if json_mode:
            prompt = f"{prompt}\n\nYou must respond with valid JSON only. No other text."
        
        # Generate unique job ID
        job_id = f"job_{int(time.time())}_{str(uuid.uuid4())[:8]}"
        
        # Create job in Firebase
        job_data = {
            'prompt': prompt,
            'status': 'pending',
            'response': '',
            'created_at': int(time.time())
        }
        
        print(f"📤 Submitting job: {job_id}")
        self.jobs_ref.child(job_id).set(job_data)
        
        # Wait for completion
        return self._wait_for_completion(job_id)
    
    def _wait_for_completion(self, job_id: str) -> str:
        """
        Poll Firebase for job completion.
        
        Args:
            job_id: The job ID to monitor
        
        Returns:
            The response text from the LLM
        
        Raises:
            TimeoutError: If max_wait_time exceeded (if set)
            Exception: If job failed
        """
        start_time = time.time()
        job_ref = self.jobs_ref.child(job_id)
        
        if self.max_wait_time == float('inf'):
            print(f"⏳ Waiting for response (unlimited time)...")
        else:
            print(f"⏳ Waiting for response (max {self.max_wait_time}s)...")
        
        last_message_time = start_time
        
        while True:
            # Check timeout (only if not unlimited)
            elapsed = time.time() - start_time
            if self.max_wait_time != float('inf') and elapsed > self.max_wait_time:
                # Clean up pending job
                job_ref.update({'status': 'timeout'})
                raise TimeoutError(
                    f"LLM server did not respond within {self.max_wait_time} seconds. "
                    "Check if the local LLM server is running."
                )
            
            # Get current job status
            job_data = job_ref.get()
            
            if not job_data:
                raise Exception(f"Job {job_id} disappeared from database")
            
            status = job_data.get('status')
            
            if status == 'completed':
                response = job_data.get('response', '')
                if not response:
                    raise Exception("Job marked completed but response is empty")
                
                print(f"\n✓ Response received ({elapsed:.1f}s)")
                
                # Optional: Clean up completed job
                # job_ref.delete()
                
                return response
            
            elif status == 'failed':
                error = job_data.get('error', 'Unknown error')
                raise Exception(f"LLM server failed to process job: {error}")
            
            elif status in ['pending', 'processing']:
                # Still waiting - show progress every 30 seconds
                current_time = time.time()
                if current_time - last_message_time >= 30:
                    mins = int(elapsed // 60)
                    secs = int(elapsed % 60)
                    if mins > 0:
                        time_str = f"{mins}m {secs}s"
                    else:
                        time_str = f"{secs}s"
                    
                    if status == 'processing':
                        print(f"⏳ Still processing... ({time_str} elapsed)")
                    else:
                        print(f"⏳ Waiting for worker... ({time_str} elapsed)")
                    
                    last_message_time = current_time
                
                # Show simple progress indicator
                dots = '.' * ((int(elapsed) % 4) + 1)
                print(f"\r⏳ {status.capitalize()}{dots:<4} ({int(elapsed)}s)", end='', flush=True)
                time.sleep(self.poll_interval)
            
            else:
                raise Exception(f"Unknown job status: {status}")
    
    def is_available(self) -> bool:
        """
        Check if Firebase LLM client is properly configured.
        
        Returns:
            True if client can connect to Firebase
        """
        try:
            # Try to read from database
            self.jobs_ref.child('_health_check').get()
            return True
        except Exception as e:
            print(f"Firebase health check failed: {e}")
            return False
    
    def get_pending_jobs_count(self) -> int:
        """
        Get count of pending jobs in the queue.
        
        Returns:
            Number of pending jobs
        """
        try:
            all_jobs = self.jobs_ref.get() or {}
            pending = [
                job for job in all_jobs.values()
                if isinstance(job, dict) and job.get('status') == 'pending'
            ]
            return len(pending)
        except Exception:
            return 0
    
    def cleanup_old_jobs(self, max_age_seconds: int = 3600):
        """
        Remove jobs older than specified age.
        
        Args:
            max_age_seconds: Maximum age of jobs to keep (default: 1 hour)
        """
        try:
            current_time = int(time.time())
            all_jobs = self.jobs_ref.get() or {}
            
            for job_id, job_data in all_jobs.items():
                if not isinstance(job_data, dict):
                    continue
                
                created_at = job_data.get('created_at', 0)
                age = current_time - created_at
                
                if age > max_age_seconds:
                    self.jobs_ref.child(job_id).delete()
                    print(f"Cleaned up old job: {job_id} (age: {age}s)")
        
        except Exception as e:
            print(f"Error cleaning up jobs: {e}")


# Compatibility function for easy migration from Gemini client
def create_firebase_llm_client() -> FirebaseLLMClient:
    """
    Create and return a configured FirebaseLLMClient instance.
    
    Returns:
        Configured FirebaseLLMClient
    """
    return FirebaseLLMClient()


if __name__ == "__main__":
    """Test the Firebase LLM client."""
    
    print("="*60)
    print("FIREBASE LLM CLIENT TEST")
    print("="*60)
    
    try:
        # Create client
        client = FirebaseLLMClient()
        
        # Check availability
        if not client.is_available():
            print("❌ Firebase connection failed")
            exit(1)
        
        print("✓ Firebase connection successful\n")
        
        # Check queue status
        pending = client.get_pending_jobs_count()
        print(f"📊 Pending jobs in queue: {pending}\n")
        
        # Test prompt
        test_prompt = "What are the top 3 varieties of maize for rainfed conditions? Be brief."
        
        print(f"Test prompt: {test_prompt}\n")
        
        # Submit and wait
        response = client.generate_content(test_prompt)
        
        print("\n" + "="*60)
        print("RESPONSE:")
        print("="*60)
        print(response)
        print("="*60)
        
        print("\n✓ Test completed successfully!")
        
    except Exception as e:
        print(f"\n❌ Error: {e}")
        import traceback
        traceback.print_exc()
