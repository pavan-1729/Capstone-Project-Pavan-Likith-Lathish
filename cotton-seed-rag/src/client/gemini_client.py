import os
import time
from typing import Tuple, List, Optional

from dotenv import load_dotenv
from google import genai
from google.genai import types
from google.genai.errors import ClientError, ServerError


# Load environment variables
load_dotenv(override=True)

GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")

if not GEMINI_API_KEY:
    raise RuntimeError(
        "GEMINI_API_KEY environment variable is not set. "
        "Please provide a valid Gemini API key in .env"
    )

client = genai.Client(api_key=GEMINI_API_KEY)

# Gemini multi-model fallback ladder
GEMINI_MODELS = [
    "gemini-3-flash-preview",
    "gemini-3.1-flash-lite-preview",
    "gemini-3.8-flash",
    "gemini-3.7-flash",
    "gemini-3.6-flash",
    "gemini-flash-latest",
]


def generate_with_fallback(
    prompt: str,
    temperature: float = 0.2,
    max_retries_per_model: int = 1,
    json_mode: bool = False,
) -> Tuple[str, str]:
    """
    Generate response using Gemini with automatic multi-model failover ladder.

    Returns:
        tuple: (response_text, model_name)
    """
    errors = []

    for model_name in GEMINI_MODELS:
        for attempt in range(max_retries_per_model + 1):
            try:
                config = types.GenerateContentConfig(
                    temperature=temperature,
                    response_mime_type="application/json" if json_mode else "text/plain",
                )

                response = client.models.generate_content(
                    model=model_name,
                    contents=prompt,
                    config=config,
                )

                if response and response.text:
                    return response.text, model_name
                else:
                    raise ValueError(f"Empty response from model {model_name}")

            except ClientError as e:
                error_str = str(e)
                errors.append(f"{model_name} ClientError: {error_str}")
                if "RESOURCE_EXHAUSTED" in error_str or "NOT_FOUND" in error_str:
                    break
                time.sleep(1.0)

            except ServerError as e:
                errors.append(f"{model_name} ServerError: {e}")
                time.sleep(1.0)

            except Exception as e:
                errors.append(f"{model_name} Exception: {e}")
                time.sleep(1.0)

    raise RuntimeError(
        "All Gemini models in fallback ladder failed. Errors logged:\n"
        + "\n".join(errors)
    )
