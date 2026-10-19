package com.shani.caradvisor.workflow;

import com.shani.caradvisor.agent.LlmClient;
import com.shani.caradvisor.data.ReferenceData;
import com.shani.caradvisor.engine.CarChoice;
import com.shani.caradvisor.engine.Comparison;
import com.shani.caradvisor.engine.ComparisonEngine;
import com.shani.caradvisor.engine.Factor;
import com.shani.caradvisor.engine.Vehicle;
import com.shani.caradvisor.workflow.Views.BreakdownView;
import com.shani.caradvisor.workflow.Views.BudgetView;
import com.shani.caradvisor.workflow.Views.CarCard;
import com.shani.caradvisor.workflow.Views.ChoiceView;
import com.shani.caradvisor.workflow.Views.CaseView;
import com.shani.caradvisor.workflow.Views.ComparisonView;
import com.shani.caradvisor.workflow.Views.FactorView;
import com.shani.caradvisor.workflow.Views.KnownItem;
import com.shani.caradvisor.workflow.Views.MessageView;
import com.shani.caradvisor.workflow.Views.PendingQuestion;
import com.shani.caradvisor.workflow.Views.ProfileField;
import com.shani.caradvisor.workflow.Views.RankingAlternative;
import com.shani.caradvisor.workflow.Views.RankingItem;
import com.shani.caradvisor.workflow.Views.RepAnswer;
import com.shani.caradvisor.workflow.Views.RepContext;
import com.shani.caradvisor.workflow.Views.RepQuestionView;
import com.shani.caradvisor.workflow.Views.SideView;
import com.shani.caradvisor.workflow.Views.SourceView;
import com.shani.caradvisor.workflow.Views.SuggestedAnswer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Builds the API views. Comparisons and rankings are computed on every read from the stored facts,
 * never stored: a stale or half-written result cannot exist, and a restart loses nothing.
 */
@Component
class CaseViewAssembler {

    private final ReferenceData data;
    private final ComparisonEngine engine;
    private final FactValues values;
    private final FactRepository facts;
    private final MessageRepository messages;
    private final QuestionRepository questions;
    private final ChoiceOptions choiceOptions;
    private final boolean assistantEnabled;
    private final String assistantMode;
    private final boolean payslipEnabled;

    CaseViewAssembler(ReferenceData data, ComparisonEngine engine, FactValues values, FactRepository facts,
                      MessageRepository messages, QuestionRepository questions, ChoiceOptions choiceOptions, LlmClient llm,
                      @Value("${advisor.assistant.enabled:true}") boolean assistantEnabled,
                      @Value("${advisor.payslip.enabled:true}") boolean payslipEnabled) {
        this.data = data;
        this.engine = engine;
        this.values = values;
        this.facts = facts;
        this.messages = messages;
        this.questions = questions;
        this.choiceOptions = choiceOptions;
        this.assistantEnabled = assistantEnabled && llm.isConfigured();
        this.assistantMode = llm.mode();
        this.payslipEnabled = payslipEnabled;
    }

    CaseState state(CaseEntity caseEntity) {
        Map<FactKey, FactEntity> known = facts.findByCaseId(caseEntity.id()).stream()
                .collect(Collectors.toMap(FactEntity::factKey, Function.identity()));
        return new CaseState(caseEntity, known, questions.findByCaseIdOrderByCreatedAtAsc(caseEntity.id()), data);
    }

    boolean assistantAvailable() {
        return assistantEnabled;
    }

    CaseView caseView(CaseEntity caseEntity) {
        CaseState state = state(caseEntity);
        return new CaseView(
                caseEntity.id(),
                caseEntity.revision(),
                state.status(),
                assistantEnabled,
                assistantMode,
                payslipEnabled,
                Arrays.stream(FactKey.values()).map(key -> profileField(state, key)).toList(),
                messageViews(caseEntity, state),
                state.canCompare() ? comparison(state) : null,
                state.missingForToday().isEmpty() ? ranking(state) : null,
                state.questions().stream()
                        .filter(RepQuestionEntity::isPending)
                        .map(q -> new PendingQuestion(q.id(), q.factKey().apiName(), q.question(), "OPEN", q.createdAt()))
                        .toList(),
                state.reviewPending() ? state.assumedForReview().stream().map(FactKey::apiName).toList() : List.of());
    }

    private List<MessageView> messageViews(CaseEntity caseEntity, CaseState state) {
        List<MessageEntity> all = messages.findByCaseIdOrderByIdAsc(caseEntity.id());
        List<ChoiceView> choices = offersFor(state, all).stream().map(o -> new ChoiceView(o.id(), o.label())).toList();
        Map<String, java.math.BigDecimal> effects = all.stream().anyMatch(m -> !m.cars().isEmpty()) ? effects(state) : Map.of();
        List<MessageView> result = new ArrayList<>();
        for (int i = 0; i < all.size(); i++) {
            MessageEntity m = all.get(i);
            List<CarCard> cards = m.cars().stream()
                    .map(id -> data.catalog().byId(id).map(v -> card(v, state, effects)).orElse(null))
                    .filter(java.util.Objects::nonNull)
                    .toList();
            result.add(new MessageView("m-" + m.id(), m.role().name().toLowerCase(), m.text(), m.createdAt(),
                    i == all.size() - 1 ? choices : List.of(), cards));
        }
        return result;
    }

    /** Numbers on a card come from the engine at read time, like every other number. */
    private static CarCard card(Vehicle v, CaseState state, Map<String, java.math.BigDecimal> effects) {
        boolean allowed = state.mayTake(v);
        return new CarCard(v.id(), v.label(), v.maker(), v.drive().hebrew(), v.group(), v.listPrice(), allowed,
                allowed ? effects.get(v.id()) : null, v.officialUrl());
    }

    private Map<String, java.math.BigDecimal> effects(CaseState state) {
        if (!state.missingForToday().isEmpty()) {
            return Map.of();
        }
        Map<String, java.math.BigDecimal> effect = new java.util.HashMap<>();
        for (Comparison c : engine.rankAlternatives(state.employee(), state.occupation(), state.todayChoice(),
                data.catalog().vehicles())) {
            if (c.alternative().choice() instanceof CarChoice.CompanyCar car) {
                effect.put(car.vehicle().id(), c.difference());
            }
        }
        return effect;
    }

    List<ChoiceOptions.Offer> offersFor(CaseState state, CaseEntity caseEntity) {
        return offersFor(state, messages.findByCaseIdOrderByIdAsc(caseEntity.id()));
    }

    /**
     * Answer buttons belong to the bot's latest message only, and are always computed from the case as it is now.
     * They stay consistent with the question because the server writes a follow-up question whenever a form edit
     * moves the first missing detail (see ChoiceService.editProfile).
     */
    private List<ChoiceOptions.Offer> offersFor(CaseState state, List<MessageEntity> all) {
        if (all.isEmpty()) {
            return List.of();
        }
        MessageEntity last = all.get(all.size() - 1);
        if (last.role() != MessageEntity.Role.BOT) {
            return List.of();
        }
        if (last.continueOnly() && (!state.missingForToday().isEmpty() || state.reviewPending())) {
            return List.of(new ChoiceOptions.Offer(ChoiceOptions.CONTINUE, "נמשיך בשאלות"));
        }
        return choiceOptions.offeredFor(state, last.offers());
    }

    RepQuestionView repView(RepQuestionEntity question, CaseEntity caseEntity) {
        CaseState state = state(caseEntity);
        FactKey key = question.factKey();
        RepAnswer answer = question.status() == RepQuestionEntity.Status.ANSWERED
                ? new RepAnswer(values.typed(key, question.answerValue()), values.display(key, question.answerValue()),
                question.effectiveFrom(), question.note(), question.answeredBy(), question.answeredAt())
                : null;
        return new RepQuestionView(
                question.id(),
                question.caseId(),
                employeeLabel(state),
                key.apiName(),
                key.label(),
                question.question(),
                repContext(state, key),
                question.status().name(),
                question.stale(),
                question.createdAt(),
                answer);
    }

    private ProfileField profileField(CaseState state, FactKey key) {
        return state.fact(key)
                .map(f -> new ProfileField(key.apiName(), key.label(), values.typed(key, f.value()), display(key, f),
                        f.status().name(), new SourceView(f.source().name(), f.sourceRef(), f.updatedAt())))
                .orElse(new ProfileField(key.apiName(), key.label(), null, "", "MISSING", null));
    }

    private String display(FactKey key, FactEntity fact) {
        String text = values.display(key, fact.value());
        return fact.effectiveFrom() == null ? text : text + " (בתוקף מ-" + FactValues.displayDate(fact.effectiveFrom()) + ")";
    }

    private ComparisonView comparison(CaseState state) {
        Comparison c = engine.compare(state.employee(), state.occupation(), state.todayChoice(), state.alternativeChoice());
        SideView today = side(c.today().choice(), c.netToday());
        if (!c.eligible()) {
            return new ComparisonView(c.rulesVersion(), false, c.eligibility().reason(), today,
                    side(state.alternativeChoice(), null),
                    null, "NONE", List.of(), null, state.estimates(), ExplanationWriter.explain(c), null);
        }
        int sign = c.difference().signum();
        return new ComparisonView(
                c.rulesVersion(),
                true,
                c.eligibility().reason(),
                today,
                side(c.alternative().choice(), c.netAlternative()),
                c.difference(),
                sign > 0 ? "GAIN" : sign < 0 ? "LOSS" : "NONE",
                c.factors().stream().map(CaseViewAssembler::factorView).toList(),
                new BreakdownView(c.today().result(), c.alternative().result()),
                state.estimates(),
                ExplanationWriter.explain(c),
                budget(state, c.alternative().choice()));
    }

    /** Strauss's rules: a budget per group; a cheaper car does not turn the rest into pay; the family track splits the gap. */
    private BudgetView budget(CaseState state, CarChoice alternative) {
        String group = state.fact(FactKey.GROUP).map(FactEntity::value).orElse(null);
        if (group == null) {
            return null;
        }
        java.math.BigDecimal amount = data.rules().budgetOf(group);
        String note;
        if (!(alternative instanceof CarChoice.CompanyCar car)) {
            note = "בוויתור אין רכב, ולכן גם אין שימוש בתקציב: במקומו מקבלים אחזקת רכב לפי טבלת העיסוקים.";
        } else if (car.vehicle().monthlyCost().compareTo(amount) <= 0) {
            note = "הרכב בתוך התקציב. תקציב שלא נוצל לא הופך לכסף בשכר.";
        } else if (car.vehicle().isFamilyTrack()) {
            note = "הרכב יקר מהתקציב. על הפער העובד/ת משלם/ת חצי, והוא מופיע בפירוט כחלק העובד במסלול המשפחתי.";
        } else {
            note = "עלות הרכב גבוהה מתקציב הקבוצה.";
        }
        return new BudgetView(group, amount, note);
    }

    /** type and vehicleId let the screen recognise which card is being compared, without guessing from names. */
    private static SideView side(CarChoice choice, java.math.BigDecimal net) {
        return choice instanceof CarChoice.CompanyCar car
                ? new SideView(choice.label(), net, car.vehicle().officialUrl(), "car", car.vehicle().id(), car.vehicle().monthlyCost())
                : new SideView(choice.label(), net, null, "waiver", null, null);
    }

    private static String officialUrl(CarChoice choice) {
        return choice instanceof CarChoice.CompanyCar car ? car.vehicle().officialUrl() : null;
    }

    private static FactorView factorView(Factor f) {
        return new FactorView(f.component().name(), f.label(), f.from(), f.to(), f.netImpact());
    }

    private List<RankingItem> ranking(CaseState state) {
        String today = state.fact(FactKey.CURRENT_CAR).map(FactEntity::value).orElse("");
        return engine.rankAlternatives(state.employee(), state.occupation(), state.todayChoice(), data.catalog().vehicles())
                .stream()
                .filter(c -> !(c.alternative().choice() instanceof CarChoice.CompanyCar car && car.vehicle().id().equals(today)))
                .sorted(java.util.Comparator.comparing((Comparison c) -> c.alternative().choice() instanceof CarChoice.NoCar))
                .map(c -> {
                    CarChoice choice = c.alternative().choice();
                    if (choice instanceof CarChoice.CompanyCar car) {
                        Vehicle v = car.vehicle();
                        return new RankingItem(new RankingAlternative("car", v.id(), v.label(), v.group(),
                                v.drive().hebrew(), v.officialUrl()), c.difference(), true);
                    }
                    return new RankingItem(new RankingAlternative("waiver", null, choice.label(), null, null, null),
                            c.difference(), false);
                })
                .toList();
    }

    /** No names: the rep needs the role and group, not who the employee is. */
    private String employeeLabel(CaseState state) {
        StringBuilder label = new StringBuilder("תיק " + state.caseEntity().id().substring(0, 6));
        state.fact(FactKey.OCCUPATION).ifPresent(f -> label.append(" · ").append(f.value()));
        state.fact(FactKey.GROUP).ifPresent(f -> label.append(" · קבוצה ").append(f.value()));
        return label.toString();
    }

    private RepContext repContext(CaseState state, FactKey asked) {
        List<KnownItem> known = new ArrayList<>();
        state.fact(FactKey.CURRENT_CAR).ifPresent(car -> {
            String text = values.display(FactKey.CURRENT_CAR, car.value());
            text += state.fact(FactKey.CURRENT_PARTICIPATION)
                    .map(p -> ", השתתפות " + values.display(FactKey.CURRENT_PARTICIPATION, p.value()))
                    .orElse("");
            known.add(new KnownItem("רכב היום", text));
        });
        if (state.caseEntity().alternative() != CaseEntity.Alternative.NONE) {
            known.add(new KnownItem("חלופה", state.caseEntity().alternative() == CaseEntity.Alternative.WAIVER
                    ? "ויתור על רכב"
                    : state.vehicle(state.caseEntity().alternativeVehicleId()).label()));
        }
        return state.fact(asked)
                .filter(f -> f.status() == FactEntity.Status.DEFAULT)
                .map(f -> new RepContext(known,
                        "בינתיים ההשוואה מחושבת לפי ברירת המחדל בקטלוג: " + values.display(asked, f.value()),
                        new SuggestedAnswer(values.typed(asked, f.value()), values.display(asked, f.value()),
                                f.source().name())))
                .orElse(new RepContext(known, null, null));
    }
}
