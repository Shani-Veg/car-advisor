package com.shani.caradvisor.workflow;

import com.shani.caradvisor.data.ReferenceData;
import com.shani.caradvisor.engine.Vehicle;
import com.shani.caradvisor.workflow.Views.CaseView;
import com.shani.caradvisor.workflow.Views.ComparisonView;
import com.shani.caradvisor.workflow.Views.ProfileField;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * All cases, one row each, plus the trends Strauss's fleet managers would ask about: which cars people look at,
 * how many look at electric, what it does to their net, and how fast reps answer. For the rep screen's Excel export.
 * No salary and no message text: the trends do not need them.
 */
@Service
public class FleetReport {

    private final CaseRepository cases;
    private final QuestionRepository questions;
    private final CaseViewAssembler views;
    private final ReferenceData data;

    FleetReport(CaseRepository cases, QuestionRepository questions, CaseViewAssembler views, ReferenceData data) {
        this.cases = cases;
        this.questions = questions;
        this.views = views;
        this.data = data;
    }

    /** One case. Null fields: not known yet (e.g. no alternative chosen, details incomplete). */
    public record Row(Instant createdAt, String status, String occupation, String group, Integer children,
                      String currentCar, String alternative, String drive, Boolean eligible, BigDecimal netToday,
                      BigDecimal netAlternative, BigDecimal difference, BigDecimal alternativeCost,
                      BigDecimal budget, BigDecimal participation, String participationStatus, String repQuestion,
                      String answeredBy, Double answerHours) {
    }

    /** A group of compared cases (by car, by drive, by eligibility group). */
    public record Bucket(String label, String detail, int cases, BigDecimal averageDifference) {
    }

    /** aboveEligibility: cases that looked at a car above their group (shown no numbers): demand worth knowing. */
    public record Trends(int cases, int compared, int electric, int waivers, int aboveEligibility,
                         BigDecimal averageDifference,
                         int repQuestions, int repAnswered, int repWaiting, Double averageAnswerHours,
                         List<Bucket> byCar, List<Bucket> byDrive, List<Bucket> byGroup) {
    }

    public record Report(Instant generatedAt, List<Row> rows, Trends trends) {
    }

    @Transactional(readOnly = true)
    public Report build() {
        List<Row> rows = cases.findAll().stream()
                .sorted(Comparator.comparing(CaseEntity::createdAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::row)
                .toList();
        return new Report(Instant.now(), rows, trends(rows));
    }

    private Row row(CaseEntity caseEntity) {
        CaseView view = views.caseView(caseEntity);
        ComparisonView c = view.comparison();
        boolean car = caseEntity.alternative() == CaseEntity.Alternative.CAR;
        Vehicle vehicle = car ? data.catalog().byId(caseEntity.alternativeVehicleId()).orElse(null) : null;
        String alternative = switch (caseEntity.alternative()) {
            case CAR -> vehicle == null ? caseEntity.alternativeVehicleId() : vehicle.label();
            case WAIVER -> "ויתור על הרכב";
            case NONE -> null;
        };
        RepQuestionEntity question = questions.findByCaseIdOrderByCreatedAtAsc(caseEntity.id()).stream()
                .reduce((first, second) -> second).orElse(null);
        return new Row(
                caseEntity.createdAt(),
                STATUS.getOrDefault(view.status(), view.status()),
                display(view, "occupation"),
                display(view, "group"),
                children(view),
                display(view, "currentCar"),
                alternative,
                vehicle == null ? null : vehicle.drive().hebrew(),
                c == null ? null : c.eligible(),
                c == null || c.today() == null ? null : c.today().net(),
                c == null || c.alternative() == null ? null : c.alternative().net(),
                c == null ? null : c.difference(),
                vehicle == null ? null : vehicle.monthlyCost(),
                c == null || c.budget() == null ? null : c.budget().amount(),
                c == null || c.alternative() == null ? null : c.alternative().participation(),
                c == null || c.alternative() == null ? null
                        : PARTICIPATION.get(c.alternative().participationStatus()),
                question == null ? "לא נשלחה" : question.stale() ? "התיישנה"
                        : question.status() == RepQuestionEntity.Status.ANSWERED ? "נענתה" : "ממתינה",
                question == null ? null : question.answeredBy(),
                question == null || question.answeredAt() == null ? null
                        : hours(Duration.between(question.createdAt(), question.answeredAt())));
    }

    private static Trends trends(List<Row> rows) {
        List<Row> compared = rows.stream().filter(r -> r.difference() != null).toList();
        List<Row> withCar = compared.stream().filter(r -> r.drive() != null).toList();
        List<Row> asked = rows.stream().filter(r -> !"לא נשלחה".equals(r.repQuestion())).toList();
        List<Double> answerHours = rows.stream().map(Row::answerHours).filter(Objects::nonNull).toList();
        return new Trends(
                rows.size(),
                compared.size(),
                (int) withCar.stream().filter(r -> "חשמלי".equals(r.drive())).count(),
                (int) compared.stream().filter(r -> "ויתור על הרכב".equals(r.alternative())).count(),
                (int) rows.stream().filter(r -> Boolean.FALSE.equals(r.eligible())).count(),
                average(compared),
                asked.size(),
                (int) asked.stream().filter(r -> "נענתה".equals(r.repQuestion())).count(),
                (int) asked.stream().filter(r -> "ממתינה".equals(r.repQuestion())).count(),
                answerHours.isEmpty() ? null : answerHours.stream().mapToDouble(Double::doubleValue).average().orElse(0),
                buckets(withCar, Row::alternative, Row::drive),
                buckets(withCar, Row::drive, r -> null),
                buckets(compared, Row::group, r -> null));
    }

    /** Most cases first; ties by name, so the sheet reads the same every time. */
    private static List<Bucket> buckets(List<Row> rows, Function<Row, String> key, Function<Row, String> detail) {
        Map<String, List<Row>> grouped = rows.stream().filter(r -> key.apply(r) != null)
                .collect(Collectors.groupingBy(key, LinkedHashMap::new, Collectors.toList()));
        List<Bucket> buckets = new ArrayList<>();
        grouped.forEach((label, group) ->
                buckets.add(new Bucket(label, detail.apply(group.get(0)), group.size(), average(group))));
        buckets.sort(Comparator.comparingInt(Bucket::cases).reversed().thenComparing(Bucket::label));
        return buckets;
    }

    private static BigDecimal average(List<Row> rows) {
        if (rows.isEmpty()) {
            return null;
        }
        BigDecimal sum = rows.stream().map(Row::difference).reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(rows.size()), 0, RoundingMode.HALF_UP);
    }

    private static double hours(Duration duration) {
        return Math.round(duration.toMinutes() / 6.0) / 10.0;
    }

    private static Integer children(CaseView view) {
        return view.profile().stream().filter(f -> f.key().equals("childrenUpTo23")).map(ProfileField::value)
                .filter(Number.class::isInstance).map(v -> ((Number) v).intValue()).findFirst().orElse(null);
    }

    private static String display(CaseView view, String key) {
        return view.profile().stream().filter(f -> f.key().equals(key)).map(ProfileField::display)
                .filter(text -> text != null && !text.isBlank()).findFirst().orElse(null);
    }

    private static final Map<String, String> STATUS = Map.of(
            "COLLECTING", "באיסוף פרטים",
            "READY", "אפשר להשוות",
            "WAITING_FOR_REP", "ממתין לנציג");

    private static final Map<String, String> PARTICIPATION = Map.of(
            "DEFAULT", "הערכה",
            "PROVIDED", "הוזן",
            "CONFIRMED", "אושר בידי נציג");
}
