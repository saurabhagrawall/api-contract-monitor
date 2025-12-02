package com.contractmonitor.contractmonitor.controller;

import com.contractmonitor.contractmonitor.entity.ApiSpec;
import com.contractmonitor.contractmonitor.service.ApiSpecService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/baseline")
@RequiredArgsConstructor
@Slf4j
public class BaselineController {
    
    private final ApiSpecService apiSpecService;
    
    /**
     * Get the current baseline spec for a service
     */
    @GetMapping("/{serviceName}")
    public ResponseEntity<?> getBaseline(@PathVariable String serviceName) {
        log.info("GET /api/baseline/{}", serviceName);
        
        Optional<ApiSpec> baseline = apiSpecService.getBaselineSpec(serviceName);
        
        if (baseline.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "serviceName", serviceName,
                "hasBaseline", false,
                "message", "No baseline set for this service"
            ));
        }
        
        return ResponseEntity.ok(Map.of(
            "serviceName", serviceName,
            "hasBaseline", true,
            "baseline", baseline.get()
        ));
    }
    
    /**
     * Set a specific spec as baseline
     */
    @PostMapping("/{serviceName}/set/{specId}")
    public ResponseEntity<?> setBaseline(
            @PathVariable String serviceName,
            @PathVariable Long specId) {
        
        log.info("POST /api/baseline/{}/set/{}", serviceName, specId);
        
        try {
            ApiSpec baseline = apiSpecService.setBaseline(serviceName, specId);
            
            return ResponseEntity.ok(Map.of(
                "message", "Baseline set successfully",
                "serviceName", serviceName,
                "baselineVersion", baseline.getVersion(),
                "baselineSetAt", baseline.getBaselineSetAt()
            ));
        } catch (Exception e) {
            log.error("Error setting baseline: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Set the latest spec as baseline
     */
    @PostMapping("/{serviceName}/set-latest")
    public ResponseEntity<?> setLatestAsBaseline(@PathVariable String serviceName) {
        log.info("POST /api/baseline/{}/set-latest", serviceName);
        
        try {
            ApiSpec baseline = apiSpecService.setLatestAsBaseline(serviceName);
            
            return ResponseEntity.ok(Map.of(
                "message", "Latest spec set as baseline successfully",
                "serviceName", serviceName,
                "baselineVersion", baseline.getVersion(),
                "baselineSetAt", baseline.getBaselineSetAt()
            ));
        } catch (Exception e) {
            log.error("Error setting latest as baseline: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Clear baseline for a service
     */
    @DeleteMapping("/{serviceName}")
    public ResponseEntity<?> clearBaseline(@PathVariable String serviceName) {
        log.info("DELETE /api/baseline/{}", serviceName);
        
        apiSpecService.clearBaseline(serviceName);
        
        return ResponseEntity.ok(Map.of(
            "message", "Baseline cleared successfully",
            "serviceName", serviceName
        ));
    }
}