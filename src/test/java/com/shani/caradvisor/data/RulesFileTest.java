package com.shani.caradvisor.data;

import com.shani.caradvisor.engine.Employee;
import com.shani.caradvisor.engine.MonthlyBenefits;
import com.shani.caradvisor.engine.NetCalculator;
import com.shani.caradvisor.engine.Rules;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** rules.json is the file Strauss will replace in a pilot, so every format they have sent must load. */
class RulesFileTest {

    private static final Path OFFICIAL = Path.of("data", "rules.json");
    private static final Path LEGACY = Path.of("src", "test", "resources", "rules-legacy.json");

    @Test
    void theCurrentFileFromStraussLoadsAsIs() {
        Rules rules = RulesLoader.load(OFFICIAL);

        assertThat(rules.familyTrack().cars()).containsExactly("CAR-19", "CAR-20");
        assertThat(rules.familyTrack().costFromCatalog()).isTrue();
        assertThat(rules.familyTrack().carPrice()).isNull();
        assertThat(rules.netMethod().pensionCredit().maxCreditedDeposit()).isEqualByComparingTo("665");
        assertThat(rules.netMethod().contributionsOnGross()).isTrue();
    }

    @Test
    void theEarlierFormatWithOneFixedPriceStillLoads() {
        Rules rules = RulesLoader.load(LEGACY);

        assertThat(rules.familyTrack().carPrice()).isEqualByComparingTo("6000");
        assertThat(rules.familyTrack().cars()).isEmpty();
        assertThat(rules.netMethod().pensionCredit().maxCreditedDeposit()).isEqualByComparingTo("665");
    }

    @Test
    void aSettingTheEngineDoesNotImplementStopsTheLoad(@TempDir Path dir) throws IOException {
        Path file = changed(dir, "\"reducesValueOfUse\": false", "\"reducesValueOfUse\": true");

        assertThatThrownBy(() -> RulesLoader.load(file))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("reducesValueOfUse")
                .hasMessageContaining("not supported");
    }

    @Test
    void roundingDuringTheCalculationIsNotSupportedEither(@TempDir Path dir) throws IOException {
        Path file = changed(dir, "\"during\": \"none\"", "\"during\": \"each_step\"");

        assertThatThrownBy(() -> RulesLoader.load(file))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("netMethod.rounding.during");
    }

    @Test
    void familyCarsThatDisagreeWithTheCatalogStopTheApp(@TempDir Path dir) throws IOException {
        for (String name : new String[]{"catalog.csv", "occupations.csv", "use-value-2026.csv"}) {
            Files.copy(Path.of("data", name), dir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
        }
        Files.writeString(dir.resolve("rules.json"), read().replace("\"CAR-20\"", "\"CAR-12\""), StandardCharsets.UTF_8);

        assertThatThrownBy(() -> ReferenceData.load(dir))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("familyTrack.cars");
    }

    /** David has commissions: pension on the whole gross is 6% of 12,200, on the base only it is 6% of 9,800. */
    @Test
    void contributionsCanBeSwitchedToTheBaseOnly(@TempDir Path dir) throws IOException {
        Employee david = new Employee("נציג/ת מכירות שטח", "ב", new BigDecimal("9800"), new BigDecimal("2400"),
                BigDecimal.ZERO, new BigDecimal("6"), BigDecimal.ZERO, new BigDecimal("2.25"), 5);

        BigDecimal onGross = pensionOf(RulesLoader.load(OFFICIAL), david);
        BigDecimal onBase = pensionOf(RulesLoader.load(changed(dir, "\"contributionsBase\": \"gross\"", "\"contributionsBase\": \"base\"")), david);

        assertThat(onGross).isEqualByComparingTo("732.00");
        assertThat(onBase).isEqualByComparingTo("588.00");
    }

    private static BigDecimal pensionOf(Rules rules, Employee employee) {
        MonthlyBenefits none = MonthlyBenefits.NONE;
        return new NetCalculator(rules).calculate(employee, none).pension();
    }

    private static Path changed(Path dir, String from, String to) throws IOException {
        String text = read();
        assertThat(text).contains(from);
        Path file = dir.resolve("rules.json");
        Files.writeString(file, text.replace(from, to), StandardCharsets.UTF_8);
        return file;
    }

    private static String read() throws IOException {
        return Files.readString(OFFICIAL, StandardCharsets.UTF_8);
    }
}
