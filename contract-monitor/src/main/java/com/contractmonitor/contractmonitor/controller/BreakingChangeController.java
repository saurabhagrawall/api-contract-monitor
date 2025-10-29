package com.contractmonitor.contractmonitor.controller;

import com.contractmonitor.contractmonitor.entity.BreakingChange;
import com.contractmonitor.contractmonitor.service.BreakingChangeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/breaking-changes")
@RequiredArgsConstructor
@Slf4j
public class BreakingChangeController {
    
    private final BreakingChangeService breakingChangeService;
    
    /**
     * Get all breaking changes for a service
     * GET /api/breaking-changes/{serviceName}
     */
    @GetMapping("/{serviceName}")
    public ResponseEntity<List<BreakingChange>> getBreakingChanges(@PathVariable String serviceName) {
        log.info("Fetching breaking changes for: {}", serviceName);
        List<BreakingChange> changes = breakingChangeService.getByServiceName(serviceName);
        return ResponseEntity.ok(changes);
    }
    
    /**
     * Get breaking changes by type for a service
     * GET /api/breaking-changes/{serviceName}/type/{changeType}
     */
    @GetMapping("/{serviceName}/type/{changeType}")
        public ResponseEntity<List<BreakingChange>> getBreakingChangesByType(
        @PathVariable String serviceName,
        @PathVariable BreakingChange.ChangeType changeType) {
        log.info("Fetching {} changes for: {}", changeType, serviceName);
        List<BreakingChange> changes = breakingChangeService.getBreakingChangesByType(serviceName, changeType);
        return ResponseEntity.ok(changes);
    }
    
    /**
     * Get breaking change count for a service
     * GET /api/breaking-changes/{serviceName}/count
     */
    @GetMapping("/{serviceName}/count")
    public ResponseEntity<?> getBreakingChangeCount(@PathVariable String serviceName) {
        Long count = breakingChangeService.countByServiceName(serviceName);
        
        return ResponseEntity.ok(Map.of(
                "serviceName", serviceName,
                "breakingChangesCount", count
        ));
    }
    
    /**
     * Get all breaking changes across all services
     * GET /api/breaking-changes
     */
    @GetMapping
    public ResponseEntity<List<BreakingChange>> getAllBreakingChanges() {
        log.info("Fetching all breaking changes");
        List<BreakingChange> changes = breakingChangeService.getAllBreakingChanges();
        return ResponseEntity.ok(changes);
    }
    
    /**
     * Get breaking changes grouped by type
     * GET /api/breaking-changes/{serviceName}/summary
     */
    @GetMapping("/{serviceName}/summary")
    public ResponseEntity<?> getBreakingChangesSummary(@PathVariable String serviceName) {
        Map<String, Long> summary = breakingChangeService.getBreakingChangesByType(serviceName);
        Long total = breakingChangeService.countByServiceName(serviceName);
        
        return ResponseEntity.ok(Map.of(
                "serviceName", serviceName,
                "totalBreakingChanges", total,
                "byType", summary
        ));
    }
    
    /**
     * Get recent breaking changes (last N)
     * GET /api/breaking-changes/{serviceName}/recent?limit=5
     */
    @GetMapping("/{serviceName}/recent")
    public ResponseEntity<List<BreakingChange>> getRecentBreakingChanges(
            @PathVariable String serviceName,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Fetching last {} breaking changes for: {}", limit, serviceName);
        List<BreakingChange> changes = breakingChangeService.getRecentChanges(serviceName, limit);
        return ResponseEntity.ok(changes);
    }
    
    /**
     * Get statistics across all services
     * GET /api/breaking-changes/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        Map<String, Object> stats = breakingChangeService.getStatistics();
        return ResponseEntity.ok(stats);
    }
}