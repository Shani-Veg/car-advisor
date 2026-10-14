package com.shani.caradvisor.agent;

import java.util.List;
import java.util.Map;

/**
 * Identifies one model call inside a chat turn: the employee's message that started the turn, and how many
 * model answers (tool-call rounds) came after it. Recording and replay use the same key, so a recorded turn
 * is found again when the same sentence is typed at the same point of the conversation.
 */
final class ConversationKey {

    private static final String SYSTEM_PREFIX = "[עדכון מהמערכת]";

    record Key(String employeeText, int step) {
    }

    private ConversationKey() {
    }

    @SuppressWarnings("unchecked")
    static Key of(List<Map<String, Object>> contents) {
        int textIndex = -1;
        String text = "";
        for (int i = contents.size() - 1; i >= 0; i--) {
            Map<String, Object> content = contents.get(i);
            if (!"user".equals(content.get("role"))) {
                continue;
            }
            String found = textOf((List<Map<String, Object>>) content.get("parts"));
            // "[system] ..." is the agent's own rewrite request, not something the employee typed.
            if (found != null && !found.startsWith("[system]")) {
                textIndex = i;
                text = found;
                break;
            }
        }
        int step = 0;
        for (int i = textIndex + 1; i < contents.size(); i++) {
            if ("model".equals(contents.get(i).get("role"))) {
                step++;
            }
        }
        return new Key(normalize(text), step);
    }

    private static String textOf(List<Map<String, Object>> parts) {
        if (parts == null) {
            return null;
        }
        StringBuilder text = new StringBuilder();
        for (Map<String, Object> part : parts) {
            if (part.get("text") instanceof String s) {
                text.append(s);
            }
        }
        return text.length() == 0 ? null : text.toString();
    }

    /**
     * The chat history merges neighbouring messages of one role, so system notes ("the rep updated...") can sit
     * in front of the employee's sentence. They are not part of what was typed.
     */
    static String normalize(String text) {
        StringBuilder kept = new StringBuilder();
        for (String line : text.split("\n")) {
            if (!line.startsWith(SYSTEM_PREFIX)) {
                kept.append(line).append(' ');
            }
        }
        return kept.toString().replaceAll("\\s+", " ").trim();
    }
}
