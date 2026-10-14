package com.shani.caradvisor.data;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Minimal CSV reader: comma separated, fields may be double-quoted, no line breaks inside fields. */
final class Csv {

    private Csv() {
    }

    static List<List<String>> read(Path file, Charset charset) {
        try {
            List<List<String>> rows = new ArrayList<>();
            for (String line : Files.readAllLines(file, charset)) {
                if (!line.isBlank()) {
                    rows.add(parseLine(line.replace("﻿", "")));
                }
            }
            return rows;
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + file, e);
        }
    }

    static List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quoted) {
                if (c == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else if (c == '"') {
                    quoted = false;
                } else {
                    current.append(c);
                }
            } else if (c == '"' && current.toString().isBlank()) {
                // Only a quote at the start of a field opens a quoted field. Mid-field quotes are
                // literal: Hebrew abbreviations in the tax file (e.g. maker names) contain them.
                quoted = true;
            } else if (c == ',') {
                fields.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString().trim());
        return fields;
    }
}
