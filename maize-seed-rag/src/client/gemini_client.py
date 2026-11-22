import os
import time

from dotenv import load_dotenv
from google import genai
from google.genai import types
from google.genai.errors import ClientError, ServerError


# =========================================================
# Load environment variables
# =========================================================

load_dotenv()

GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")

if not GEMINI_API_KEY:
    raise RuntimeError(
        "GEMINI_API_KEY environment variable is not set."
    )


# =========================================================
# Gemini client
# =========================================================

client = genai.Client(
    api_key=GEMINI_API_KEY
)


# =========================================================
# Model fallback order
# =========================================================

GEMINI_MODELS = [
    "gemini-3.8-flash",
    "gemini-3.7-flash",
    "gemini-3.6-flash",
    "gemini-3.5-flash-lite",
    "gemini-3.1-flash-lite",
]


# =========================================================
# Generate response with fallback
# =========================================================

def generate_with_fallback(
    prompt,
    temperature=0.2,
    max_retries_per_model=1,
    json_mode=False
):
    """
    Generate a response using Gemini with model fallback.

    Parameters
    ----------
    prompt : str
        Prompt sent to Gemini.

    temperature : float
        Generation temperature.

    max_retries_per_model : int
        Number of retries for temporary server errors.

    json_mode : bool
        If True, Gemini is instructed through the API to return
        application/json.

    Returns
    -------
    tuple
        (response_text, model_name)
    """

    errors = []

    for model_name in GEMINI_MODELS:

        print()
        print("-" * 70)
        print(f"Trying Gemini model: {model_name}")
        print("-" * 70)

        for attempt in range(max_retries_per_model + 1):

            try:

                # -------------------------------------------------
                # IMPORTANT:
                #
                # There are NO tools and NO function calling here.
                #
                # Therefore we do NOT configure:
                # automatic_function_calling
                #
                # This removes the AFC warning from this request.
                # -------------------------------------------------

                if json_mode:

                    config = types.GenerateContentConfig(
                        temperature=temperature,
                        response_mime_type="application/json"
                    )

                else:

                    config = types.GenerateContentConfig(
                        temperature=temperature
                    )

                response = client.models.generate_content(
                    model=model_name,
                    contents=prompt,
                    config=config
                )

                # -------------------------------------------------
                # Extract text safely
                # -------------------------------------------------

                text = response.text

                if text and text.strip():

                    print(
                        f"SUCCESS: {model_name}"
                    )

                    # ALWAYS return exactly two values.
                    return text.strip(), model_name

                error_message = (
                    f"{model_name}: empty response"
                )

                print(error_message)
                errors.append(error_message)

                break

            # -----------------------------------------------------
            # Temporary server error
            # -----------------------------------------------------

            except ServerError as e:

                error_message = str(e)

                print(
                    f"SERVER ERROR: {model_name}"
                )

                print(error_message)

                errors.append(
                    f"{model_name}: {error_message}"
                )

                if attempt < max_retries_per_model:

                    print("Retrying...")

                    time.sleep(1)

                else:

                    print(
                        "Moving to next model..."
                    )

            # -----------------------------------------------------
            # Client error
            # -----------------------------------------------------

            except ClientError as e:

                error_message = str(e)

                print(
                    f"CLIENT ERROR: {model_name}"
                )

                print(error_message)

                errors.append(
                    f"{model_name}: {error_message}"
                )

                print(
                    "Moving to next model..."
                )

                break

            # -----------------------------------------------------
            # Unexpected error
            # -----------------------------------------------------

            except Exception as e:

                error_message = str(e)

                print(
                    f"UNEXPECTED ERROR: {model_name}"
                )

                print(error_message)

                errors.append(
                    f"{model_name}: {error_message}"
                )

                print(
                    "Moving to next model..."
                )

                break

    # =========================================================
    # All models failed
    # =========================================================

    error_summary = "\n".join(errors)

    raise RuntimeError(
        "All Gemini models failed.\n\n"
        + error_summary
    )