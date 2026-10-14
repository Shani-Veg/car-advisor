package com.shani.caradvisor.api;

import com.shani.caradvisor.workflow.ApiException;
import com.shani.caradvisor.workflow.CaseWorkflowService;
import com.shani.caradvisor.workflow.RepQuestionEntity;
import com.shani.caradvisor.workflow.Views.RepQuestionView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

/** Transport rep screen endpoints. */
@RestController
@RequestMapping("/api/rep/questions")
public class RepController {

    private final CaseWorkflowService workflow;

    public RepController(CaseWorkflowService workflow) {
        this.workflow = workflow;
    }

    public record AnswerRequest(Object value, LocalDate effectiveFrom, String note, String clientRequestId) {
    }

    @GetMapping
    public List<RepQuestionView> list(@RequestParam(name = "status", defaultValue = "OPEN") String status) {
        try {
            return workflow.repQuestions(RepQuestionEntity.Status.valueOf(status));
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("INVALID_STATUS", "סטטוס לא מוכר: " + status);
        }
    }

    @PostMapping("/{questionId}/answer")
    public RepQuestionView answer(@PathVariable("questionId") String questionId, @RequestBody AnswerRequest body,
                                  Principal rep) {
        return workflow.answer(questionId, body.value(), body.effectiveFrom(), body.note(), body.clientRequestId(),
                rep.getName());
    }
}
