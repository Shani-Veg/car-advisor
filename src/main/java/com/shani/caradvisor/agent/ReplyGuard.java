package com.shani.caradvisor.agent;

import java.util.Collection;
import java.util.Comparator;
import java.util.regex.Pattern;

/**
 * Stops amounts in the model's own words. Car names contain digits (MG4, ATTO 3, MODEL 3) and years are
 * harmless, so those are removed first; what remains may not contain a multi-digit number, a decimal,
 * or a digit next to % or ₪.
 */
public final class ReplyGuard {

    private static final Pattern YEAR = Pattern.compile("(?<!\\p{Nd})(19|20)\\p{Nd}{2}(?!\\p{Nd})");
    private static final Pattern AMOUNT = Pattern.compile("\\p{Nd}[\\p{Nd},.]*\\p{Nd}|\\p{Nd}\\s*[%₪]|[%₪]\\s*\\p{Nd}");

    /** Small models sometimes slip an Arabic letter into a Hebrew word. */
    private static final Pattern FOREIGN_SCRIPT = Pattern.compile("[\\u0600-\\u06FF\\u0750-\\u077F\\uFB50-\\uFDFF\\uFE70-\\uFEFF]");

    private ReplyGuard() {
    }

    public static boolean containsForeignScript(String reply) {
        return FOREIGN_SCRIPT.matcher(reply).find();
    }

    public static boolean containsAmount(String reply, Collection<String> safeNames) {
        String text = reply;
        for (String name : safeNames.stream().sorted(Comparator.comparingInt(String::length).reversed()).toList()) {
            text = Pattern.compile(Pattern.quote(name), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
                    .matcher(text).replaceAll(" ");
        }
        text = YEAR.matcher(text).replaceAll(" ");
        return AMOUNT.matcher(text).find();
    }
}
