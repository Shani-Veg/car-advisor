package com.shani.caradvisor.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A small Excel (.xlsx) writer: right-to-left sheets, text and numbers, a few fixed styles.
 * Enough for the rep's export, without pulling a large library into a 512 MB server.
 */
final class Xlsx {

    /** Cell styles, by their index in styles.xml. */
    enum Style { PLAIN, HEADER, TITLE, SHEKEL, PERCENT, DECIMAL, NOTE }

    record Cell(Object value, Style style) {
    }

    static final class Sheet {
        private final String name;
        private final double[] widths;
        private final List<List<Cell>> rows = new ArrayList<>();
        private boolean table;

        Sheet(String name, double... widths) {
            this.name = name;
            this.widths = widths;
        }

        Sheet row(Cell... cells) {
            rows.add(List.of(cells));
            return this;
        }

        Sheet blank() {
            rows.add(List.of());
            return this;
        }

        /** The first row is a header: frozen while scrolling, with filter buttons. */
        Sheet asTable() {
            table = true;
            return this;
        }
    }

    /** A plain cell: text, or a number (counts) without a format. */
    static Cell text(Object value) {
        return new Cell(value, Style.PLAIN);
    }

    static Cell cell(Object value, Style style) {
        return new Cell(value, style);
    }

    static byte[] write(List<Sheet> sheets) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes, StandardCharsets.UTF_8)) {
            put(zip, "[Content_Types].xml", contentTypes(sheets.size()));
            put(zip, "_rels/.rels", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">\
                    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>\
                    </Relationships>""");
            put(zip, "xl/workbook.xml", workbook(sheets));
            put(zip, "xl/_rels/workbook.xml.rels", workbookRels(sheets.size()));
            put(zip, "xl/styles.xml", STYLES);
            for (int i = 0; i < sheets.size(); i++) {
                put(zip, "xl/worksheets/sheet" + (i + 1) + ".xml", sheet(sheets.get(i)));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bytes.toByteArray();
    }

    private static void put(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String contentTypes(int sheets) {
        StringBuilder xml = new StringBuilder("""
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">\
                <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>\
                <Default Extension="xml" ContentType="application/xml"/>\
                <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>\
                <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>""");
        for (int i = 1; i <= sheets; i++) {
            xml.append("<Override PartName=\"/xl/worksheets/sheet").append(i)
                    .append(".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>");
        }
        return xml.append("</Types>").toString();
    }

    private static String workbook(List<Sheet> sheets) {
        StringBuilder xml = new StringBuilder("""
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" \
                xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">\
                <bookViews><workbookView/></bookViews><sheets>""");
        for (int i = 0; i < sheets.size(); i++) {
            xml.append("<sheet name=\"").append(escape(sheets.get(i).name)).append("\" sheetId=\"").append(i + 1)
                    .append("\" r:id=\"rId").append(i + 1).append("\"/>");
        }
        xml.append("</sheets>");
        // a table sheet's filter buttons need this defined name
        for (int i = 0; i < sheets.size(); i++) {
            Sheet sheet = sheets.get(i);
            if (sheet.table && !sheet.rows.isEmpty()) {
                xml.append(i == firstTable(sheets) ? "<definedNames>" : "")
                        .append("<definedName name=\"_xlnm._FilterDatabase\" localSheetId=\"").append(i)
                        .append("\" hidden=\"1\">'").append(escape(sheet.name).replace("'", "''")).append("'!")
                        .append(range(sheet)).append("</definedName>");
            }
        }
        if (firstTable(sheets) >= 0) {
            xml.append("</definedNames>");
        }
        return xml.append("</workbook>").toString();
    }

    private static int firstTable(List<Sheet> sheets) {
        for (int i = 0; i < sheets.size(); i++) {
            if (sheets.get(i).table && !sheets.get(i).rows.isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    private static String workbookRels(int sheets) {
        StringBuilder xml = new StringBuilder("""
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">""");
        for (int i = 1; i <= sheets; i++) {
            xml.append("<Relationship Id=\"rId").append(i)
                    .append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet")
                    .append(i).append(".xml\"/>");
        }
        xml.append("<Relationship Id=\"rId").append(sheets + 1)
                .append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>");
        return xml.append("</Relationships>").toString();
    }

    private static String sheet(Sheet sheet) {
        StringBuilder xml = new StringBuilder("""
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""");
        xml.append("<sheetViews><sheetView rightToLeft=\"1\" workbookViewId=\"0\"");
        xml.append(sheet.table
                ? "><pane ySplit=\"1\" topLeftCell=\"A2\" activePane=\"bottomLeft\" state=\"frozen\"/></sheetView>"
                : "/>");
        xml.append("</sheetViews><cols>");
        for (int i = 0; i < sheet.widths.length; i++) {
            xml.append("<col min=\"").append(i + 1).append("\" max=\"").append(i + 1).append("\" width=\"")
                    .append(sheet.widths[i]).append("\" customWidth=\"1\"/>");
        }
        xml.append("</cols><sheetData>");
        for (int r = 0; r < sheet.rows.size(); r++) {
            List<Cell> row = sheet.rows.get(r);
            xml.append("<row r=\"").append(r + 1).append("\">");
            for (int c = 0; c < row.size(); c++) {
                Cell cell = row.get(c);
                if (cell == null || cell.value() == null) {
                    continue;
                }
                String ref = column(c) + (r + 1);
                int style = cell.style().ordinal();
                if (cell.value() instanceof Number number) {
                    xml.append("<c r=\"").append(ref).append("\" s=\"").append(style).append("\"><v>")
                            .append(new BigDecimal(number.toString()).toPlainString()).append("</v></c>");
                } else {
                    xml.append("<c r=\"").append(ref).append("\" s=\"").append(style)
                            .append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
                            .append(escape(cell.value().toString())).append("</t></is></c>");
                }
            }
            xml.append("</row>");
        }
        xml.append("</sheetData>");
        if (sheet.table && !sheet.rows.isEmpty()) {
            xml.append("<autoFilter ref=\"").append(range(sheet).replace("$", "")).append("\"/>");
        }
        return xml.append("</worksheet>").toString();
    }

    private static String range(Sheet sheet) {
        int columns = sheet.rows.stream().mapToInt(List::size).max().orElse(1);
        return "$A$1:$" + column(columns - 1) + "$" + sheet.rows.size();
    }

    static String column(int index) {
        StringBuilder name = new StringBuilder();
        for (int i = index; i >= 0; i = i / 26 - 1) {
            name.insert(0, (char) ('A' + i % 26));
        }
        return name.toString();
    }

    static String escape(String text) {
        StringBuilder out = new StringBuilder(text.length());
        for (char ch : text.toCharArray()) {
            switch (ch) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append("&quot;");
                default -> {
                    // characters XML 1.0 does not allow are dropped rather than breaking the file
                    if (ch >= 0x20 || ch == '\t' || ch == '\n' || ch == '\r') {
                        out.append(ch);
                    }
                }
            }
        }
        return out.toString();
    }

    /** The order of cellXfs matches Style. */
    private static final String STYLES = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">\
            <numFmts count="2">\
            <numFmt numFmtId="164" formatCode="#,##0 &quot;₪&quot;;[Red]-#,##0 &quot;₪&quot;"/>\
            <numFmt numFmtId="165" formatCode="0.0"/>\
            </numFmts>\
            <fonts count="4">\
            <font><sz val="11"/><name val="Arial"/></font>\
            <font><b/><sz val="11"/><color rgb="FFFFFFFF"/><name val="Arial"/></font>\
            <font><b/><sz val="16"/><color rgb="FF142F66"/><name val="Arial"/></font>\
            <font><i/><sz val="10"/><color rgb="FF5B6475"/><name val="Arial"/></font>\
            </fonts>\
            <fills count="3">\
            <fill><patternFill patternType="none"/></fill>\
            <fill><patternFill patternType="gray125"/></fill>\
            <fill><patternFill patternType="solid"><fgColor rgb="FF1E4FA8"/><bgColor indexed="64"/></patternFill></fill>\
            </fills>\
            <borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders>\
            <cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>\
            <cellXfs count="7">\
            <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>\
            <xf numFmtId="0" fontId="1" fillId="2" borderId="0" xfId="0" applyFont="1" applyFill="1"/>\
            <xf numFmtId="0" fontId="2" fillId="0" borderId="0" xfId="0" applyFont="1"/>\
            <xf numFmtId="164" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1"/>\
            <xf numFmtId="9" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1"/>\
            <xf numFmtId="165" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1"/>\
            <xf numFmtId="0" fontId="3" fillId="0" borderId="0" xfId="0" applyFont="1"/>\
            </cellXfs>\
            <cellStyles count="1"><cellStyle name="Normal" xfId="0" builtinId="0"/></cellStyles>\
            </styleSheet>""";

    private Xlsx() {
    }
}
