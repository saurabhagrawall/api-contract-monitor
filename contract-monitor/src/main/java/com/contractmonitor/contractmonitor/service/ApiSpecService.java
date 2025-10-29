package com.contractmonitor.contractmonitor.service;

import com.contractmonitor.contractmonitor.entity.ApiSpec;
import com.contractmonitor.contractmonitor.repository.ApiSpecRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ApiSpecService {
    
    private final ApiSpecRepository apiSpecRepository;
    private final OpenApiClient openApiClient;
    
    /**
     * Fetch and save the current OpenAPI spec from a service
     */
    public ApiSpec fetchAndSaveSpec(String serviceName) {
        log.info("Fetching and saving spec for {}", serviceName);
        
        // Fetch spec from the microservice
        String specContent = openApiClient.fetchOpenApiSpec(serviceName);
        
        // Generate version (timestamp-based for now)
        String version = generateVersion();
        
        // Create and save ApiSpec entity
        ApiSpec apiSpec = new ApiSpec();
        apiSpec.setServiceName(serviceName);
        apiSpec.setVersion(version);
        apiSpec.setSpecContent(specContent);
        
        ApiSpec saved = apiSpecRepository.save(apiSpec);
        log.info("Saved spec for {} with version {}", serviceName, version);
        
        return saved;
    }
    
    /**
     * Get the most recent spec for a service
     */
    public Optional<ApiSpec> getLatestSpec(String serviceName) {
        return apiSpecRepository.findTopByServiceNameOrderByFetchedAtDesc(serviceName);
    }
    
    /**
     * Get all specs for a service (history)
     */
    public List<ApiSpec> getSpecHistory(String serviceName) {
        return apiSpecRepository.findByServiceNameOrderByFetchedAtDesc(serviceName);
    }
    
    /**
     * Get a specific version of a spec
     */
    public Optional<ApiSpec> getSpecByVersion(String serviceName, String version) {
        return apiSpecRepository.findByServiceNameAndVersion(serviceName, version);
    }
    
    /**
     * Get the two most recent specs for comparison
     */
    public List<ApiSpec> getLastTwoSpecs(String serviceName) {
        List<ApiSpec> history = apiSpecRepository.findByServiceNameOrderByFetchedAtDesc(serviceName);
        
        if (history.size() < 2) {
            log.warn("Not enough specs to compare for {}. Found: {}", serviceName, history.size());
            return history;
        }
        
        return history.subList(0, 2);  // Returns [newest, second-newest]
    }
    
    /**
     * Delete old specs (keep only last N versions)
     */
    public void cleanupOldSpecs(String serviceName, int keepCount) {
        List<ApiSpec> allSpecs = apiSpecRepository.findByServiceNameOrderByFetchedAtDesc(serviceName);
        
        if (allSpecs.size() <= keepCount) {
            log.info("No cleanup needed for {}. Total specs: {}", serviceName, allSpecs.size());
            return;
        }
        
        // Keep first N, delete the rest
        List<ApiSpec> toDelete = allSpecs.subList(keepCount, allSpecs.size());
        apiSpecRepository.deleteAll(toDelete);
        
        log.info("Deleted {} old specs for {}", toDelete.size(), serviceName);
    }
    
    /**
     * Generate version string (timestamp-based)
     */
    private String generateVersion() {
        return LocalDateTime.now().toString();
    }
    
    /**
     * Check if we have any specs for a service
     */
    public boolean hasSpecs(String serviceName) {
        return !apiSpecRepository.findByServiceName(serviceName).isEmpty();
    }
    
    /**
     * Get total number of specs stored
     */
    public long getTotalSpecCount() {
        return apiSpecRepository.count();
    }

    public List<ApiSpec> getAllLatestSpecs() {
        log.info("Fetching all latest specs for all services");

        // Get distinct service names
        List<String> serviceNames = apiSpecRepository.findDistinctServiceNames();

        List<ApiSpec> latestSpecs = new ArrayList<>();
        for (String serviceName : serviceNames) {
            apiSpecRepository.findTopByServiceNameOrderByFetchedAtDesc(serviceName)
                    .ifPresent(latestSpecs::add);
        }

        log.info("Found latest specs for {} services", latestSpecs.size());
        return latestSpecs;
    }
}