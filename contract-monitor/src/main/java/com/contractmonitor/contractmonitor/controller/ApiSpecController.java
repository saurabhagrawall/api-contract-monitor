package com.contractmonitor.contractmonitor.controller;

import com.contractmonitor.contractmonitor.entity.ApiSpec;
import com.contractmonitor.contractmonitor.service.ApiSpecService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/specs")
@RequiredArgsConstructor
@Slf4j
public class ApiSpecController {
    
    private final ApiSpecService apiSpecService;
    
    /**
     * Get the latest spec for a service
     * GET /api/specs/{serviceName}/latest
     */
    @GetMapping("/{serviceName}/latest")
    public ResponseEntity<?> getLatestSpec(@PathVariable String serviceName) {
        log.info("Fetching latest spec for: {}", serviceName);
        
        Optional<ApiSpec> spec = apiSpecService.getLatestSpec(serviceName);
        
        if (spec.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "error", "No specs found",
                            "message", "No OpenAPI specs found for " + serviceName
                    ));
        }
        
        return ResponseEntity.ok(spec.get());
    }
    
    /**
     * Get spec history for a service
     * GET /api/specs/{serviceName}/history
     */
    @GetMapping("/{serviceName}/history")
    public ResponseEntity<List<ApiSpec>> getSpecHistory(@PathVariable String serviceName) {
        log.info("Fetching spec history for: {}", serviceName);
        List<ApiSpec> history = apiSpecService.getSpecHistory(serviceName);
        return ResponseEntity.ok(history);
    }
    
    /**
     * Get a specific version of a spec
     * GET /api/specs/{serviceName}/version/{version}
     */
    @GetMapping("/{serviceName}/version/{version}")
    public ResponseEntity<?> getSpecByVersion(
            @PathVariable String serviceName,
            @PathVariable String version) {
        log.info("Fetching spec version {} for: {}", version, serviceName);
        
        Optional<ApiSpec> spec = apiSpecService.getSpecByVersion(serviceName, version);
        
        if (spec.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "error", "Spec not found",
                            "message", "No spec found for " + serviceName + " version " + version
                    ));
        }
        
        return ResponseEntity.ok(spec.get());
    }
    
    /**
     * Fetch and save current spec for a service
     * POST /api/specs/{serviceName}/fetch
     */
    @PostMapping("/{serviceName}/fetch")
    public ResponseEntity<?> fetchAndSaveSpec(@PathVariable String serviceName) {
        log.info("Fetching and saving spec for: {}", serviceName);
        
        try {
            ApiSpec spec = apiSpecService.fetchAndSaveSpec(serviceName);
            return ResponseEntity.ok(Map.of(
                    "message", "Spec fetched and saved successfully",
                    "spec", spec
            ));
        } catch (Exception e) {
            log.error("Error fetching spec for {}: {}", serviceName, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to fetch spec",
                            "message", e.getMessage()
                    ));
        }
    }
    
    /**
     * Check if service has any specs
     * GET /api/specs/{serviceName}/exists
     */
    @GetMapping("/{serviceName}/exists")
    public ResponseEntity<?> hasSpecs(@PathVariable String serviceName) {
        boolean exists = apiSpecService.hasSpecs(serviceName);
        
        return ResponseEntity.ok(Map.of(
                "serviceName", serviceName,
                "hasSpecs", exists
        ));
    }
    
    /**
     * Get total spec count across all services
     * GET /api/specs/count
     */
    @GetMapping("/count")
    public ResponseEntity<?> getTotalCount() {
        long count = apiSpecService.getTotalSpecCount();
        
        return ResponseEntity.ok(Map.of(
                "totalSpecs", count
        ));
    }
    
    /**
     * Cleanup old specs for a service
     * DELETE /api/specs/{serviceName}/cleanup?keep=10
     */
    @DeleteMapping("/{serviceName}/cleanup")
    public ResponseEntity<?> cleanupOldSpecs(
            @PathVariable String serviceName,
            @RequestParam(defaultValue = "10") int keep) {
        log.info("Cleaning up old specs for {}, keeping last {}", serviceName, keep);
        
        try {
            apiSpecService.cleanupOldSpecs(serviceName, keep);
            return ResponseEntity.ok(Map.of(
                    "message", "Cleanup completed successfully",
                    "serviceName", serviceName,
                    "keptVersions", keep
            ));
        } catch (Exception e) {
            log.error("Error during cleanup for {}: {}", serviceName, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Cleanup failed",
                            "message", e.getMessage()
                    ));
        }
    }
}