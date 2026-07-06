package com.pcveldi.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimiterServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void allowsRequestWhenUnderLimit() {
        when(valueOperations.increment(anyString())).thenReturn(5L);

        boolean allowed = rateLimiterService.isAllowed("user-123");

        assertTrue(allowed);
    }

    @Test
    void blocksRequestWhenOverLimit() {
        when(valueOperations.increment(anyString())).thenReturn(25L);

        boolean allowed = rateLimiterService.isAllowed("user-123");

        assertFalse(allowed);
    }
}
