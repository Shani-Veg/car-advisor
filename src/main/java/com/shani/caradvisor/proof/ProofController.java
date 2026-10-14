package com.shani.caradvisor.proof;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public on purpose: the proof uses only Strauss's sample data and leaves nothing behind. */
@RestController
public class ProofController {

    private final ProofService proof;

    public ProofController(ProofService proof) {
        this.proof = proof;
    }

    @GetMapping("/api/proof")
    public ProofReport run() {
        return proof.run();
    }
}
