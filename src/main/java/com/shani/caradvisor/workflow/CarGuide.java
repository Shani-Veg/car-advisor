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
 * Texts come in one layout: the first line is a short heading shown above the car cards, and any further lines are
 * shown under them. So the cars are always near the top of the answer.
 *
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
            text.append("לא מצאתי ").append(what).append(" בזכאות שלך.");
        } else {
            text.append(what).append(" בזכאות שלך").append(personal ? ", מהמשתלם לנטו:" : ":");
            if (allowed.size() > MAX_CARDS) {
                text.append("\nועוד ").append(allowed.size() - MAX_CARDS).append(" ברשימת החלופות.");
            }
            if (!personal) {
                text.append("\nכשאדע את כל הפרטים שלך, אראה גם מה כל רכב עושה לנטו.");
            }
        }
        if (!others.isEmpty()) {
            text.append("\nמחוץ לזכאות שלך: ").append(others.stream().limit(MAX_OTHERS)
                    .map(v -> v.label() + " (" + groupName(v) + ")").collect(Collectors.joining(", "))).append(".");
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
        StringBuilder text = new StringBuilder("על " + v.label() + " של " + v.maker() + ":");
        text.append("\n• ").append(driveExplanation(v.drive()));
        text.append("\n• ").append(groupLine(state, v)).append(" · מחיר מחירון ").append(Money.display(v.listPrice()));
        text.append("\n• שווי שימוש ").append(Money.display(v.monthlyUseValue())).append(" בחודש")
                .append(today.filter(t -> !t.id().equals(v.id()))
                        .map(t -> " (היום: " + Money.display(t.monthlyUseValue()) + ")").orElse(""));
        text.append("\n• השתתפות ").append(Money.display(v.defaultParticipation())).append(" לפי הקטלוג, עד אישור הנציג");
        text.append("\n• ").append(personalLine(state, v, effect.get(v.id()), today));
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
        StringBuilder text = new StringBuilder("השוואה בין " + chosen.stream().map(Vehicle::label).collect(Collectors.joining(" ל-")) + ":");
        chosen.stream().filter(v -> effect.containsKey(v.id()))
                .max(Comparator.comparing(v -> effect.get(v.id())))
                .filter(best -> chosen.stream().filter(v -> effect.containsKey(v.id())).count() > 1)
                .ifPresent(best -> text.append("\nהכי משתלם לנטו: ").append(best.label()).append(" (לפי הנטו בלבד, בלי גודל או טווח)."));
        text.append("\nשווי שימוש בחודש: ").append(chosen.stream()
                .map(v -> v.label() + " " + Money.display(v.monthlyUseValue())).collect(Collectors.joining(", "))).append(".");
        List<Map<String, Object>> forModel = chosen.stream().map(v -> modelView(v, effect.get(v.id()), state.mayTake(v))).toList();
        return new Answer(text.toString(), chosen.stream().map(Vehicle::id).toList(), forModel);
    }

    /** What pays best for the net: the best cars as cards, and giving up the car when it is an option. Today's car is not an option. */
    public Answer best(String caseId) {
        CaseState state = state(caseId);
        if (!state.missingForToday().isEmpty()) {
            return new Answer("כדי לדעת מה הכי משתלם חסרים עוד: "
                    + state.missingForToday().stream().map(FactKey::label).collect(Collectors.joining(", ")) + ".", List.of(), List.of());
        }
        String today = state.fact(FactKey.CURRENT_CAR).map(FactEntity::value).orElse("");
        List<Comparison> ranked = engine.rankAlternatives(state.employee(), state.occupation(), state.todayChoice(),
                data.catalog().vehicles());
        List<Vehicle> cars = ranked.stream()
                .filter(c -> c.alternative().choice() instanceof CarChoice.CompanyCar)
                .map(c -> ((CarChoice.CompanyCar) c.alternative().choice()).vehicle())
                .filter(v -> !v.id().equals(today))
                .limit(3)
                .toList();
        Optional<Comparison> waiver = ranked.stream().filter(c -> c.alternative().choice() instanceof CarChoice.NoCar).findFirst();
        boolean waiverFirst = waiver.isPresent() && (cars.isEmpty() || waiver.get().difference().compareTo(
                ranked.stream().filter(c -> c.alternative().choice() instanceof CarChoice.CompanyCar car && car.vehicle().id().equals(cars.get(0).id()))
                        .findFirst().orElseThrow().difference()) > 0);
        StringBuilder text = new StringBuilder(waiverFirst
                ? "הכי משתלם לנטו: ויתור על הרכב, הנטו " + signedPerMonth(waiver.get().difference()) + ". ומבין הרכבים:"
                : "הרכבים הכי משתלמים לנטו שלך:");
        if (waiver.isPresent() && !waiverFirst) {
            text.append("\nויתור על הרכב: הנטו ").append(signedPerMonth(waiver.get().difference())).append(".");
        }
        text.append("\nזה מיון לפי הנטו בלבד, לפי השתתפות משוערת.");
        Map<String, BigDecimal> effect = effects(state);
        List<Map<String, Object>> forModel = new ArrayList<>(cars.stream().map(v -> modelView(v, effect.get(v.id()), true)).toList());
        waiver.ifPresent(w -> forModel.add(Map.of("option", "giving up the car", "effectOnNet",
                w.difference().signum() > 0 ? "higher than today" : "lower than today")));
        return new Answer(text.toString(), cars.stream().map(Vehicle::id).toList(), forModel);
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
            case ELECTRIC -> "חשמלי: נטען מהחשמל בלבד. בשטראוס מגיע עליו החזר חשמל של " + Money.display(evBonus) + " נטו בחודש";
            case PLUG_IN -> "פלאג-אין: בנזין וסוללה שנטענת מהשקע, למרחק קצר על חשמל. החזר החשמל של שטראוס רק לחשמלי מלא";
            case HYBRID -> "היברידי: בנזין וחשמל יחד. נטען בזמן הנסיעה, לא מהשקע";
            case PETROL -> "בנזין בלבד";
        };
    }

    private String driveExplanation(Drive drive) {
        return driveExplanation(drive, data.rules().electric().netBonus());
    }

    private String groupLine(CaseState state, Vehicle v) {
        String mine = state.fact(FactKey.GROUP).map(FactEntity::value).orElse(null);
        if (v.isFamilyTrack()) {
            return "מסלול ברוכת ילדים (מ-" + data.rules().familyTrack().minChildren() + " ילדים)"
                    + (state.mayTake(v) ? "" : ", לא בזכאות שלך");
        }
        if (mine == null) {
            return "קבוצה " + v.group();
        }
        return "קבוצה " + v.group() + (state.mayTake(v) ? ", בזכאות שלך" : ", מעל הקבוצה שלך (" + mine + ")");
    }

    private String personalLine(CaseState state, Vehicle v, BigDecimal difference, Optional<Vehicle> today) {
        if (!state.mayTake(v)) {
            return "לא בזכאות שלך, ולכן אין חישוב נטו";
        }
        if (today.map(t -> t.id().equals(v.id())).orElse(false)) {
            return "זה הרכב שלך היום";
        }
        if (difference == null) {
            List<String> missing = state.missingForToday().stream().map(FactKey::label).toList();
            return "כדי לחשב מה הוא עושה לנטו שלך חסרים עוד: " + String.join(", ", missing);
        }
        return "אצלך: הנטו " + signedPerMonth(difference);
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
