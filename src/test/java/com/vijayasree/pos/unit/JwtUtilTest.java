package com.vijayasree.pos.unit;

import com.vijayasree.pos.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    // Must be at least 256 bits (32 chars) for HS384
    private static final String TEST_SECRET =
            "testSecretKeyForJUnitTestsOnlyNotForProduction123456";
    private static final long EXPIRATION_MS = 86400000L; // 24 hours

    @BeforeEach
    void setup() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION_MS);
    }

    @Test
    @DisplayName("Generate token — extractUsername returns correct username")
    void generateToken_extractUsername_returnsCorrectUsername() {
        String token = jwtUtil.generateToken(
                "ramamohan", "Admin", "PRODUCT_VIEW,SALES_CHECKOUT");

        assertThat(jwtUtil.extractUsername(token)).isEqualTo("ramamohan");
    }

    @Test
    @DisplayName("Generate token — extractRole returns correct role")
    void generateToken_extractRole_returnsCorrectRole() {
        String token = jwtUtil.generateToken(
                "ramamohan", "Admin", "PRODUCT_VIEW");

        assertThat(jwtUtil.extractRole(token)).isEqualTo("Admin");
    }

    @Test
    @DisplayName("Generate token — extractPermissions returns correct list")
    void generateToken_extractPermissions_returnsCorrectList() {
        String token = jwtUtil.generateToken(
                "ramamohan", "Admin", "PRODUCT_VIEW,SALES_CHECKOUT,STOCK_ADJUST");

        List<String> permissions = jwtUtil.extractPermissions(token);

        assertThat(permissions).containsExactly(
                "PRODUCT_VIEW", "SALES_CHECKOUT", "STOCK_ADJUST");
    }

    @Test
    @DisplayName("Valid token — isTokenValid returns true")
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtUtil.generateToken(
                "ramamohan", "Admin", "PRODUCT_VIEW");

        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("Tampered token — isTokenValid returns false")
    void isTokenValid_tamperedToken_returnsFalse() {
        String token = jwtUtil.generateToken(
                "ramamohan", "Admin", "PRODUCT_VIEW");

        String tampered = token + "tampered";

        assertThat(jwtUtil.isTokenValid(tampered)).isFalse();
    }

    @Test
    @DisplayName("Expired token — isTokenValid returns false")
    void isTokenValid_expiredToken_returnsFalse() {
        // Set expiration to -1ms so token is already expired when created
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1L);

        String token = jwtUtil.generateToken(
                "ramamohan", "Admin", "PRODUCT_VIEW");

        assertThat(jwtUtil.isTokenValid(token)).isFalse();
    }

    @Test
    @DisplayName("Empty permissions string — extractPermissions returns empty list")
    void extractPermissions_emptyString_returnsEmptyList() {
        String token = jwtUtil.generateToken("ramamohan", "Admin", "");

        List<String> permissions = jwtUtil.extractPermissions(token);

        assertThat(permissions).isEmpty();
    }

    @Test
    @DisplayName("Completely invalid token string — isTokenValid returns false")
    void isTokenValid_randomString_returnsFalse() {
        assertThat(jwtUtil.isTokenValid("not.a.jwt")).isFalse();
    }
}