package com.shani.caradvisor.workflow;

import com.shani.caradvisor.engine.Comparison;
import com.shani.caradvisor.engine.Factor;
import com.shani.caradvisor.engine.Money;

import java.util.Comparator;

/**
 * The two plain-Hebrew lines under every comparison. Built from the engine's numbers by code,
 * so the explanation can never quote a number the engine did not produce. Also the fallback when the LLM is off.
 */
final class ExplanationWriter {

    private ExplanationWriter() {
    }

    static String explain(Comparison comparison) {
        if (!comparison.eligible()) {
            return comparison.eligibility().reason();
        }
        String alternative = comparison.alternative().choice().label();
        int sign = comparison.difference().signum();
        if (sign == 0) {
            return "עם " + alternative + " הנטו המשוער נשאר כמו היום.";
        }
        String headline = "עם " + alternative + " הנטו המשוער " + (sign > 0 ? "עולה" : "יורד")
                + " ב-" + Money.display(comparison.difference().abs()) + " בחודש.";
        return comparison.factors().stream()
                .filter(f -> f.netImpact().signum() != 0)
                .max(Comparator.comparing((Factor f) -> f.netImpact().abs()))
                .map(main -> headline + " השינוי העיקרי: " + main.label() + ", "
                        + Money.display(main.netImpact().abs()) + (main.netImpact().signum() > 0 ? " לטובתך." : " לחובתך."))
                .orElse(headline);
    }
}
