package com.oploss;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;

@Service
public class AiServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(AiServiceClient.class);
    private final RestTemplate restTemplate;
    private final String baseUrl = "http://localhost:5000";

    public AiServiceClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 10 seconds
        factory.setReadTimeout(10000);    // 10 seconds
        this.restTemplate = new RestTemplate(factory);
    }

    public Map<String, Object> getDescription(String scenario) {
        return callAiService("/describe", scenario);
    }

    public Map<String, Object> getRecommendation(String scenario) {
        return callAiService("/recommend", scenario);
    }

    private Map<String, Object> callAiService(String endpoint, String scenario) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("scenario", scenario);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(baseUrl + endpoint, request, Map.class);
            return response.getBody();
            
        } catch (Exception e) {
            logger.error("Failed to call AI service at {}: {}", endpoint, e.getMessage());
            // Return a specific error object or null as requested to prevent a system-wide exception
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "AI Service Unavailable");
            errorResponse.put("details", e.getMessage());
            return errorResponse; // Returning a specific error object instead of throwing
        }
    }
}
