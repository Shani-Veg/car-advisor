package com.shani.caradvisor.data;

import com.shani.caradvisor.engine.Rules;
import com.shani.caradvisor.engine.Vehicle;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/** All swappable inputs, loaded and cross-checked once at startup. A bad file stops the app instead of producing wrong numbers. */
public record ReferenceData(Rules rules, UseValueIndex useValues, Catalog catalog, Occupations occupations) {

    public static ReferenceData load(Path dataDir) {
        Rules rules = RulesLoader.load(dataDir.resolve("rules.json"));
        UseValueIndex useValues = UseValueIndex.load(dataDir.resolve("use-value-2026.csv"), rules.driveCodes());
        Catalog catalog = Catalog.load(dataDir.resolve("catalog.csv"), useValues.singleTaxYear(), useValues);
        Occupations occupations = Occupations.load(dataDir.resolve("occupations.csv"));
        checkFamilyCars(rules, catalog);
        return new ReferenceData(rules, useValues, catalog, occupations);
    }

    /** The rules file and the catalog must agree on which cars belong to the family track. */
    private static void checkFamilyCars(Rules rules, Catalog catalog) {
        List<String> declared = rules.familyTrack().cars();
        if (declared.isEmpty()) {
            return;
        }
        Set<String> inCatalog = catalog.vehicles().stream()
                .filter(Vehicle::isFamilyTrack).map(Vehicle::id).collect(Collectors.toCollection(TreeSet::new));
        if (!new TreeSet<>(declared).equals(inCatalog)) {
            throw new IllegalStateException("rules.json familyTrack.cars " + new TreeSet<>(declared)
                    + " does not match the family-track cars in the catalog " + inCatalog);
        }
    }
}
