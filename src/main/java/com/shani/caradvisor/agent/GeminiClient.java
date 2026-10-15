package com.shani.caradvisor.agent;

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

/**
 * Gemini over plain HTTPS. Tries the configured models in order: a busy model (429/5xx/timeout)
 * falls through to the next one. The API key comes from the environment and is never logged.
 */
@Component
@ConditionalOnProperty(name = "advisor.llm.mode", havingValue = "live", matchIfMissing = true)
public class GeminiClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);
    private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    private final String apiKey;
    private final List<String> models;
    private final Duration timeout;
    private final HttpClient http;
    private final TurnRecorder recorder;
    private final ObjectMapper json = new ObjectMapper();

    public GeminiClient(@Value("${advisor.gemini.api-key:}") String apiKey,
                        @Value("${advisor.gemini.models:gemini-3.5-flash-lite,gemini-flash-lite-latest}") String models,
                        @Value("${advisor.gemini.timeout-seconds:40}") int timeoutSeconds,
                        @Value("${advisor.llm.record-file:}") String recordFile) {
        this.recorder = recordFile == null || recordFile.isBlank() ? null : new TurnRecorder(java.nio.file.Path.of(recordFile.trim()));
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.models = Arrays.stream(models.split(",")).map(String::trim).filter(m -> !m.isEmpty()).toList();
        this.timeout = Duration.ofSeconds(timeoutSeconds);
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    @Override
    public boolean isConfigured() {
        return !apiKey.isEmpty() && !models.isEmpty();
    }

    @Override
    @SuppressWarnings("unchecked")
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
        String payload = json.writeValueAsString(body);

        for (String model : models) {
            long started = System.currentTimeMillis();
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(String.format(ENDPOINT, model)))
                        .timeout(timeout)
                        .header("Content-Type", "application/json; charset=utf-8")
                        .header("x-goog-api-key", apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                        .build();
                HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                int status = response.statusCode();
                if (status == 429 || status >= 500) {
                    log.warn("Gemini model {} busy (HTTP {}), trying the next one", model, status);
                    continue;
                }
                if (status != 200) {
                    log.error("Gemini model {} rejected the request: HTTP {}", model, status);
                    continue;
                }
                Map<String, Object> parsed = json.readValue(response.body(), Map.class);
                Turn turn = parse(parsed, model);
                if (recorder != null) {
                    recorder.record(ConversationKey.of(contents), turn);
                }
                log.info("Gemini {} answered in {} ms ({} tool calls)", model, System.currentTimeMillis() - started,
                        turn.calls().size());
                return turn;
            } catch (java.io.IOException e) {
                log.warn("Gemini model {} unreachable: {}", model, e.getClass().getSimpleName());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new UnavailableException("interrupted");
            }
        }
        throw new UnavailableException("No Gemini model answered");
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> extract(String instruction, byte[] document, String mimeType, Map<String, Object> schema) {
        if (!isConfigured()) {
            throw new UnavailableException("GEMINI_API_KEY is not set");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("contents", List.of(Map.of("role", "user", "parts", List.of(
                Map.of("text", instruction),
                Map.of("inline_data", Map.of("mime_type", mimeType, "data", Base64.getEncoder().encodeToString(document)))))));
        body.put("generationConfig", Map.of("responseMimeType", "application/json", "responseSchema", schema,
                "temperature", 0));
        String payload = json.writeValueAsString(body);
        for (String model : models) {
            long started = System.currentTimeMillis();
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(String.format(ENDPOINT, model)))
                        .timeout(timeout)
                        .header("Content-Type", "application/json; charset=utf-8")
                        .header("x-goog-api-key", apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                        .build();
                HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                if (response.statusCode() != 200) {
                    log.warn("Gemini model {} could not read the document (HTTP {})", model, response.statusCode());
                    continue;
                }
                String text = parse(json.readValue(response.body(), Map.class), model).text();
                Map<String, Object> fields = json.readValue(text, Map.class);
                if (recorder != null) {
                    recorder.recordExtraction(DocumentHash.sha256(document), fields);
                }
                // Never log what was read: it is the employee's salary.
                log.info("Gemini {} read a {} document in {} ms", model, mimeType, System.currentTimeMillis() - started);
                return fields;
            } catch (java.io.IOException | tools.jackson.core.JacksonException e) {
                log.warn("Gemini model {} could not read the document: {}", model, e.getClass().getSimpleName());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new UnavailableException("interrupted");
            }
        }
        throw new UnavailableException("No Gemini model read the document");
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
