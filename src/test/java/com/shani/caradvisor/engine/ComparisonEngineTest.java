package com.shani.caradvisor.engine;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class ComparisonEngineTest {

    private static final Employee NOA = new Employee("מנהל/ת מוצר", "ג", new BigDecimal("18500"), BigDecimal.ZERO,
            BigDecimal.ZERO, new BigDecimal("6"), new BigDecimal("2.5"), new BigDecimal("2.75"), 2);
    private static final Employee DAVID = new Employee("נציג/ת מכירות שטח", "ב", new BigDecimal("9800"),
            new BigDecimal("2400"), BigDecimal.ZERO, new BigDecimal("6"), BigDecimal.ZERO, new BigDecimal("2.25"), 5);

    @Test
    void electricRefundAddsExactly250ToTheNet() {
        NetCalculator calculator = new NetCalculator(TestData.DATA.rules());
        MonthlyBenefits withoutRefund = new MonthlyBenefits(new BigDecimal("3130"), BigDecimal.ZERO,
                new BigDecimal("300"), BigDecimal.ZERO, BigDecimal.ZERO);

        BigDecimal gross = calculator.grossUp(NOA, withoutRefund, new BigDecimal("250"));
        BigDecimal before = calculator.calculate(NOA, withoutRefund).net();
        BigDecimal after = calculator.calculate(NOA, withoutRefund.with(MonthlyBenefits.Component.EV_REFUND, gross)).net();

        assertThat(after.subtract(before)).isCloseTo(new BigDecimal("250"), within(new BigDecimal("0.02")));
    }

    @Test
    void anOptionTheEmployeeCannotTakeGetsNoNumbers() {
        Employee groupB = TestData.employee("מפתח/ת תוכנה", "ב", 21000, 0);

        Comparison comparison = TestData.ENGINE.compare(groupB, TestData.occupation("מפתח/ת תוכנה"),
                TestData.withDefaultParticipation("MG4"), TestData.withDefaultParticipation("MODEL 3"));

        assertThat(comparison.eligible()).isFalse();
        assertThat(comparison.netAlternative()).isNull();
        assertThat(comparison.difference()).isNull();
        assertThat(comparison.factors()).isEmpty();
    }

    /**
     * Noa asked about ATTO 3 (+661). Waiving the car pays her most (+3,199), and MG4 is the best car (+1,013).
     * Numbers cross-checked against a separate Python implementation.
     */
    @Test
    void rankingShowsNoaBetterOptionsThanTheOneSheAskedAbout() {
        List<Comparison> ranked = TestData.ENGINE.rankAlternatives(NOA, TestData.occupation("מנהל/ת מוצר"),
                new CarChoice.CompanyCar(TestData.car("COROLLA CROSS"), new BigDecimal("300")),
                TestData.DATA.catalog().vehicles());

        assertThat(ranked.get(0).alternative().choice()).isInstanceOf(CarChoice.NoCar.class);
        assertThat(ranked.get(0).difference()).isCloseTo(new BigDecimal("3199"), within(BigDecimal.ONE));
        Comparison bestCar = ranked.stream()
                .filter(c -> c.alternative().choice() instanceof CarChoice.CompanyCar)
                .findFirst().orElseThrow();
        assertThat(bestCar.alternative().choice().label()).startsWith("MG4");
        assertThat(bestCar.difference()).isCloseTo(new BigDecimal("1013"), within(BigDecimal.ONE));
        assertThat(ranked).allSatisfy(c -> assertThat(TestData.sumOfImpacts(c)).isEqualByComparingTo(c.difference()));
        assertThat(ranked).noneMatch(c -> c.alternative().choice().label().startsWith("MODEL 3"));
    }

    @Test
    void familyTrackRankingShowsTheSantaFeHurtsLessThanTheCarnival() {
        List<Comparison> ranked = TestData.ENGINE.rankAlternatives(DAVID, TestData.occupation("נציג/ת מכירות שטח"),
                new CarChoice.CompanyCar(TestData.car("NIRO"), new BigDecimal("250")),
                TestData.DATA.catalog().vehicles());

        assertThat(difference(ranked, "SANTA FE")).isCloseTo(new BigDecimal("-1845"), within(BigDecimal.ONE));
        assertThat(difference(ranked, "CARNIVAL")).isCloseTo(new BigDecimal("-3522"), within(BigDecimal.ONE));
        assertThat(ranked).noneMatch(c -> c.alternative().choice() instanceof CarChoice.NoCar);
    }

    private static BigDecimal difference(List<Comparison> ranked, String commercialName) {
        return ranked.stream()
                .filter(c -> c.alternative().choice().label().startsWith(commercialName))
                .findFirst().orElseThrow()
                .difference();
    }
}
