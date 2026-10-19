package com.shani.caradvisor.agent;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads a PDF payslip on our own server and keeps only what the comparison needs: payroll terms and their numbers.
 * It works by allowlist, not by blocklist: a line leaves only if it has a known payroll term, and from that line
 * only the term and the numbers around it leave. Names, ID numbers, addresses, bank details, the employer and every
 * other word never reach the model. Numbers next to a personal label (ID, bank, account...) are dropped too, and so
 * are numbers that look like identifiers (long digit runs, leading zeros) or years.
 */
final class PayslipText {

    /** The terms that may leave, longest first so "קרן פנסיה" wins over "פנסיה". */
    private static final List<String> TERMS = List.of(
            "השתתפות עובד ברכב", "השתתפות עובד רכב", "השתתפות ברכב", "השתתפות רכב", "ניכוי עובד לפנסיה",
            "משכורת בסיס", "שכר משולב", "שכר יסוד", "שכר בסיס", "תגמולי עובד", "קרן פנסיה", "קופת פנסיה",
            "קרן השתלמות", "קופת השתלמות", "נקודות זיכוי", "נק' זיכוי", "נק זיכוי", "שווי שימוש", "שווי רכב",
            "זקיפת רכב", "פרמיות", "פרמיה", "עמלות", "עמלה", "בונוס", "מענק", "פנסיה", "השתלמות", "השתתפות");

    /** Labels of personal details: a number next to one of these never leaves, even on a line with a payroll term. */
    private static final List<String> PERSONAL = List.of(
            "תעודת זהות", "מספר זהות", "ת.ז", "ת\"ז", "תז", "מספר עובד", "שם העובד", "שם", "כתובת", "בנק", "סניף",
            "חשבון", "תיק ניכויים", "טלפון", "נייד", "דוא\"ל", "מייל", "דרכון", "מיקוד");

    /** Other payroll lines: never sent, but they own the numbers next to them (so "מס הכנסה 3,443" is not read as salary). */
    private static final List<String> OTHER = List.of(
            "מס הכנסה", "ביטוח לאומי", "מס בריאות", "סה\"כ", "ברוטו", "נטו", "נסיעות", "הבראה", "ביגוד", "ותק",
            "שעות נוספות", "שעות", "ימים", "תעריף", "כמות", "מקדמה", "הלוואה", "ועד");

    private static final Pattern NUMBER = Pattern.compile("[-−]?\\d{1,3}(?:,\\d{3})+(?:\\.\\d+)?%?|[-−]?\\d+(?:\\.\\d+)?%?");
    private static final Pattern TOKEN = Pattern.compile(alternatives(TERMS) + "|" + alternatives(PERSONAL)
            + "|" + alternatives(OTHER) + "|" + NUMBER.pattern() + "|[\\p{L}]+");
    private static final int MAX_NUMBERS = 4;

    private PayslipText() {
    }

    /** What may leave from one PDF; empty when the PDF has no text layer (a scan), which is never sent at all. */
    static Optional<List<String>> fromPdf(byte[] pdf) {
        String text;
        try (PDDocument document = Loader.loadPDF(pdf)) {
            // content order keeps each table row on its own line; sorting by position would merge side-by-side tables
            text = new PDFTextStripper().getText(document);
        } catch (IOException e) {
            return Optional.empty();
        }
        if (text.replaceAll("\\s", "").length() < 20) {
            return Optional.empty();
        }
        return Optional.of(payrollLines(text));
    }

    /**
     * "18,500.00שכר יסוד" -> "שכר יסוד: 18,500.00"; a line without a payroll term gives nothing.
     * Each document writes its numbers on one side of the term (after it, or before it in the order some PDFs store
     * right-to-left text); numbers are taken from that side only, so two columns merged into one line do not mix.
     */
    static List<String> payrollLines(String text) {
        List<List<Token>> rows = text.lines().map(PayslipText::tokens).toList();
        int side = numberSide(rows);
        List<String> lines = new ArrayList<>();
        for (List<Token> tokens : rows) {
            for (int i = 0; i < tokens.size(); i++) {
                if (tokens.get(i).kind() != Kind.TERM) {
                    continue;
                }
                int first = i;
                StringBuilder label = new StringBuilder(tokens.get(i).text());
                while (i + 1 < tokens.size() && tokens.get(i + 1).kind() == Kind.TERM) {
                    label.append(' ').append(tokens.get(++i).text());
                }
                List<String> numbers = new ArrayList<>();
                if (side <= 0) {
                    collect(tokens, first - 1, -1, numbers);
                }
                if (side >= 0) {
                    collect(tokens, i + 1, 1, numbers);
                }
                if (!numbers.isEmpty()) {
                    lines.add(label + ": " + String.join(" ", numbers));
                }
            }
        }
        return lines;
    }

    /** 1 = numbers follow their terms, -1 = they come before them, 0 = cannot tell (both sides are read). */
    private static int numberSide(List<List<Token>> rows) {
        int after = 0;
        int before = 0;
        for (List<Token> tokens : rows) {
            for (int i = 0; i < tokens.size(); i++) {
                Kind kind = tokens.get(i).kind();
                if (kind != Kind.TERM && kind != Kind.OTHER) {
                    continue;
                }
                boolean next = i + 1 < tokens.size() && tokens.get(i + 1).kind() == Kind.NUMBER;
                boolean previous = i > 0 && tokens.get(i - 1).kind() == Kind.NUMBER;
                if (next && !previous) {
                    after++;
                } else if (previous && !next) {
                    before++;
                }
            }
        }
        return Integer.compare(after, before);
    }

    /** Numbers on one side of a term, up to the next term, a personal label or the end of the line. */
    private static void collect(List<Token> tokens, int from, int step, List<String> numbers) {
        int taken = 0;
        for (int j = from; j >= 0 && j < tokens.size() && taken < MAX_NUMBERS; j += step) {
            Token token = tokens.get(j);
            if (token.kind() != Kind.NUMBER && token.kind() != Kind.WORD) {
                return;
            }
            if (token.kind() == Kind.NUMBER && safe(token.text())) {
                numbers.add(token.text());
                taken++;
            }
        }
    }

    /** Not an identifier, an account or a year. */
    static boolean safe(String number) {
        String digits = number.replaceAll("[^0-9]", "");
        boolean plainInteger = number.matches("[-−]?\\d+");
        if (plainInteger && digits.length() >= 7) {
            return false;
        }
        if (plainInteger && digits.length() >= 3 && digits.startsWith("0")) {
            return false;
        }
        if (plainInteger && digits.length() == 4) {
            int value = Integer.parseInt(digits);
            return value < 1990 || value > 2100;
        }
        return true;
    }

    private enum Kind { TERM, PERSONAL, OTHER, NUMBER, WORD }

    private record Token(Kind kind, String text) {
    }

    private static List<Token> tokens(String line) {
        List<Token> tokens = new ArrayList<>();
        Matcher matcher = TOKEN.matcher(line);
        while (matcher.find()) {
            String text = matcher.group();
            Kind kind = TERMS.contains(text) ? Kind.TERM
                    : PERSONAL.contains(text) ? Kind.PERSONAL
                    : OTHER.contains(text) ? Kind.OTHER
                    : NUMBER.matcher(text).matches() ? Kind.NUMBER : Kind.WORD;
            tokens.add(new Token(kind, text));
        }
        return tokens;
    }

    private static String alternatives(List<String> words) {
        return words.stream().sorted((a, b) -> b.length() - a.length()).map(Pattern::quote)
                .reduce((a, b) -> a + "|" + b).orElseThrow();
    }
}
