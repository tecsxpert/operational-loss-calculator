import os
import logging
import bleach
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

    # Input Sanitization & Prompt Injection Middleware
    @app.before_request
    def sanitize_input():
        if request.method == 'POST' and request.is_json:
            data = request.get_json()
            if data and 'description' in data:
                # 1. Strip HTML tags
                original_text = data['description']
                sanitized_text = bleach.clean(original_text, tags=[], strip=True)
                
                # 2. Simple Prompt Injection Detection
                injection_keywords = ["ignore previous instructions", "system prompt", "you are now"]
                for keyword in injection_keywords:
                    if keyword in sanitized_text.lower():
                        logger.warning(f"Possible prompt injection detected: {sanitized_text}")
                        return jsonify({"error": "Security violation: Potential prompt injection detected."}), 400
                
                # Update the data with sanitized text
                data['description'] = sanitized_text
                # Note: Flask's request.get_json() is cached, so we don't modify it directly here
                # In a real middleware, you'd store this in g or a custom attribute


    
    @app.route('/health', methods=['GET'])
    def health_check():
        return {"status": "healthy", "service": "operational-loss-calculator-ai"}, 200

    return app

if __name__ == '__main__':
    app = create_app()
    port = int(os.environ.get("PORT", 5000))
    logger.info(f"Starting AI Service on port {port}...")
    app.run(host='0.0.0.0', port=port, debug=True)
