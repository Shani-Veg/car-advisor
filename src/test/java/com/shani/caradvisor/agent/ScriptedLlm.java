package com.shani.caradvisor.agent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/** A model that plays back prepared turns and records what it was sent. Makes agent tests deterministic. */
class ScriptedLlm implements LlmClient {

    private final Deque<Object> script = new ArrayDeque<>();
    final List<List<Map<String, Object>>> received = new ArrayList<>();

    void play(Object... turns) {
        script.clear();
        received.clear();
        script.addAll(List.of(turns));
    }

    static Turn call(String name, Map<String, Object> args) {
        Map<String, Object> raw = Map.of("role", "model", "parts", List.of(Map.of("functionCall", Map.of("name", name, "args", args))));
        return new Turn(raw, "", List.of(new FunctionCall(null, name, args)), "scripted");
    }

    static Turn text(String text) {
        return new Turn(Map.of("role", "model", "parts", List.of(Map.of("text", text))), text, List.of(), "scripted");
    }

    /** What extract() returns; null = the model cannot read documents. */
    Map<String, Object> document;
    /** How many files reached the model, and what the last one was. */
    int documentsRead;
    byte[] lastDocument;
    String lastMimeType;

    @Override
    public Map<String, Object> extract(String instruction, byte[] file, String mimeType, Map<String, Object> schema) {
        documentsRead++;
        lastDocument = file;
        lastMimeType = mimeType;
        if (document == null) {
            throw new UnavailableException("no document scripted");
        }
        return document;
    }

    @Override
    public boolean isConfigured() {
        return true;
    }

    @Override
    public Turn next(String systemInstruction, List<Map<String, Object>> contents, List<Map<String, Object>> functions) {
        received.add(new ArrayList<>(contents));
        Object next = script.poll();
        if (next == null || next instanceof RuntimeException) {
            throw next == null ? new UnavailableException("script ended") : (RuntimeException) next;
        }
        return (Turn) next;
    }
}
