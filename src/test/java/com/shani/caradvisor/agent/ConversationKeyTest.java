package com.shani.caradvisor.agent;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationKeyTest {

    private static Map<String, Object> user(String text) {
        return Map.of("role", "user", "parts", List.of(Map.of("text", text)));
    }

    private static Map<String, Object> model(String text) {
        return Map.of("role", "model", "parts", List.of(Map.of("text", text)));
    }

    private static Map<String, Object> toolResult() {
        return Map.of("role", "user", "parts", List.of(Map.of("functionResponse", Map.of("name", "record_facts"))));
    }

    @Test
    void theFirstModelCallOfATurnIsStepZero() {
        var key = ConversationKey.of(List.of(model("שלום"), user("שכר 18,500")));

        assertThat(key.employeeText()).isEqualTo("שכר 18,500");
        assertThat(key.step()).isZero();
    }

    @Test
    void toolRoundsCountAsLaterStepsOfTheSameMessage() {
        var contents = List.of(model("שלום"), user("שכר 18,500"), model("(קריאה לכלי)"), toolResult());

        var key = ConversationKey.of(contents);

        assertThat(key.employeeText()).isEqualTo("שכר 18,500");
        assertThat(key.step()).isEqualTo(1);
    }

    @Test
    void systemNotesMergedInFrontOfTheSentenceAreIgnored() {
        var plain = ConversationKey.of(List.of(user("מה הכי משתלם לי?")));
        var merged = ConversationKey.of(List.of(user("[עדכון מהמערכת] נציג התחבורה עדכן: השתתפות 350 ₪.\nמה הכי משתלם לי?")));

        assertThat(merged).isEqualTo(plain);
    }

    @Test
    void theAgentsOwnRewriteRequestDoesNotReplaceTheEmployeesSentence() {
        var contents = List.of(model("שלום"), user("מה הכי משתלם לי?"), model("(קריאה לכלי)"), toolResult(),
                model("הנטו יעלה ב-661"), user("[system] Your last reply contained numbers. Rewrite it."));

        var key = ConversationKey.of(contents);

        assertThat(key.employeeText()).isEqualTo("מה הכי משתלם לי?");
        assertThat(key.step()).isEqualTo(2);
    }

    @Test
    void whitespaceDifferencesDoNotMatter() {
        assertThat(ConversationKey.normalize("  מה   הכי\nמשתלם  לי?  ")).isEqualTo("מה הכי משתלם לי?");
    }
}
