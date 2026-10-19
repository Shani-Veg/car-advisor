package com.shani.caradvisor.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * What the assistant is doing, with nothing personal in it: how each chat turn was answered (by code or by the
 * model) and how long it took, how the model calls went (answered, busy, timed out, a fallback model), and how
 * often a guard had to step in. Kept in memory since the server started; every chat turn is also one log line.
 * Message texts, amounts and names are never recorded.
 */
@Component
public class AssistantMetrics {

    private static final Logger log = LoggerFactory.getLogger(AssistantMetrics.class);
    private static final int WINDOW = 500;

    /** How a chat message was answered. */
    public enum Path { CODE_ANSWER, CODE_CARS, MODEL, UNAVAILABLE }

    /** How one call to one model ended. */
    public enum CallOutcome { ANSWERED, BUSY, TIMEOUT, REJECTED, UNREACHABLE, UNREADABLE }

    /** A guard that changed what would otherwise have happened. */
    public enum Guard {
        /** the reply had an amount, a foreign letter or "the screen": the model was asked to rewrite it */
        REPLY_REWRITTEN,
        /** the rewrite was still not acceptable: the server's own text was used */
        REPLY_REPLACED,
        /** "here are the options" with no car shown: the model was sent back to look them up */
        CLAIMED_CARS,
        /** an amount the employee never wrote was not saved */
        AMOUNT_NOT_IN_MESSAGE,
        /** a detail the rules refuse (for example a higher group than the occupation allows) */
        VALUE_REJECTED,
        /** too many messages in a minute */
        RATE_LIMITED,
        /** the model kept calling tools without a reply */
        STEP_LIMIT
    }

    public record Latency(long count, long p50, long p95, long max) {
    }

    public record Snapshot(Instant since, long minutesUp, long turns, double answeredByCode,
                           Map<Path, Long> turnsByPath, Map<Path, Latency> turnLatencyMs,
                           Map<String, Map<CallOutcome, Long>> modelCalls, Latency modelCallMs,
                           long fallbackAnswers, long backoffRounds, Map<Guard, Long> guards,
                           Map<String, Long> toolCalls, Map<String, Long> payslipReads) {
    }

    private final Instant since = Instant.now();
    private final Map<Path, AtomicLong> turns = counters(Path.class);
    private final Map<Path, Window> turnLatency = new EnumMap<>(Path.class);
    private final Map<String, Map<CallOutcome, AtomicLong>> calls = new ConcurrentHashMap<>();
    private final Window callLatency = new Window();
    private final AtomicLong fallbacks = new AtomicLong();
    private final AtomicLong backoffs = new AtomicLong();
    private final Map<Guard, AtomicLong> guards = counters(Guard.class);
    private final Map<String, AtomicLong> tools = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> payslips = new ConcurrentHashMap<>();

    public AssistantMetrics() {
        for (Path path : Path.values()) {
            turnLatency.put(path, new Window());
        }
    }

    /** One chat message, answered. The log line carries a short case reference for tracing, never the text. */
    public void turn(Path path, long millis, String caseId, String model, List<String> toolNames, List<Guard> raised) {
        turns.get(path).incrementAndGet();
        turnLatency.get(path).add(millis);
        toolNames.forEach(this::tool);
        raised.forEach(this::guard);
        log.info("turn case={} path={} ms={} model={} tools={} guards={}", shortId(caseId), path, millis,
                model == null ? "-" : model, toolNames.isEmpty() ? "-" : String.join(",", toolNames),
                raised.isEmpty() ? "-" : raised);
    }

    public void modelCall(String model, CallOutcome outcome, long millis, boolean fallback) {
        calls.computeIfAbsent(model, m -> counters(CallOutcome.class)).get(outcome).incrementAndGet();
        callLatency.add(millis);
        if (outcome == CallOutcome.ANSWERED && fallback) {
            fallbacks.incrementAndGet();
        }
    }

    /** Every model was busy, so the client waited and went round again. */
    public void backoff() {
        backoffs.incrementAndGet();
    }

    public void guard(Guard guard) {
        guards.get(guard).incrementAndGet();
    }

    public void tool(String name) {
        tools.computeIfAbsent(name, n -> new AtomicLong()).incrementAndGet();
    }

    public void payslip(String outcome) {
        payslips.computeIfAbsent(outcome, o -> new AtomicLong()).incrementAndGet();
    }

    public Snapshot snapshot() {
        Map<Path, Long> byPath = values(turns);
        long total = byPath.values().stream().mapToLong(Long::longValue).sum();
        long byCode = byPath.get(Path.CODE_ANSWER) + byPath.get(Path.CODE_CARS);
        Map<Path, Latency> latency = new EnumMap<>(Path.class);
        turnLatency.forEach((path, window) -> latency.put(path, window.latency()));
        Map<String, Map<CallOutcome, Long>> byModel = new TreeMap<>();
        calls.forEach((model, outcomes) -> byModel.put(model, values(outcomes)));
        return new Snapshot(since, Duration.between(since, Instant.now()).toMinutes(), total,
                total == 0 ? 0 : Math.round(1000.0 * byCode / total) / 1000.0, byPath, latency, byModel,
                callLatency.latency(), fallbacks.get(), backoffs.get(), values(guards), sorted(tools), sorted(payslips));
    }

    private static String shortId(String caseId) {
        return caseId == null ? "-" : caseId.substring(0, Math.min(6, caseId.length()));
    }

    private static <E extends Enum<E>> Map<E, AtomicLong> counters(Class<E> type) {
        Map<E, AtomicLong> map = new EnumMap<>(type);
        for (E value : type.getEnumConstants()) {
            map.put(value, new AtomicLong());
        }
        return map;
    }

    private static <E extends Enum<E>> Map<E, Long> values(Map<E, AtomicLong> counters) {
        Map<E, Long> map = new LinkedHashMap<>();
        counters.forEach((key, value) -> map.put(key, value.get()));
        return map;
    }

    private static Map<String, Long> sorted(Map<String, AtomicLong> counters) {
        Map<String, Long> map = new TreeMap<>();
        counters.forEach((key, value) -> map.put(key, value.get()));
        return map;
    }

    /** The last WINDOW durations, for percentiles that follow the recent behaviour. */
    private static final class Window {
        private final long[] values = new long[WINDOW];
        private long count;

        synchronized void add(long millis) {
            values[(int) (count % WINDOW)] = millis;
            count++;
        }

        synchronized Latency latency() {
            int size = (int) Math.min(count, WINDOW);
            if (size == 0) {
                return new Latency(0, 0, 0, 0);
            }
            long[] sorted = Arrays.copyOf(values, size);
            Arrays.sort(sorted);
            return new Latency(count, percentile(sorted, 50), percentile(sorted, 95), sorted[size - 1]);
        }

        private static long percentile(long[] sorted, int p) {
            int index = (int) Math.ceil(p / 100.0 * sorted.length) - 1;
            return sorted[Math.max(0, Math.min(index, sorted.length - 1))];
        }
    }
}
