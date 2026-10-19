package com.shani.caradvisor.workflow;

import com.shani.caradvisor.workflow.ChoiceOptions.Offer;
import com.shani.caradvisor.workflow.Views.CaseView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.shani.caradvisor.engine.Drive;

/**
 * The guided part of the chat, deterministic and never calling a language model: a tap on an answer button goes
 * through the same workflow as the form, and the server writes the next question. A detail edited in the form that
 * answers the pending question gets the same follow-up, so the buttons under the last message always match it.
 * So the guided flow keeps working when the assistant is down, and in a recorded demo.
 */
@Service
@Transactional
public class ChoiceService {

    private final CaseRepository cases;
    private final MessageRepository messages;
    private final CaseViewAssembler views;
    private final ChoiceOptions options;
    private final CaseWorkflowService workflow;
    private final DirectAnswers directAnswers;
    private final CarGuide guide;
    private final CarRequests carRequests;

    ChoiceService(CaseRepository cases, MessageRepository messages, CaseViewAssembler views, ChoiceOptions options,
                  CaseWorkflowService workflow, DirectAnswers directAnswers, CarGuide guide, CarRequests carRequests) {
        this.cases = cases;
        this.messages = messages;
        this.views = views;
        this.options = options;
        this.workflow = workflow;
        this.directAnswers = directAnswers;
        this.guide = guide;
        this.carRequests = carRequests;
    }

    /**
     * A plain request for cars ("רכב חשמלי", "משהו יוקרתי", "מה הכי משתלם לי?") is answered by code at once, with the
     * cars as cards. Empty for anything else, which goes to the model. In the middle of the questions the chat then
     * offers to carry on with them.
     */
    public Optional<CaseView> answerCarRequest(String caseId, String text, String clientRequestId) {
        CaseEntity caseEntity = cases.findById(caseId)
                .orElseThrow(() -> ApiException.notFound("UNKNOWN_CASE", "התיק לא נמצא"));
        Optional<CarRequests.Request> request = carRequests.parse(text);
        if (request.isEmpty()) {
            return Optional.empty();
        }
        if (clientRequestId != null && messages.findByCaseIdAndClientRequestId(caseId, clientRequestId).isPresent()) {
            return Optional.of(views.caseView(caseEntity));
        }
        CaseState state = views.state(caseEntity);
        workflow.recordEmployeeMessage(caseId, text, clientRequestId);
        if (state.reviewPending()) {
            workflow.deferAssumptions(caseId);
        }
        CarRequests.Request r = request.get();
        if (r.waiver()) {
            workflow.chooseAlternative(caseId, "waiver", null, null);
            if (state.missingForToday().isEmpty()) {
                sayComparison(caseId);
            } else {
                workflow.addBotMessage(caseId, "כדי לחשב מה ויתור על הרכב עושה לנטו חסרים עוד: "
                        + String.join(", ", state.missingForToday().stream().map(FactKey::label).toList()) + ".", true);
            }
            return Optional.of(workflow.view(caseId));
        }
        CarGuide.Answer answer = r.best() ? guide.best(caseId)
                : guide.find(caseId, r.drives(), r.maxListPrice(), null, r.order());
        workflow.addBotMessage(caseId, answer.text(), !state.missingForToday().isEmpty(), answer.cars());
        return Optional.of(workflow.view(caseId));
    }

    /**
     * A typed reply that is plainly the answer to the open question ("24000" to the salary question) is handled by
     * code: no assistant needed, so it also works when the assistant is down. Empty when the text is anything else.
     */
    public Optional<CaseView> answerDirectly(String caseId, String text, String clientRequestId) {
        CaseEntity caseEntity = cases.findById(caseId)
                .orElseThrow(() -> ApiException.notFound("UNKNOWN_CASE", "התיק לא נמצא"));
        if (clientRequestId != null && messages.findByCaseIdAndClientRequestId(caseId, clientRequestId).isPresent()) {
            return Optional.of(views.caseView(caseEntity));
        }
        Optional<DirectAnswers.Answer> answer = firstMissing(caseEntity).flatMap(key -> directAnswers.parse(key, text));
        if (answer.isEmpty()) {
            return Optional.empty();
        }
        long messageId = workflow.recordEmployeeMessage(caseId, text, clientRequestId).orElseThrow();
        workflow.setFact(caseId, answer.get().key(), answer.get().value(), FactEntity.Source.EMPLOYEE_MESSAGE,
                "m-" + messageId, null);
        askNext(caseEntity);
        return Optional.of(workflow.view(caseId));
    }

    public CaseView choose(String caseId, String choiceId, String clientRequestId) {
        CaseEntity caseEntity = cases.findById(caseId)
                .orElseThrow(() -> ApiException.notFound("UNKNOWN_CASE", "התיק לא נמצא"));
        // A retried tap (same request id) must not be refused just because the offered buttons already moved on.
        if (clientRequestId != null && messages.findByCaseIdAndClientRequestId(caseId, clientRequestId).isPresent()) {
            return views.caseView(caseEntity);
        }
        Offer offer = offeredNow(caseEntity).stream()
                .filter(o -> o.id().equals(choiceId))
                .findFirst()
                .orElseThrow(() -> ApiException.badRequest("INVALID_CHOICE", "הבחירה כבר אינה זמינה."));
        if (offer.id().startsWith(ChoiceOptions.CLIENT_ACTION)) {
            throw ApiException.badRequest("CLIENT_ACTION", "את הפעולה הזו מבצע המסך עצמו");
        }

        Optional<Long> messageId = workflow.recordEmployeeMessage(caseId, offer.label(), clientRequestId);
        long sourceMessage = messageId.orElseThrow();
        if (offer.id().startsWith("fact:")) {
            String[] parts = offer.id().split(":", 3);
            FactKey key = FactKey.fromApiName(parts[1]);
            workflow.setFact(caseId, key, parts[2], FactEntity.Source.EMPLOYEE_MESSAGE, "m-" + sourceMessage, null);
            askNext(caseEntity);
        } else if (ChoiceOptions.CONTINUE.equals(offer.id())) {
            askNext(caseEntity);
        } else if ("best".equals(offer.id())) {
            workflow.deferAssumptions(caseId);
            CarGuide.Answer answer = guide.best(caseId);
            workflow.addBotMessage(caseId, answer.text(), false, answer.cars());
        } else if (ChoiceOptions.REVIEW_OK.equals(offer.id())) {
            workflow.confirmAssumptions(caseId, "m-" + sourceMessage);
            workflow.addBotMessage(caseId, ChatSummaries.assumptionsConfirmed(), MessageEntity.Offers.NEXT_STEPS);
        } else if (ChoiceOptions.REVIEW_LATER.equals(offer.id())) {
            workflow.deferAssumptions(caseId);
            workflow.addBotMessage(caseId, ChatSummaries.assumptionsDeferred(), MessageEntity.Offers.NEXT_STEPS);
        } else if (offer.id().startsWith(ChoiceOptions.FIND)) {
            Drive drive = Drive.valueOf(offer.id().substring(ChoiceOptions.FIND.length()));
            CarGuide.Answer answer = guide.find(caseId, Set.of(drive), null, null);
            workflow.addBotMessage(caseId, answer.text(), false, answer.cars());
        } else if (ChoiceOptions.REP_ASK.equals(offer.id())) {
            workflow.requestRepConfirmation(caseId);
            workflow.addBotMessage(caseId, ChatSummaries.repRequested(workflow.view(caseId)));
        }
        return workflow.view(caseId);
    }

    /**
     * The buttons on a car card in the chat: "פרטים" explains the model, "השוואה" compares it with today. Said in the
     * chat as if typed, and answered by code, so they work with the assistant off or replaying.
     */
    public CaseView carAction(String caseId, String vehicleId, String action, String clientRequestId) {
        CaseEntity caseEntity = cases.findById(caseId)
                .orElseThrow(() -> ApiException.notFound("UNKNOWN_CASE", "התיק לא נמצא"));
        if (clientRequestId != null && messages.findByCaseIdAndClientRequestId(caseId, clientRequestId).isPresent()) {
            return views.caseView(caseEntity);
        }
        if ("details".equals(action)) {
            CarGuide.Answer answer = guide.describe(caseId, vehicleId);
            workflow.recordEmployeeMessage(caseId, "פרטים על " + label(vehicleId), clientRequestId);
            workflow.addBotMessage(caseId, answer.text(), false, answer.cars());
        } else if ("compare".equals(action)) {
            workflow.chooseAlternative(caseId, "car", vehicleId, null);
            workflow.recordEmployeeMessage(caseId, "להשוות את " + label(vehicleId) + " מול היום", clientRequestId);
            sayComparison(caseId);
        } else {
            throw ApiException.badRequest("UNKNOWN_ACTION", "פעולה לא מוכרת: " + action);
        }
        return workflow.view(caseId);
    }

    private String label(String vehicleId) {
        return guide.label(vehicleId);
    }

    /** After a comparison the only button worth offering is confirming the participation with the rep. */
    private void sayComparison(String caseId) {
        CaseView view = workflow.view(caseId);
        workflow.addBotMessage(caseId, view.comparison() != null ? ChatSummaries.comparisonSummary(view)
                : "בחרתי את החלופה. ההשוואה תופיע כשיהיו מספיק נתונים.", MessageEntity.Offers.REP);
    }

    /** The next question, the review of the assumed details, or (when all is known) the next steps. */
    private void askNext(CaseEntity caseEntity) {
        CaseState state = views.state(caseEntity);
        boolean done = state.missingForToday().isEmpty() && !state.reviewPending();
        workflow.addBotMessage(caseEntity.id(), nextQuestionOrDone(caseEntity),
                done ? MessageEntity.Offers.NEXT_STEPS : MessageEntity.Offers.NONE);
    }

    /** The "confirm with the rep" button next to the comparison: the same step as the chat button, said in the chat. */
    public CaseView requestRep(String caseId, String clientRequestId) {
        CaseEntity caseEntity = cases.findById(caseId)
                .orElseThrow(() -> ApiException.notFound("UNKNOWN_CASE", "התיק לא נמצא"));
        if (clientRequestId != null && messages.findByCaseIdAndClientRequestId(caseId, clientRequestId).isPresent()) {
            return views.caseView(caseEntity);
        }
        workflow.requestRepConfirmation(caseId);
        workflow.recordEmployeeMessage(caseId, "אבקש מנציג התחבורה לאשר את ההשתתפות", clientRequestId);
        workflow.addBotMessage(caseId, ChatSummaries.repRequested(workflow.view(caseId)));
        return workflow.view(caseId);
    }

    /**
     * The employee approved details read from their payslip. The chat says what changed, which details are still
     * assumed, and continues with the next question (if the payslip came before the conversation ended).
     */
    public CaseView applyPayslip(String caseId, Map<FactKey, Object> values, String clientRequestId) {
        CaseEntity caseEntity = cases.findById(caseId)
                .orElseThrow(() -> ApiException.notFound("UNKNOWN_CASE", "התיק לא נמצא"));
        if (clientRequestId != null && messages.findByCaseIdAndClientRequestId(caseId, clientRequestId).isPresent()) {
            return views.caseView(caseEntity);
        }
        workflow.applyPayslip(caseId, values);
        workflow.recordEmployeeMessage(caseId, "העליתי תלוש שכר ואישרתי את הפרטים שנקראו ממנו", clientRequestId);
        CaseView after = workflow.view(caseId);
        List<FactKey> missing = views.state(caseEntity).missingForToday();
        String next = missing.isEmpty() ? ChatSummaries.PROFILE_COMPLETE : ChatSummaries.nextQuestion(missing.get(0));
        workflow.addBotMessage(caseId, ChatSummaries.payslipApplied(after, new ArrayList<>(values.keySet())) + " " + next,
                missing.isEmpty() ? MessageEntity.Offers.NEXT_STEPS : MessageEntity.Offers.NONE);
        return workflow.view(caseId);
    }

    /**
     * Saves a detail from the form. When that moves the first missing detail forward (the form answered what the
     * chat was asking), the server continues the conversation with the next question, exactly as after a button.
     */
    public CaseView editProfile(String caseId, FactKey key, Object value, Long expectedVersion) {
        CaseEntity caseEntity = cases.findById(caseId)
                .orElseThrow(() -> ApiException.notFound("UNKNOWN_CASE", "התיק לא נמצא"));
        Optional<FactKey> before = firstMissing(caseEntity);
        workflow.setFact(caseId, key, value, FactEntity.Source.FORM, null, expectedVersion);
        Optional<FactKey> after = firstMissing(caseEntity);
        if (before.isPresent() && !before.equals(after) && lastMessageIsFromBot(caseId)) {
            askNext(caseEntity);
        }
        return workflow.view(caseId);
    }

    private Optional<FactKey> firstMissing(CaseEntity caseEntity) {
        return views.state(caseEntity).missingForToday().stream().findFirst();
    }

    private boolean lastMessageIsFromBot(String caseId) {
        List<MessageEntity> all = messages.findByCaseIdOrderByIdAsc(caseId);
        return !all.isEmpty() && all.get(all.size() - 1).role() == MessageEntity.Role.BOT;
    }

    private List<Offer> offeredNow(CaseEntity caseEntity) {
        CaseState state = views.state(caseEntity);
        return views.offersFor(state, caseEntity);
    }

    private String nextQuestionOrDone(CaseEntity caseEntity) {
        CaseState state = views.state(caseEntity);
        List<FactKey> missing = state.missingForToday();
        if (!missing.isEmpty()) {
            return ChatSummaries.nextQuestion(missing.get(0));
        }
        return state.reviewPending() ? ChatSummaries.assumptionsReview(workflow.view(caseEntity.id()), false)
                : ChatSummaries.profileComplete(workflow.view(caseEntity.id()));
    }
}
