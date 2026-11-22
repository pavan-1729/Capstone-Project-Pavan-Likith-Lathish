"""
Gemini API Client with Multi-Model Fallback & JSON Mode
======================================================
Provides a robust, fault-tolerant interface to Google's Gemini models
with automatic model fallback, JSON response parsing, and error handling.
"""

import json
import logging
import os
import re
from typing import Any, Dict, List, Optional

from dotenv import load_dotenv

load_dotenv()

logger = logging.getLogger(__name__)


# Cascading model fallback list (ordered by capability and availability)
DEFAULT_MODEL_FALLBACKS = [
    "gemini-2.5-flash",
    "gemini-2.0-flash",
    "gemini-1.5-flash",
    "gemini-1.5-pro",
    "gemini-1.5-flash-8b",
]


class GeminiClient:
    """Client for interacting with Google Gemini API with automatic fallback."""

    def __init__(
        self,
        api_key: Optional[str] = None,
        models: Optional[List[str]] = None,
        timeout: int = 60,
    ):
        """
        Initialize the Gemini Client.

        Args:
            api_key: Optional Gemini API key. Defaults to GEMINI_API_KEY environment variable.
            models: List of model identifiers in order of priority for cascading fallback.
            timeout: Timeout in seconds for API calls.
        """
        self.api_key = api_key if api_key is not None else os.getenv("GEMINI_API_KEY")
        
        # Determine model priority list
        custom_model = os.getenv("GEMINI_MODEL")
        if models:
            self.models = models
        elif custom_model:
            # Put custom model first, followed by fallbacks
            self.models = [custom_model] + [m for m in DEFAULT_MODEL_FALLBACKS if m != custom_model]
        else:
            self.models = list(DEFAULT_MODEL_FALLBACKS)

        self.timeout = timeout
        self._genai = None
        self._initialized = False

        if self.api_key:
            self._init_genai()

    def _init_genai(self) -> None:
        """Initialize the underlying google.generativeai library."""
        try:
            import google.generativeai as genai
            genai.configure(api_key=self.api_key)
            self._genai = genai
            self._initialized = True
        except ImportError:
            logger.warning("google-generativeai package not installed.")
            self._initialized = False

    @property
    def is_available(self) -> bool:
        """Check if client has a valid API key and configured library."""
        return bool(self.api_key and self._initialized)

    def generate_content(
        self,
        prompt: str,
        system_instruction: Optional[str] = None,
        json_mode: bool = True,
        temperature: float = 0.2,
    ) -> Dict[str, Any]:
        """
        Generate content using Gemini with cascading model fallback.

        Args:
            prompt: User prompt content.
            system_instruction: Optional system instruction / persona.
            json_mode: If True, request and parse structured JSON.
            temperature: Sampling temperature (0.0 to 1.0).

        Returns:
            Dictionary containing 'text', 'data' (if json_mode), and 'model_used'.

        Raises:
            RuntimeError: If all model fallbacks fail or API key is not configured.
        """
        if not self.is_available:
            raise RuntimeError(
                "GeminiClient is not configured. Please set GEMINI_API_KEY in your .env file "
                "or pass api_key to GeminiClient."
            )

        last_error = None

        generation_config = {
            "temperature": temperature,
        }
        if json_mode:
            generation_config["response_mime_type"] = "application/json"

        for model_name in self.models:
            try:
                logger.info(f"Attempting generation with model: {model_name}")
                model_kwargs = {"model_name": model_name}
                if system_instruction:
                    model_kwargs["system_instruction"] = system_instruction

                model = self._genai.GenerativeModel(**model_kwargs)
                response = model.generate_content(
                    prompt,
                    generation_config=generation_config,
                )

                if not response.text:
                    raise ValueError(f"Empty response from model {model_name}")

                raw_text = response.text.strip()
                result = {
                    "text": raw_text,
                    "model_used": model_name,
                    "raw_response": response,
                }

                if json_mode:
                    parsed_data = self._clean_and_parse_json(raw_text)
                    result["data"] = parsed_data

                return result

            except Exception as e:
                logger.warning(f"Model {model_name} failed: {e}. Trying next model...")
                last_error = e
                continue

        error_msg = f"All Gemini model fallbacks failed. Last error: {last_error}"
        logger.error(error_msg)
        raise RuntimeError(error_msg)

    @staticmethod
    def _clean_and_parse_json(text: str) -> Dict[str, Any]:
        """Clean markdown markers and parse JSON safely."""
        text = text.strip()

        # Remove ```json ... ``` or ``` ... ``` wrappers
        json_match = re.search(r"```(?:json)?\s*([\s\S]*?)\s*```", text)
        if json_match:
            text = json_match.group(1).strip()

        try:
            return json.loads(text)
        except json.JSONDecodeError as err:
            # Attempt to locate first { and last }
            first_brace = text.find("{")
            last_brace = text.rfind("}")
            if first_brace != -1 and last_brace != -1 and last_brace > first_brace:
                extracted = text[first_brace : last_brace + 1]
                try:
                    return json.loads(extracted)
                except Exception:
                    pass
            logger.error(f"Failed to parse JSON response: {err}\nText was: {text[:200]}...")
            raise ValueError(f"Invalid JSON response from LLM: {err}")
