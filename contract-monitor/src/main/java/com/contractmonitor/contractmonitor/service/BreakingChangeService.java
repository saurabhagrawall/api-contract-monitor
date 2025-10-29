package com.contractmonitor.contractmonitor.service;

import com.contractmonitor.contractmonitor.entity.BreakingChange;
import com.contractmonitor.contractmonitor.repository.BreakingChangeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BreakingChangeService {
    
    private final BreakingChangeRepository breakingChangeRepository;
    
    /**
     * Save a breaking change
     */
    public BreakingChange save(BreakingChange breakingChange) {
        log.info("Recording breaking change: {} in {} at {}", 
                breakingChange.getChangeType(), 
                breakingChange.getServiceName(), 
                breakingChange.getPath());
        return breakingChangeRepository.save(breakingChange);
    }
    
    /**
     * Save multiple breaking changes
     */
    public List<BreakingChange> saveAll(List<BreakingChange> changes) {
        log.info("Recording {} breaking changes", changes.size());
        return breakingChangeRepository.saveAll(changes);
    }
    
    /**
     * Get all breaking changes for a service
     */
    public List<BreakingChange> getByServiceName(String serviceName) {
        return breakingChangeRepository.findByServiceNameOrderByDetectedAtDesc(serviceName);
    }
    
    /**
     * Get breaking changes by type
     */
    public List<BreakingChange> getByType(BreakingChange.ChangeType changeType) {
        return breakingChangeRepository.findByChangeType(changeType);
    }

    /**
     * Get breaking changes by service and type
     */
    public List<BreakingChange> getBreakingChangesByType(String serviceName, BreakingChange.ChangeType changeType) {
        return breakingChangeRepository.findByServiceNameAndChangeType(serviceName, changeType);
    }
    
    /**
     * Get count of breaking changes for a service
     */
    public Long countByServiceName(String serviceName) {
        return breakingChangeRepository.countByServiceName(serviceName);
    }
    
    /**
     * Get all breaking changes across all services
     */
    public List<BreakingChange> getAllBreakingChanges() {
        return breakingChangeRepository.findAll();
    }
    
    /**
     * Get breaking changes grouped by type
     */
    public Map<String, Long> getBreakingChangesByType(String serviceName) {
        List<BreakingChange> changes = breakingChangeRepository.findByServiceName(serviceName);
        
        return changes.stream()
                .collect(Collectors.groupingBy(
                        change -> change.getChangeType().toString(), // Convert enum to String
                        Collectors.counting()));
    }
    
    /**
     * Get recent breaking changes (last N)
     */
    public List<BreakingChange> getRecentChanges(String serviceName, int limit) {
        List<BreakingChange> allChanges = breakingChangeRepository
                .findByServiceNameOrderByDetectedAtDesc(serviceName);
        
        return allChanges.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Check if service has any breaking changes
     */
    public boolean hasBreakingChanges(String serviceName) {
        return breakingChangeRepository.countByServiceName(serviceName) > 0;
    }
    
    /**
     * Delete all breaking changes for a service
     */
    public void deleteAllForService(String serviceName) {
        List<BreakingChange> changes = breakingChangeRepository.findByServiceName(serviceName);
        breakingChangeRepository.deleteAll(changes);
        log.info("Deleted {} breaking changes for {}", changes.size(), serviceName);
    }
    
    /**
     * Get summary statistics
     */
    public Map<String, Object> getStatistics() {
        long total = breakingChangeRepository.count();
        
        Map<String, Long> byType = breakingChangeRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        change -> change.getChangeType().toString(), // Convert enum to String
                        Collectors.counting()));
        
        return Map.of(
                "totalBreakingChanges", total,
                "breakingChangesByType", byType
        );
    }
}