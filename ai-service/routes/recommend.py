import os
import logging
from datetime import datetime
from flask import Blueprint, request, jsonify
from services.groq_client import GroqClient

# Configure logger
logger = logging.getLogger(__name__)

recommend_bp = Blueprint('recommend', __name__)
groq_client = GroqClient()

def load_prompt(filename):
    path = os.path.join(os.path.dirname(__file__), '../prompts', filename)
    try:
        with open(path, 'r') as f:
            return f.read()
    except FileNotFoundError:
        logger.error(f"Prompt file not found: {path}")
        return "Recommend mitigation strategies for: {description}"

@recommend_bp.route('/recommend', methods=['POST'])
def recommend_actions():
    # Use sanitized JSON if available
    data = getattr(request, 'sanitized_json', request.get_json(silent=True) or {})
    
    # Support both 'description' and 'scenario'
    description = data.get('description') or data.get('scenario')
    
    # Input Validation
    if not description:
        return jsonify({"error": "Bad Request", "message": "Missing 'description' or 'scenario' in request body"}), 400
    
    severity = data.get('severity', 'Medium')
    
    try:
        # Load and format prompt
        prompt_template = load_prompt('recommend.txt')
        prompt = prompt_template.replace('{description}', description).replace('{severity}', severity)
        
        # Call Groq
        result = groq_client.get_structured_response(
            prompt=prompt,
            system_prompt="You are a senior operational risk analyst. Your task is to provide three highly specific, actionable mitigation strategies."
        )
        
        # Add metadata
        result['is_fallback'] = False
        result['generated_at'] = datetime.now().isoformat()
        
        return jsonify(result), 200

    except Exception as e:
        logger.error(f"Error in /recommend: {str(e)}")
        # Fallback response with 3 items as required
        fallback = {
            "recommendations": [
                "Review and update standard operating procedures for the affected area.",
                "Conduct mandatory refresher training for all staff involved in the process.",
                "Implement automated monitoring and alerts to detect similar events in real-time."
            ],
            "is_fallback": True,
            "generated_at": datetime.now().isoformat()
        }
        return jsonify(fallback), 200
