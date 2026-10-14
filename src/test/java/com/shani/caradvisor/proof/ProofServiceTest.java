package com.shani.caradvisor.proof;

import com.shani.caradvisor.workflow.CaseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProofServiceTest {

    @Autowired
    private ProofService proof;

    @Autowired
    private CaseRepository cases;

    @Test
    void everyLiveCheckPasses() {
        ProofReport report = proof.run();

        String failures = report.sections().stream()
                .flatMap(s -> s.checks().stream())
                .filter(c -> !c.passed())
                .map(c -> c.name() + " -> expected " + c.expected() + ", got " + c.actual())
                .collect(Collectors.joining("\n"));
        assertThat(report.failed()).as(failures).isZero();
        assertThat(report.passed()).isGreaterThanOrEqualTo(25);
    }

    @Test
    void theProofLeavesNoDataBehind() {
        long before = cases.count();

        proof.run();

        assertThat(cases.count()).isEqualTo(before);
    }
}
