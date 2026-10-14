package com.shani.caradvisor.agent;

import java.util.List;
import java.util.Map;

/**
 * One model turn. History uses Gemini's content format (role + parts) as plain maps, and the model's own
 * turn is kept raw so provider data such as Gemini 3 thought signatures goes back unchanged.
 * Tests and the demo fallback plug in a scripted implementation.
 */
public interface LlmClient {

    boolean isConfigured();

    /** LIVE = a real model answers; REPLAY = recorded turns are played back (declared to the client). */
    default String mode() {
        return "LIVE";
    }

    Turn next(String systemInstruction, List<Map<String, Object>> contents, List<Map<String, Object>> functions);

    record FunctionCall(String id, String name, Map<String, Object> args) {
    }

    /** rawContent is appended to the history as is; text is the joined text parts. */
    record Turn(Map<String, Object> rawContent, String text, List<FunctionCall> calls, String model) {
    }

    class UnavailableException extends RuntimeException {
        public UnavailableException(String message) {
            super(message);
        }
    }
}
