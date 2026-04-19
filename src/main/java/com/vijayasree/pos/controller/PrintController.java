package com.vijayasree.pos.controller;

import com.vijayasree.pos.service.PrintSseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Handles the SSE stream that the cashier's Print Station subscribes to.
 * The browser's EventSource API cannot send custom headers, so the JWT
 * is passed as a ?token= query parameter instead and picked up by JwtAuthFilter.
 */
@RestController
@RequestMapping("/api/print")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PrintController {

    private final PrintSseService printSseService;

    /**
     * The Print Station connects here on page load and stays connected.
     * Every new checkout broadcasts a "new-sale" event through this stream.
     * Usage: GET /api/print/events?token=<jwt>
     */
    @PreAuthorize("hasAuthority('PRINT_STATION')")
    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToEvents() {
        return printSseService.subscribe();
    }
}