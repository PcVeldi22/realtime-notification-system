package com.pcveldi.notification.controller;

import com.pcveldi.notification.dto.NotificationRequest;
import com.pcveldi.notification.model.NotificationEvent;
import com.pcveldi.notification.model.NotificationRecord;
import com.pcveldi.notification.repository.NotificationRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
    private final NotificationRepository notificationRepository;

    private static final String TOPIC = "notification.events";

    @PostMapping
    public ResponseEntity<String> publish(@Valid @RequestBody NotificationRequest request) {
        NotificationEvent event = NotificationEvent.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .body(request.getBody())
                .channels(request.getChannels())
                .priority(request.getPriority())
                .createdAt(LocalDateTime.now())
                .build();

        kafkaTemplate.send(TOPIC, event.getUserId(), event);
        return ResponseEntity.accepted().body(event.getEventId());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Page<NotificationRecord>> getHistory(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<NotificationRecord> history = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
        return ResponseEntity.ok(history);
    }
}
