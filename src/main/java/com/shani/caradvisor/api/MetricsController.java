package com.shani.caradvisor.api;

import com.shani.caradvisor.observability.AssistantMetrics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The assistant's health for whoever runs it: live counters since start, and the latest runs of the evaluation
 * suite (evals/run_evals.py appends one line per run), so quality can be followed over time. Behind the rep login.
 */
@RestController
@RequestMapping("/api/rep/metrics")
public class MetricsController {

    private static final int EVAL_RUNS = 12;

    private final AssistantMetrics metrics;
    private final Path evalHistory;
    private final ObjectMapper json = new ObjectMapper();

    public MetricsController(AssistantMetrics metrics,
                             @Value("${advisor.evals.history-file:evals/history.jsonl}") String evalHistory) {
        this.metrics = metrics;
        this.evalHistory = Path.of(evalHistory);
    }

    public record MetricsView(AssistantMetrics.Snapshot live, List<Map<String, Object>> evalRuns) {
    }

    @GetMapping
    public MetricsView metrics() {
        return new MetricsView(metrics.snapshot(), evalRuns());
    }

    /** The latest runs, newest first. A missing or partly written file simply shows fewer runs. */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> evalRuns() {
        if (!Files.isRegularFile(evalHistory)) {
            return List.of();
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(evalHistory, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return List.of();
        }
        List<Map<String, Object>> runs = new ArrayList<>();
        for (int i = lines.size() - 1; i >= 0 && runs.size() < EVAL_RUNS; i--) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            try {
                runs.add(json.readValue(line, Map.class));
            } catch (RuntimeException e) {
                // a line being written right now, or edited by hand: skip it
            }
        }
        return runs;
    }
}
