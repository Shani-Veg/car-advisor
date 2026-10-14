package com.shani.caradvisor.engine;

import com.shani.caradvisor.proof.ReferenceCase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * The 6 sample employees Strauss supplied, with their reference nets (data/reference-cases.json).
 * Strauss calls those nets "illustration only", so we accept up to 1 NIS of rounding difference.
 */
class ReferenceCasesTest {

    private static final BigDecimal ONE_SHEKEL = BigDecimal.ONE;

    static Stream<Arguments> cases() {
        return ReferenceCase.load(Path.of("data", "reference-cases.json")).stream()
                .map(c -> Arguments.of(c.id() + " " + c.question(), c));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void matchesStraussReferenceWithinOneShekel(String name, ReferenceCase c) {
        Comparison comparison = TestData.ENGINE.compare(c.employee(), c.occupation(TestData.DATA),
                c.todayChoice(TestData.DATA), c.alternativeChoice(TestData.DATA));

        assertThat(comparison.eligible()).isEqualTo(c.expectedEligible());
        assertThat(comparison.netToday()).isCloseTo(c.expectedAmount("netToday"), within(ONE_SHEKEL));
        assertThat(comparison.netAlternative()).isCloseTo(c.expectedAmount("netAlternative"), within(ONE_SHEKEL));
        assertThat(comparison.difference()).isCloseTo(c.expectedAmount("difference"), within(ONE_SHEKEL));

        NetResult todayNet = comparison.today().result();
        NetResult alternativeNet = comparison.alternative().result();
        assertThat(todayNet.incomeTax()).isCloseTo(c.expectedAmount("taxToday"), within(ONE_SHEKEL));
        assertThat(alternativeNet.incomeTax()).isCloseTo(c.expectedAmount("taxAlternative"), within(ONE_SHEKEL));
        assertThat(todayNet.nationalInsurance().add(todayNet.healthTax()))
                .isCloseTo(c.expectedAmount("socialToday"), within(ONE_SHEKEL));
        assertThat(alternativeNet.nationalInsurance().add(alternativeNet.healthTax()))
                .isCloseTo(c.expectedAmount("socialAlternative"), within(ONE_SHEKEL));
        assertThat(comparison.alternative().benefits().evGrossUp())
                .isCloseTo(c.expectedAmount("evGrossUp"), within(ONE_SHEKEL));
        assertThat(comparison.alternative().benefits().familyShare())
                .isEqualByComparingTo(c.expectedAmount("familyShare"));

        assertThat(TestData.sumOfImpacts(comparison)).isEqualByComparingTo(comparison.difference());
    }
}
