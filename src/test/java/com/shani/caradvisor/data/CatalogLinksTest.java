package com.shani.caradvisor.data;

import com.shani.caradvisor.engine.Vehicle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CatalogLinksTest {

    private static final ReferenceData DATA = ReferenceData.load(Path.of("data"));

    @Test
    void everyLinkIsAnHttpsPageAndDolphinHasNone() {
        List<Vehicle> withLinks = DATA.catalog().vehicles().stream().filter(v -> v.officialUrl() != null).toList();

        assertThat(withLinks).hasSize(19);
        assertThat(withLinks).allSatisfy(v -> assertThat(v.officialUrl()).startsWith("https://"));
        // Its official page now redirects to the Dolphin Surf, a different car: no link beats a wrong one.
        assertThat(DATA.catalog().byCommercialName("BYD DOLPHIN").orElseThrow().officialUrl()).isNull();
        assertThat(DATA.catalog().byCommercialName("MG4").orElseThrow().officialUrl())
                .isEqualTo("https://mg-israel.co.il/mg4-family/");
    }

    @Test
    void aCatalogWithoutTheLinkColumnStillLoads(@TempDir Path dir) throws Exception {
        Path catalog = dir.resolve("catalog.csv");
        List<String> lines = Files.readAllLines(Path.of("data", "catalog.csv"), StandardCharsets.UTF_8).stream()
                .map(line -> line.substring(0, line.lastIndexOf(',')))
                .toList();
        Files.write(catalog, lines, StandardCharsets.UTF_8);

        Catalog loaded = Catalog.load(catalog, 2026, DATA.useValues());

        assertThat(loaded.vehicles()).hasSize(20).allSatisfy(v -> assertThat(v.officialUrl()).isNull());
    }

    @Test
    void aLinkThatIsNotHttpsStopsTheLoad(@TempDir Path dir) throws Exception {
        Path catalog = dir.resolve("catalog.csv");
        List<String> lines = Files.readAllLines(Path.of("data", "catalog.csv"), StandardCharsets.UTF_8);
        String first = lines.get(1);
        lines.set(1, first.substring(0, first.lastIndexOf(',') + 1) + "javascript:alert(1)");
        Files.write(catalog, lines, StandardCharsets.UTF_8);

        assertThatThrownBy(() -> Catalog.load(catalog, 2026, DATA.useValues()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("https only");
    }
}
