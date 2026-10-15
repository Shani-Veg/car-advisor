package com.shani.caradvisor.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Plays back model turns recorded from a real Gemini run (demo/recorded-turns.jsonl). Needs no network and no key,
 * so the demo survives a bad connection. Everything after the model is unchanged: the same tools run, the same
 * validations apply, and the server still writes every number. A sentence that was never recorded gets
 * "assistant unavailable", and the form keeps working. The API declares this mode (assistantMode = REPLAY).
 */
@Component
@ConditionalOnProperty(name = "advisor.llm.mode", havingValue = "replay")
public class ReplayLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(ReplayLlmClient.class);

    private final Map<ConversationKey.Key, Turn> turns = new HashMap<>();
    private final Map<String, Map<String, Object>> documents = new HashMap<>();

    public ReplayLlmClient(@Value("${advisor.llm.replay-file:demo/recorded-turns.jsonl}") String file) {
        Path path = Path.of(file);
        if (!Files.exists(path)) {
            log.error("Replay mode is on but {} does not exist", path.toAbsolutePath());
            return;
        }
        ObjectMapper json = new ObjectMapper();
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) {
                    load(json, line);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + path, e);
        }
        log.warn("REPLAY mode: {} recorded turns and {} recorded document readings from {}. The assistant does not call Gemini.",
                turns.size(), documents.size(), path.toAbsolutePath());
    }

    @SuppressWarnings("unchecked")
    private void load(ObjectMapper json, String line) {
        Map<String, Object> entry = json.readValue(line, Map.class);
        if ("document".equals(entry.get("kind"))) {
            documents.put((String) entry.get("sha256"), (Map<String, Object>) entry.get("fields"));
            return;
        }
        ConversationKey.Key key = new ConversationKey.Key((String) entry.get("employeeText"),
                ((Number) entry.get("step")).intValue());
        String text = entry.get("text") == null ? "" : (String) entry.get("text");
        List<Map<String, Object>> rawCalls = (List<Map<String, Object>>) entry.getOrDefault("calls", List.of());

        List<FunctionCall> calls = new ArrayList<>();
        List<Map<String, Object>> parts = new ArrayList<>();
        if (!text.isBlank()) {
            parts.add(Map.of("text", text));
        }
        for (Map<String, Object> call : rawCalls) {
            Map<String, Object> args = call.get("args") instanceof Map<?, ?> a ? (Map<String, Object>) a : Map.of();
            calls.add(new FunctionCall(null, (String) call.get("name"), args));
            parts.add(Map.of("functionCall", Map.of("name", call.get("name"), "args", args)));
        }
        // A later recording of the same sentence replaces an earlier one.
        turns.put(key, new Turn(Map.of("role", "model", "parts", parts), text, calls, "replay"));
    }

    @Override
    public boolean isConfigured() {
        return !turns.isEmpty();
    }

    @Override
    public String mode() {
        return "REPLAY";
    }

    /** Only a document that was read during the recording (same bytes) can be read again. */
    @Override
    public Map<String, Object> extract(String instruction, byte[] document, String mimeType, Map<String, Object> schema) {
        Map<String, Object> fields = documents.get(DocumentHash.sha256(document));
        if (fields == null) {
            throw new UnavailableException("No recorded reading for this document");
        }
        return fields;
    }

    @Override
    public Turn next(String systemInstruction, List<Map<String, Object>> contents, List<Map<String, Object>> functions) {
        ConversationKey.Key key = ConversationKey.of(contents);
        Turn turn = turns.get(key);
        if (turn == null) {
            throw new UnavailableException("No recorded turn for this sentence (step " + key.step() + ")");
        }
        return turn;
    }
}
