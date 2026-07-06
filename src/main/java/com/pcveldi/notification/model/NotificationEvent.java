package com.pcveldi.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent implements Serializable {

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private String userId;
    private String title;
    private String body;
    private List<Channel> channels;
    private Priority priority;
    private LocalDateTime createdAt;

    public enum Channel {
        WEBSOCKET, PUSH, EMAIL, SMS
    }

    public enum Priority {
        LOW, NORMAL, HIGH, CRITICAL
    }
}
