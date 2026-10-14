package com.shani.caradvisor.data;

import com.shani.caradvisor.engine.Occupation;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record Occupations(List<Occupation> all) {

    public static Occupations load(Path file) {
        List<List<String>> rows = Csv.read(file, StandardCharsets.UTF_8);
        Header header = new Header(rows.get(0));
        List<Occupation> occupations = new ArrayList<>();
        for (List<String> row : rows.subList(1, rows.size())) {
            occupations.add(new Occupation(
                    header.get(row, "occupation"),
                    header.get(row, "group"),
                    header.bool(row, "carMandatory"),
                    header.bool(row, "waiverAllowed"),
                    header.decimal(row, "waiverMaintenance")));
        }
        return new Occupations(List.copyOf(occupations));
    }

    public Optional<Occupation> byName(String name) {
        return all.stream().filter(o -> o.name().equals(name.trim())).findFirst();
    }
}
