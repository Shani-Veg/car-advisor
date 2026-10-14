package com.shani.caradvisor.engine;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EligibilityCheckerTest {

    private final EligibilityChecker checker = new EligibilityChecker(TestData.DATA.rules());

    @Test
    void carFromAHigherGroupIsRefused() {
        Employee groupB = TestData.employee("מפתח/ת תוכנה", "ב", 21000, 0);

        Eligibility result = checker.check(groupB, TestData.occupation("מפתח/ת תוכנה"),
                TestData.withDefaultParticipation("COROLLA CROSS"));

        assertThat(result.eligible()).isFalse();
        assertThat(result.code()).isEqualTo(Eligibility.Code.HIGHER_GROUP);
    }

    @Test
    void carFromALowerGroupIsAllowed() {
        Employee groupD = TestData.employee("מנהל/ת מחלקה", "ד", 32000, 0);

        Eligibility result = checker.check(groupD, TestData.occupation("מנהל/ת מחלקה"),
                TestData.withDefaultParticipation("PICANTO"));

        assertThat(result.eligible()).isTrue();
        assertThat(result.code()).isEqualTo(Eligibility.Code.OWN_OR_LOWER_GROUP);
    }

    @Test
    void familyTrackNeedsFourChildren() {
        Occupation occupation = TestData.occupation("מנהל/ת מוצר");
        CarChoice carnival = TestData.withDefaultParticipation("CARNIVAL");

        Eligibility three = checker.check(TestData.employee("מנהל/ת מוצר", "ג", 18500, 3), occupation, carnival);
        Eligibility four = checker.check(TestData.employee("מנהל/ת מוצר", "ג", 18500, 4), occupation, carnival);

        assertThat(three.eligible()).isFalse();
        assertThat(three.code()).isEqualTo(Eligibility.Code.FAMILY_TRACK_NEEDS_CHILDREN);
        assertThat(four.eligible()).isTrue();
        assertThat(four.code()).isEqualTo(Eligibility.Code.FAMILY_TRACK);
    }

    @Test
    void fieldRoleCannotWaiveTheCar() {
        Employee fieldSales = TestData.employee("נציג/ת מכירות שטח", "ב", 9800, 0);

        Eligibility result = checker.check(fieldSales, TestData.occupation("נציג/ת מכירות שטח"), new CarChoice.NoCar());

        assertThat(result.eligible()).isFalse();
        assertThat(result.code()).isEqualTo(Eligibility.Code.CAR_REQUIRED_BY_ROLE);
    }

    /** The challenge's own tip: it must work for a group B employee who wants to waive, not only the scripted example. */
    @Test
    void groupBDataAnalystMayWaive() {
        Employee analyst = TestData.employee("אנליסט/ית נתונים", "ב", 15000, 0);

        Eligibility result = checker.check(analyst, TestData.occupation("אנליסט/ית נתונים"), new CarChoice.NoCar());

        assertThat(result.eligible()).isTrue();
        assertThat(result.reason()).contains("1,500");
    }
}
