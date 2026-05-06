import os
import logging
import bleach
import re
from flask import Flask, request, jsonify
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address
from dotenv import load_dotenv, find_dotenv

# Load environment variables
load_dotenv(find_dotenv())

# Import routes
from routes.describe import describe_bp
from routes.recommend import recommend_bp
from routes.report import report_bp

# Configure Logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Prompt Injection detection list (from Day 6 progress)
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
    # Use bleach for safer HTML stripping
    return bleach.clean(text, tags=[], strip=True)

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

def create_app():
    app = Flask(__name__)
    
    # Configure Rate Limiter (30 req/min as per Day 3 spec)
    limiter = Limiter(
        get_remote_address,
        app=app,
        default_limits=["30 per minute"],
        storage_uri="memory://",
    )

    # Register Blueprints
    app.register_blueprint(describe_bp)
    app.register_blueprint(recommend_bp)
    app.register_blueprint(report_bp)

    # Security Middleware (from Day 6 progress)
    @app.before_request
    def security_middleware():
        if request.is_json and request.get_data():
            try:
                data = request.get_json(force=True, silent=True)
                if data:
                    # 1. Strip HTML tags recursively
                    sanitized_data = sanitize_data(data)
                    
                    # 2. Check for Prompt Injection recursively
                    if check_injection_in_data(sanitized_data):
                        logger.warning(f"Malicious input detected and blocked: {sanitized_data}")
                        return jsonify({"error": "Bad Request", "message": "Malicious input detected. Prompt injection blocked."}), 400
                    
                    # Attach sanitized data to request object
                    request.sanitized_json = sanitized_data
            except Exception as e:
                logger.error(f"Error in security middleware: {e}")
                pass 

    @app.errorhandler(429)
    def ratelimit_handler(e):
        return jsonify({"error": "Too Many Requests", "message": f"Rate limit exceeded: {e.description}"}), 429

    @app.route('/health', methods=['GET'])
    def health_check():
        return {"status": "healthy", "service": "operational-loss-calculator-ai"}, 200

    return app

if __name__ == '__main__':
    app = create_app()
    port = int(os.environ.get("PORT", 5000))
    logger.info(f"Starting AI Service on port {port}...")
    app.run(host='0.0.0.0', port=port, debug=True)
