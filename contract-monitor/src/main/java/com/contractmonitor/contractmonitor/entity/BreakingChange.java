package com.contractmonitor.contractmonitor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "breaking_changes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BreakingChange {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String serviceName;
    
    @Column(nullable = false)
    private String changeType; // FIELD_REMOVED, TYPE_CHANGED, ENDPOINT_REMOVED
    
    @Column(nullable = false)
    private String path; // e.g., "/api/users"
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private String oldVersion;
    
    @Column(nullable = false)
    private String newVersion;
    
    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt;
    
    @PrePersist
    protected void onCreate() {
        detectedAt = LocalDateTime.now();
    }
}