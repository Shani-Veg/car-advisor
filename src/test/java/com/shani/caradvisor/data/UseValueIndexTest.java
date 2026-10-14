package com.shani.caradvisor.data;

import com.shani.caradvisor.engine.Drive;
import com.shani.caradvisor.engine.VehicleKey;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class UseValueIndexTest {

    private static final ReferenceData DATA = ReferenceData.load(Path.of("data"));

    @Test
    void loadsTheWholeTaxAuthorityFile() {
        assertThat(DATA.useValues().size()).isEqualTo(77_988);
        assertThat(DATA.useValues().singleTaxYear()).isEqualTo(2026);
        assertThat(DATA.catalog().vehicles()).hasSize(20);
    }

    /** Same year, maker and model code, different vehicle type: a different car. A key without the type returns the wrong one. */
    @Test
    void vehicleTypeIsPartOfTheKey() {
        UseValueRecord atto3 = DATA.useValues().find(new VehicleKey(2026, 2025, 1, 1014, 3)).orElseThrow();
        UseValueRecord tang = DATA.useValues().find(new VehicleKey(2026, 2025, 2, 1014, 3)).orElseThrow();

        assertThat(atto3.commercialName()).isEqualTo("ATTO 3");
        assertThat(atto3.monthlyUseValue()).isEqualByComparingTo(new BigDecimal("3130"));
        assertThat(tang.commercialName()).isEqualTo("TANG");
        assertThat(tang.monthlyUseValue()).isEqualByComparingTo(new BigDecimal("6950"));
    }

    /** The file's header labels the drive column in a different order than the data uses; rules.json's mapping is the right one. */
    @Test
    void driveCodesFollowRulesJsonNotTheFileHeader() {
        assertThat(DATA.catalog().byCommercialName("MG4").orElseThrow().key())
                .satisfies(key -> assertThat(DATA.useValues().find(key).orElseThrow().drive()).isEqualTo(Drive.ELECTRIC));
        assertThat(DATA.catalog().byCommercialName("NIRO").orElseThrow().key())
                .satisfies(key -> assertThat(DATA.useValues().find(key).orElseThrow().drive()).isEqualTo(Drive.HYBRID));
        assertThat(DATA.catalog().byCommercialName("SPORTAGE HEV").orElseThrow().key())
                .satisfies(key -> assertThat(DATA.useValues().find(key).orElseThrow().drive()).isEqualTo(Drive.PLUG_IN));
    }

    @Test
    void searchByNameReturnsCandidatesToChooseFrom() {
        assertThat(DATA.useValues().search("atto", 2025, 10))
                .extracting(UseValueRecord::commercialName)
                .contains("ATTO 3");
    }
}
