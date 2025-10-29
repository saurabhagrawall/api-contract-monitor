package com.contractmonitor.contractmonitor.repository;

import com.contractmonitor.contractmonitor.entity.BreakingChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BreakingChangeRepository extends JpaRepository<BreakingChange, Long> {
    
    List<BreakingChange> findByServiceName(String serviceName);
    
    List<BreakingChange> findByServiceNameOrderByDetectedAtDesc(String serviceName);
    
    List<BreakingChange> findByChangeType(BreakingChange.ChangeType changeType);

    List<BreakingChange> findByServiceNameAndChangeType(String serviceName, BreakingChange.ChangeType changeType);
    
    Long countByServiceName(String serviceName);
}