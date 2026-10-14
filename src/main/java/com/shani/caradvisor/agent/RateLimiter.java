package com.shani.caradvisor.agent;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Sliding one-minute window, per case and overall. Protects the model quota (and its cost) from one
 * runaway client. In memory: good for a single server, a pilot behind several servers would use a shared store.
 */
final class RateLimiter {

    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final int perCase;
    private final int overall;
    private final Map<String, Deque<Instant>> byCase = new HashMap<>();
    private final Deque<Instant> all = new ArrayDeque<>();

    RateLimiter(int perCase, int overall) {
        this.perCase = perCase;
        this.overall = overall;
    }

    synchronized boolean tryAcquire(String caseId, Instant now) {
        Instant cutoff = now.minus(WINDOW);
        Deque<Instant> mine = byCase.computeIfAbsent(caseId, id -> new ArrayDeque<>());
        prune(mine, cutoff);
        prune(all, cutoff);
        if (mine.size() >= perCase || all.size() >= overall) {
            return false;
        }
        mine.addLast(now);
        all.addLast(now);
        return true;
    }

    private static void prune(Deque<Instant> times, Instant cutoff) {
        while (!times.isEmpty() && times.peekFirst().isBefore(cutoff)) {
            times.pollFirst();
        }
    }
}
