package com.shani.caradvisor.engine;

import com.shani.caradvisor.data.ReferenceData;

import java.math.BigDecimal;
import java.nio.file.Path;

/** The real project data files, loaded once for all engine tests. */
final class TestData {

    static final ReferenceData DATA = ReferenceData.load(Path.of("data"));
    static final ComparisonEngine ENGINE = new ComparisonEngine(DATA.rules());

    private TestData() {
    }

    static Vehicle car(String commercialName) {
        return DATA.catalog().byCommercialName(commercialName).orElseThrow();
    }

    static Occupation occupation(String name) {
        return DATA.occupations().byName(name).orElseThrow();
    }

    static CarChoice.CompanyCar withDefaultParticipation(String commercialName) {
        Vehicle vehicle = car(commercialName);
        return new CarChoice.CompanyCar(vehicle, vehicle.defaultParticipation());
    }

    static Employee employee(String occupation, String group, long baseSalary, int children) {
        return new Employee(occupation, group, BigDecimal.valueOf(baseSalary), BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("6"), new BigDecimal("2.5"), new BigDecimal("2.25"), children);
    }

    static BigDecimal sumOfImpacts(Comparison comparison) {
        return comparison.factors().stream().map(Factor::netImpact).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
