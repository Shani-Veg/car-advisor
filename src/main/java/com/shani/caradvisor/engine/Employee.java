package com.shani.caradvisor.engine;

import java.math.BigDecimal;
import java.util.Objects;

/** Everything the engine needs about the employee. Complete by contract: missing data is handled before the engine. */
public record Employee(
        String occupation,
        String group,
        BigDecimal baseSalary,
        BigDecimal commissions,
        BigDecimal bonus,
        BigDecimal pensionPercent,
        BigDecimal studyFundPercent,
        BigDecimal creditPoints,
        int childrenUpTo23) {

    public Employee {
        Objects.requireNonNull(occupation, "occupation");
        Objects.requireNonNull(group, "group");
        Objects.requireNonNull(baseSalary, "baseSalary");
        Objects.requireNonNull(commissions, "commissions");
        Objects.requireNonNull(bonus, "bonus");
        Objects.requireNonNull(pensionPercent, "pensionPercent");
        Objects.requireNonNull(studyFundPercent, "studyFundPercent");
        Objects.requireNonNull(creditPoints, "creditPoints");
        if (childrenUpTo23 < 0) {
            throw new IllegalArgumentException("childrenUpTo23 must be >= 0");
        }
    }

    /** Salary the pension, study fund and pension credit are computed on (base + commissions + bonus). */
    public BigDecimal salary() {
        return baseSalary.add(commissions).add(bonus);
    }
}
