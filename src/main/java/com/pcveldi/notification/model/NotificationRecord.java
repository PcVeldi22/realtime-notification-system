package com.pcveldi.notification.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRecord {

    @Id
    private String eventId;

    @Column(nullable = false)
    private String userId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    private NotificationEvent.Channel channel;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    private Integer attemptCount;
    private String failureReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime deliveredAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (attemptCount == null) {
            attemptCount = 0;
        }
    }

    public enum DeliveryStatus {
        PENDING, DELIVERED, FAILED, SKIPPED_QUIET_HOURS, RATE_LIMITED
    }
}
