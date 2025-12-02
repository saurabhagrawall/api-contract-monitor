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
    @Enumerated(EnumType.STRING)
    private ChangeType changeType;
    
    @Column(nullable = false, length = 500)
    private String path;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private String oldVersion;
    
    @Column(nullable = false)
    private String newVersion;
    
    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt;
    
    // AI-generated fields
    @Column(columnDefinition = "TEXT")
    private String aiSuggestion;
    
    @Column(columnDefinition = "TEXT")
    private String predictedImpact;
    
    @Column(columnDefinition = "TEXT")
    private String plainEnglishExplanation;
    
    // NEW: Status management fields
    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @Column(name = "resolved_by", length = 100)
    private String resolvedBy;
    
    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;
    
    // Enum definitions
    public enum ChangeType {
        ENDPOINT_REMOVED,
        METHOD_REMOVED,
        FIELD_REMOVED,
        TYPE_CHANGED,
        SCHEMA_REMOVED
    }
    
    // NEW: Status enum
    public enum Status {
        ACTIVE,          // Newly detected, needs attention
        ACKNOWLEDGED,    // Team knows, working on it
        RESOLVED,        // Fixed in code
        IGNORED          // Intentional change, not a bug
    }
    
    @PrePersist
    protected void onCreate() {
        detectedAt = LocalDateTime.now();
        if (status == null) {
            status = Status.ACTIVE;
        }
    }
}