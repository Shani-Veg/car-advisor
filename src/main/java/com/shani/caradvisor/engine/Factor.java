package com.shani.caradvisor.engine;

import java.math.BigDecimal;

/**
 * One line of the "why": a component that changed (from/to, monthly) and its effect on the net.
 * The netImpact of all factors adds up exactly to the comparison's difference.
 */
public record Factor(MonthlyBenefits.Component component, BigDecimal from, BigDecimal to, BigDecimal netImpact) {

    public String label() {
        return component.hebrew();
    }
}
