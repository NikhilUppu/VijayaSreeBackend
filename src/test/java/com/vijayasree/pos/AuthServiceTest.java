package com.vijayasree.pos;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthServiceTest extends BaseIntegrationTest {

    @Autowired ObjectMapper objectMapper;

    @Test
    @DisplayName("Login with correct credentials returns JWT token")
    void loginSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "ramamohan",
                                "password", "vst@2026"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.username").value("ramamohan"))
                .andExpect(jsonPath("$.data.role").value("Admin"));
    }

    @Test
    @DisplayName("Login with wrong password returns 400")
    void loginWrongPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "ramamohan",
                                "password", "wrongpassword"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login with non-existent user returns 404")
    void loginUserNotFound() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "nobody",
                                "password", "password123"
                        ))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Login with deactivated account returns 400")
    void loginDeactivatedUser() throws Exception {
        adminUser.setActive(false);
        userRepository.save(adminUser);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "ramamohan",
                                "password", "vst@2026"
                        ))))
                .andExpect(status().isBadRequest());
    }
}