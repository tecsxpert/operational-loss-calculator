import re
from flask import Flask, request, jsonify
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address
from dotenv import load_dotenv, find_dotenv
from groq_client import GroqClient
from prompts import SYSTEM_PROMPT_DESCRIBE, SYSTEM_PROMPT_RECOMMEND

load_dotenv(find_dotenv())

app = Flask(__name__)

# Configure Limiter
limiter = Limiter(
    get_remote_address,
    app=app,
    default_limits=["30 per minute"],
    storage_uri="memory://"
)

# Initialize GroqClient
groq_client = GroqClient()

# Prompt Injection detection list
MALICIOUS_PHRASES = [
    "override system instructions",
    "ignore previous instructions",
    "system prompt",
    "forget previous",
    "you are now",
    "disregard"
]

def strip_html_tags(text):
    if not isinstance(text, str):
        return text
    clean = re.compile('<.*?>')
    return re.sub(clean, '', text)

def sanitize_data(data):
    if isinstance(data, dict):
        return {k: sanitize_data(v) for k, v in data.items()}
    elif isinstance(data, list):
        return [sanitize_data(i) for i in data]
    elif isinstance(data, str):
        return strip_html_tags(data)
    else:
        return data

def contains_prompt_injection(text):
    if not isinstance(text, str):
        return False
    text_lower = text.lower()
    return any(phrase in text_lower for phrase in MALICIOUS_PHRASES)

def check_injection_in_data(data):
    if isinstance(data, dict):
        return any(check_injection_in_data(v) for v in data.values())
    elif isinstance(data, list):
        return any(check_injection_in_data(i) for i in data)
    elif isinstance(data, str):
        return contains_prompt_injection(data)
    return False

@app.before_request
def security_middleware():
    if request.is_json and request.get_data():
        # Get raw json
        try:
            data = request.get_json(force=True, silent=True)
            if data:
                # 1. Strip HTML tags
                sanitized_data = sanitize_data(data)
                
                # 2. Check for Prompt Injection
                if check_injection_in_data(sanitized_data):
                    return jsonify({"error": "Bad Request", "message": "Malicious input detected. Prompt injection blocked."}), 400
                
                # Update the request data with sanitized data
                # Flask doesn't easily allow mutating request.json, but we can store it in request.environ or g
                # Better yet, since we can't easily overwrite request.json cleanly, we attach to request module
                request.sanitized_json = sanitized_data
        except Exception:
            pass # Malformed JSON

@app.errorhandler(429)
def ratelimit_handler(e):
    return jsonify({"error": "Too Many Requests", "message": f"Rate limit exceeded: {e.description}"}), 429

@app.route('/describe', methods=['POST'])
def describe_incident():
    data = getattr(request, 'sanitized_json', request.get_json(silent=True) or {})
    scenario = data.get("scenario")
    
    if not scenario:
        return jsonify({"error": "Bad Request", "message": "Scenario is required"}), 400
        
    prompt = f"Analyze the following operational loss scenario and describe the risk type and root cause. MUST output JSON with keys 'risk_type', 'root_cause', and 'description'.\n\nScenario: {scenario}"
    
    try:
        result = groq_client.get_structured_response(
            prompt=prompt,
            system_prompt=SYSTEM_PROMPT_DESCRIBE
        )
        return jsonify(result), 200
    except Exception as e:
        return jsonify({"error": "Internal Server Error", "message": str(e)}), 500

@app.route('/recommend', methods=['POST'])
def recommend_action():
    data = getattr(request, 'sanitized_json', request.get_json(silent=True) or {})
    scenario = data.get("scenario")
    
    if not scenario:
        return jsonify({"error": "Bad Request", "message": "Scenario is required"}), 400
        
    prompt = f"Based on the following operational loss scenario, recommend three actionable mitigation strategies. MUST output JSON with a key 'recommendations' which is a list of strings.\n\nScenario: {scenario}"
    
    try:
        result = groq_client.get_structured_response(
            prompt=prompt,
            system_prompt=SYSTEM_PROMPT_RECOMMEND
        )
        return jsonify(result), 200
    except Exception as e:
        return jsonify({"error": "Internal Server Error", "message": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
