import os
import json
import time
import logging
from typing import Dict, Any, Optional
from groq import Groq

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger("GroqClient")

class GroqClient:
    def __init__(self, api_key: Optional[str] = None):
        """
        Initialize the Groq Client.
        If api_key is not provided, the SDK will automatically look for GROQ_API_KEY environment variable.
        """
        # Automatically loads from the GROQ_API_KEY environment variable
        # assuming dotenv has been loaded by the caller.
        self.client = Groq(api_key=api_key)
        self.default_model = "llama-3.3-70b-versatile"

    def get_structured_response(self, prompt: str, system_prompt: Optional[str] = None, max_attempts: int = 3, temperature: float = 0.3, max_tokens: int = 1024) -> Dict[str, Any]:
        """
        Calls the Groq API and expects a JSON formatted response.
        Implements a 3-retry mechanism with exponential backoff.
        
        Args:
            prompt: The user input prompt. MUST explicitly ask for JSON.
            system_prompt: Optional system instructions.
            max_attempts: Number of total attempts (default 3).
            temperature: Sampling temperature (0.3 for factual processing).
            max_tokens: The maximum number of tokens to generate.
            
        Returns:
            A parsed JSON dictionary.
        """
        messages = []
        if system_prompt:
            messages.append({"role": "system", "content": system_prompt})
            
        # Ensure the prompt contains instructions to output JSON, required by Groq's json_object format
        messages.append({"role": "user", "content": prompt})

        attempt = 0
        backoff = 1  # initial backoff in seconds

        while attempt < max_attempts:
            attempt += 1
            try:
                response = self.client.chat.completions.create(
                    model=self.default_model,
                    messages=messages,
                    temperature=temperature,
                    max_tokens=max_tokens,
                    response_format={"type": "json_object"}
                )
                
                content = response.choices[0].message.content
                if not content:
                    raise ValueError("Empty response received from Groq.")
                    
                # Parse JSON string into dictionary
                parsed_json = json.loads(content)
                logger.debug(f"Successfully obtained and parsed JSON response on attempt {attempt}.")
                return parsed_json
                
            except Exception as e:
                logger.error(f"Attempt {attempt} failed: {e}")
                if attempt == max_attempts:
                    logger.error("Max retries reached. Failing.")
                    raise
                
                logger.info(f"Retrying in {backoff} seconds...")
                time.sleep(backoff)
                backoff *= 2  # Exponential backoff (1s, 2s, 4s...)

if __name__ == "__main__":
    from dotenv import load_dotenv, find_dotenv
    load_dotenv(find_dotenv())
    
    # Test the client
    client = GroqClient()
    print("Testing GroqClient structured output with retries...")
    
    try:
        result = client.get_structured_response(
            prompt="Generate a JSON object with keys 'status' and 'message'. Make up a status value.",
            system_prompt="You are a helpful assistant that outputs valid JSON."
        )
        print("Success! Result:")
        print(json.dumps(result, indent=2))
    except Exception as err:
        print(f"Test failed: {err}")
