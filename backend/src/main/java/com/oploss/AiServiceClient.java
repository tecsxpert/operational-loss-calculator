package com.oploss;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.ParameterizedTypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

@Service
public class AiServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(AiServiceClient.class);
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public AiServiceClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 10 seconds timeout as per requirements
        factory.setReadTimeout(10000);    // 10 seconds timeout
        this.restTemplate = new RestTemplate(factory);
        
        // Support HTTPS for production deployments via environment variable
        String envUrl = System.getenv("AI_SERVICE_URL");
        this.baseUrl = (envUrl != null && !envUrl.isEmpty()) 
            ? envUrl 
            : "http://localhost:5000";
        logger.info("AI Service URL configured: {}", this.baseUrl);
    }

    public Map<String, Object> getDescription(String scenario) {
        return callAiService("/describe", scenario, "Medium");
    }

    public Map<String, Object> getDescription(String scenario, String severity) {
        return callAiService("/describe", scenario, severity);
    }

    public Map<String, Object> getRecommendation(String scenario) {
        return callAiService("/recommend", scenario, "Medium");
    }

    public Map<String, Object> getRecommendation(String scenario, String severity) {
        return callAiService("/recommend", scenario, severity);
    }

    public Map<String, Object> generateReport(String scenario, String severity) {
        return callAiService("/generate-report", scenario, severity);
    }

    private Map<String, Object> callAiService(String endpoint, String scenario, String severity) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("scenario", scenario);
            requestBody.put("description", scenario); // Support both keys
            requestBody.put("severity", severity);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                baseUrl + endpoint, 
                request, 
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return response.getBody();
            
        } catch (Exception e) {
            logger.error("Failed to call AI service at {}: {}", endpoint, e.getMessage());
            // Return a specific error object instead of throwing
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "AI Service Unavailable");
            errorResponse.put("details", e.getMessage());
            errorResponse.put("is_fallback", true);
            return errorResponse; 
        }
    }
}
