import os
import logging
from datetime import datetime
from flask import Blueprint, request, jsonify
from services.groq_client import GroqClient

# Configure logger
logger = logging.getLogger(__name__)

report_bp = Blueprint('report', __name__)
groq_client = GroqClient()

def load_prompt(filename):
    path = os.path.join(os.path.dirname(__file__), '../prompts', filename)
    with open(path, 'r') as f:
        return f.read()

@report_bp.route('/generate-report', methods=['POST'])
def generate_report():
    data = request.get_json()
    
    # Input Validation
    if not data or 'description' not in data:
        return jsonify({"error": "Missing 'description' in request body"}), 400
    
    description = data['description']
    severity = data.get('severity', 'Medium')
    
    try:
        # Load and format prompt
        prompt_template = load_prompt('report.txt')
        prompt = prompt_template.format(description=description, severity=severity)
        
        # Call Groq
        result = groq_client.get_structured_response(
            prompt=prompt,
            system_prompt="You are a senior risk reporting officer."
        )
        
        # Add metadata
        result['generated_at'] = datetime.now().isoformat()
        result['is_fallback'] = False
        
        return jsonify(result), 200
        
    except Exception as e:
        logger.error(f"Error in /generate-report: {str(e)}")
        # Fallback response
        fallback = {
            "title": "Incident Report - Error",
            "summary": "The AI service was unable to generate a full report.",
            "overview": "Processing failed due to technical issues.",
            "key_items": ["System timeout or API error"],
            "recommendations": ["Contact IT support", "Retry generation later"],
            "generated_at": datetime.now().isoformat(),
            "is_fallback": True
        }
        return jsonify(fallback), 200
