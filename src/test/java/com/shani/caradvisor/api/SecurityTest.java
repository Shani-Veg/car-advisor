package com.shani.caradvisor.api;

import com.shani.caradvisor.workflow.CaseWorkflowService;
import com.shani.caradvisor.workflow.FactEntity;
import com.shani.caradvisor.workflow.FactKey;
import com.shani.caradvisor.workflow.RepQuestionEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"advisor.rep.username=rep", "advisor.rep.password=test-pass"})
@AutoConfigureMockMvc
class SecurityTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CaseWorkflowService workflow;

    @Test
    void theRepApiNeedsALogin() throws Exception {
        mvc.perform(get("/api/rep/questions")).andExpect(status().isUnauthorized());
        mvc.perform(get("/rep.html")).andExpect(status().isUnauthorized());
    }

    @Test
    void aWrongPasswordIsRejected() throws Exception {
        mvc.perform(get("/api/rep/questions").with(httpBasic("rep", "guess"))).andExpect(status().isUnauthorized());
    }

    @Test
    void theRepGetsInWithThePassword() throws Exception {
        mvc.perform(get("/api/rep/questions").with(httpBasic("rep", "test-pass"))).andExpect(status().isOk());
    }

    @Test
    void theEmployeeApiIsOpenAndEveryResponseCarriesTheCsp() throws Exception {
        mvc.perform(post("/api/cases"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy", containsString("script-src 'self'")))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void errorsCarryACodeAndNothingInternal() throws Exception {
        mvc.perform(get("/api/cases/not-a-case"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("UNKNOWN_CASE"))
                .andExpect(content().string(not(containsString("Exception"))));
    }

    @Test
    void anAnswerRecordsTheLoggedInRep() throws Exception {
        String id = workflow.create().caseId();
        Map.of(FactKey.OCCUPATION, "מנהל/ת מוצר", FactKey.BASE_SALARY, "18500", FactKey.COMMISSIONS, "0",
                        FactKey.BONUS, "0", FactKey.PENSION_PERCENT, "6", FactKey.STUDY_FUND_PERCENT, "2.5",
                        FactKey.CREDIT_POINTS, "2.75", FactKey.CHILDREN_UP_TO_23, "2", FactKey.CURRENT_CAR, "CAR-09",
                        FactKey.CURRENT_PARTICIPATION, "300")
                .forEach((key, value) -> workflow.setFact(id, key, value, FactEntity.Source.FORM, null, null));
        workflow.chooseAlternative(id, "car", "CAR-10", null);
        String questionId = workflow.repQuestions(RepQuestionEntity.Status.OPEN).stream()
                .filter(q -> q.caseId().equals(id)).findFirst().orElseThrow().id();

        mvc.perform(post("/api/rep/questions/" + questionId + "/answer")
                        .with(httpBasic("rep", "test-pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":350,\"clientRequestId\":\"sec-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer.answeredBy").value("rep"));
    }
}
