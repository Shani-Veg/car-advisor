package com.shani.caradvisor.engine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/** Compares today's situation with an alternative. Pure and deterministic: same input, same output. */
public final class ComparisonEngine {

    private final Rules rules;
    private final NetCalculator calculator;
    private final EligibilityChecker eligibilityChecker;

    public ComparisonEngine(Rules rules) {
        this.rules = rules;
        this.calculator = new NetCalculator(rules);
        this.eligibilityChecker = new EligibilityChecker(rules);
    }

    public Comparison compare(Employee employee, Occupation occupation, CarChoice today, CarChoice alternative) {
        OptionResult todayResult = evaluate(employee, occupation, today);
        BigDecimal netToday = Money.shekels(todayResult.result().net());

        Eligibility eligibility = eligibilityChecker.check(employee, occupation, alternative);
        if (!eligibility.eligible()) {
            return new Comparison(todayResult, eligibility, null, netToday, null, null, List.of(), rules.version());
        }

        OptionResult alternativeResult = evaluate(employee, occupation, alternative);
        BigDecimal netAlternative = Money.shekels(alternativeResult.result().net());
        List<Factor> factors = explain(employee, todayResult.benefits(), alternativeResult.benefits());
        return new Comparison(todayResult, eligibility, alternativeResult, netToday, netAlternative,
                netAlternative.subtract(netToday), factors, rules.version());
    }

    /**
     * Every option the employee may take (catalog cars plus waiver), best net first.
     * Participation is the catalog default, so callers must present it as an estimate.
     */
    public List<Comparison> rankAlternatives(Employee employee, Occupation occupation, CarChoice today,
                                             Collection<Vehicle> catalog) {
        List<Comparison> ranked = new ArrayList<>();
        for (Vehicle vehicle : catalog) {
            Comparison comparison = compare(employee, occupation, today,
                    new CarChoice.CompanyCar(vehicle, vehicle.defaultParticipation()));
            if (comparison.eligible()) {
                ranked.add(comparison);
            }
        }
        Comparison waiver = compare(employee, occupation, today, new CarChoice.NoCar());
        if (waiver.eligible()) {
            ranked.add(waiver);
        }
        ranked.sort(Comparator.comparing(Comparison::difference).reversed());
        return ranked;
    }

    public MonthlyBenefits benefitsOf(Employee employee, Occupation occupation, CarChoice choice) {
        if (choice instanceof CarChoice.CompanyCar car) {
            Vehicle vehicle = car.vehicle();
            MonthlyBenefits benefits = new MonthlyBenefits(
                    vehicle.monthlyUseValue(), Money.ZERO, car.participation(), familyShare(employee, vehicle), Money.ZERO);
            if (vehicle.drive() == Drive.ELECTRIC) {
                benefits = benefits.with(MonthlyBenefits.Component.EV_REFUND, evRefundGross(employee, benefits));
            }
            return benefits;
        }
        return MonthlyBenefits.NONE.with(MonthlyBenefits.Component.MAINTENANCE, occupation.waiverMaintenance());
    }

    private OptionResult evaluate(Employee employee, Occupation occupation, CarChoice choice) {
        MonthlyBenefits benefits = benefitsOf(employee, occupation, choice);
        return new OptionResult(choice, benefits, calculator.calculate(employee, benefits));
    }

    /** Family track: the employee pays their share of the gap between the car's cost and the group budget. */
    private BigDecimal familyShare(Employee employee, Vehicle vehicle) {
        if (!vehicle.isFamilyTrack()) {
            return Money.ZERO;
        }
        Rules.FamilyTrack track = rules.familyTrack();
        BigDecimal cost = track.costFromCatalog() && vehicle.monthlyCost() != null
                ? vehicle.monthlyCost()
                : track.carPrice();
        BigDecimal gap = Money.max(Money.ZERO, cost.subtract(rules.budgetOf(employee.group())));
        return gap.multiply(track.employeeShareOfGap());
    }

    private BigDecimal evRefundGross(Employee employee, MonthlyBenefits withoutRefund) {
        Rules.Electric electric = rules.electric();
        return electric.isGrossUp()
                ? calculator.grossUp(employee, withoutRefund, electric.netBonus())
                : electric.netBonus();
    }

    /**
     * Moves from today's components to the alternative's one at a time (fixed order) and records
     * the net change of each step. Steps use whole-shekel nets, so the impacts telescope to
     * exactly the displayed difference: the table and the waterfall always agree.
     */
    private List<Factor> explain(Employee employee, MonthlyBenefits from, MonthlyBenefits to) {
        List<Factor> factors = new ArrayList<>();
        MonthlyBenefits current = from;
        BigDecimal previousNet = Money.shekels(calculator.calculate(employee, current).net());
        for (MonthlyBenefits.Component component : MonthlyBenefits.Component.values()) {
            if (from.get(component).compareTo(to.get(component)) == 0) {
                continue;
            }
            current = current.with(component, to.get(component));
            BigDecimal net = Money.shekels(calculator.calculate(employee, current).net());
            factors.add(new Factor(component, Money.cents(from.get(component)), Money.cents(to.get(component)),
                    net.subtract(previousNet)));
            previousNet = net;
        }
        return factors;
    }
}
