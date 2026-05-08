# Operational Loss Calculator - AI Microservice

This is a Flask-based AI microservice that leverages Groq (LLaMA 3.3 70B) to analyze operational loss events, provide mitigation recommendations, and generate comprehensive incident reports.

## 🚀 Features
- **Event Description**: Summarizes and analyzes risk levels.
- **Recommendations**: Provides 3 actionable mitigation strategies.
- **Report Generation**: Creates full executive incident reports.
- **High Reliability**: Built-in 3-retry mechanism with exponential backoff for Groq API calls.
- **Safe Fallbacks**: Guaranteed JSON responses even during API outages.

## 🛠️ Tech Stack
- Python 3.11+
- Flask
- Groq Cloud SDK
- python-dotenv

## ⚙️ Setup Instructions

1. **Install Dependencies**:
   ```bash
   pip install -r requirements.txt
   ```

2. **Configure Environment**:
   Create a `.env` file in the `ai-service/` directory:
   ```env
   GROQ_API_KEY=your_groq_api_key_here
   PORT=5000
   ```

3. **Run the Service**:
   ```bash
   python app.py
   ```

## 📡 API Reference

### 1. Health Check
`GET /health`
- **Response**: `{"status": "healthy", "service": "operational-loss-calculator-ai"}`

### 2. Describe Event
`POST /describe`
- **Body**: `{"description": "string", "severity": "string"}`
- **Response**: Returns summary, detailed_analysis, impact, risk_level, and generated_at.

### 3. Get Recommendations
`POST /recommend`
- **Body**: `{"description": "string", "severity": "string"}`
- **Response**: Returns a list of 3 recommendation objects.

### 4. Generate Report
`POST /generate-report`
- **Body**: `{"description": "string", "severity": "string"}`
- **Response**: Returns a full report including title, summary, overview, and key items.

## 📁 Folder Structure
- `routes/`: API endpoint definitions.
- `services/`: Core logic (Groq client).
- `prompts/`: AI prompt templates.
- `app.py`: Entry point.
