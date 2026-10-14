package com.shani.caradvisor.proof;

import java.time.Instant;
import java.util.List;

/** The result of running every live check, as the proof page shows it. */
public record ProofReport(Instant generatedAt, String rulesVersion, int passed, int failed, List<Section> sections) {

    public record Section(String title, String description, List<Check> checks) {
    }

    public record Check(String name, String expected, String actual, boolean passed) {
    }
}
