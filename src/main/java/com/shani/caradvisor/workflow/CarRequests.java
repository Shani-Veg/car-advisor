package com.shani.caradvisor.workflow;

import com.shani.caradvisor.engine.Drive;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Short, plain requests for cars ("רכב חשמלי", "משהו יוקרתי", "היברידי עד 180 אלף", "מה הכי משתלם לי?") are
 * understood by code and answered at once, without the language model: faster, and the cars always show.
 * Anything with more in it (a question about a model, a comparison, a negation, other details) goes to the model.
 */
@Component
class CarRequests {

    /** What the employee asked to see. best: the cars that pay most; waiver: compare giving up the car. */
    record Request(Set<Drive> drives, BigDecimal maxListPrice, CarGuide.Order order, boolean best, boolean waiver) {
    }

    private static final int MAX_WORDS = 12;
    private static final BigDecimal THOUSAND = BigDecimal.valueOf(1000);
    private static final BigDecimal MIN_PRICE = BigDecimal.valueOf(50_000);
    private static final BigDecimal MAX_PRICE = BigDecimal.valueOf(1_000_000);

    private static final Pattern ELECTRIC = word("[והבלשמ]{0,2}חשמלי(?:ת|ים|ות)?");
    private static final Pattern PLUG_IN = word("[והבלשמ]{0,2}פלאג(?:[- ]?אין|ין)?");
    private static final Pattern HYBRID = word("[והבלשמ]{0,2}היברידי(?:ת|ים|ות)?|[והבלשמ]{0,2}היבריד");
    private static final Pattern PETROL = word("[והבלשמ]{0,2}בנזין");
    private static final Pattern LUXURY = word("[והבלשמ]{0,2}(?:יוקרתי(?:ת|ים|ות)?|יוקרה|מפואר(?:ת|ים|ות)?|פרימיום)");
    private static final Pattern CHEAP = word("[והבלשמ]{0,2}זול(?:ה|ים|ות)?");
    private static final Pattern BEST = Pattern.compile("הכי משתלם|משתלם לי|הכי כדאי");
    private static final Pattern WHAT_CARS = Pattern.compile("(?:אילו|איזה|אלו|מה)\\s+רכבים|רכבים\\s+(?:יש|אפשר|מותר)|מה\\s+יש\\s+לי");
    private static final Pattern PRICE = Pattern.compile("עד\\s*(\\d[\\d,]*(?:\\.\\d+)?)\\s*(אלף|א'|k)?");
    /** Asking for an explanation, a comparison or something negated: the model handles it. */
    private static final Pattern NOT_A_LIST = word("לא|בלי|אין|ללא|חוץ|מלבד|ההבדל|הבדל|למה|מדוע|איך|תסביר|הסבר|ספר|תספר|השווה|להשוות|לעומת|בין|עדיף");
    /** "אני רוצה לוותר על הרכב": the employee raises it, so the system compares it (it never suggests it). */
    private static final Pattern WAIVER = word("[והבלשמ]{0,2}(?:לוותר|ויתור|וויתור)");
    /** A model or maker named: a question about that car, for the model. */
    private static final Pattern NAMES_A_CAR = Pattern.compile("[A-Za-z]|טסלה|קיה|טויוטה|יונדאי|מזדה|סקודה|סיאט|אטו|דולפין|קורולה|נירו|קונה|טוסון|ספורטאז|קרניבל|סנטה|ראב|אוקטביה|ליאון|פיקנטו|יאריס|סיל");

    Optional<Request> parse(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        // "פלאג אין" is one word here, not a negation
        String text = raw.replaceAll("[?!.,:;\"״()]", " ").replaceAll("פלאג[- ]אין", "פלאגין").replaceAll("\\s+", " ").trim();
        if (text.isEmpty() || text.split(" ").length > MAX_WORDS || NOT_A_LIST.matcher(text).find()
                || NAMES_A_CAR.matcher(text).find()) {
            return Optional.empty();
        }
        Set<Drive> drives = EnumSet.noneOf(Drive.class);
        if (ELECTRIC.matcher(text).find()) {
            drives.add(Drive.ELECTRIC);
        }
        if (PLUG_IN.matcher(text).find()) {
            drives.add(Drive.PLUG_IN);
        }
        if (HYBRID.matcher(text).find()) {
            drives.add(Drive.HYBRID);
        }
        if (PETROL.matcher(text).find()) {
            drives.add(Drive.PETROL);
        }
        boolean luxury = LUXURY.matcher(text).find();
        boolean cheap = CHEAP.matcher(text).find();
        boolean best = BEST.matcher(text).find();
        BigDecimal price = price(text);
        boolean asksForCars = WHAT_CARS.matcher(text).find() || (price != null && text.contains("רכב"));
        if (WAIVER.matcher(text).find()) {
            // giving up the car next to a kind of car ("לוותר או לקחת חשמלי?") is a real question: for the model
            return drives.isEmpty() && !luxury && !cheap && !best && price == null
                    ? Optional.of(new Request(Set.of(), null, CarGuide.Order.BEST_FOR_NET, false, true))
                    : Optional.empty();
        }
        if (drives.isEmpty() && !luxury && !cheap && !best && !asksForCars) {
            return Optional.empty();
        }
        if (luxury && cheap) {
            return Optional.empty();
        }
        CarGuide.Order order = luxury ? CarGuide.Order.MOST_LUXURIOUS : cheap ? CarGuide.Order.CHEAPEST : CarGuide.Order.BEST_FOR_NET;
        boolean onlyBest = best && drives.isEmpty() && !luxury && !cheap && price == null;
        return Optional.of(new Request(Set.copyOf(drives), price, order, onlyBest, false));
    }

    /** "עד 200 אלף", "עד 200,000", "עד 200" (thousands): a list price, when it is a plausible one. */
    private static BigDecimal price(String text) {
        Matcher m = PRICE.matcher(text);
        if (!m.find()) {
            return null;
        }
        BigDecimal value = new BigDecimal(m.group(1).replace(",", ""));
        if (m.group(2) != null || value.compareTo(THOUSAND) < 0) {
            value = value.multiply(THOUSAND);
        }
        return value.compareTo(MIN_PRICE) >= 0 && value.compareTo(MAX_PRICE) <= 0 ? value : null;
    }

    private static Pattern word(String alternatives) {
        return Pattern.compile("(?:^|\\s)(?:" + alternatives + ")(?=\\s|$)");
    }
}
