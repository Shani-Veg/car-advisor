package com.shani.caradvisor.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Appends every real model turn to a JSON-lines file (one object per line), so a good run can be played back
 * later without Gemini. Only enabled on purpose: the file contains what the employee typed.
 */
final class TurnRecorder {

    private static final Logger log = LoggerFactory.getLogger(TurnRecorder.class);

    private final Path file;
    private final ObjectMapper json = new ObjectMapper();

    TurnRecorder(Path file) {
        this.file = file.toAbsolutePath();
        log.warn("Recording model turns to {} (messages typed by the employee are written there)", this.file);
    }

    synchronized void record(ConversationKey.Key key, LlmClient.Turn turn) {
        Map<String, Object> line = new LinkedHashMap<>();
        line.put("employeeText", key.employeeText());
        line.put("step", key.step());
        line.put("model", turn.model());
        line.put("text", turn.text());
        line.put("calls", turn.calls().stream()
                .map(call -> Map.of("name", call.name(), "args", call.args()))
                .toList());
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, json.writeValueAsString(line) + "\n", StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.warn("Could not record the turn: {}", e.getMessage());
        }
    }
}
