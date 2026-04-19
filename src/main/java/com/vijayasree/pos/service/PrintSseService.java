package com.vijayasree.pos.service;

import com.vijayasree.pos.dto.response.SaleResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages SSE connections from the Print Station (cashier's iPad).
 * When a sale completes, broadcast() pushes the receipt data to all
 * connected print stations so they can queue and print the bill.
 */
@Service
@Slf4j
public class PrintSseService {

    // Thread-safe list — multiple iPads could be connected simultaneously
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /**
     * Called when the Print Station page opens — creates a persistent SSE connection.
     * Returns an emitter that stays open until the client disconnects or times out.
     */
    public SseEmitter subscribe() {
        // 0L = no timeout — connection stays alive until client closes it
        SseEmitter emitter = new SseEmitter(0L);

        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        // Send a confirmation event so the frontend knows it's connected
        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException e) {
            emitters.remove(emitter);
        }

        log.info("Print Station connected. Total connected: {}", emitters.size());
        return emitter;
    }

    /**
     * Called by SaleController after a checkout completes.
     * Pushes the new sale to all connected Print Station tabs.
     */
    public void broadcast(SaleResponse sale) {
        if (emitters.isEmpty()) return;

        List<SseEmitter> dead = new ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("new-sale")
                        .data(sale, MediaType.APPLICATION_JSON));
            } catch (Exception e) {
                // Connection dropped — mark for removal
                dead.add(emitter);
            }
        }

        emitters.removeAll(dead);
        log.info("Broadcasted sale {} to {} print station(s)", sale.getReceiptNo(), emitters.size());
    }

    public int getConnectedCount() {
        return emitters.size();
    }
}