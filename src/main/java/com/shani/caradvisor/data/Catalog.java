package com.shani.caradvisor.data;

import com.shani.caradvisor.engine.Drive;
import com.shani.caradvisor.engine.Vehicle;
import com.shani.caradvisor.engine.VehicleKey;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** The company's car catalog. Every car is checked against the tax authority file when loaded. */
public record Catalog(List<Vehicle> vehicles) {

    public static Catalog load(Path file, int taxYear, UseValueIndex useValues) {
        List<List<String>> rows = Csv.read(file, StandardCharsets.UTF_8);
        Header header = new Header(rows.get(0));
        List<Vehicle> vehicles = new ArrayList<>();
        for (List<String> row : rows.subList(1, rows.size())) {
            String id = header.get(row, "id");
            VehicleKey key = new VehicleKey(
                    taxYear,
                    header.integer(row, "regYear"),
                    header.integer(row, "vehicleType"),
                    header.integer(row, "makerCode"),
                    header.integer(row, "modelCode"));
            UseValueRecord official = useValues.find(key).orElseThrow(() ->
                    new IllegalStateException("Catalog car " + id + " is not in the tax-authority file: " + key));
            BigDecimal useValue = header.decimal(row, "monthlyUseValue");
            if (useValue.compareTo(official.monthlyUseValue()) != 0) {
                throw new IllegalStateException("Catalog car " + id + " use value " + useValue
                        + " differs from the tax authority's " + official.monthlyUseValue());
            }
            vehicles.add(new Vehicle(
                    id,
                    header.get(row, "group"),
                    header.get(row, "maker"),
                    header.get(row, "commercialName"),
                    header.get(row, "modelDescription"),
                    key,
                    Drive.fromHebrew(header.get(row, "drive")),
                    header.decimal(row, "listPrice"),
                    useValue,
                    header.decimal(row, "monthlyCost"),
                    header.decimal(row, "defaultParticipation"),
                    officialUrl(id, header.optional(row, "officialUrl"))));
        }
        return new Catalog(List.copyOf(vehicles));
    }

    /** Links end up in the employee's browser, so only plain https pages are accepted. */
    private static String officialUrl(String id, String url) {
        if (url == null) {
            return null;
        }
        if (!url.startsWith("https://") || url.chars().anyMatch(c -> c <= ' ' || c == '"' || c == '<' || c == '>')) {
            throw new IllegalStateException("Catalog car " + id + " has an invalid officialUrl (https only): " + url);
        }
        return url;
    }

    public Optional<Vehicle> byId(String id) {
        return vehicles.stream().filter(v -> v.id().equals(id)).findFirst();
    }

    public Optional<Vehicle> byCommercialName(String name) {
        String wanted = name.trim().toUpperCase(Locale.ROOT);
        return vehicles.stream()
                .filter(v -> v.commercialName().toUpperCase(Locale.ROOT).equals(wanted))
                .findFirst();
    }
}
