package com.shani.caradvisor.engine;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** The business rules, loaded from rules.json. Changing a rule means changing the file, not the code. */
public record Rules(
        String version,
        Map<String, BigDecimal> budgets,
        List<String> groupOrder,
        Electric electric,
        FamilyTrack familyTrack,
        Waiver waiver,
        NetMethod netMethod,
        Map<Integer, Drive> driveCodes) {

    public record Electric(BigDecimal netBonus, String method) {
        /** "gross_up": the employer pays the tax so the employee sees exactly netBonus. Otherwise netBonus is paid as gross. */
        public boolean isGrossUp() {
            return "gross_up".equals(method);
        }
    }

    public record FamilyTrack(
            int minChildren,
            int childMaxAge,
            BigDecimal carPrice,
            BigDecimal employeeShareOfGap,
            boolean costFromCatalog) {
    }

    public record Waiver(boolean maintenanceTaxable, boolean maintenancePensionable) {
    }

    public record NetMethod(
            int taxYear,
            BigDecimal creditPointValue,
            List<Bracket> brackets,
            SocialRate nationalInsurance,
            SocialRate health,
            PensionCredit pensionCredit) {
    }

    /** upTo == null marks the top bracket. */
    public record Bracket(BigDecimal upTo, BigDecimal rate) {
    }

    public record SocialRate(BigDecimal threshold, BigDecimal ceiling, BigDecimal lowRate, BigDecimal highRate) {
    }

    public record PensionCredit(BigDecimal rate, BigDecimal incomeCeiling, BigDecimal maxDepositRate) {
        public BigDecimal maxCreditedDeposit() {
            return incomeCeiling.multiply(maxDepositRate);
        }
    }

    public BigDecimal budgetOf(String group) {
        BigDecimal budget = budgets.get(group);
        if (budget == null) {
            throw new IllegalArgumentException("No budget for group " + group);
        }
        return budget;
    }

    public int rankOf(String group) {
        int rank = groupOrder.indexOf(group);
        if (rank < 0) {
            throw new IllegalArgumentException("Unknown group " + group);
        }
        return rank;
    }
}
