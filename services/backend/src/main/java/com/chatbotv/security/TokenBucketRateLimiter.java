package com.chatbotv.security;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenBucketRateLimiter {
    private final int capacity;
    private final double refillPerSecond;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public TokenBucketRateLimiter(int capacity, double refillPerSecond) {
        this.capacity = capacity;
        this.refillPerSecond = refillPerSecond;
    }

    public boolean allow(String key) {
        Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket(capacity, Instant.now().toEpochMilli()));
        synchronized (bucket) {
            long now = Instant.now().toEpochMilli();
            double delta = (now - bucket.lastRefillMs) / 1000.0;
            bucket.tokens = Math.min(capacity, bucket.tokens + delta * refillPerSecond);
            bucket.lastRefillMs = now;
            if (bucket.tokens >= 1.0) {
                bucket.tokens -= 1.0;
                return true;
            }
            return false;
        }
    }

    private static class Bucket {
        double tokens;
        long lastRefillMs;

        Bucket(double tokens, long lastRefillMs) {
            this.tokens = tokens;
            this.lastRefillMs = lastRefillMs;
        }
    }
}
