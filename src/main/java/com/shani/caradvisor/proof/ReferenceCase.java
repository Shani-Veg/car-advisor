package com.shani.caradvisor.proof;

import com.shani.caradvisor.data.ReferenceData;
import com.shani.caradvisor.engine.CarChoice;
import com.shani.caradvisor.engine.Employee;
import com.shani.caradvisor.engine.Occupation;
import com.shani.caradvisor.engine.Vehicle;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** One of Strauss's sample employees with their reference results (data/reference-cases.json). */
public record ReferenceCase(String id, String question, Employee employee, Map<String, Object> today,
                            Map<String, Object> alternative, Map<String, Object> expected) {

    @SuppressWarnings("unchecked")
    public static List<ReferenceCase> load(Path file) {
        List<Map<String, Object>> raw = new ObjectMapper().readValue(file.toFile(), List.class);
        return raw.stream().map(c -> new ReferenceCase(
                (String) c.get("id"),
                (String) c.get("question"),
                new Employee(
                        (String) c.get("occupation"),
                        (String) c.get("group"),
                        decimal(c.get("baseSalary")),
                        decimal(c.get("commissions")),
                        decimal(c.get("bonus")),
                        decimal(c.get("pensionPct")),
                        decimal(c.get("studyFundPct")),
                        decimal(c.get("creditPoints")),
                        ((Number) c.get("childrenUpTo23")).intValue()),
                (Map<String, Object>) c.get("today"),
                (Map<String, Object>) c.get("alternative"),
                (Map<String, Object>) c.get("expected"))).toList();
    }

    public Occupation occupation(ReferenceData data) {
        return data.occupations().byName(employee.occupation()).orElseThrow();
    }

    /** Today uses the employee's own participation; an alternative car uses the catalog default, as Strauss did. */
    public CarChoice todayChoice(ReferenceData data) {
        return choice(data, today, true);
    }

    public CarChoice alternativeChoice(ReferenceData data) {
        return choice(data, alternative, false);
    }

    public BigDecimal expectedAmount(String key) {
        return decimal(expected.get(key));
    }

    public boolean expectedEligible() {
        return Boolean.TRUE.equals(expected.get("eligible"));
    }

    private static CarChoice choice(ReferenceData data, Map<String, Object> spec, boolean today) {
        if ("waiver".equals(spec.get("type"))) {
            return new CarChoice.NoCar();
        }
        Vehicle vehicle = data.catalog().byCommercialName((String) spec.get("commercialName")).orElseThrow();
        BigDecimal participation = today ? decimal(spec.get("participation")) : vehicle.defaultParticipation();
        return new CarChoice.CompanyCar(vehicle, participation);
    }

    private static BigDecimal decimal(Object value) {
        return new BigDecimal(value.toString());
    }
}
