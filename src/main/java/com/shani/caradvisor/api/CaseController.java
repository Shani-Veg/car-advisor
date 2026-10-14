package com.shani.caradvisor.api;

import com.shani.caradvisor.agent.AdvisorAgent;
import com.shani.caradvisor.workflow.ApiException;
import com.shani.caradvisor.workflow.CaseWorkflowService;
import com.shani.caradvisor.workflow.FactEntity;
import com.shani.caradvisor.workflow.FactKey;
import com.shani.caradvisor.workflow.Views.CaseView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Employee screen endpoints. Thin: every rule lives in CaseWorkflowService. */
@RestController
@RequestMapping("/api/cases")
public class CaseController {

    private final CaseWorkflowService workflow;
    private final AdvisorAgent agent;

    public CaseController(CaseWorkflowService workflow, AdvisorAgent agent) {
        this.workflow = workflow;
        this.agent = agent;
    }

    public record MessageRequest(String text, String clientRequestId) {
    }

    public record ChoiceRequest(String choiceId, String clientRequestId) {
    }

    public record ProfileUpdate(Object value, Long expectedVersion) {
    }

    /** clientRequestId is accepted for symmetry with the other commands; choosing the same option twice is already a no-op. */
    public record AlternativeRequest(String type, String vehicleId, Long expectedVersion, String clientRequestId) {
    }

    @PostMapping
    public CaseView create() {
        return workflow.create();
    }

    @GetMapping("/{caseId}")
    public CaseView get(@PathVariable("caseId") String caseId) {
        return workflow.view(caseId);
    }

    @PostMapping("/{caseId}/messages")
    public CaseView message(@PathVariable("caseId") String caseId, @RequestBody MessageRequest body) {
        return agent.reply(caseId, body.text(), body.clientRequestId());
    }

    @PostMapping("/{caseId}/choices")
    public CaseView choice(@PathVariable("caseId") String caseId, @RequestBody ChoiceRequest body) {
        throw ApiException.notFound("UNKNOWN_CHOICE", "הבחירה לא נמצאה");
    }

    @PutMapping("/{caseId}/profile/{key}")
    public CaseView updateProfile(@PathVariable("caseId") String caseId, @PathVariable("key") String key,
                                  @RequestBody ProfileUpdate body) {
        return workflow.setFact(caseId, FactKey.fromApiName(key), body.value(), FactEntity.Source.FORM, null,
                body.expectedVersion());
    }

    @PostMapping("/{caseId}/alternative")
    public CaseView alternative(@PathVariable("caseId") String caseId, @RequestBody AlternativeRequest body) {
        return workflow.chooseAlternative(caseId, body.type(), body.vehicleId(), body.expectedVersion());
    }
}
