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

    /**
     * Get the baseline spec for a service
     */
    public Optional<ApiSpec> getBaselineSpec(String serviceName) {
        log.info("Fetching baseline spec for {}", serviceName);
        return apiSpecRepository.findByServiceNameAndIsBaselineTrue(serviceName);
    }
    
    /**
     * Set a specific spec as the baseline
     */
    public ApiSpec setBaseline(String serviceName, Long specId) {
        log.info("Setting baseline for {} to spec ID {}", serviceName, specId);
        
        // Get the spec to be marked as baseline
        ApiSpec spec = apiSpecRepository.findById(specId)
                .orElseThrow(() -> new RuntimeException("Spec not found with ID: " + specId));
        
        // Verify it belongs to the correct service
        if (!spec.getServiceName().equals(serviceName)) {
            throw new RuntimeException("Spec ID " + specId + " does not belong to service " + serviceName);
        }
        
        // Clear any existing baseline for this service
        apiSpecRepository.clearBaseline(serviceName);
        
        // Set new baseline
        spec.setIsBaseline(true);
        spec.setBaselineSetAt(LocalDateTime.now());
        
        ApiSpec saved = apiSpecRepository.save(spec);
        log.info("Successfully set baseline for {} at version {}", serviceName, spec.getVersion());
        
        return saved;
    }
    
    /**
     * Set the latest spec as baseline
     */
    public ApiSpec setLatestAsBaseline(String serviceName) {
        log.info("Setting latest spec as baseline for {}", serviceName);
        
        ApiSpec latestSpec = getLatestSpec(serviceName)
                .orElseThrow(() -> new RuntimeException("No specs found for service: " + serviceName));
        
        return setBaseline(serviceName, latestSpec.getId());
    }
    
    /**
     * Clear baseline for a service
     */
    public void clearBaseline(String serviceName) {
        log.info("Clearing baseline for {}", serviceName);
        apiSpecRepository.clearBaseline(serviceName);
    }
    
    /**
     * Get the spec to compare against (baseline or previous)
     */
    public Optional<ApiSpec> getComparisonSpec(String serviceName) {
        // Try to get baseline first
        Optional<ApiSpec> baseline = getBaselineSpec(serviceName);
        if (baseline.isPresent()) {
            log.info("Using baseline spec for comparison for {}", serviceName);
            return baseline;
        }
        
        // Fall back to previous spec
        log.info("No baseline found, using previous spec for comparison for {}", serviceName);
        List<ApiSpec> lastTwo = getLastTwoSpecs(serviceName);
        if (lastTwo.size() >= 2) {
            return Optional.of(lastTwo.get(1)); // Second newest
        }
        
        return Optional.empty();
    }
}