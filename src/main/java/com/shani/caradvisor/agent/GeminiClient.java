package com.shani.caradvisor.agent;

import com.shani.caradvisor.observability.AssistantMetrics;
import com.shani.caradvisor.observability.AssistantMetrics.CallOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gemini over plain HTTPS. Tries the configured models in order: a busy model (429/5xx), a slow one or an
 * unreadable answer falls through to the next one. When every model was only busy, it waits (exponential backoff
 * with jitter, or the server's own short retry delay) and goes round again, a bounded number of times; a long
 * quota wait is not worth a chat user's time, so then it gives up at once and the app carries on without the model.
 * The API key comes from the environment and is never logged.
 */
@Component
@ConditionalOnProperty(name = "advisor.llm.mode", havingValue = "live", matchIfMissing = true)
public class GeminiClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);
    static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";
    /** Never wait longer than this between rounds: past it, the employee is better served by the form and the buttons. */
    static final long MAX_BACKOFF_MS = 4000;
    private static final Pattern RETRY_DELAY = Pattern.compile("\"retryDelay\"\\s*:\\s*\"(\\d+(?:\\.\\d+)?)s\"");

    private final String apiKey;
    private final List<String> models;
    private final Duration timeout;
    private final Duration firstTry;
    private final String endpoint;
    private final int busyRetries;
    private final long backoffMs;
    private final HttpClient http;
    private final TurnRecorder recorder;
    private final AssistantMetrics metrics;
    private final ObjectMapper json = new ObjectMapper();

    public GeminiClient(@Value("${advisor.gemini.api-key:}") String apiKey,
                        @Value("${advisor.gemini.models:gemini-3.5-flash-lite,gemini-flash-lite-latest}") String models,
                        @Value("${advisor.gemini.timeout-seconds:40}") int timeoutSeconds,
                        @Value("${advisor.gemini.first-try-seconds:8}") int firstTrySeconds,
                        @Value("${advisor.llm.record-file:}") String recordFile,
                        @Value("${advisor.gemini.endpoint:" + ENDPOINT + "}") String endpoint,
                        @Value("${advisor.gemini.busy-retries:2}") int busyRetries,
                        @Value("${advisor.gemini.backoff-ms:800}") long backoffMs,
                        AssistantMetrics metrics) {
        this.recorder = recordFile == null || recordFile.isBlank() ? null : new TurnRecorder(java.nio.file.Path.of(recordFile.trim()));
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.models = Arrays.stream(models.split(",")).map(String::trim).filter(m -> !m.isEmpty()).toList();
        this.timeout = Duration.ofSeconds(timeoutSeconds);
        this.firstTry = Duration.ofSeconds(Math.min(firstTrySeconds, timeoutSeconds));
        this.endpoint = endpoint;
        this.busyRetries = Math.max(0, busyRetries);
        this.backoffMs = Math.max(1, backoffMs);
        this.metrics = metrics;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    @Override
    public boolean isConfigured() {
        return !apiKey.isEmpty() && !models.isEmpty();
    }

    @Override
    public Turn next(String systemInstruction, List<Map<String, Object>> contents, List<Map<String, Object>> functions) {
        if (!isConfigured()) {
            throw new UnavailableException("GEMINI_API_KEY is not set");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("systemInstruction", Map.of("parts", List.of(Map.of("text", systemInstruction))));
        body.put("contents", contents);
        if (!functions.isEmpty()) {
            body.put("tools", List.of(Map.of("functionDeclarations", functions)));
        }
        Turn turn = call(json.writeValueAsString(body), true, "chat", GeminiClient::parse);
        if (recorder != null) {
            recorder.record(ConversationKey.of(contents), turn);
        }
        return turn;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> extract(String instruction, byte[] document, String mimeType, Map<String, Object> schema) {
        if (!isConfigured()) {
            throw new UnavailableException("GEMINI_API_KEY is not set");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        // text (the payroll lines of a PDF) goes as text; only an image the employee cropped goes as a file
        Map<String, Object> content = mimeType.startsWith("text/")
                ? Map.of("text", new String(document, StandardCharsets.UTF_8))
                : Map.of("inline_data", Map.of("mime_type", mimeType, "data", Base64.getEncoder().encodeToString(document)));
        body.put("contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", instruction), content))));
        body.put("generationConfig", Map.of("responseMimeType", "application/json", "responseSchema", schema,
                "temperature", 0));
        // Never log what was read: it is the employee's salary.
        Map<String, Object> fields = call(json.writeValueAsString(body), false, "document",
                (response, model) -> json.readValue(parse(response, model).text(), Map.class));
        if (recorder != null) {
            recorder.recordExtraction(DocumentHash.sha256(document), fields);
        }
        return fields;
    }

    /** What to do with a 200 answer; an exception means "unreadable", and the next model is tried. */
    @FunctionalInterface
    interface Reader<T> {
        T read(Map<String, Object> response, String model) throws Exception;
    }

    /**
     * One request: each model in order, then, if every one was only busy, a pause and another round.
     * A chat waits at most firstTry for a model that has a fallback after it.
     */
    @SuppressWarnings("unchecked")
    private <T> T call(String payload, boolean chat, String purpose, Reader<T> reader) {
        for (int round = 0; ; round++) {
            boolean onlyBusy = true;
            long serverDelayMs = 0;
            for (int i = 0; i < models.size(); i++) {
                String model = models.get(i);
                Duration wait = chat && i < models.size() - 1 ? firstTry : timeout;
                long started = System.currentTimeMillis();
                try {
                    HttpRequest request = HttpRequest.newBuilder(URI.create(String.format(endpoint, model)))
                            .timeout(wait)
                            .header("Content-Type", "application/json; charset=utf-8")
                            .header("x-goog-api-key", apiKey)
                            .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                            .build();
                    HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                    long took = System.currentTimeMillis() - started;
                    int status = response.statusCode();
                    if (status == 429 || status >= 500) {
                        metrics.modelCall(model, CallOutcome.BUSY, took, i > 0);
                        serverDelayMs = Math.max(serverDelayMs, retryDelayMs(response));
                        log.warn("Gemini model {} busy (HTTP {}) for {}, trying the next one", model, status, purpose);
                        continue;
                    }
                    if (status != 200) {
                        onlyBusy = false;
                        metrics.modelCall(model, CallOutcome.REJECTED, took, i > 0);
                        log.error("Gemini model {} rejected the {} request: HTTP {}", model, purpose, status);
                        continue;
                    }
                    T result;
                    try {
                        Map<String, Object> parsed = json.readValue(response.body(), Map.class);
                        result = reader.read(parsed, model);
                    } catch (Exception e) {
                        onlyBusy = false;
                        metrics.modelCall(model, CallOutcome.UNREADABLE, took, i > 0);
                        log.warn("Gemini model {} gave an unusable {} answer: {}", model, purpose, e.getClass().getSimpleName());
                        continue;
                    }
                    metrics.modelCall(model, CallOutcome.ANSWERED, took, i > 0);
                    log.info("Gemini {} answered the {} request in {} ms", model, purpose, took);
                    return result;
                } catch (java.net.http.HttpTimeoutException e) {
                    onlyBusy = false;
                    metrics.modelCall(model, CallOutcome.TIMEOUT, System.currentTimeMillis() - started, i > 0);
                    log.warn("Gemini model {} did not answer within {} s, trying the next one", model, wait.toSeconds());
                } catch (java.io.IOException e) {
                    onlyBusy = false;
                    metrics.modelCall(model, CallOutcome.UNREACHABLE, System.currentTimeMillis() - started, i > 0);
                    log.warn("Gemini model {} unreachable: {}", model, e.getClass().getSimpleName());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new UnavailableException("interrupted");
                }
            }
            // A timeout already cost the employee seconds, and a quota that frees up only later is not worth waiting for.
            if (!onlyBusy || round >= busyRetries || serverDelayMs > MAX_BACKOFF_MS) {
                throw new UnavailableException("No Gemini model answered");
            }
            long pause = backoff(round, serverDelayMs);
            metrics.backoff();
            log.warn("Every Gemini model is busy; trying again in {} ms (round {})", pause, round + 2);
            try {
                Thread.sleep(pause);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new UnavailableException("interrupted");
            }
        }
    }

    /** Exponential backoff with jitter (base, twice the base, ...), at least the server's own delay, at most MAX_BACKOFF_MS. */
    long backoff(int round, long serverDelayMs) {
        long base = backoffMs << Math.min(round, 10);
        long jittered = base + ThreadLocalRandom.current().nextLong(base / 4 + 1);
        return Math.min(Math.max(jittered, serverDelayMs), MAX_BACKOFF_MS);
    }

    /** Gemini says how long to wait in the error body ("retryDelay": "17s"), sometimes also in Retry-After. */
    static long retryDelayMs(HttpResponse<String> response) {
        long header = response.headers().firstValue("Retry-After").map(value -> {
            try {
                return Long.parseLong(value.trim()) * 1000;
            } catch (NumberFormatException e) {
                return 0L;
            }
        }).orElse(0L);
        Matcher matcher = RETRY_DELAY.matcher(response.body() == null ? "" : response.body());
        long body = matcher.find() ? Math.round(Double.parseDouble(matcher.group(1)) * 1000) : 0;
        return Math.max(header, body);
    }

    @SuppressWarnings("unchecked")
    private static Turn parse(Map<String, Object> response, String model) {
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        if (candidates == null || candidates.isEmpty() || candidates.get(0).get("content") == null) {
            throw new UnavailableException("Empty answer from " + model);
        }
        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.getOrDefault("parts", List.of());
        StringBuilder text = new StringBuilder();
        List<FunctionCall> calls = new ArrayList<>();
        for (Map<String, Object> part : parts) {
            if (part.get("functionCall") instanceof Map<?, ?> call) {
                Map<String, Object> args = call.get("args") instanceof Map<?, ?> a ? (Map<String, Object>) a : Map.of();
                calls.add(new FunctionCall((String) call.get("id"), (String) call.get("name"), args));
            } else if (part.get("text") instanceof String t && !Boolean.TRUE.equals(part.get("thought"))) {
                text.append(t);
            }
        }
        Map<String, Object> raw = new LinkedHashMap<>(content);
        raw.putIfAbsent("role", "model");
        return new Turn(raw, text.toString().trim(), calls, model);
    }
}
