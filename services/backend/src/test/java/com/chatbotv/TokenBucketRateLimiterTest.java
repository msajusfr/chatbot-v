package com.chatbotv;

import com.chatbotv.security.TokenBucketRateLimiter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenBucketRateLimiterTest {
    @Test
    void shouldThrottleWhenBucketEmpty() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 0);
        assertTrue(limiter.allow("127.0.0.1"));
        assertFalse(limiter.allow("127.0.0.1"));
    }
}
