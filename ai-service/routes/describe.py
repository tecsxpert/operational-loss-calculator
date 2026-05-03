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
    with open(path, 'r') as f:
        return f.read()

@describe_bp.route('/describe', methods=['POST'])
def describe_event():
    data = request.get_json()
    
    # Input Validation
    if not data or 'description' not in data:
        return jsonify({"error": "Missing 'description' in request body"}), 400
    
    description = data['description']
    severity = data.get('severity', 'Medium') # Default to Medium if not provided
    
    try:
        # Load and format prompt
        prompt_template = load_prompt('describe.txt')
        prompt = prompt_template.format(description=description, severity=severity)
        
        # Call Groq
        result = groq_client.get_structured_response(
            prompt=prompt,
            system_prompt="You are an expert risk analyst specializing in operational loss."
        )
        
        # Add metadata
        result['generated_at'] = datetime.now().isoformat()
        result['is_fallback'] = False
        
        return jsonify(result), 200

        
    except Exception as e:
        logger.error(f"Error in /describe: {str(e)}")
        # Fallback response to avoid 500 errors
        fallback = {
            "summary": "Error processing event description.",
            "detailed_analysis": "The AI service was unable to process the request at this time.",
            "impact": "Unknown",
            "risk_level": "Unknown",
            "generated_at": datetime.now().isoformat(),
            "is_fallback": True
        }
        return jsonify(fallback), 200 # Returning 200 with fallback as per requirements to avoid 500s

