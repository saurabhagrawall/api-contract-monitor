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
    @Enumerated(EnumType.STRING)  // CHANGED: Now using enum instead of String
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

    // AI-generated fields (already present in your file)
    @Column(columnDefinition = "TEXT")
    private String aiSuggestion;

    @Column(columnDefinition = "TEXT")
    private String predictedImpact;

    @Column(columnDefinition = "TEXT")
    private String plainEnglishExplanation;

    // Add the enum definition
    public enum ChangeType {
        ENDPOINT_REMOVED,
        METHOD_REMOVED,
        FIELD_REMOVED,
        TYPE_CHANGED,
        SCHEMA_REMOVED
    }

    @PrePersist
    protected void onCreate() {
        detectedAt = LocalDateTime.now();
    }
}