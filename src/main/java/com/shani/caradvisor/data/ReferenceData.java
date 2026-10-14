package com.shani.caradvisor.data;

import com.shani.caradvisor.engine.Rules;

import java.nio.file.Path;

/** All swappable inputs, loaded and cross-checked once at startup. A bad file stops the app instead of producing wrong numbers. */
public record ReferenceData(Rules rules, UseValueIndex useValues, Catalog catalog, Occupations occupations) {

    public static ReferenceData load(Path dataDir) {
        Rules rules = RulesLoader.load(dataDir.resolve("rules.json"));
        UseValueIndex useValues = UseValueIndex.load(dataDir.resolve("use-value-2026.csv"), rules.driveCodes());
        Catalog catalog = Catalog.load(dataDir.resolve("catalog.csv"), useValues.singleTaxYear(), useValues);
        Occupations occupations = Occupations.load(dataDir.resolve("occupations.csv"));
        return new ReferenceData(rules, useValues, catalog, occupations);
    }
}
