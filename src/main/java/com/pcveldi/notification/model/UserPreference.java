package com.pcveldi.notification.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreference {

    @Id
    private String userId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_enabled_channels")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<NotificationEvent.Channel> enabledChannels = new HashSet<>();

    private String email;
    private String phoneNumber;
    private String pushDeviceToken;

    private LocalTime quietHoursStart;
    private LocalTime quietHoursEnd;

    @Builder.Default
    private boolean quietHoursEnabled = false;

    private String timezone;
}
