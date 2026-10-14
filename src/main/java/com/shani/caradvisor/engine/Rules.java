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

    /**
     * cars: catalog ids of the family-track cars, as declared by the rules file (may be empty in older files).
     * carPrice: one fixed price for the whole track, only in older files; newer files take each car's cost from the catalog.
     */
    public record FamilyTrack(
            int minChildren,
            int childMaxAge,
            List<String> cars,
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
            PensionCredit pensionCredit,
            boolean contributionsOnGross) {
    }

    /** upTo == null marks the top bracket. */
    public record Bracket(BigDecimal upTo, BigDecimal rate) {
    }

    public record SocialRate(BigDecimal threshold, BigDecimal ceiling, BigDecimal lowRate, BigDecimal highRate) {
    }

    public record PensionCredit(BigDecimal rate, BigDecimal incomeCeiling, BigDecimal maxContributionRate) {
        /** The deposit that earns the credit is capped at this share of the income ceiling (7% of 9,500 = 665). */
        public BigDecimal maxCreditedDeposit() {
            return incomeCeiling.multiply(maxContributionRate);
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
