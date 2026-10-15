package com.shani.caradvisor.workflow;

import com.shani.caradvisor.agent.AdvisorAgent;
import com.shani.caradvisor.workflow.Views.CaseView;
import com.shani.caradvisor.workflow.Views.ProfileField;
import com.shani.caradvisor.workflow.Views.RepQuestionView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/** The workflow scenarios behind the demo: missing data, rep questions, stale answers, conflicts. */
@SpringBootTest
class CaseWorkflowServiceTest {

    @Autowired
    private CaseWorkflowService workflow;

    @Autowired
    private AdvisorAgent agent;

    @Test
    void aNewCaseIsCollectingAndHasNoNumbers() {
        CaseView view = workflow.create();

        assertThat(view.status()).isEqualTo("COLLECTING");
        // the four things the chat asks about are missing; the secondary details are flagged estimates
        assertThat(view.profile()).filteredOn(f -> f.status().equals("MISSING")).extracting(ProfileField::key)
                .containsExactlyInAnyOrder("occupation", "group", "baseSalary", "childrenUpTo23", "currentCar",
                        "currentParticipation", "alternativeParticipation");
        assertThat(view.profile()).filteredOn(f -> f.status().equals("DEFAULT")).extracting(ProfileField::key)
                .containsExactlyInAnyOrder("commissions", "bonus", "pensionPercent", "studyFundPercent", "creditPoints");
        assertThat(view.messages()).hasSize(1);
        assertThat(view.comparison()).isNull();
        assertThat(view.ranking()).isNull();
        assertThat(view.assistantAvailable()).isFalse();
    }

    @Test
    void occupationFillsTheGroupAsADefault() {
        String id = workflow.create().caseId();

        CaseView view = form(id, FactKey.OCCUPATION, "מנהל/ת מוצר");

        ProfileField group = field(view, "group");
        assertThat(group.value()).isEqualTo("ג");
        assertThat(group.status()).isEqualTo("DEFAULT");
    }

    @Test
    void noaFromQuestionToConfirmedAnswer() {
        String id = noaToday();

        CaseView beforeAlternative = workflow.view(id);
        assertThat(beforeAlternative.status()).isEqualTo("COLLECTING");
        assertThat(beforeAlternative.ranking().get(0).alternative().type()).isEqualTo("waiver");

        // Comparing asks nobody: the catalog's participation is used, marked as an estimate.
        CaseView compared = workflow.chooseAlternative(id, "car", "CAR-10", null);
        assertThat(compared.status()).isEqualTo("READY");
        assertThat(compared.pendingQuestions()).isEmpty();
        assertThat(compared.comparison().alternative().officialUrl()).isEqualTo("https://bydauto.co.il/model/byd-atto-3-evo/");
        assertThat(compared.ranking()).anySatisfy(item -> {
            assertThat(item.alternative().type()).isEqualTo("waiver");
            assertThat(item.alternative().officialUrl()).isNull();
        });
        assertThat(compared.comparison().difference()).isCloseTo(new BigDecimal("661"), within(BigDecimal.ONE));
        assertThat(compared.comparison().estimates()).contains("alternativeParticipation");

        // The employee asks to have it confirmed: one question to the rep.
        CaseView waiting = workflow.requestRepConfirmation(id);
        assertThat(waiting.status()).isEqualTo("WAITING_FOR_REP");
        assertThat(waiting.pendingQuestions()).hasSize(1);
        assertThatThrownBy(() -> workflow.requestRepConfirmation(id))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.code()).isEqualTo("NOTHING_TO_CONFIRM"));

        RepQuestionView question = workflow.repQuestions(RepQuestionEntity.Status.OPEN).stream()
                .filter(q -> q.caseId().equals(id)).findFirst().orElseThrow();
        assertThat(question.context().suggestedAnswer().display()).isEqualTo("300 ₪");

        workflow.answer(question.id(), 350, LocalDate.of(2027, 1, 1), "", "req-1", "rep");

        CaseView ready = workflow.view(id);
        assertThat(ready.status()).isEqualTo("READY");
        assertThat(field(ready, "alternativeParticipation").status()).isEqualTo("CONFIRMED");
        assertThat(ready.comparison().estimates()).doesNotContain("alternativeParticipation");
        // 50 NIS more participation comes straight off the net.
        assertThat(ready.comparison().difference()).isCloseTo(new BigDecimal("611"), within(BigDecimal.ONE));
        assertThat(ready.messages()).anySatisfy(m -> assertThat(m.role()).isEqualTo("system"));
        assertThat(ready.version()).isGreaterThan(waiting.version());
    }

    @Test
    void changingTheAlternativeMakesTheOldQuestionStale() {
        String id = noaToday();
        workflow.chooseAlternative(id, "car", "CAR-10", null);
        workflow.requestRepConfirmation(id);
        String atto3Question = openQuestionFor(id).id();

        CaseView mg4 = workflow.chooseAlternative(id, "car", "CAR-06", null);

        assertThat(mg4.pendingQuestions()).isEmpty();
        assertThat(mg4.status()).isEqualTo("READY");
        assertThatThrownBy(() -> workflow.answer(atto3Question, 350, null, "", "req-2", "rep"))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.code()).isEqualTo("STALE_QUESTION"));
    }

    @Test
    void answeringTwiceIsSafe() {
        String id = noaToday();
        workflow.chooseAlternative(id, "car", "CAR-10", null);
        workflow.requestRepConfirmation(id);
        String questionId = openQuestionFor(id).id();

        workflow.answer(questionId, 350, null, "", "req-3", "rep");
        RepQuestionView repeated = workflow.answer(questionId, 350, null, "", "req-3", "rep");

        assertThat(repeated.status()).isEqualTo("ANSWERED");
        assertThatThrownBy(() -> workflow.answer(questionId, 400, null, "", "req-other", "rep"))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.code()).isEqualTo("ALREADY_ANSWERED"));
    }

    @Test
    void anOutdatedVersionIsRejected() {
        String id = workflow.create().caseId();
        CaseView first = form(id, FactKey.BASE_SALARY, "18,500");

        workflow.setFact(id, FactKey.BONUS, "0", FactEntity.Source.FORM, null, first.version());

        assertThatThrownBy(() -> workflow.setFact(id, FactKey.BONUS, "100", FactEntity.Source.FORM, null, first.version()))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.code()).isEqualTo("VERSION_CONFLICT"));
    }

    @Test
    void aCarFromAHigherGroupGetsAReasonButNoNumbersAndNoRepQuestion() {
        String id = workflow.create().caseId();
        fillToday(id, "מפתח/ת תוכנה", "21000", "CAR-06", "250");

        CaseView view = workflow.chooseAlternative(id, "car", "CAR-09", null);

        assertThat(view.comparison().eligible()).isFalse();
        assertThat(view.comparison().difference()).isNull();
        assertThat(view.comparison().eligibilityReason()).contains("גבוהה");
        assertThat(view.pendingQuestions()).isEmpty();
        assertThatThrownBy(() -> workflow.requestRepConfirmation(id))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.code()).isEqualTo("NOTHING_TO_CONFIRM"));
    }

    @Test
    void approvedPayslipDetailsBecomeTheEmployeesOwn() {
        String id = workflow.create().caseId();
        form(id, FactKey.OCCUPATION, "מנהל/ת מוצר");

        CaseView view = workflow.applyPayslip(id, java.util.Map.of(FactKey.CREDIT_POINTS, "2.75", FactKey.PENSION_PERCENT, 6));

        assertThat(field(view, "creditPoints").status()).isEqualTo("PROVIDED");
        assertThat(field(view, "creditPoints").source().ref()).isEqualTo("payslip");
        assertThat(field(view, "bonus").status()).isEqualTo("DEFAULT");
    }

    @Test
    void aPayslipCannotSetTheJobOrABadValue() {
        String id = workflow.create().caseId();

        assertThatThrownBy(() -> workflow.applyPayslip(id, java.util.Map.of(FactKey.OCCUPATION, "מנהל/ת מוצר")))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.code()).isEqualTo("NOT_A_PAYSLIP_FIELD"));
        // all or nothing: the good value next to a bad one is not stored either
        assertThatThrownBy(() -> workflow.applyPayslip(id, java.util.Map.of(FactKey.BONUS, "500", FactKey.PENSION_PERCENT, "250")))
                .isInstanceOf(ApiException.class);
        assertThat(field(workflow.view(id), "bonus").status()).isEqualTo("DEFAULT");
    }

    @Test
    void nobodyTalksTheirWayIntoAHigherGroup() {
        String id = workflow.create().caseId();
        form(id, FactKey.OCCUPATION, "מנהל/ת מוצר");

        assertThatThrownBy(() -> workflow.setFact(id, FactKey.GROUP, "ד", FactEntity.Source.EMPLOYEE_MESSAGE, "m-1", null))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.code()).isEqualTo("GROUP_MISMATCH"));
    }

    @Test
    void theOccupationTableOverridesAGroupStatedBeforeIt() {
        String id = workflow.create().caseId();
        workflow.setFact(id, FactKey.GROUP, "ד", FactEntity.Source.EMPLOYEE_MESSAGE, "m-1", null);

        CaseView view = form(id, FactKey.OCCUPATION, "מנהל/ת מוצר");

        assertThat(field(view, "group").value()).isEqualTo("ג");
    }

    @Test
    void answersRecordWhichRepAnswered() {
        String id = noaToday();
        workflow.chooseAlternative(id, "car", "CAR-10", null);
        workflow.requestRepConfirmation(id);

        RepQuestionView answered = workflow.answer(openQuestionFor(id).id(), 350, null, "", "req-9", "dana");

        assertThat(answered.answer().answeredBy()).isEqualTo("dana");
    }

    @Test
    void badValuesAreRejectedWithAReason() {
        String id = workflow.create().caseId();

        assertThatThrownBy(() -> form(id, FactKey.BASE_SALARY, "הרבה"))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.code()).isEqualTo("INVALID_VALUE"));
        assertThatThrownBy(() -> form(id, FactKey.CHILDREN_UP_TO_23, "2.5"))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.code()).isEqualTo("INVALID_VALUE"));
        assertThatThrownBy(() -> form(id, FactKey.OCCUPATION, "אסטרונאוט"))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.code()).isEqualTo("UNKNOWN_OCCUPATION"));
        assertThatThrownBy(() -> form(id, FactKey.ALTERNATIVE_PARTICIPATION, "100"))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.code()).isEqualTo("REP_ONLY_FIELD"));
    }

    @Test
    void chatIsUnavailableWhileTheAssistantIsOff() {
        String id = workflow.create().caseId();

        assertThatThrownBy(() -> agent.reply(id, "שלום", "req-4"))
                .isInstanceOfSatisfying(ApiException.class, e -> {
                    assertThat(e.status()).isEqualTo(503);
                    assertThat(e.code()).isEqualTo("ASSISTANT_UNAVAILABLE");
                });
        assertThat(workflow.view(id).messages()).hasSize(1);
    }

    private String noaToday() {
        String id = workflow.create().caseId();
        fillToday(id, "מנהל/ת מוצר", "18500", "CAR-09", "300");
        form(id, FactKey.STUDY_FUND_PERCENT, "2.5");
        form(id, FactKey.CREDIT_POINTS, "2.75");
        form(id, FactKey.CHILDREN_UP_TO_23, "2");
        return id;
    }

    private void fillToday(String id, String occupation, String salary, String car, String participation) {
        form(id, FactKey.OCCUPATION, occupation);
        form(id, FactKey.BASE_SALARY, salary);
        form(id, FactKey.COMMISSIONS, "0");
        form(id, FactKey.BONUS, "0");
        form(id, FactKey.PENSION_PERCENT, "6");
        form(id, FactKey.STUDY_FUND_PERCENT, "2.5");
        form(id, FactKey.CREDIT_POINTS, "2.25");
        form(id, FactKey.CHILDREN_UP_TO_23, "0");
        form(id, FactKey.CURRENT_CAR, car);
        form(id, FactKey.CURRENT_PARTICIPATION, participation);
    }

    private CaseView form(String id, FactKey key, Object value) {
        return workflow.setFact(id, key, value, FactEntity.Source.FORM, null, null);
    }

    private RepQuestionView openQuestionFor(String caseId) {
        return workflow.repQuestions(RepQuestionEntity.Status.OPEN).stream()
                .filter(q -> q.caseId().equals(caseId) && !q.stale())
                .findFirst().orElseThrow();
    }

    private static ProfileField field(CaseView view, String key) {
        return view.profile().stream().filter(f -> f.key().equals(key)).findFirst().orElseThrow();
    }
}
