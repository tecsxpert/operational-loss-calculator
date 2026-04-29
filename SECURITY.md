# Security and Threat Model

This document outlines the security considerations and threat model for the Operational Loss Calculator AI integration. Before deploying or expanding the capabilities of our AI component, operators and developers must review and address these specific threats.

## Identified Specific Threats

1. **Prompt Injection**
   - **Description:** Malicious users might craft input data (e.g., in loss description fields) intended to manipulate the Large Language Model's instructions. This might bypass the system prompt's restrictions and compel the AI to execute unintended actions or leak sensitive instructions.
   - **Mitigation:** Rely on structural prompt design and robust output validation (like structured JSON enforcing). Never grant the LLM direct, unsupervised execution privileges over infrastructure.

2. **Sensitive Data Leakage (PII & Financials)**
   - **Description:** The operational loss context might naturally include highly sensitive company financial data, customer details, or Personally Identifiable Information (PII). Sending this raw data to a third-party LLM (Groq) could violate compliance (GDPR, PCI-DSS, etc.).
   - **Mitigation:** Implement strict data sanitization and masking *before* dispatching the prompt to the API. Exclude actual customer names, exact massive loss dollar amounts if proprietary, or PII.

3. **API Key Exposure**
   - **Description:** Storing the `GROQ_API_KEY` in source control, client-side browser code (e.g., React frontend), or logging could result in malicious actors hijacking the key for their own workloads.
   - **Mitigation:** API keys must only be loaded via environment variables (`.env` files included in `.gitignore`) and strictly accessed by backend code (the Node/Python service). Never commit credentials to Git.

4. **Rate Limit Exhaustion (Denial of Service)**
   - **Description:** Attackers (or misconfigured internal scripts) might flood the system with requests, causing the Groq API to hit its rate limits (429 errors). This causes a denial-of-service for legitimate application workflows.
   - **Mitigation:** Implement strict rate-limiting on our own API endpoints before making downstream calls. The `GroqClient` utilizes exponential backoff for resilience against transient errors.

5. **Insecure Output Handling**
   - **Description:** Failing to validate the structural integrity and content of the JSON returned by the Groq API. An attacker might manipulate the AI to return malicious payloads (like XSS scripts or SQL injection vectors disguised as string values) that the downstream application blindly executes or renders.
   - **Mitigation:** Enforce JSON schema validation (e.g., using Zod or Pydantic) upon receiving data from the LLM. Treat all AI output as un-trusted, user-equivalent input until it is sanitized.
