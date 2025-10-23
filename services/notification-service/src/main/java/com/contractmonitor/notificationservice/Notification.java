package com.contractmonitor.notificationservice;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "User ID is required")
    @Column(nullable = false)
    private Long userId;
    
    @NotBlank(message = "Type is required")
    @Column(nullable = false, length = 50)
    private String type; // EMAIL, SMS, PUSH
    
    @NotBlank(message = "Message is required")
    @Column(nullable = false, length = 500)
    private String message;
    
    @Column(nullable = false, length = 20)
    private String status; // PENDING, SENT, FAILED
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
    }
}