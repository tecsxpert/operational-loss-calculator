import os
import logging
from datetime import datetime
from flask import Blueprint, request, jsonify
from services.groq_client import GroqClient

# Configure logger
logger = logging.getLogger(__name__)

describe_bp = Blueprint('describe', __name__)
groq_client = GroqClient()

def load_prompt(filename):
    path = os.path.join(os.path.dirname(__file__), '../prompts', filename)
    try:
        with open(path, 'r') as f:
            return f.read()
    except FileNotFoundError:
        logger.error(f"Prompt file not found: {path}")
        return "Analyze this operational loss scenario: {description}"

@describe_bp.route('/describe', methods=['POST'])
def describe_event():
    # Use sanitized JSON if available from middleware, otherwise use raw JSON
    data = getattr(request, 'sanitized_json', request.get_json(silent=True) or {})
    
    # Support both 'description' (local) and 'scenario' (origin/main)
    description = data.get('description') or data.get('scenario')
    
    # Input Validation
    if not description:
        return jsonify({"error": "Bad Request", "message": "Missing 'description' or 'scenario' in request body"}), 400
    
    severity = data.get('severity', 'Medium') 
    
    try:
        # Load and format prompt
        prompt_template = load_prompt('describe.txt')
        # Format with what we have
        prompt = prompt_template.replace('{description}', description).replace('{severity}', severity)
        
        # Call Groq
        # Note: In origin/main, the system prompt was in prompts.py. 
        # Here we use the one from describe.txt or a default.
        result = groq_client.get_structured_response(
            prompt=prompt,
            system_prompt="You are a senior operational risk analyst with deep expertise in Basel III operational risk categorization."
        )
        
        # Add metadata (merged from local features)
        result['generated_at'] = datetime.now().isoformat()
        result['is_fallback'] = False
        
        return jsonify(result), 200

    except Exception as e:
        logger.error(f"Error in /describe: {str(e)}")
        # Fallback response to avoid 500 errors (merged from local requirements)
        fallback = {
            "risk_type": "Unknown",
            "root_cause": "Processing Error",
            "description": f"The AI service was unable to process the request: {str(e)}",
            "generated_at": datetime.now().isoformat(),
            "is_fallback": True
        }
        return jsonify(fallback), 200 
