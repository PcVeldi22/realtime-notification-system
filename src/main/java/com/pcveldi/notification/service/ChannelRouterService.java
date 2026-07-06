package com.pcveldi.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcveldi.notification.config.RedisConfig;
import com.pcveldi.notification.model.NotificationEvent;
import com.pcveldi.notification.model.NotificationRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChannelRouterService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public NotificationRecord.DeliveryStatus routeToWebsocket(NotificationEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            redisTemplate.convertAndSend(RedisConfig.WEBSOCKET_FANOUT_CHANNEL, payload);
            return NotificationRecord.DeliveryStatus.DELIVERED;
        } catch (Exception e) {
            log.error("Failed to route event {} to websocket channel", event.getEventId(), e);
            return NotificationRecord.DeliveryStatus.FAILED;
        }
    }

    public NotificationRecord.DeliveryStatus routeToPush(NotificationEvent event, String deviceToken) {
        if (deviceToken == null || deviceToken.isBlank()) {
            return NotificationRecord.DeliveryStatus.FAILED;
        }
        log.info("Dispatching push notification for event {} to device {}", event.getEventId(), deviceToken);
        // Integration point for FCM/APNs SDK
        return NotificationRecord.DeliveryStatus.DELIVERED;
    }

    public NotificationRecord.DeliveryStatus routeToEmail(NotificationEvent event, String email) {
        if (email == null || email.isBlank()) {
            return NotificationRecord.DeliveryStatus.FAILED;
        }
        log.info("Dispatching email notification for event {} to {}", event.getEventId(), email);
        // Integration point for AWS SES / SMTP
        return NotificationRecord.DeliveryStatus.DELIVERED;
    }

    public NotificationRecord.DeliveryStatus routeToSms(NotificationEvent event, String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return NotificationRecord.DeliveryStatus.FAILED;
        }
        log.info("Dispatching SMS notification for event {} to {}", event.getEventId(), phoneNumber);
        // Integration point for Twilio
        return NotificationRecord.DeliveryStatus.DELIVERED;
    }
}
