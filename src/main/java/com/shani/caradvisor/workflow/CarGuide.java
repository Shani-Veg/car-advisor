package com.shani.caradvisor.workflow;

import com.shani.caradvisor.data.ReferenceData;
import com.shani.caradvisor.engine.CarChoice;
import com.shani.caradvisor.engine.Comparison;
import com.shani.caradvisor.engine.ComparisonEngine;
import com.shani.caradvisor.engine.Drive;
import com.shani.caradvisor.engine.Money;
import com.shani.caradvisor.engine.Vehicle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Helps the employee find their way in the catalog: which cars of a kind they may take, what one model means for
 * them, and how a few models compare. Every sentence with a number is written here, from the catalog and the engine,
 * so the chat can show real cars and amounts without the language model writing a single number. Works the same with
 * the model on, off, or replaying a recording.
 */
@Service
@Transactional(readOnly = true)
public class CarGuide {

    /** What the chat shows: the sentence, the cars to show as cards under it, and a summary for the model (no amounts). */
    public record Answer(String text, List<String> cars, List<Map<String, Object>> forModel) {
    }

    private static final int MAX_CARDS = 6;
    private static final int MAX_OTHERS = 3;

    private final CaseRepository cases;
    private final CaseViewAssembler views;
    private final ReferenceData data;
    private final ComparisonEngine engine;

    CarGuide(CaseRepository cases, CaseViewAssembler views, ReferenceData data, ComparisonEngine engine) {
        this.cases = cases;
        this.views = views;
        this.data = data;
        this.engine = engine;
    }

    /**
     * Cars of the given drive types (all when empty), optionally up to a list price, and optionally losing at most
     * maxNetDecrease a month. Allowed cars first, best for the net; cars above the employee's group are only named.
     */
    public Answer find(String caseId, Set<Drive> drives, BigDecimal maxListPrice, BigDecimal maxNetDecrease) {
        CaseState state = state(caseId);
        Map<String, BigDecimal> effect = effects(state);
        String today = state.fact(FactKey.CURRENT_CAR).map(FactEntity::value).orElse("");
        List<Vehicle> matching = data.catalog().vehicles().stream()
                .filter(v -> drives.isEmpty() || drives.contains(v.drive()))
                .filter(v -> maxListPrice == null || v.listPrice().compareTo(maxListPrice) <= 0)
                .filter(v -> !v.id().equals(today))
                .toList();
        List<Vehicle> allowed = new ArrayList<>(matching.stream()
                .filter(state::mayTake)
                .filter(v -> maxNetDecrease == null || effect.get(v.id()) == null
                        || effect.get(v.id()).compareTo(maxNetDecrease.negate()) >= 0)
                .toList());
        List<Vehicle> others = matching.stream().filter(v -> !state.mayTake(v)).toList();
        boolean personal = !effect.isEmpty();
        allowed.sort(personal
                ? Comparator.comparing((Vehicle v) -> effect.getOrDefault(v.id(), BigDecimal.ZERO)).reversed()
                : Comparator.comparing(Vehicle::listPrice));

        String what = kind(drives) + conditions(maxListPrice, maxNetDecrease);
        StringBuilder text = new StringBuilder();
        if (allowed.isEmpty()) {
            text.append("לא מצאתי ").append(what).append(" שזמינים לך.");
        } else {
            // The cards under the message show each car with its numbers; the text only frames them.
            text.append(what).append(" שזמינים לך").append(personal ? ", מהמשתלם ביותר לנטו:" : ":");
            if (allowed.size() > MAX_CARDS) {
                text.append("\nועוד ").append(allowed.size() - MAX_CARDS).append(" ברשימת החלופות במסך.");
            }
        }
        if (!others.isEmpty()) {
            text.append("\nבקטלוג יש גם ").append(others.stream().limit(MAX_OTHERS)
                    .map(v -> v.label() + " (" + groupName(v) + ")").collect(Collectors.joining(", ")))
                    .append(", מחוץ לזכאות שלך.");
        }
        if (!allowed.isEmpty()) {
            text.append(personal ? "\nלחצו על \"פרטים\" כדי להבין דגם, או על \"השוואה\" כדי לראות אותו מול הרכב שלך היום."
                    : "\nכשאדע את כל הפרטים שלך, אראה גם מה כל רכב עושה לנטו.");
        }
        List<Map<String, Object>> forModel = new ArrayList<>();
        allowed.stream().limit(MAX_CARDS).forEach(v -> forModel.add(modelView(v, effect.get(v.id()), true)));
        others.stream().limit(MAX_OTHERS).forEach(v -> forModel.add(modelView(v, null, false)));
        return new Answer(text.toString(), allowed.stream().limit(MAX_CARDS).map(Vehicle::id).toList(), forModel);
    }

    /** One model, in the terms that matter for the decision, and what it means for this employee. */
    public Answer describe(String caseId, String vehicleId) {
        CaseState state = state(caseId);
        Vehicle v = vehicle(vehicleId);
        Map<String, BigDecimal> effect = effects(state);
        Optional<Vehicle> today = todayCar(state);
        StringBuilder text = new StringBuilder(v.label() + ", " + v.maker() + ":");
        text.append("\n• הנעה: ").append(driveExplanation(v.drive()));
        text.append("\n• ").append(groupLine(state, v));
        text.append("\n• מחיר מחירון: ").append(Money.display(v.listPrice()));
        text.append("\n• שווי שימוש: ").append(Money.display(v.monthlyUseValue())).append(" בחודש, שנוסף לשכר לצורך המס")
                .append(today.filter(t -> !t.id().equals(v.id()))
                        .map(t -> " (ברכב שלך היום: " + Money.display(t.monthlyUseValue()) + ")").orElse(""));
        text.append("\n• השתתפות: ").append(Money.display(v.defaultParticipation()))
                .append(" בחודש לפי הקטלוג. נציג התחבורה מאשר את הסכום הסופי");
        text.append("\n• ").append(personalLine(state, v, effect.get(v.id()), today));
        if (v.officialUrl() != null) {
            text.append("\nלמפרט המלא (טווח, גודל, אבזור) יש קישור לעמוד הדגם באתר היבואן.");
        }
        return new Answer(text.toString(), List.of(v.id()), List.of(modelView(v, effect.get(v.id()), state.mayTake(v))));
    }

    /** Two to four models side by side, with the one that is best for the net named. */
    public Answer compare(String caseId, List<String> vehicleIds) {
        List<Vehicle> chosen = vehicleIds.stream().distinct().map(this::vehicle).limit(4).toList();
        if (chosen.size() < 2) {
            throw ApiException.badRequest("COMPARE_NEEDS_TWO", "כדי להשוות צריך לפחות שני דגמים");
        }
        CaseState state = state(caseId);
        Map<String, BigDecimal> effect = effects(state);
        StringBuilder text = new StringBuilder("השוואה בין " + chosen.stream().map(Vehicle::label).collect(Collectors.joining(", ")) + ":");
        for (Vehicle v : chosen) {
            text.append("\n• ").append(v.label()).append(": ").append(v.drive().hebrew()).append(", ").append(groupName(v))
                    .append(", מחיר מחירון ").append(Money.display(v.listPrice()))
                    .append(", שווי שימוש ").append(Money.display(v.monthlyUseValue()));
            if (!state.mayTake(v)) {
                text.append(". לא בזכאות שלך");
            } else if (effect.containsKey(v.id())) {
                text.append(". הנטו ").append(signedPerMonth(effect.get(v.id())));
            }
        }
        chosen.stream().filter(v -> effect.containsKey(v.id()))
                .max(Comparator.comparing(v -> effect.get(v.id())))
                .filter(best -> chosen.stream().filter(v -> effect.containsKey(v.id())).count() > 1)
                .ifPresent(best -> text.append("\nהכי משתלם לנטו מבין אלה: ").append(best.label())
                        .append(". זה מיון לפי הנטו בלבד, בלי שיקולים כמו גודל או טווח."));
        List<Map<String, Object>> forModel = chosen.stream().map(v -> modelView(v, effect.get(v.id()), state.mayTake(v))).toList();
        return new Answer(text.toString(), chosen.stream().map(Vehicle::id).toList(), forModel);
    }

    /** The change in net for every car the employee may take, once their details are complete. Empty before that. */
    private Map<String, BigDecimal> effects(CaseState state) {
        if (!state.missingForToday().isEmpty()) {
            return Map.of();
        }
        Map<String, BigDecimal> effect = new LinkedHashMap<>();
        for (Comparison c : engine.rankAlternatives(state.employee(), state.occupation(), state.todayChoice(),
                data.catalog().vehicles())) {
            if (c.alternative().choice() instanceof CarChoice.CompanyCar car) {
                effect.put(car.vehicle().id(), c.difference());
            }
        }
        return effect;
    }

    static String driveExplanation(Drive drive, BigDecimal evBonus) {
        return switch (drive) {
            case ELECTRIC -> "חשמלי. נוסע על סוללה בלבד ונטען מהחשמל. בשטראוס מגיע עליו החזר חשמל של "
                    + Money.display(evBonus) + " נטו בחודש";
            case PLUG_IN -> "פלאג-אין. מנוע בנזין וסוללה שנטענת גם מהשקע, כך שאפשר לנסוע מרחק קצר על חשמל בלבד. "
                    + "החזר החשמל של שטראוס חל רק על רכב חשמלי מלא";
            case HYBRID -> "היברידי. מנוע בנזין ומנוע חשמלי שעובדים יחד. הסוללה נטענת בזמן הנסיעה, לא מהשקע";
            case PETROL -> "בנזין. מנוע בנזין בלבד";
        };
    }

    private String driveExplanation(Drive drive) {
        return driveExplanation(drive, data.rules().electric().netBonus());
    }

    private String groupLine(CaseState state, Vehicle v) {
        String mine = state.fact(FactKey.GROUP).map(FactEntity::value).orElse(null);
        if (v.isFamilyTrack()) {
            return "מסלול ברוכת ילדים: למי שיש לו לפחות " + data.rules().familyTrack().minChildren()
                    + " ילדים עד גיל " + data.rules().familyTrack().childMaxAge()
                    + (state.mayTake(v) ? "" : ". לפי מספר הילדים שמסרת, הוא לא בזכאות שלך");
        }
        if (mine == null) {
            return "קבוצה " + v.group();
        }
        return "קבוצה " + v.group() + (state.mayTake(v) ? " (בזכאות שלך: קבוצה " + mine + ")" : ", מעל הקבוצה שלך (" + mine + ")");
    }

    private String personalLine(CaseState state, Vehicle v, BigDecimal difference, Optional<Vehicle> today) {
        if (!state.mayTake(v)) {
            return "הרכב לא בזכאות שלך, ולכן אין חישוב נטו";
        }
        if (today.map(t -> t.id().equals(v.id())).orElse(false)) {
            return "זה הרכב שלך היום";
        }
        if (difference == null) {
            List<String> missing = state.missingForToday().stream().map(FactKey::label).toList();
            return "כדי לחשב מה הוא עושה לנטו שלך חסרים עוד: " + String.join(", ", missing);
        }
        return "אצלך: הנטו " + signedPerMonth(difference) + " לעומת היום (לפי השתתפות משוערת)";
    }

    private Map<String, Object> modelView(Vehicle v, BigDecimal difference, boolean allowed) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("vehicleId", v.id());
        view.put("option", v.label() + " " + v.maker());
        view.put("drive", v.drive().hebrew());
        view.put("group", v.group());
        view.put("allowed", allowed);
        if (difference != null) {
            view.put("effectOnNet", difference.signum() > 0 ? "higher than today" : difference.signum() < 0 ? "lower than today" : "same as today");
        }
        return view;
    }

    private static String signedPerMonth(BigDecimal amount) {
        if (amount.signum() == 0) {
            return "לא משתנה";
        }
        return (amount.signum() > 0 ? "עולה ב-" : "יורד ב-") + Money.display(amount.abs()) + " בחודש";
    }

    private static String groupName(Vehicle v) {
        return v.isFamilyTrack() ? "מסלול ברוכת ילדים" : "קבוצה " + v.group();
    }

    private static String kind(Collection<Drive> drives) {
        if (drives.isEmpty()) {
            return "רכבים";
        }
        return drives.stream().sorted().map(d -> switch (d) {
            case ELECTRIC -> "רכבים חשמליים";
            case PLUG_IN -> "רכבי פלאג-אין";
            case HYBRID -> "רכבים היברידיים";
            case PETROL -> "רכבי בנזין";
        }).collect(Collectors.joining(" או "));
    }

    private static String conditions(BigDecimal maxListPrice, BigDecimal maxNetDecrease) {
        String text = "";
        if (maxListPrice != null) {
            text += " עד מחיר מחירון של " + Money.display(maxListPrice);
        }
        if (maxNetDecrease != null) {
            text += (text.isEmpty() ? "" : ",") + " שהנטו שלך לא יורד בהם ביותר מ-" + Money.display(maxNetDecrease);
        }
        return text;
    }

    /** The catalog name of a car, as the chat writes it. */
    public String label(String vehicleId) {
        return vehicle(vehicleId).label();
    }

    private Optional<Vehicle> todayCar(CaseState state) {
        return state.fact(FactKey.CURRENT_CAR).map(FactEntity::value).flatMap(id -> data.catalog().byId(id));
    }

    private Vehicle vehicle(String id) {
        return data.catalog().byId(id == null ? "" : id)
                .orElseThrow(() -> ApiException.badRequest("UNKNOWN_VEHICLE", "הרכב " + id + " לא נמצא בקטלוג"));
    }

    private CaseState state(String caseId) {
        return views.state(cases.findById(caseId)
                .orElseThrow(() -> ApiException.notFound("UNKNOWN_CASE", "התיק לא נמצא")));
    }
}
