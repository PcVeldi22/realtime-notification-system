package com.pcveldi.notification.service;

import com.pcveldi.notification.model.NotificationEvent;
import com.pcveldi.notification.model.NotificationRecord;
import com.pcveldi.notification.model.UserPreference;
import com.pcveldi.notification.repository.NotificationRepository;
import com.pcveldi.notification.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatchService {

    private final ChannelRouterService channelRouterService;
    private final RateLimiterService rateLimiterService;
    private final NotificationRepository notificationRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    @KafkaListener(topics = "notification.events", groupId = "notification-dispatch-group")
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("Received notification event {} for user {}", event.getEventId(), event.getUserId());

        if (!rateLimiterService.isAllowed(event.getUserId())) {
            log.warn("Rate limit exceeded for user {}, dropping event {}", event.getUserId(), event.getEventId());
            persistSkipped(event, NotificationRecord.DeliveryStatus.RATE_LIMITED);
            return;
        }

        Optional<UserPreference> preferenceOpt = userPreferenceRepository.findById(event.getUserId());
        UserPreference preference = preferenceOpt.orElseGet(() ->
                UserPreference.builder().userId(event.getUserId()).build());

        if (isWithinQuietHours(preference) && event.getPriority() != NotificationEvent.Priority.CRITICAL) {
            log.info("Skipping event {} for user {} due to quiet hours", event.getEventId(), event.getUserId());
            persistSkipped(event, NotificationRecord.DeliveryStatus.SKIPPED_QUIET_HOURS);
            return;
        }

        event.getChannels().forEach(channel -> dispatchToChannel(event, channel, preference));
    }

    private void dispatchToChannel(NotificationEvent event, NotificationEvent.Channel channel,
                                    UserPreference preference) {
        NotificationRecord.DeliveryStatus status = switch (channel) {
            case WEBSOCKET -> channelRouterService.routeToWebsocket(event);
            case PUSH -> channelRouterService.routeToPush(event, preference.getPushDeviceToken());
            case EMAIL -> channelRouterService.routeToEmail(event, preference.getEmail());
            case SMS -> channelRouterService.routeToSms(event, preference.getPhoneNumber());
        };

        NotificationRecord record = NotificationRecord.builder()
                .eventId(event.getEventId() + ":" + channel)
                .userId(event.getUserId())
                .title(event.getTitle())
                .body(event.getBody())
                .channel(channel)
                .status(status)
                .deliveredAt(status == NotificationRecord.DeliveryStatus.DELIVERED ? LocalDateTime.now() : null)
                .build();

        notificationRepository.save(record);
    }

    private void persistSkipped(NotificationEvent event, NotificationRecord.DeliveryStatus status) {
        NotificationRecord record = NotificationRecord.builder()
                .eventId(event.getEventId())
                .userId(event.getUserId())
                .title(event.getTitle())
                .body(event.getBody())
                .status(status)
                .build();
        notificationRepository.save(record);
    }

    private boolean isWithinQuietHours(UserPreference preference) {
        if (!preference.isQuietHoursEnabled()
                || preference.getQuietHoursStart() == null
                || preference.getQuietHoursEnd() == null) {
            return false;
        }
        LocalTime now = LocalTime.now();
        return now.isAfter(preference.getQuietHoursStart()) && now.isBefore(preference.getQuietHoursEnd());
    }
}
