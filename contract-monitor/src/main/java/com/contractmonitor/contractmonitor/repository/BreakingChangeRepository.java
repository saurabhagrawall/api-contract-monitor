package com.contractmonitor.contractmonitor.repository;

import com.contractmonitor.contractmonitor.entity.BreakingChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BreakingChangeRepository extends JpaRepository<BreakingChange, Long> {
    
    List<BreakingChange> findByServiceName(String serviceName);
    
    List<BreakingChange> findByServiceNameOrderByDetectedAtDesc(String serviceName);
    
    List<BreakingChange> findByChangeType(BreakingChange.ChangeType changeType);
    
    List<BreakingChange> findByServiceNameAndChangeType(String serviceName, BreakingChange.ChangeType changeType);
    
    Long countByServiceName(String serviceName);
    
    // NEW: Status-based queries
    List<BreakingChange> findByStatus(BreakingChange.Status status);
    
    List<BreakingChange> findByServiceNameAndStatus(String serviceName, BreakingChange.Status status);
    
    List<BreakingChange> findByServiceNameAndStatusOrderByDetectedAtDesc(String serviceName, BreakingChange.Status status);
    
    Long countByServiceNameAndStatus(String serviceName, BreakingChange.Status status);
    
    @Query("SELECT COUNT(b) FROM BreakingChange b WHERE b.status = 'ACTIVE'")
    Long countActiveBreakingChanges();
}