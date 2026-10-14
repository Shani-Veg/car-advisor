package com.shani.caradvisor.agent;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReplyGuardTest {

    private static final List<String> CARS = List.of("ATTO 3", "ATTO 3 (2025)", "MG4", "MG4 (2025)", "MAZDA 3", "EV3");

    @Test
    void carNamesYearsAndSingleDigitsPass() {
        assertThat(ReplyGuard.containsAmount("בדקתי את ATTO 3 ואת MG4 משנת 2025", CARS)).isFalse();
        assertThat(ReplyGuard.containsAmount("בדקתי את האטו 3 בשבילך", CARS)).isFalse();
        assertThat(ReplyGuard.containsAmount("Mazda 3 נמוכה יותר", CARS)).isFalse();
    }

    @Test
    void amountsPercentagesAndDecimalsAreBlocked() {
        assertThat(ReplyGuard.containsAmount("הנטו יעלה ב-661", CARS)).isTrue();
        assertThat(ReplyGuard.containsAmount("תוספת של 1,013 בחודש", CARS)).isTrue();
        assertThat(ReplyGuard.containsAmount("הפרשה של 6%", CARS)).isTrue();
        assertThat(ReplyGuard.containsAmount("2.5 נקודות", CARS)).isTrue();
        assertThat(ReplyGuard.containsAmount("עוד ₪ 5", CARS)).isTrue();
    }

    @Test
    void arabicLettersInsideHebrewAreDetected() {
        assertThat(ReplyGuard.containsForeignScript("ההשוואה מוצגת במסך")).isFalse();
        assertThat(ReplyGuard.containsForeignScript("ההשוואה מוצגת במסך، ובהמשך دال")).isTrue();
        assertThat(ReplyGuard.containsForeignScript("הנטו צפוי לעלות")).isFalse();
    }
}
