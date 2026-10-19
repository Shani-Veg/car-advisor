package com.shani.caradvisor.observability;

import com.shani.caradvisor.observability.AssistantMetrics.Guard;
import com.shani.caradvisor.observability.AssistantMetrics.Path;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AssistantMetricsTest {

    @Test
    void turnsAreCountedByPathWithPercentilesToolsAndGuards() {
        AssistantMetrics metrics = new AssistantMetrics();
        for (int i = 1; i <= 100; i++) {
            metrics.turn(Path.MODEL, i * 10L, "case-" + i, "gemini", List.of("find_cars"), List.of());
        }
        metrics.turn(Path.CODE_CARS, 5, "c", null, List.of(), List.of());
        metrics.turn(Path.MODEL, 20, "c", "gemini", List.of("record_facts"), List.of(Guard.REPLY_REWRITTEN));

        AssistantMetrics.Snapshot snapshot = metrics.snapshot();

        assertThat(snapshot.turns()).isEqualTo(102);
        assertThat(snapshot.turnsByPath().get(Path.MODEL)).isEqualTo(101);
        assertThat(snapshot.turnLatencyMs().get(Path.MODEL).p50()).isEqualTo(500);
        assertThat(snapshot.turnLatencyMs().get(Path.MODEL).p95()).isEqualTo(950);
        assertThat(snapshot.turnLatencyMs().get(Path.MODEL).max()).isEqualTo(1000);
        assertThat(snapshot.answeredByCode()).isEqualTo(0.01);
        assertThat(snapshot.toolCalls()).containsEntry("find_cars", 100L).containsEntry("record_facts", 1L);
        assertThat(snapshot.guards().get(Guard.REPLY_REWRITTEN)).isEqualTo(1);
    }
}
