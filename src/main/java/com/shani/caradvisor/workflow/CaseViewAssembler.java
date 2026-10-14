package com.shani.caradvisor.workflow;

import com.shani.caradvisor.agent.LlmClient;
import com.shani.caradvisor.data.ReferenceData;
import com.shani.caradvisor.engine.CarChoice;
import com.shani.caradvisor.engine.Comparison;
import com.shani.caradvisor.engine.ComparisonEngine;
import com.shani.caradvisor.engine.Factor;
import com.shani.caradvisor.engine.Vehicle;
import com.shani.caradvisor.workflow.Views.BreakdownView;
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

    CaseViewAssembler(ReferenceData data, ComparisonEngine engine, FactValues values, FactRepository facts,
                      MessageRepository messages, QuestionRepository questions, ChoiceOptions choiceOptions, LlmClient llm,
                      @Value("${advisor.assistant.enabled:true}") boolean assistantEnabled) {
        this.data = data;
        this.engine = engine;
        this.values = values;
        this.facts = facts;
        this.messages = messages;
        this.questions = questions;
        this.choiceOptions = choiceOptions;
        this.assistantEnabled = assistantEnabled && llm.isConfigured();
        this.assistantMode = llm.mode();
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
                Arrays.stream(FactKey.values()).map(key -> profileField(state, key)).toList(),
                messageViews(caseEntity, state),
                state.canCompare() ? comparison(state) : null,
                state.missingForToday().isEmpty() ? ranking(state) : null,
                state.questions().stream()
                        .filter(RepQuestionEntity::isPending)
                        .map(q -> new PendingQuestion(q.id(), q.factKey().apiName(), q.question(), "OPEN", q.createdAt()))
                        .toList());
    }

    private List<MessageView> messageViews(CaseEntity caseEntity, CaseState state) {
        List<MessageEntity> all = messages.findByCaseIdOrderByIdAsc(caseEntity.id());
        List<ChoiceView> choices = offersFor(state, all).stream().map(o -> new ChoiceView(o.id(), o.label())).toList();
        List<MessageView> result = new ArrayList<>();
        for (int i = 0; i < all.size(); i++) {
            MessageEntity m = all.get(i);
            result.add(new MessageView("m-" + m.id(), m.role().name().toLowerCase(), m.text(), m.createdAt(),
                    i == all.size() - 1 ? choices : List.of()));
        }
        return result;
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
        return choiceOptions.offeredFor(state);
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
        return fact.effectiveFrom() == null ? text : text + " (בתוקף מ-" + fact.effectiveFrom() + ")";
    }

    private ComparisonView comparison(CaseState state) {
        Comparison c = engine.compare(state.employee(), state.occupation(), state.todayChoice(), state.alternativeChoice());
        SideView today = side(c.today().choice(), c.netToday());
        if (!c.eligible()) {
            return new ComparisonView(c.rulesVersion(), false, c.eligibility().reason(), today,
                    side(state.alternativeChoice(), null),
                    null, "NONE", List.of(), null, state.estimates(), ExplanationWriter.explain(c));
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
                ExplanationWriter.explain(c));
    }

    /** type and vehicleId let the screen recognise which card is being compared, without guessing from names. */
    private static SideView side(CarChoice choice, java.math.BigDecimal net) {
        return choice instanceof CarChoice.CompanyCar car
                ? new SideView(choice.label(), net, car.vehicle().officialUrl(), "car", car.vehicle().id())
                : new SideView(choice.label(), net, null, "waiver", null);
    }

    private static String officialUrl(CarChoice choice) {
        return choice instanceof CarChoice.CompanyCar car ? car.vehicle().officialUrl() : null;
    }

    private static FactorView factorView(Factor f) {
        return new FactorView(f.component().name(), f.label(), f.from(), f.to(), f.netImpact());
    }

    private List<RankingItem> ranking(CaseState state) {
        return engine.rankAlternatives(state.employee(), state.occupation(), state.todayChoice(), data.catalog().vehicles())
                .stream()
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
