package com.internship.tool.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.boot.web.client.RestTemplateBuilder;
import java.time.Duration;
import java.util.Map;

@Service
public class AiServiceClient {

    private final RestTemplate restTemplate;
    private final String AI_SERVICE_URL = "http://localhost:5000";

    public AiServiceClient(RestTemplateBuilder restTemplateBuilder) {
        // Set 10s timeout as per Day 4 requirements
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }

    public Map<String, Object> describeEvent(String description, String severity) {
        return callAiEndpoint("/describe", description, severity);
    }

    public Map<String, Object> getRecommendations(String description, String severity) {
        return callAiEndpoint("/recommend", description, severity);
    }

    public Map<String, Object> generateReport(String description, String severity) {
        return callAiEndpoint("/generate-report", description, severity);
    }

    private Map<String, Object> callAiEndpoint(String endpoint, String description, String severity) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = Map.of(
                "description", description,
                "severity", severity
            );

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            return restTemplate.postForObject(AI_SERVICE_URL + endpoint, request, Map.class);
            
        } catch (Exception e) {
            // Log the error and return null as per Day 4 requirements
            System.err.println("AI Service Error: " + e.getMessage());
            return null;
        }
    }
}
