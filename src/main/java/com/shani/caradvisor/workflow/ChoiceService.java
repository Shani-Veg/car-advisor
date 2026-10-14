package com.shani.caradvisor.workflow;

import com.shani.caradvisor.workflow.ChoiceOptions.Offer;
import com.shani.caradvisor.workflow.Views.CaseView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    ChoiceService(CaseRepository cases, MessageRepository messages, CaseViewAssembler views, ChoiceOptions options,
                  CaseWorkflowService workflow) {
        this.cases = cases;
        this.messages = messages;
        this.views = views;
        this.options = options;
        this.workflow = workflow;
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

        Optional<Long> messageId = workflow.recordEmployeeMessage(caseId, offer.label(), clientRequestId);
        long sourceMessage = messageId.orElseThrow();
        if (offer.id().startsWith("fact:")) {
            String[] parts = offer.id().split(":", 3);
            FactKey key = FactKey.fromApiName(parts[1]);
            workflow.setFact(caseId, key, parts[2], FactEntity.Source.EMPLOYEE_MESSAGE, "m-" + sourceMessage, null);
            workflow.addBotMessage(caseId, nextQuestionOrDone(caseEntity));
        } else if (offer.id().startsWith("alt:")) {
            String target = offer.id().substring("alt:".length());
            if ("waiver".equals(target)) {
                workflow.chooseAlternative(caseId, "waiver", null, null);
            } else {
                workflow.chooseAlternative(caseId, "car", target, null);
            }
            CaseView view = workflow.view(caseId);
            workflow.addBotMessage(caseId, view.comparison() != null
                    ? ChatSummaries.comparisonSummary(view)
                    : "בחרתי את החלופה. ההשוואה תופיע כשיהיו מספיק נתונים.");
        } else if ("best".equals(offer.id())) {
            workflow.addBotMessage(caseId, ChatSummaries.rankingSummary(workflow.view(caseId)));
        }
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
            workflow.addBotMessage(caseId, nextQuestionOrDone(caseEntity));
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
        List<FactKey> missing = views.state(caseEntity).missingForToday();
        return missing.isEmpty() ? ChatSummaries.PROFILE_COMPLETE : ChatSummaries.nextQuestion(missing.get(0));
    }
}
