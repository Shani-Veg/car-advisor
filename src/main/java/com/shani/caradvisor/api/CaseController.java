package com.shani.caradvisor.api;

import com.shani.caradvisor.agent.AdvisorAgent;
import com.shani.caradvisor.agent.PayslipReader;
import com.shani.caradvisor.workflow.ApiException;
import com.shani.caradvisor.workflow.CaseWorkflowService;
import com.shani.caradvisor.workflow.ChoiceService;
import com.shani.caradvisor.workflow.FactKey;
import com.shani.caradvisor.workflow.Views.CaseView;
import com.shani.caradvisor.workflow.Views.PayslipReading;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Employee screen endpoints. Thin: every rule lives in CaseWorkflowService. */
@RestController
@RequestMapping("/api/cases")
public class CaseController {

    private final CaseWorkflowService workflow;
    private final AdvisorAgent agent;
    private final ChoiceService choices;
    private final PayslipReader payslips;

    /** The upload is the one non-JSON request: this header forces a CORS preflight, so other sites cannot post it. */
    static final String UPLOAD_HEADER = "X-Requested-With";
    static final String UPLOAD_HEADER_VALUE = "car-advisor";

    public CaseController(CaseWorkflowService workflow, AdvisorAgent agent, ChoiceService choices, PayslipReader payslips) {
        this.workflow = workflow;
        this.agent = agent;
        this.choices = choices;
        this.payslips = payslips;
    }

    public record RepRequest(String clientRequestId) {
    }

    /** action: "details" or "compare". */
    public record CarActionRequest(String action, String clientRequestId) {
    }

    /** values: field key (as in the profile) to the value the employee approved. */
    public record PayslipApproval(Map<String, Object> values, String clientRequestId) {
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
        return choices.choose(caseId, body.choiceId(), body.clientRequestId());
    }

    @PutMapping("/{caseId}/profile/{key}")
    public CaseView updateProfile(@PathVariable("caseId") String caseId, @PathVariable("key") String key,
                                  @RequestBody ProfileUpdate body) {
        return choices.editProfile(caseId, FactKey.fromApiName(key), body.value(), body.expectedVersion());
    }

    /** The buttons on a car card in the chat. */
    @PostMapping("/{caseId}/cars/{vehicleId}")
    public CaseView carAction(@PathVariable("caseId") String caseId, @PathVariable("vehicleId") String vehicleId,
                              @RequestBody CarActionRequest body) {
        return choices.carAction(caseId, vehicleId, body.action(), body.clientRequestId());
    }

    @PostMapping("/{caseId}/rep-request")
    public CaseView requestRep(@PathVariable("caseId") String caseId, @RequestBody RepRequest body) {
        return choices.requestRep(caseId, body.clientRequestId());
    }

    /** Reads the payslip and returns what was found. Stores nothing: the employee approves first. */
    @PostMapping(path = "/{caseId}/payslip", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PayslipReading readPayslip(@PathVariable("caseId") String caseId,
                                      @RequestHeader(value = UPLOAD_HEADER, required = false) String requestedWith,
                                      @RequestParam("file") MultipartFile file) throws IOException {
        if (!UPLOAD_HEADER_VALUE.equals(requestedWith)) {
            throw ApiException.badRequest("UPLOAD_HEADER_MISSING", "בקשת העלאה חסרה");
        }
        if (file.getSize() > PayslipReader.MAX_BYTES) {
            throw ApiException.badRequest("FILE_TOO_LARGE", "הקובץ גדול מדי. אפשר להעלות עד 5 מגה.");
        }
        return payslips.read(caseId, file.getBytes());
    }

    @PostMapping("/{caseId}/payslip/apply")
    public CaseView applyPayslip(@PathVariable("caseId") String caseId, @RequestBody PayslipApproval body) {
        Map<FactKey, Object> values = new LinkedHashMap<>();
        if (body.values() != null) {
            body.values().forEach((key, value) -> values.put(FactKey.fromApiName(key), value));
        }
        return choices.applyPayslip(caseId, values, body.clientRequestId());
    }

    @PostMapping("/{caseId}/alternative")
    public CaseView alternative(@PathVariable("caseId") String caseId, @RequestBody AlternativeRequest body) {
        return workflow.chooseAlternative(caseId, body.type(), body.vehicleId(), body.expectedVersion());
    }
}
