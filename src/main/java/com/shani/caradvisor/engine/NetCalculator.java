package com.shani.caradvisor.engine;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Monthly gross-to-net following rules.json "netMethod" (monthly, not cumulative).
 * Use value is taxable but never paid in cash; pension, study fund, the employee's
 * participation and the family-track share come off the net.
 */
public final class NetCalculator {

    private static final BigDecimal TWO = BigDecimal.valueOf(2);
    private static final BigDecimal GROSS_UP_PRECISION = new BigDecimal("0.001");

    private final Rules rules;
    private final Rules.NetMethod method;

    public NetCalculator(Rules rules) {
        this.rules = rules;
        this.method = rules.netMethod();
    }

    public NetResult calculate(Employee employee, MonthlyBenefits benefits) {
        Breakdown b = compute(employee, benefits);
        return new NetResult(
                Money.cents(b.grossCash),
                Money.cents(b.taxableIncome),
                Money.cents(b.incomeTax),
                Money.cents(b.nationalInsurance),
                Money.cents(b.healthTax),
                Money.cents(b.pension),
                Money.cents(b.studyFund),
                Money.cents(benefits.participation()),
                Money.cents(benefits.familyShare()),
                Money.cents(b.net));
    }

    /**
     * The gross amount that, added on top of this situation, raises the net by exactly {@code targetNet}.
     * Bisection rather than a formula: net is monotonic in gross pay, but brackets and ceilings make a closed form messy.
     */
    public BigDecimal grossUp(Employee employee, MonthlyBenefits benefits, BigDecimal targetNet) {
        BigDecimal baseNet = compute(employee, benefits).net;
        BigDecimal low = Money.ZERO;
        // Marginal deductions stay well under 75%, so 4x the target always brackets the answer.
        BigDecimal high = targetNet.multiply(BigDecimal.valueOf(4));
        while (high.subtract(low).compareTo(GROSS_UP_PRECISION) > 0) {
            BigDecimal mid = low.add(high).divide(TWO);
            MonthlyBenefits withExtra = benefits.with(
                    MonthlyBenefits.Component.EV_REFUND, benefits.evGrossUp().add(mid));
            BigDecimal gain = compute(employee, withExtra).net.subtract(baseNet);
            if (gain.compareTo(targetNet) < 0) {
                low = mid;
            } else {
                high = mid;
            }
        }
        return high.setScale(2, RoundingMode.CEILING);
    }

    private Breakdown compute(Employee employee, MonthlyBenefits benefits) {
        Rules.Waiver waiver = rules.waiver();
        BigDecimal salary = employee.salary();

        BigDecimal grossCash = salary.add(benefits.maintenance()).add(benefits.evGrossUp());
        BigDecimal taxableIncome = grossCash.add(benefits.useValue());
        if (!waiver.maintenanceTaxable()) {
            taxableIncome = taxableIncome.subtract(benefits.maintenance());
        }

        // Rules file: contributions are computed on the whole gross (base + commissions + bonus), or on the base only.
        BigDecimal contributionBase = method.contributionsOnGross() ? salary : employee.baseSalary();
        BigDecimal pensionBase = waiver.maintenancePensionable()
                ? contributionBase.add(benefits.maintenance())
                : contributionBase;
        BigDecimal pension = Money.percentOf(pensionBase, employee.pensionPercent());
        BigDecimal studyFund = Money.percentOf(contributionBase, employee.studyFundPercent());

        BigDecimal credits = employee.creditPoints().multiply(method.creditPointValue()).add(pensionCredit(pension));
        BigDecimal incomeTax = Money.max(Money.ZERO, bracketTax(taxableIncome).subtract(credits));
        BigDecimal nationalInsurance = social(taxableIncome, method.nationalInsurance());
        BigDecimal healthTax = social(taxableIncome, method.health());

        BigDecimal net = grossCash
                .subtract(incomeTax)
                .subtract(nationalInsurance)
                .subtract(healthTax)
                .subtract(pension)
                .subtract(studyFund)
                .subtract(benefits.participation())
                .subtract(benefits.familyShare());

        return new Breakdown(grossCash, taxableIncome, incomeTax, nationalInsurance, healthTax, pension, studyFund, net);
    }

    /** 35% credit on the employee's pension deposit, up to maxContributionRate of the income ceiling. */
    private BigDecimal pensionCredit(BigDecimal pension) {
        Rules.PensionCredit credit = method.pensionCredit();
        return credit.rate().multiply(Money.min(pension, credit.maxCreditedDeposit()));
    }

    private BigDecimal bracketTax(BigDecimal income) {
        BigDecimal tax = Money.ZERO;
        BigDecimal lower = Money.ZERO;
        for (Rules.Bracket bracket : method.brackets()) {
            if (income.compareTo(lower) <= 0) {
                break;
            }
            BigDecimal upper = bracket.upTo() == null ? income : Money.min(income, bracket.upTo());
            tax = tax.add(upper.subtract(lower).multiply(bracket.rate()));
            if (bracket.upTo() == null) {
                break;
            }
            lower = bracket.upTo();
        }
        return tax;
    }

    /** National insurance / health: low rate up to the threshold, high rate above it, nothing above the ceiling. */
    private BigDecimal social(BigDecimal income, Rules.SocialRate rate) {
        BigDecimal base = Money.min(income, rate.ceiling());
        BigDecimal low = Money.min(base, rate.threshold());
        BigDecimal high = Money.max(Money.ZERO, base.subtract(rate.threshold()));
        return low.multiply(rate.lowRate()).add(high.multiply(rate.highRate()));
    }

    private record Breakdown(
            BigDecimal grossCash,
            BigDecimal taxableIncome,
            BigDecimal incomeTax,
            BigDecimal nationalInsurance,
            BigDecimal healthTax,
            BigDecimal pension,
            BigDecimal studyFund,
            BigDecimal net) {
    }
}
