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
    with open(path, 'r') as f:
        return f.read()

@recommend_bp.route('/recommend', methods=['POST'])
def recommend_actions():
    data = request.get_json()
    
    # Input Validation
    if not data or 'description' not in data:
        return jsonify({"error": "Missing 'description' in request body"}), 400
    
    description = data['description']
    severity = data.get('severity', 'Medium')
    
    try:
        # Load and format prompt
        prompt_template = load_prompt('recommend.txt')
        prompt = prompt_template.format(description=description, severity=severity)
        
        # Call Groq
        result = groq_client.get_structured_response(
            prompt=prompt,
            system_prompt="You are an expert risk consultant providing mitigation strategies."
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
                {
                    "action_type": "Process",
                    "description": "Review and update standard operating procedures for the affected area.",
                    "priority": "High"
                },
                {
                    "action_type": "Training",
                    "description": "Conduct mandatory refresher training for all staff involved in the process.",
                    "priority": "Medium"
                },
                {
                    "action_type": "Technology",
                    "description": "Implement automated monitoring and alerts to detect similar events in real-time.",
                    "priority": "High"
                }
            ],
            "is_fallback": True,
            "generated_at": datetime.now().isoformat()
        }
        return jsonify(fallback), 200

