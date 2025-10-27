package com.contractmonitor.contractmonitor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisReport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String serviceName;
    
    @Column(nullable = false)
    private Integer breakingChangesCount;
    
    @Column(nullable = false)
    private Integer nonBreakingChangesCount;
    
    @Column(columnDefinition = "TEXT")
    private String summary;
    
    @Column(name = "analyzed_at", nullable = false)
    private LocalDateTime analyzedAt;
    
    @PrePersist
    protected void onCreate() {
        analyzedAt = LocalDateTime.now();
    }
}