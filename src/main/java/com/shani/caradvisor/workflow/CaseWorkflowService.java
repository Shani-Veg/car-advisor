package com.shani.caradvisor.workflow;

import com.shani.caradvisor.data.ReferenceData;
import com.shani.caradvisor.engine.Occupation;
import com.shani.caradvisor.engine.Vehicle;
import com.shani.caradvisor.workflow.Views.CaseView;
import com.shani.caradvisor.workflow.Views.RepQuestionView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * The one place that changes a case. The chat agent, the employee form and the rep screen all call
 * these commands, so every path validates, stores and invalidates the same way.
 */
@Service
@Transactional
public class CaseWorkflowService {

    static final String WELCOME = "שלום! אעזור לבדוק מה יקרה לנטו שלך אם תחליף/י רכב או תוותר/י עליו. "
            + "אפשר לכתוב לי, או למלא את הפרטים בטופס.";

    /**
     * Secondary details a conversation should not have to ask about. They are common values, stored as estimates
     * (status DEFAULT) so every screen marks them, until the employee gives the real number.
     */
    private static final Map<FactKey, String> ASSUMED = new LinkedHashMap<>();
    /** Source reference of an assumed detail (the source type stays CATALOG_DEFAULT: a stored enum is not widened). */
    static final String ASSUMPTION_REF = "assumption";

    static {
        ASSUMED.put(FactKey.COMMISSIONS, "0");
        ASSUMED.put(FactKey.BONUS, "0");
        ASSUMED.put(FactKey.PENSION_PERCENT, "6");
        ASSUMED.put(FactKey.STUDY_FUND_PERCENT, "2.5");
        ASSUMED.put(FactKey.CREDIT_POINTS, "2.25");
    }

    private final CaseRepository cases;
    private final FactRepository facts;
    private final MessageRepository messages;
    private final QuestionRepository questions;
    private final FactValues values;
    private final ReferenceData data;
    private final CaseViewAssembler views;

    CaseWorkflowService(CaseRepository cases, FactRepository facts, MessageRepository messages,
                        QuestionRepository questions, FactValues values, ReferenceData data, CaseViewAssembler views) {
        this.cases = cases;
        this.facts = facts;
        this.messages = messages;
        this.questions = questions;
        this.values = values;
        this.data = data;
        this.views = views;
    }

    public CaseView create() {
        CaseEntity created = cases.save(new CaseEntity(UUID.randomUUID().toString(), Instant.now()));
        messages.save(new MessageEntity(created.id(), MessageEntity.Role.BOT, WELCOME, null, Instant.now()));
        ASSUMED.forEach((key, value) ->
                applyFact(created, key, values.normalize(key, value), FactEntity.Source.CATALOG_DEFAULT, ASSUMPTION_REF, null));
        return views.caseView(created);
    }

    @Transactional(readOnly = true)
    public CaseView view(String caseId) {
        return views.caseView(load(caseId));
    }

    public boolean assistantAvailable() {
        return views.assistantAvailable();
    }

    public CaseView setFact(String caseId, FactKey key, Object rawValue, FactEntity.Source source, String sourceRef,
                            Long expectedVersion) {
        requireRepFor(key, source);
        CaseEntity caseEntity = load(caseId);
        checkVersion(caseEntity, expectedVersion);
        applyFact(caseEntity, key, values.normalize(key, rawValue), source, sourceRef, null);
        caseEntity.touch(Instant.now());
        return views.caseView(caseEntity);
    }

    /** For the chat: the model often repeats facts it already sent. Returns false when nothing changed. */
    public boolean setFactIfChanged(String caseId, FactKey key, Object rawValue, FactEntity.Source source,
                                    String sourceRef) {
        requireRepFor(key, source);
        CaseEntity caseEntity = load(caseId);
        String value = values.normalize(key, rawValue);
        Optional<FactEntity> existing = facts.findByCaseIdAndFactKey(caseId, key);
        if (existing.isPresent() && existing.get().value().equals(value)
                && existing.get().status() != FactEntity.Status.DEFAULT) {
            return false;
        }
        applyFact(caseEntity, key, value, source, sourceRef, null);
        caseEntity.touch(Instant.now());
        return true;
    }

    /** Only the transport rep knows the participation for a new car; anyone else's value would look confirmed. */
    private static void requireRepFor(FactKey key, FactEntity.Source source) {
        if (key == FactKey.ALTERNATIVE_PARTICIPATION && source != FactEntity.Source.REP) {
            throw ApiException.badRequest("REP_ONLY_FIELD", "את ההשתתפות ברכב החלופי מאשר נציג התחבורה");
        }
    }

    public CaseView chooseAlternative(String caseId, String type, String vehicleId, Long expectedVersion) {
        CaseEntity caseEntity = load(caseId);
        checkVersion(caseEntity, expectedVersion);
        String before = caseEntity.alternativeSubject();
        if ("waiver".equals(type)) {
            caseEntity.chooseWaiver();
        } else if ("car".equals(type)) {
            Vehicle vehicle = data.catalog().byId(vehicleId == null ? "" : vehicleId).orElseThrow(() ->
                    ApiException.badRequest("UNKNOWN_VEHICLE", "הרכב " + vehicleId + " לא נמצא בקטלוג"));
            caseEntity.chooseCar(vehicle.id());
        } else {
            throw ApiException.badRequest("INVALID_ALTERNATIVE", "סוג חלופה לא מוכר: " + type);
        }

        if (!Objects.equals(before, caseEntity.alternativeSubject())) {
            staleQuestions(caseEntity.id(), FactKey.ALTERNATIVE_PARTICIPATION);
            if (caseEntity.alternative() == CaseEntity.Alternative.CAR) {
                Vehicle vehicle = data.catalog().byId(caseEntity.alternativeVehicleId()).orElseThrow();
                applyFact(caseEntity, FactKey.ALTERNATIVE_PARTICIPATION, vehicle.defaultParticipation().toPlainString(),
                        FactEntity.Source.CATALOG_DEFAULT, vehicle.id(), null);
                if (mayTake(caseEntity, vehicle)) {
                    askRep(caseEntity, FactKey.ALTERNATIVE_PARTICIPATION,
                            "מה סכום ההשתתפות של העובד/ת ב-" + vehicle.label() + "?", vehicle.id());
                }
            } else {
                facts.findByCaseIdAndFactKey(caseEntity.id(), FactKey.ALTERNATIVE_PARTICIPATION).ifPresent(facts::delete);
            }
        }
        caseEntity.touch(Instant.now());
        return views.caseView(caseEntity);
    }

    /** Stores the employee's chat message once. A retried request (same clientRequestId) returns empty. */
    public Optional<Long> recordEmployeeMessage(String caseId, String text, String clientRequestId) {
        CaseEntity caseEntity = load(caseId);
        if (text == null || text.isBlank()) {
            throw ApiException.badRequest("EMPTY_MESSAGE", "ההודעה ריקה");
        }
        if (text.length() > 2000) {
            throw ApiException.badRequest("MESSAGE_TOO_LONG", "ההודעה ארוכה מדי");
        }
        if (clientRequestId != null
                && messages.findByCaseIdAndClientRequestId(caseId, clientRequestId).isPresent()) {
            return Optional.empty();
        }
        MessageEntity saved = messages.save(new MessageEntity(caseId, MessageEntity.Role.EMPLOYEE, text.trim(),
                clientRequestId, Instant.now()));
        caseEntity.touch(Instant.now());
        return Optional.of(saved.id());
    }

    public void addBotMessage(String caseId, String text) {
        addBotMessage(caseId, text, false);
    }

    /** continueOnly: the reply answered a side question, so the chat offers to carry on instead of repeating the buttons. */
    public void addBotMessage(String caseId, String text, boolean continueOnly) {
        CaseEntity caseEntity = load(caseId);
        MessageEntity message = new MessageEntity(caseId, MessageEntity.Role.BOT, text, null, Instant.now());
        if (continueOnly) {
            message.markContinueOnly();
        }
        messages.save(message);
        caseEntity.touch(Instant.now());
    }

    @Transactional(readOnly = true)
    public List<RepQuestionView> repQuestions(RepQuestionEntity.Status status) {
        return questions.findByStatusOrderByCreatedAtAsc(status).stream()
                .map(q -> views.repView(q, load(q.caseId())))
                .toList();
    }

    public RepQuestionView answer(String questionId, Object rawValue, LocalDate effectiveFrom, String note,
                                  String clientRequestId, String answeredBy) {
        RepQuestionEntity question = questions.findById(questionId).orElseThrow(() ->
                ApiException.notFound("UNKNOWN_QUESTION", "השאלה לא נמצאה"));
        CaseEntity caseEntity = load(question.caseId());
        if (question.status() == RepQuestionEntity.Status.ANSWERED) {
            if (clientRequestId != null && clientRequestId.equals(question.answerRequestId())) {
                return views.repView(question, caseEntity);
            }
            throw ApiException.conflict("ALREADY_ANSWERED", "השאלה כבר נענתה");
        }
        if (question.stale()) {
            throw ApiException.conflict("STALE_QUESTION", "העובד/ת שינה/תה את הבחירה, והשאלה כבר לא רלוונטית");
        }
        String value = values.normalize(question.factKey(), rawValue);
        question.answer(value, effectiveFrom, note, clientRequestId, answeredBy, Instant.now());
        applyFact(caseEntity, question.factKey(), value, FactEntity.Source.REP, question.id(), effectiveFrom);
        messages.save(new MessageEntity(caseEntity.id(), MessageEntity.Role.SYSTEM,
                "נציג התחבורה עדכן: " + question.factKey().label() + " " + values.display(question.factKey(), value)
                        + (effectiveFrom == null ? "" : ", בתוקף מ-" + effectiveFrom) + ". ההשוואה עודכנה.",
                null, Instant.now()));
        caseEntity.touch(Instant.now());
        return views.repView(question, caseEntity);
    }

    private void applyFact(CaseEntity caseEntity, FactKey key, String value, FactEntity.Source source, String sourceRef,
                           LocalDate effectiveFrom) {
        if (key == FactKey.GROUP) {
            requireGroupMatchesOccupation(caseEntity, value);
        }
        FactEntity fact = facts.findByCaseIdAndFactKey(caseEntity.id(), key)
                .orElseGet(() -> new FactEntity(caseEntity.id(), key));
        fact.set(value, source, sourceRef, effectiveFrom, Instant.now());
        facts.save(fact);

        if (key == FactKey.OCCUPATION) {
            defaultGroupFromOccupation(caseEntity, value);
        }
        if (key == FactKey.CURRENT_CAR) {
            staleQuestions(caseEntity.id(), FactKey.CURRENT_PARTICIPATION);
            if (FactValues.NO_CAR.equals(value)) {
                facts.findByCaseIdAndFactKey(caseEntity.id(), FactKey.CURRENT_PARTICIPATION).ifPresent(facts::delete);
            } else {
                assumeParticipation(caseEntity, value);
            }
        }
    }

    /** Until the employee says what they pay today, assume the catalog's usual participation for that car. */
    private void assumeParticipation(CaseEntity caseEntity, String vehicleId) {
        FactEntity existing = facts.findByCaseIdAndFactKey(caseEntity.id(), FactKey.CURRENT_PARTICIPATION).orElse(null);
        if (existing != null && existing.status() != FactEntity.Status.DEFAULT) {
            return;
        }
        data.catalog().byId(vehicleId).ifPresent(vehicle -> {
            FactEntity fact = existing != null ? existing : new FactEntity(caseEntity.id(), FactKey.CURRENT_PARTICIPATION);
            fact.set(vehicle.defaultParticipation().toPlainString(), FactEntity.Source.CATALOG_DEFAULT, vehicle.id(),
                    null, Instant.now());
            facts.save(fact);
        });
    }

    /**
     * The occupation table is the authority on the eligibility group: it fills the group, and overrides one the
     * employee stated that does not match. Nobody talks their way into a higher group.
     */
    private void defaultGroupFromOccupation(CaseEntity caseEntity, String occupationName) {
        Occupation occupation = data.occupations().byName(occupationName).orElseThrow();
        FactEntity group = facts.findByCaseIdAndFactKey(caseEntity.id(), FactKey.GROUP).orElse(null);
        if (group == null || !group.value().equals(occupation.group())) {
            FactEntity fact = group != null ? group : new FactEntity(caseEntity.id(), FactKey.GROUP);
            fact.set(occupation.group(), FactEntity.Source.CATALOG_DEFAULT, "occupation", null, Instant.now());
            facts.save(fact);
        }
    }

    private void requireGroupMatchesOccupation(CaseEntity caseEntity, String group) {
        facts.findByCaseIdAndFactKey(caseEntity.id(), FactKey.OCCUPATION).ifPresent(occupationFact -> {
            Occupation occupation = data.occupations().byName(occupationFact.value()).orElseThrow();
            if (!occupation.group().equals(group)) {
                throw ApiException.badRequest("GROUP_MISMATCH", "לפי טבלת העיסוקים, " + occupation.name()
                        + " שייך/ת לקבוצה " + occupation.group() + ". שינוי קבוצת זכאות נעשה מול משאבי אנוש.");
            }
        });
    }

    /** No rep question for a car the employee is known not to be allowed. */
    private boolean mayTake(CaseEntity caseEntity, Vehicle vehicle) {
        CaseState state = views.state(caseEntity);
        if (vehicle.isFamilyTrack()) {
            return state.fact(FactKey.CHILDREN_UP_TO_23)
                    .map(f -> Integer.parseInt(f.value()) >= data.rules().familyTrack().minChildren())
                    .orElse(true);
        }
        return state.fact(FactKey.GROUP)
                .map(f -> data.rules().rankOf(vehicle.group()) <= data.rules().rankOf(f.value()))
                .orElse(true);
    }

    private void askRep(CaseEntity caseEntity, FactKey key, String text, String subject) {
        boolean alreadyAsked = questions.findByCaseIdAndFactKeyAndStatus(caseEntity.id(), key, RepQuestionEntity.Status.OPEN)
                .stream()
                .anyMatch(q -> q.isPending() && subject.equals(q.subject()));
        if (!alreadyAsked) {
            String id = "q-" + UUID.randomUUID().toString().substring(0, 8);
            questions.save(new RepQuestionEntity(id, caseEntity.id(), key, text, subject, Instant.now()));
        }
    }

    private void staleQuestions(String caseId, FactKey key) {
        questions.findByCaseIdAndFactKeyAndStatus(caseId, key, RepQuestionEntity.Status.OPEN)
                .forEach(RepQuestionEntity::markStale);
    }

    private CaseEntity load(String caseId) {
        return cases.findById(caseId).orElseThrow(() -> ApiException.notFound("UNKNOWN_CASE", "התיק לא נמצא"));
    }

    private static void checkVersion(CaseEntity caseEntity, Long expectedVersion) {
        if (expectedVersion != null && expectedVersion != caseEntity.revision()) {
            throw ApiException.conflict("VERSION_CONFLICT", "המידע התעדכן בינתיים. טענו מחדש ובדקו שוב.");
        }
    }
}
