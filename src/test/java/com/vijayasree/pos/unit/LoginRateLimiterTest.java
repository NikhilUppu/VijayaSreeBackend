package com.vijayasree.pos.unit;

import com.vijayasree.pos.security.LoginRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class LoginRateLimiterTest {

    private LoginRateLimiter rateLimiter;

    @BeforeEach
    void setup() {
        rateLimiter = new LoginRateLimiter();
    }

    @Test
    @DisplayName("First attempt — allowed through")
    void firstAttempt_isAllowed() {
        assertThatNoException().isThrownBy(
                () -> rateLimiter.isAllowed("ramamohan"));
    }

    @Test
    @DisplayName("First 5 attempts — all allowed")
    void fiveAttempts_allAllowed() {
        assertThatNoException().isThrownBy(() -> {
            for (int i = 0; i < 5; i++) {
                rateLimiter.isAllowed("ramamohan");
            }
        });
    }

    @Test
    @DisplayName("6th attempt — throws IllegalArgumentException with wait message")
    void sixthAttempt_throwsWithWaitMessage() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.isAllowed("ramamohan");
        }

        assertThatThrownBy(() -> rateLimiter.isAllowed("ramamohan"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Too many login attempts");
    }

    @Test
    @DisplayName("Different users tracked independently — userB not affected by userA")
    void differentUsers_trackedIndependently() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.isAllowed("userA");
        }

        // userB has 0 attempts — should still be allowed
        assertThatNoException().isThrownBy(
                () -> rateLimiter.isAllowed("userB"));
    }

    @Test
    @DisplayName("Reset attempts — user can login again after reset")
    void resetAttempts_clearsCounter() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.isAllowed("ramamohan");
        }

        // Reset (called after successful login)
        rateLimiter.resetAttempts("ramamohan");

        // Should be allowed again from scratch
        assertThatNoException().isThrownBy(
                () -> rateLimiter.isAllowed("ramamohan"));
    }

    @Test
    @DisplayName("Username check is case-insensitive")
    void usernameCheck_caseInsensitive() {
        // Fill up attempts using uppercase
        for (int i = 0; i < 5; i++) {
            rateLimiter.isAllowed("RAMAMOHAN");
        }

        // 6th attempt with lowercase should still be blocked
        // because internally it lowercases the key
        assertThatThrownBy(() -> rateLimiter.isAllowed("ramamohan"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Too many login attempts");
    }

    @Test
    @DisplayName("Reset is case-insensitive — uppercase reset clears lowercase attempts")
    void reset_caseInsensitive() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.isAllowed("ramamohan");
        }

        // Reset using uppercase — should still clear because internally lowercased
        rateLimiter.resetAttempts("RAMAMOHAN");

        assertThatNoException().isThrownBy(
                () -> rateLimiter.isAllowed("ramamohan"));
    }
}