package com.contractmonitor.contractmonitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

@Service
@Slf4j
public class OpenApiClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${microservices.user-service.url}")
    private String userServiceUrl;
    
    @Value("${microservices.order-service.url}")
    private String orderServiceUrl;
    
    @Value("${microservices.product-service.url}")
    private String productServiceUrl;
    
    @Value("${microservices.notification-service.url}")
    private String notificationServiceUrl;
    
    public OpenApiClient() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Fetch OpenAPI spec from a microservice
     */
    public String fetchOpenApiSpec(String serviceName) {
        String url = getServiceUrl(serviceName);
        String apiDocsUrl = url + "/api-docs";
        
        try {
            log.info("Fetching OpenAPI spec from: {}", apiDocsUrl);
            String spec = restTemplate.getForObject(apiDocsUrl, String.class);
            log.info("Successfully fetched spec for {}", serviceName);
            return spec;
        } catch (ResourceAccessException e) {
            log.error("Failed to connect to {}: {}", serviceName, e.getMessage());
            throw new RuntimeException("Service " + serviceName + " is not available", e);
        } catch (Exception e) {
            log.error("Error fetching spec from {}: {}", serviceName, e.getMessage());
            throw new RuntimeException("Failed to fetch OpenAPI spec from " + serviceName, e);
        }
    }
    
    /**
     * Get the base URL for a service
     */
    private String getServiceUrl(String serviceName) {
        return switch (serviceName.toLowerCase()) {
            case "user-service" -> userServiceUrl;
            case "order-service" -> orderServiceUrl;
            case "product-service" -> productServiceUrl;
            case "notification-service" -> notificationServiceUrl;
            default -> throw new IllegalArgumentException("Unknown service: " + serviceName);
        };
    }
    
    /**
     * Check if a service is available
     */
    public boolean isServiceAvailable(String serviceName) {
        try {
            String url = getServiceUrl(serviceName);
            restTemplate.getForObject(url + "/api-docs", String.class);
            return true;
        } catch (Exception e) {
            log.warn("Service {} is not available: {}", serviceName, e.getMessage());
            return false;
        }
    }
}