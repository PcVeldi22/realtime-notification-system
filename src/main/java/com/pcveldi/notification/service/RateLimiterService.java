package com.pcveldi.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final int MAX_NOTIFICATIONS_PER_WINDOW = 20;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    /**
     * Sliding-window rate limit backed by Redis so the limit holds correctly
     * across multiple running instances of this service.
     */
    public boolean isAllowed(String userId) {
        String key = "ratelimit:notifications:" + userId;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, WINDOW);
        }
        return count == null || count <= MAX_NOTIFICATIONS_PER_WINDOW;
    }

    public long remaining(String userId) {
        String key = "ratelimit:notifications:" + userId;
        Object value = redisTemplate.opsForValue().get(key);
        long used = value == null ? 0 : Long.parseLong(value.toString());
        return Math.max(0, MAX_NOTIFICATIONS_PER_WINDOW - used);
    }
}
