package com.contractmonitor.contractmonitor.controller;

import com.contractmonitor.contractmonitor.entity.BreakingChange;
import com.contractmonitor.contractmonitor.service.BreakingChangeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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

    /**
     * Get breaking changes by status
     * GET /api/breaking-changes/{serviceName}/status/{status}
     */
    @GetMapping("/{serviceName}/status/{status}")
    public ResponseEntity<List<BreakingChange>> getByStatus(
            @PathVariable String serviceName,
            @PathVariable BreakingChange.Status status) {
        log.info("Fetching {} breaking changes for: {}", status, serviceName);
        List<BreakingChange> changes = breakingChangeService.getByServiceNameAndStatus(serviceName, status);
        return ResponseEntity.ok(changes);
    }
    
    /**
     * Get active breaking changes
     * GET /api/breaking-changes/{serviceName}/active
     */
    @GetMapping("/{serviceName}/active")
    public ResponseEntity<List<BreakingChange>> getActiveChanges(@PathVariable String serviceName) {
        log.info("Fetching active breaking changes for: {}", serviceName);
        List<BreakingChange> changes = breakingChangeService.getActiveChanges(serviceName);
        return ResponseEntity.ok(changes);
    }
    
    /**
     * Update breaking change status
     * PUT /api/breaking-changes/{id}/status
     * Body: { "status": "RESOLVED", "resolvedBy": "user@example.com", "notes": "Fixed in PR #123" }
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        
        log.info("Updating status for breaking change: {}", id);
        
        try {
            String statusStr = request.get("status");
            String resolvedBy = request.getOrDefault("resolvedBy", "system");
            String notes = request.getOrDefault("notes", "");
            
            if (statusStr == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Status is required"
                ));
            }
            
            BreakingChange.Status status = BreakingChange.Status.valueOf(statusStr.toUpperCase());
            BreakingChange updated = breakingChangeService.updateStatus(id, status, resolvedBy, notes);
            
            return ResponseEntity.ok(Map.of(
                "message", "Status updated successfully",
                "breakingChange", updated
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Invalid status. Must be one of: ACTIVE, ACKNOWLEDGED, RESOLVED, IGNORED"
            ));
        } catch (Exception e) {
            log.error("Error updating status: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Acknowledge a breaking change
     * POST /api/breaking-changes/{id}/acknowledge
     */
    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<?> acknowledge(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {
        
        log.info("Acknowledging breaking change: {}", id);
        
        String acknowledgedBy = request != null ? request.getOrDefault("acknowledgedBy", "system") : "system";
        BreakingChange updated = breakingChangeService.acknowledge(id, acknowledgedBy);
        
        return ResponseEntity.ok(Map.of(
            "message", "Breaking change acknowledged",
            "breakingChange", updated
        ));
    }
    
    /**
     * Resolve a breaking change
     * POST /api/breaking-changes/{id}/resolve
     * Body: { "resolvedBy": "user@example.com", "notes": "Fixed in commit abc123" }
     */
    @PostMapping("/{id}/resolve")
    public ResponseEntity<?> resolve(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {
        
        log.info("Resolving breaking change: {}", id);

        // Handling null request body
        if (request == null) {
            request = new HashMap<>();
        }
        
        String resolvedBy = request.getOrDefault("resolvedBy", "system");
        String notes = request.getOrDefault("notes", "Resolved");
        
        BreakingChange updated = breakingChangeService.resolve(id, resolvedBy, notes);
        
        return ResponseEntity.ok(Map.of(
            "message", "Breaking change resolved",
            "breakingChange", updated
        ));
    }
    
    /**
     * Ignore a breaking change
     * POST /api/breaking-changes/{id}/ignore
     * Body: { "ignoredBy": "user@example.com", "reason": "Intentional deprecation" }
     */
    @PostMapping("/{id}/ignore")
    public ResponseEntity<?> ignore(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {
        
        log.info("Ignoring breaking change: {}", id);

        // Handling null request body
        if (request == null) {
            request = new HashMap<>();
        }
        
        String ignoredBy = request.getOrDefault("ignoredBy", "system");
        String reason = request.getOrDefault("reason", "Marked as intentional");
        
        BreakingChange updated = breakingChangeService.ignore(id, ignoredBy, reason);
        
        return ResponseEntity.ok(Map.of(
            "message", "Breaking change ignored",
            "breakingChange", updated
        ));
    }
}