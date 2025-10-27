package com.contractmonitor.contractmonitor.controller;

import com.contractmonitor.contractmonitor.entity.AnalysisReport;
import com.contractmonitor.contractmonitor.service.AnalysisService;
import com.contractmonitor.contractmonitor.service.OpenApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@Slf4j
public class AnalysisController {
    
    private final AnalysisService analysisService;
    private final OpenApiClient openApiClient;
    
    /**
     * Trigger analysis for a specific service
     * POST /api/analysis/{serviceName}
     */
    @PostMapping("/{serviceName}")
    public ResponseEntity<?> analyzeService(@PathVariable String serviceName) {
        log.info("Received request to analyze: {}", serviceName);
        
        try {
            // Check if service is available
            if (!openApiClient.isServiceAvailable(serviceName)) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of(
                                "error", "Service not available",
                                "message", serviceName + " is currently offline or unreachable"
                        ));
            }
            
            // Perform analysis
            AnalysisReport report = analysisService.analyzeService(serviceName);
            
            return ResponseEntity.ok(Map.of(
                    "message", "Analysis completed successfully",
                    "report", report
            ));
            
        } catch (Exception e) {
            log.error("Error analyzing {}: {}", serviceName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Analysis failed",
                            "message", e.getMessage()
                    ));
        }
    }
    
    /**
     * Analyze all services
     * POST /api/analysis/all
     */
    @PostMapping("/all")
    public ResponseEntity<?> analyzeAllServices() {
        log.info("Received request to analyze all services");
        
        List<String> services = List.of("user-service", "order-service", 
                                       "product-service", "notification-service");
        
        Map<String, Object> results = new java.util.HashMap<>();
        int successCount = 0;
        int failCount = 0;
        
        for (String service : services) {
            try {
                if (!openApiClient.isServiceAvailable(service)) {
                    results.put(service, Map.of("status", "offline"));
                    failCount++;
                    continue;
                }
                
                AnalysisReport report = analysisService.analyzeService(service);
                results.put(service, Map.of(
                        "status", "success",
                        "breakingChanges", report.getBreakingChangesCount()
                ));
                successCount++;
                
            } catch (Exception e) {
                log.error("Error analyzing {}: {}", service, e.getMessage());
                results.put(service, Map.of(
                        "status", "error",
                        "message", e.getMessage()
                ));
                failCount++;
            }
        }
        
        return ResponseEntity.ok(Map.of(
                "totalServices", services.size(),
                "successCount", successCount,
                "failCount", failCount,
                "results", results
        ));
    }
    
    /**
     * Get latest analysis report for a service
     * GET /api/analysis/{serviceName}/latest
     */
    @GetMapping("/{serviceName}/latest")
    public ResponseEntity<?> getLatestReport(@PathVariable String serviceName) {
        AnalysisReport report = analysisService.getLatestReport(serviceName);
        
        if (report == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "error", "No reports found",
                            "message", "No analysis reports found for " + serviceName
                    ));
        }
        
        return ResponseEntity.ok(report);
    }
    
    /**
     * Get analysis history for a service
     * GET /api/analysis/{serviceName}/history
     */
    @GetMapping("/{serviceName}/history")
    public ResponseEntity<List<AnalysisReport>> getReportHistory(@PathVariable String serviceName) {
        List<AnalysisReport> history = analysisService.getReportHistory(serviceName);
        return ResponseEntity.ok(history);
    }
    
    /**
     * Check service availability
     * GET /api/analysis/status/{serviceName}
     */
    @GetMapping("/status/{serviceName}")
    public ResponseEntity<?> checkServiceStatus(@PathVariable String serviceName) {
        boolean available = openApiClient.isServiceAvailable(serviceName);
        
        return ResponseEntity.ok(Map.of(
                "serviceName", serviceName,
                "available", available,
                "status", available ? "online" : "offline"
        ));
    }
    
    /**
     * Check all services status
     * GET /api/analysis/status
     */
    @GetMapping("/status")
    public ResponseEntity<?> checkAllServicesStatus() {
        List<String> services = List.of("user-service", "order-service", 
                                       "product-service", "notification-service");
        
        Map<String, Boolean> statuses = new java.util.HashMap<>();
        for (String service : services) {
            statuses.put(service, openApiClient.isServiceAvailable(service));
        }
        
        long onlineCount = statuses.values().stream().filter(status -> status).count();
        
        return ResponseEntity.ok(Map.of(
                "services", statuses,
                "onlineCount", onlineCount,
                "totalCount", services.size()
        ));
    }
}