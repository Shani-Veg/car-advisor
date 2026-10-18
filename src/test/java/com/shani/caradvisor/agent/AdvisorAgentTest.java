package com.shani.caradvisor.agent;

import com.shani.caradvisor.workflow.ApiException;
import com.shani.caradvisor.workflow.CaseWorkflowService;
import com.shani.caradvisor.workflow.FactEntity;
import com.shani.caradvisor.workflow.FactKey;
import com.shani.caradvisor.workflow.Views.CaseView;
import com.shani.caradvisor.workflow.Views.MessageView;
import com.shani.caradvisor.workflow.Views.ProfileField;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "advisor.assistant.enabled=true")
@Import(AdvisorAgentTest.ScriptedModel.class)
class AdvisorAgentTest {

    @TestConfiguration
    static class ScriptedModel {
        @Bean
        @Primary
        ScriptedLlm scriptedLlm() {
            return new ScriptedLlm();
        }
    }

    @Autowired
    private AdvisorAgent agent;

    @Autowired
    private CaseWorkflowService workflow;

    @Autowired
    private ScriptedLlm llm;

    @Test
    void recordsWhatTheEmployeeSaidAndConfirmsItWithServerWrittenNumbers() {
        String id = workflow.create().caseId();
        llm.play(
                ScriptedLlm.call(AgentTools.RECORD_FACTS,
                        Map.of("occupation", "מנהל/ת מוצר", "baseSalary", 18500, "childrenUpTo23", 2)),
                ScriptedLlm.text("תודה! כמה אחוזים את/ה מפריש/ה לפנסיה?"));

        CaseView view = agent.reply(id, "אני מנהלת מוצר, שכר בסיס 18,500 ויש לי שני ילדים", "c-1");

        assertThat(field(view, "baseSalary").status()).isEqualTo("PROVIDED");
        assertThat(field(view, "baseSalary").source().type()).isEqualTo("EMPLOYEE_MESSAGE");
        assertThat(field(view, "childrenUpTo23").value()).isEqualTo(2);
        assertThat(botTexts(view)).anySatisfy(t -> assertThat(t).startsWith("רשמתי:").contains("18,500 ₪"));
        assertThat(botTexts(view)).contains("תודה! כמה אחוזים את/ה מפריש/ה לפנסיה?");
    }

    @Test
    void anAmountTheEmployeeNeverWroteIsNotSaved() {
        String id = workflow.create().caseId();
        llm.play(
                ScriptedLlm.call(AgentTools.RECORD_FACTS, Map.of("baseSalary", 20000)),
                ScriptedLlm.text("אפשר לכתוב לי את השכר בספרות?"));

        CaseView view = agent.reply(id, "השכר שלי בסביבות עשרים אלף", "c-2");

        assertThat(field(view, "baseSalary").status()).isEqualTo("MISSING");
        assertThat(llm.received.get(1).toString()).contains("does not appear in the employee's message");
    }

    @Test
    void digitsInTheModelsReplyNeverReachTheEmployee() {
        String id = workflow.create().caseId();
        llm.play(
                ScriptedLlm.text("הנטו שלך יעלה ב-661 ₪"),
                ScriptedLlm.text("ייתכן שהנטו יעלה בגלל"));

        CaseView view = agent.reply(id, "מה יקרה לנטו?", "c-3");

        assertThat(botTexts(view)).noneMatch(t -> t.contains("661"));
        assertThat(botTexts(view)).contains("ייתכן שהנטו יעלה בגלל");
    }

    @Test
    void choosingACarAddsTheServersSummaryAndOffersTheRep() {
        String id = noaToday();
        llm.play(
                ScriptedLlm.call(AgentTools.CHOOSE_ALTERNATIVE, Map.of("type", "car", "vehicleId", "CAR-10")),
                ScriptedLlm.text("בדקתי את האטו 3 בשבילך, ההשוואה במסך."));

        CaseView view = agent.reply(id, "מה אם אעבור לאטו 3?", "c-4");

        assertThat(view.status()).isEqualTo("READY");
        assertThat(botTexts(view)).anySatisfy(t -> assertThat(t).contains("661 ₪").contains("אפשר לבקש מנציג התחבורה"));
    }

    @Test
    void theEmployeeCanAskInWordsToHaveTheParticipationConfirmed() {
        String id = noaToday();
        workflow.chooseAlternative(id, "car", "CAR-10", null);
        llm.play(
                ScriptedLlm.call(AgentTools.REQUEST_REP_CONFIRMATION, Map.of()),
                ScriptedLlm.text("ביקשתי מהנציג לאשר."));

        CaseView view = agent.reply(id, "אני רוצה את האטו 3, תבדוק מול הנציג", "c-rep");

        assertThat(view.status()).isEqualTo("WAITING_FOR_REP");
        assertThat(botTexts(view)).anySatisfy(t -> assertThat(t).startsWith("שלחתי לנציג התחבורה שאלה:"));
    }

    @Test
    void factsTheModelRepeatsAreNotConfirmedAgain() {
        String id = workflow.create().caseId();
        llm.play(
                ScriptedLlm.call(AgentTools.RECORD_FACTS, Map.of("occupation", "מנהל/ת מוצר", "baseSalary", 18500)),
                ScriptedLlm.text("תודה!"),
                ScriptedLlm.call(AgentTools.RECORD_FACTS,
                        Map.of("occupation", "מנהל/ת מוצר", "baseSalary", 18500, "pensionPercent", 6)),
                ScriptedLlm.text("מעולה."));

        agent.reply(id, "מנהלת מוצר, שכר 18500", "c-6");
        CaseView view = agent.reply(id, "פנסיה 6%", "c-7");

        String lastConfirmation = botTexts(view).stream().filter(t -> t.startsWith("רשמתי:")).reduce((a, b) -> b).orElseThrow();
        assertThat(lastConfirmation).contains("פנסיה").doesNotContain("שכר בסיס");
    }

    @Test
    void askingWhatPaysBestAddsTheServersTopThree() {
        String id = noaToday();
        llm.play(ScriptedLlm.call(AgentTools.LIST_OPTIONS, Map.of()), ScriptedLlm.text("הנה האפשרויות המשתלמות."));

        CaseView view = agent.reply(id, "מה הכי משתלם לי?", "c-8");

        assertThat(botTexts(view)).anySatisfy(t -> assertThat(t)
                .startsWith("לפי המערכת")
                .contains("ויתור על רכב (+3,199 ₪ בחודש)")
                .contains("MG4 (2025) (+1,013 ₪ בחודש, לפי השתתפות משוערת)"));
    }

    @Test
    void aRetriedMessageIsHandledOnce() {
        String id = workflow.create().caseId();
        llm.play(ScriptedLlm.text("שלום! במה אפשר לעזור?"));

        agent.reply(id, "שלום", "same-id");
        CaseView again = agent.reply(id, "שלום", "same-id");

        assertThat(again.messages()).filteredOn(m -> m.role().equals("employee")).hasSize(1);
        assertThat(llm.received).hasSize(1);
    }

    @Test
    void whenTheModelIsDownTheMessageIsKeptAndTheFormStillWorks() {
        String id = workflow.create().caseId();
        llm.play(new LlmClient.UnavailableException("all models busy"));

        assertThatThrownBy(() -> agent.reply(id, "שלום", "c-5"))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.status()).isEqualTo(503));

        CaseView view = workflow.view(id);
        assertThat(view.messages()).filteredOn(m -> m.role().equals("employee")).hasSize(1);
        CaseView afterForm = workflow.setFact(id, FactKey.BASE_SALARY, "18500", FactEntity.Source.FORM, null, null);
        assertThat(field(afterForm, "baseSalary").status()).isEqualTo("PROVIDED");
    }

    @Test
    void askingForElectricCarsShowsThemAsCardsWithTheServersNumbers() {
        String id = noaToday();
        llm.play(
                ScriptedLlm.call(AgentTools.FIND_CARS, Map.of("drives", List.of("electric"), "maxListPrice", 180000)),
                ScriptedLlm.text("הנה הרכבים החשמליים שזמינים לך. כדאי להתחיל מהראשון ברשימה."));

        CaseView view = agent.reply(id, "אני רוצה רכב חשמלי עד 180 אלף", "c-find");

        // the list with its cards comes first, the model's words about it after
        MessageView cars = view.messages().get(view.messages().size() - 2);
        assertThat(botTexts(view)).last().isEqualTo("הנה הרכבים החשמליים שזמינים לך. כדאי להתחיל מהראשון ברשימה.");
        assertThat(cars.text()).startsWith("רכבים חשמליים עד מחיר מחירון של 180,000 ₪ שזמינים לך");
        assertThat(cars.cars()).isNotEmpty().allSatisfy(car -> assertThat(car.drive()).isEqualTo("חשמלי"));
    }

    @Test
    void aPriceTheEmployeeDidNotWriteIsNotUsedAsAFilter() {
        String id = noaToday();
        llm.play(
                ScriptedLlm.call(AgentTools.FIND_CARS, Map.of("drives", List.of("electric"), "maxListPrice", 150000)),
                ScriptedLlm.text("אלה האפשרויות."));

        CaseView view = agent.reply(id, "אני רוצה רכב חשמלי זול", "c-find-2");

        assertThat(botTexts(view)).anySatisfy(t -> assertThat(t).startsWith("רכבים חשמליים שזמינים לך"));
        assertThat(llm.received.get(1).toString()).contains("maxListPrice").contains("does not appear in the employee's message");
    }

    @Test
    void aModelTheEmployeeAsksAboutIsExplainedByTheServer() {
        String id = noaToday();
        llm.play(
                ScriptedLlm.call(AgentTools.DESCRIBE_CAR, Map.of("vehicleId", "CAR-10")),
                ScriptedLlm.text("זה רכב חשמלי בקבוצה שלך."));

        CaseView view = agent.reply(id, "ספר לי על האטו 3", "c-describe");

        assertThat(botTexts(view)).anySatisfy(t -> assertThat(t).startsWith("ATTO 3 (2025)").contains("661 ₪"));
    }

    private String noaToday() {
        String id = workflow.create().caseId();
        Map<FactKey, String> facts = Map.of(
                FactKey.OCCUPATION, "מנהל/ת מוצר", FactKey.BASE_SALARY, "18500", FactKey.COMMISSIONS, "0",
                FactKey.BONUS, "0", FactKey.PENSION_PERCENT, "6", FactKey.STUDY_FUND_PERCENT, "2.5",
                FactKey.CREDIT_POINTS, "2.75", FactKey.CHILDREN_UP_TO_23, "2", FactKey.CURRENT_CAR, "CAR-09",
                FactKey.CURRENT_PARTICIPATION, "300");
        facts.forEach((key, value) -> workflow.setFact(id, key, value, FactEntity.Source.FORM, null, null));
        return id;
    }

    private static List<String> botTexts(CaseView view) {
        return view.messages().stream().filter(m -> m.role().equals("bot")).map(MessageView::text).toList();
    }

    private static ProfileField field(CaseView view, String key) {
        return view.profile().stream().filter(f -> f.key().equals(key)).findFirst().orElseThrow();
    }
}
