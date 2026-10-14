package com.shani.caradvisor.data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Column lookup by header name, so swapped-in data files may reorder columns. */
final class Header {

    private final Map<String, Integer> columns = new HashMap<>();

    Header(List<String> names) {
        for (int i = 0; i < names.size(); i++) {
            columns.put(names.get(i).trim(), i);
        }
    }

    String get(List<String> row, String column) {
        Integer index = columns.get(column);
        if (index == null) {
            throw new IllegalStateException("Missing column '" + column + "'");
        }
        return row.get(index).trim();
    }

    /** A column a data file may leave out (or leave empty). */
    String optional(List<String> row, String column) {
        Integer index = columns.get(column);
        if (index == null || index >= row.size() || row.get(index).isBlank()) {
            return null;
        }
        return row.get(index).trim();
    }

    int integer(List<String> row, String column) {
        return Integer.parseInt(get(row, column));
    }

    BigDecimal decimal(List<String> row, String column) {
        return new BigDecimal(get(row, column));
    }

    boolean bool(List<String> row, String column) {
        return Boolean.parseBoolean(get(row, column));
    }
}
