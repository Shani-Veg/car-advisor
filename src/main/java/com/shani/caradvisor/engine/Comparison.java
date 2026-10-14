package com.shani.caradvisor.engine;

import java.math.BigDecimal;
import java.util.List;

/**
 * Today vs. one alternative. Net amounts are whole shekels.
 * When the alternative is not allowed, alternative/netAlternative/difference are null:
 * no number is shown for an option the employee cannot take.
 */
public record Comparison(
        OptionResult today,
        Eligibility eligibility,
        OptionResult alternative,
        BigDecimal netToday,
        BigDecimal netAlternative,
        BigDecimal difference,
        List<Factor> factors,
        String rulesVersion) {

    public boolean eligible() {
        return eligibility.eligible();
    }
}
