package com.contractmonitor.contractmonitor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_specs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiSpec {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String serviceName;
    
    @Column(nullable = false)
    private String version;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String specContent; // JSON of OpenAPI spec
    
    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;
    
    @PrePersist
    protected void onCreate() {
        fetchedAt = LocalDateTime.now();
    }
}