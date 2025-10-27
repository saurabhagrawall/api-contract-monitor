package com.contractmonitor.contractmonitor.repository;

import com.contractmonitor.contractmonitor.entity.AnalysisReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisReportRepository extends JpaRepository<AnalysisReport, Long> {
    
    List<AnalysisReport> findByServiceName(String serviceName);
    
    List<AnalysisReport> findByServiceNameOrderByAnalyzedAtDesc(String serviceName);
    
    Optional<AnalysisReport> findTopByServiceNameOrderByAnalyzedAtDesc(String serviceName);
    
    List<AnalysisReport> findAllByOrderByAnalyzedAtDesc();
}