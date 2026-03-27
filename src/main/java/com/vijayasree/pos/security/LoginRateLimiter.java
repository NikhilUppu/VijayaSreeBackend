package com.vijayasree.pos.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 60_000; // 1 minute

    private final Map<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

    public boolean isAllowed(String username) {
        String key = username.toLowerCase();
        AttemptInfo info = attempts.getOrDefault(key, new AttemptInfo());

        long now = Instant.now().toEpochMilli();

        // Reset window if expired
        if (now - info.windowStart > WINDOW_MS) {
            info = new AttemptInfo();
            info.windowStart = now;
        }

        if (info.count >= MAX_ATTEMPTS) {
            long waitSeconds = (WINDOW_MS - (now - info.windowStart)) / 1000;
            throw new IllegalArgumentException(
                    "Too many login attempts. Please wait " + waitSeconds + " seconds.");
        }

        info.count++;
        attempts.put(key, info);
        return true;
    }

    public void resetAttempts(String username) {
        attempts.remove(username.toLowerCase());
    }

    private static class AttemptInfo {
        int count = 0;
        long windowStart = Instant.now().toEpochMilli();
    }
}