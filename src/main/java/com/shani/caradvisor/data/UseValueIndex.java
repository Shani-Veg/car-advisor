package com.shani.caradvisor.data;

import com.shani.caradvisor.engine.Drive;
import com.shani.caradvisor.engine.VehicleKey;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * In-memory index of the tax authority's use-value file (windows-1255, 19 columns), keyed by the full official key.
 */
public final class UseValueIndex {

    public static final Charset TAX_AUTHORITY_CHARSET = Charset.forName("windows-1255");

    private static final int TAX_YEAR = 0;
    private static final int REGISTRATION_YEAR = 1;
    private static final int VEHICLE_TYPE = 2;
    private static final int MAKER_CODE = 3;
    private static final int MAKER = 4;
    private static final int MODEL_CODE = 5;
    private static final int MODEL_DESCRIPTION = 6;
    private static final int COMMERCIAL_NAME = 8;
    // The file's own header labels this column "1-electric, 2-hybrid, 3-plug-in", but the data uses
    // 1-hybrid, 2-plug-in, 3-electric (every EV in the catalog is 3). We trust rules.json's mapping, not the header.
    private static final int DRIVE = 10;
    private static final int EFFECTIVE_DATE = 15;
    private static final int LIST_PRICE = 16;
    private static final int USE_VALUE = 18;

    private final Map<VehicleKey, UseValueRecord> byKey;
    private final List<UseValueRecord> records;

    private UseValueIndex(Map<VehicleKey, UseValueRecord> byKey, List<UseValueRecord> records) {
        this.byKey = byKey;
        this.records = records;
    }

    public static UseValueIndex load(Path file, Map<Integer, Drive> driveCodes) {
        List<List<String>> rows = Csv.read(file, TAX_AUTHORITY_CHARSET);
        Map<VehicleKey, UseValueRecord> byKey = new HashMap<>(rows.size() * 2);
        List<UseValueRecord> records = new ArrayList<>(rows.size());
        for (List<String> row : rows.subList(1, rows.size())) {
            VehicleKey key = new VehicleKey(
                    integer(row, TAX_YEAR),
                    integer(row, REGISTRATION_YEAR),
                    integer(row, VEHICLE_TYPE),
                    integer(row, MAKER_CODE),
                    integer(row, MODEL_CODE));
            Drive drive = driveCodes.get(integer(row, DRIVE));
            if (drive == null) {
                throw new IllegalStateException("Unknown drive code " + row.get(DRIVE) + " for " + key);
            }
            UseValueRecord record = new UseValueRecord(
                    key,
                    row.get(MAKER),
                    row.get(MODEL_DESCRIPTION),
                    row.get(COMMERCIAL_NAME),
                    drive,
                    row.get(EFFECTIVE_DATE),
                    new BigDecimal(row.get(LIST_PRICE)),
                    new BigDecimal(row.get(USE_VALUE)));
            if (byKey.put(key, record) != null) {
                throw new IllegalStateException("Duplicate tax-authority key " + key + ": the lookup would be ambiguous");
            }
            records.add(record);
        }
        return new UseValueIndex(byKey, Collections.unmodifiableList(records));
    }

    public Optional<UseValueRecord> find(VehicleKey key) {
        return Optional.ofNullable(byKey.get(key));
    }

    /** Candidates for a car described in words, e.g. "atto" + 2025. The caller asks the employee to pick one. */
    public List<UseValueRecord> search(String text, Integer registrationYear, int limit) {
        String needle = text.trim().toUpperCase(Locale.ROOT);
        return records.stream()
                .filter(r -> registrationYear == null || r.key().registrationYear() == registrationYear)
                .filter(r -> r.commercialName().toUpperCase(Locale.ROOT).contains(needle)
                        || r.maker().contains(text.trim()))
                .sorted(Comparator.comparing((UseValueRecord r) -> r.key().registrationYear()).reversed()
                        .thenComparing(UseValueRecord::commercialName))
                .limit(limit)
                .toList();
    }

    public int size() {
        return records.size();
    }

    /** The file is published per tax year; the catalog's keys use that year. */
    public int singleTaxYear() {
        Set<Integer> years = new TreeSet<>();
        records.forEach(r -> years.add(r.key().taxYear()));
        if (years.size() != 1) {
            throw new IllegalStateException("Expected one tax year in the use-value file, found " + years);
        }
        return years.iterator().next();
    }

    private static int integer(List<String> row, int column) {
        return Integer.parseInt(row.get(column).trim());
    }
}
