package com.shani.caradvisor.workflow;

import com.shani.caradvisor.data.ReferenceData;
import com.shani.caradvisor.engine.Occupation;
import com.shani.caradvisor.engine.Vehicle;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Reads a short typed reply as the answer to the question the chat is asking ("24000" to "what is your salary?"),
 * in plain code, with no language model. So a direct answer works when the assistant is down, in a recording, and
 * costs no model call. Anything that is not clearly an answer is left to the assistant.
 */
@Component
class DirectAnswers {

    record Answer(FactKey key, String value) {
    }

    private static final BigDecimal MIN_BASE_SALARY = BigDecimal.valueOf(1_000);
    private static final Pattern NUMBER = Pattern.compile("\\d+(\\.\\d+)?");
    // a dot stays when it sits inside a number ("2.5")
    private static final Pattern PUNCTUATION = Pattern.compile("[,₪%!?;:()\\-]|(?<!\\d)\\.|\\.(?!\\d)");
    private static final Pattern QUOTES = Pattern.compile("[\"״׳']");
    /** Words that may surround an occupation or a car and still leave the reply a pure answer. */
    private static final Set<String> SAYING = Set.of("שלום", "היי", "אני", "עובד", "עובדת", "בתור", "בתפקיד", "תפקידי",
            "התפקיד", "שלי", "הוא", "זה", "נוהג", "נוהגת", "עם", "רכב", "הרכב", "יש", "לי", "מדובר");
    private static final Set<String> FILLER = Set.of("שכר", "משכורת", "בסיס", "ברוטו", "בערך", "שקל", "שקלים", "שח", "הוא",
            "זה", "בחודש", "לחודש", "אחוז", "אחוזים", "ילדים", "ילד", "ילדות", "יש", "לי", "רק", "נקודות", "זיכוי");
    private static final Map<String, Integer> COUNT_WORDS = Map.ofEntries(
            Map.entry("אין", 0), Map.entry("ללא", 0), Map.entry("אפס", 0), Map.entry("אחד", 1), Map.entry("אחת", 1),
            Map.entry("שני", 2), Map.entry("שניים", 2), Map.entry("שתי", 2), Map.entry("שתיים", 2),
            Map.entry("שלושה", 3), Map.entry("שלוש", 3), Map.entry("ארבעה", 4), Map.entry("ארבע", 4),
            Map.entry("חמישה", 5), Map.entry("חמש", 5));

    private final ReferenceData data;
    private final FactValues values;

    DirectAnswers(ReferenceData data, FactValues values) {
        this.data = data;
        this.values = values;
    }

    Optional<Answer> parse(FactKey pending, String text) {
        if (text == null || text.isBlank() || text.length() > 80) {
            return Optional.empty();
        }
        Optional<String> raw = switch (pending.type()) {
            case OCCUPATION -> occupation(text);
            case CURRENT_CAR -> car(text);
            case COUNT -> count(text);
            case MONEY, PERCENT, POINTS -> number(text).filter(n -> pending != FactKey.BASE_SALARY
                    || new BigDecimal(n).compareTo(MIN_BASE_SALARY) >= 0);
            case GROUP -> Optional.empty();
        };
        return raw.flatMap(value -> validated(pending, value));
    }

    private Optional<Answer> validated(FactKey key, String value) {
        try {
            return Optional.of(new Answer(key, values.normalize(key, value)));
        } catch (ApiException e) {
            return Optional.empty();
        }
    }

    private static List<String> words(String text) {
        List<String> words = new ArrayList<>();
        String plain = QUOTES.matcher(text.toLowerCase(Locale.ROOT)).replaceAll("");
        for (String word : PUNCTUATION.matcher(plain).replaceAll(" ").trim().split("\\s+")) {
            if (!word.isBlank()) {
                words.add(word);
            }
        }
        return words;
    }

    /** One number, optionally with filler words and thousands commas ("שכר 24,000 ש\"ח"). */
    private static Optional<String> number(String text) {
        String withoutThousands = text.replaceAll("(?<=\\d),(?=\\d{3})", "");
        List<String> rest = words(withoutThousands).stream().filter(w -> !FILLER.contains(w)).toList();
        return rest.size() == 1 && NUMBER.matcher(rest.get(0)).matches() ? Optional.of(rest.get(0)) : Optional.empty();
    }

    private static Optional<String> count(String text) {
        List<String> rest = words(text).stream().filter(w -> !FILLER.contains(w) || COUNT_WORDS.containsKey(w)).toList();
        if (rest.size() != 1) {
            return Optional.empty();
        }
        String word = rest.get(0);
        if (COUNT_WORDS.containsKey(word)) {
            return Optional.of(String.valueOf(COUNT_WORDS.get(word)));
        }
        return word.matches("\\d{1,2}") ? Optional.of(word) : Optional.empty();
    }

    /** "מנהלת מוצר" and "מנהל מוצר" both mean the table's "מנהל/ת מוצר"; the reply may carry other words around it. */
    private Optional<String> occupation(String text) {
        String said = String.join(" ", words(text));
        List<Occupation> matches = data.occupations().all().stream()
                .filter(o -> variants(o.name()).stream().anyMatch(v -> onlySaying(said, v)))
                .toList();
        return matches.size() == 1 ? Optional.of(matches.get(0).name()) : Optional.empty();
    }

    private static Set<String> variants(String name) {
        Set<String> variants = new HashSet<>();
        variants.add(String.join(" ", words(name.replaceAll("/\\S+", ""))));
        variants.add(String.join(" ", words(name.replace("/", ""))));
        return variants;
    }

    private static boolean contains(String said, String variant) {
        return (" " + said + " ").contains(" " + variant + " ");
    }

    /** The reply is the name and nothing but small talk around it; a longer message may carry more facts for the assistant. */
    private static boolean onlySaying(String said, String name) {
        if (!contains(said, name)) {
            return false;
        }
        String rest = (" " + said + " ").replace(" " + name + " ", " ").trim();
        return rest.isEmpty() || Arrays.stream(rest.split("\\s+")).allMatch(SAYING::contains);
    }

    /** A catalog model name typed in Latin letters ("Corolla Cross"), or "no car". */
    private Optional<String> car(String text) {
        String said = String.join(" ", words(text));
        if (said.equals("אין") || said.matches("(אין|בלי|ללא)( לי)? רכב( חברה)?")) {
            return Optional.of(FactValues.NO_CAR);
        }
        List<Vehicle> hits = data.catalog().vehicles().stream()
                .filter(v -> onlySaying(said, String.join(" ", words(v.commercialName()))))
                .toList();
        if (hits.isEmpty()) {
            return Optional.empty();
        }
        int longest = hits.stream().mapToInt(v -> v.commercialName().length()).max().orElse(0);
        List<Vehicle> best = hits.stream().filter(v -> v.commercialName().length() == longest).toList();
        return best.size() == 1 ? Optional.of(best.get(0).id()) : Optional.empty();
    }
}
