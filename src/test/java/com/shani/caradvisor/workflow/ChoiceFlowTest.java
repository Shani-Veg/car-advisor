package com.shani.caradvisor.workflow;

import com.shani.caradvisor.workflow.Views.CaseView;
import com.shani.caradvisor.workflow.Views.ChoiceView;
import com.shani.caradvisor.workflow.Views.MessageView;
import com.shani.caradvisor.workflow.Views.ProfileField;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Answer buttons in the chat: server-decided, deterministic, and working with the assistant off (the test default). */
@SpringBootTest
class ChoiceFlowTest {

    @Autowired
    private CaseWorkflowService workflow;

    @Autowired
    private ChoiceService choices;

    @Test
    void theFirstQuestionOffersTheJobsFromTheOccupationTable() {
        CaseView view = workflow.create();

        assertThat(workflow.assistantAvailable()).isFalse();
        assertThat(offered(view)).hasSize(14).contains("fact:occupation:מנהל/ת מוצר");
        assertThat(lastChoices(view)).extracting(ChoiceView::label).contains("מנהל/ת מוצר", "נציג/ת מכירות שטח");
    }

    @Test
    void tappingAJobFillsTheProfileAndTheServerAsksTheNextQuestion() {
        String id = workflow.create().caseId();

        CaseView view = choices.choose(id, "fact:occupation:מנהל/ת מוצר", "tap-1");

        ProfileField occupation = field(view, "occupation");
        assertThat(occupation.value()).isEqualTo("מנהל/ת מוצר");
        assertThat(occupation.source().type()).isEqualTo("EMPLOYEE_MESSAGE");
        assertThat(field(view, "group").value()).isEqualTo("ג");
        assertThat(texts(view, "employee")).containsExactly("מנהל/ת מוצר");
        assertThat(lastText(view)).isEqualTo("מה שכר הבסיס החודשי שלך (ברוטו)?");
        assertThat(lastChoices(view)).isEmpty();
    }

    @Test
    void childrenAndTheCurrentCarComeAsButtonsAndNoCarIsOneOfThem() {
        String id = workflow.create().caseId();
        askForChildren(id);

        assertThat(offered(workflow.view(id))).containsExactly("fact:childrenUpTo23:0", "fact:childrenUpTo23:1",
                "fact:childrenUpTo23:2", "fact:childrenUpTo23:3");

        CaseView atCar = choices.choose(id, "fact:childrenUpTo23:2", "kids");

        assertThat(lastText(atCar)).isEqualTo("איזה רכב חברה יש לך היום?");
        assertThat(offered(atCar)).hasSize(21).contains("fact:currentCar:CAR-09", "fact:currentCar:NONE");
    }

    @Test
    void whenEverythingIsKnownTheButtonsAreTheNextStepsWithoutTodaysCar() {
        String id = workflow.create().caseId();
        completeProfile(id);

        List<String> offers = offered(workflow.view(id));

        assertThat(offers).contains("best", "alt:waiver").doesNotContain("alt:CAR-09");
        assertThat(offers).hasSize(4);
    }

    @Test
    void tappingAnAlternativeComparesItAsksTheRepAndOffersTheOtherOptions() {
        String id = workflow.create().caseId();
        completeProfile(id);
        String offeredCar = offered(workflow.view(id)).stream()
                .filter(o -> o.startsWith("alt:CAR-"))
                .findFirst().orElseThrow();
        String carId = offeredCar.substring("alt:".length());

        CaseView view = choices.choose(id, offeredCar, "alt-tap");

        assertThat(view.status()).isEqualTo("WAITING_FOR_REP");
        assertThat(view.comparison().alternative().vehicleId()).isEqualTo(carId);
        assertThat(view.comparison().alternative().type()).isEqualTo("car");
        assertThat(view.comparison().today().vehicleId()).isEqualTo("CAR-09");
        assertThat(lastText(view)).contains("שלחתי שאלה לנציג");
        assertThat(offered(view)).contains("best").doesNotContain(offeredCar);
    }

    @Test
    void tappingTheWaiverComparesAgainstGivingUpTheCar() {
        String id = workflow.create().caseId();
        completeProfile(id);

        CaseView view = choices.choose(id, "alt:waiver", "waiver-tap");

        assertThat(view.comparison().alternative().type()).isEqualTo("waiver");
        assertThat(view.comparison().alternative().vehicleId()).isNull();
        assertThat(offered(view)).doesNotContain("alt:waiver");
    }

    @Test
    void askingWhatPaysBestAnswersWithTheServersTopThree() {
        String id = workflow.create().caseId();
        completeProfile(id);

        CaseView view = choices.choose(id, "best", "best-tap");

        assertThat(lastText(view)).startsWith("לפי המערכת").contains("ויתור על רכב (+3,199 ₪ בחודש)");
        assertThat(texts(view, "employee")).contains("מה הכי משתלם לי?");
    }

    @Test
    void aFormAnswerToThePendingQuestionContinuesTheConversation() {
        String id = workflow.create().caseId();

        CaseView view = choices.editProfile(id, FactKey.OCCUPATION, "מנהל/ת מוצר", null);

        assertThat(lastText(view)).isEqualTo("מה שכר הבסיס החודשי שלך (ברוטו)?");
        assertThat(texts(view, "bot")).hasSize(2);
        assertThat(lastChoices(view)).isEmpty();
    }

    @Test
    void editingAnotherDetailKeepsTheCurrentQuestionAndItsButtons() {
        String id = workflow.create().caseId();

        CaseView view = choices.editProfile(id, FactKey.BASE_SALARY, "18500", null);

        assertThat(texts(view, "bot")).hasSize(1);
        assertThat(offered(view)).hasSize(14);
        assertThat(field(view, "baseSalary").display()).contains("18,500");
    }

    @Test
    void theButtonsFollowTheQuestionAfterFormAnswersIfTheFormSkipsAhead() {
        String id = workflow.create().caseId();
        choices.editProfile(id, FactKey.BASE_SALARY, "18500", null);

        CaseView view = choices.editProfile(id, FactKey.OCCUPATION, "מנהל/ת מוצר", null);

        // salary was already known, so the next missing detail is the one after it
        assertThat(lastText(view)).isEqualTo("כמה ילדים עד גיל 23 יש לך?");
    }

    @Test
    void anIdThatWasNotOfferedIsRefused() {
        String id = workflow.create().caseId();

        assertThatThrownBy(() -> choices.choose(id, "fact:occupation:אסטרונאוט", "forged"))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.code()).isEqualTo("INVALID_CHOICE"));
        assertThatThrownBy(() -> choices.choose(id, "fact:baseSalary:99999999", "forged-2"))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.code()).isEqualTo("INVALID_CHOICE"));
        assertThat(field(workflow.view(id), "occupation").status()).isEqualTo("MISSING");
    }

    @Test
    void aRetriedTapIsAppliedOnce() {
        String id = workflow.create().caseId();

        choices.choose(id, "fact:occupation:מנהל/ת מוצר", "same-tap");
        CaseView again = choices.choose(id, "fact:occupation:מנהל/ת מוצר", "same-tap");

        assertThat(texts(again, "employee")).hasSize(1);
        assertThat(texts(again, "bot")).hasSize(2);
    }

    @Test
    void theChatAsksOnlyTheFourMainQuestionsAndAssumesTheRest() {
        String id = workflow.create().caseId();

        choices.choose(id, "fact:occupation:מנהל/ת מוצר", "q1");
        CaseView view = workflow.setFact(id, FactKey.BASE_SALARY, "18500", FactEntity.Source.FORM, null, null);
        workflow.addBotMessage(id, ChatSummaries.nextQuestion(FactKey.CHILDREN_UP_TO_23));
        choices.choose(id, "fact:childrenUpTo23:1", "q3");
        view = choices.choose(id, "fact:currentCar:CAR-09", "q4");

        assertThat(lastText(view)).startsWith("יש לי את כל מה שצריך.")
                .contains("עמלות").contains("הפרשת עובד לפנסיה").contains("השתתפות ברכב היום").contains("מסומנים כהערכה");
        assertThat(field(view, "currentParticipation").status()).isEqualTo("DEFAULT");
        assertThat(field(view, "currentParticipation").source().type()).isEqualTo("CATALOG_DEFAULT");
        assertThat(field(view, "pensionPercent").source().type()).isEqualTo("CATALOG_DEFAULT");
        assertThat(field(view, "pensionPercent").source().ref()).isEqualTo("assumption");
        assertThat(offered(view)).contains("best");
    }

    @Test
    void aNumberTheEmployeeGivesReplacesTheAssumption() {
        String id = workflow.create().caseId();

        CaseView view = choices.editProfile(id, FactKey.PENSION_PERCENT, "7", null);

        assertThat(field(view, "pensionPercent").status()).isEqualTo("PROVIDED");
        assertThat(field(view, "pensionPercent").source().type()).isEqualTo("FORM");
    }

    @Test
    void aSideQuestionOffersToContinueInsteadOfTheSameButtons() {
        String id = workflow.create().caseId();
        workflow.recordEmployeeMessage(id, "מה זה שווי שימוש?", "side-1");
        workflow.addBotMessage(id, "שווי שימוש הוא סכום שמתווסף להכנסה החייבת בגלל השימוש ברכב.", true);

        CaseView view = workflow.view(id);

        assertThat(offered(view)).containsExactly("continue");
        assertThat(lastChoices(view)).extracting(ChoiceView::label).containsExactly("נמשיך בשאלות");

        CaseView next = choices.choose(id, "continue", "go-on");

        assertThat(lastText(next)).isEqualTo("באיזה תפקיד את/ה עובד/ת?");
        assertThat(offered(next)).hasSize(14);
    }

    // --- helpers ---

    /** Job by button, the money details by form, then the question about children is on screen. */
    private void askForChildren(String id) {
        choices.choose(id, "fact:occupation:מנהל/ת מוצר", "job");
        workflow.setFact(id, FactKey.BASE_SALARY, "18500", FactEntity.Source.FORM, null, null);
        workflow.setFact(id, FactKey.COMMISSIONS, "0", FactEntity.Source.FORM, null, null);
        workflow.setFact(id, FactKey.BONUS, "0", FactEntity.Source.FORM, null, null);
        workflow.setFact(id, FactKey.PENSION_PERCENT, "6", FactEntity.Source.FORM, null, null);
        workflow.setFact(id, FactKey.STUDY_FUND_PERCENT, "2.5", FactEntity.Source.FORM, null, null);
        workflow.setFact(id, FactKey.CREDIT_POINTS, "2.75", FactEntity.Source.FORM, null, null);
        workflow.addBotMessage(id, ChatSummaries.nextQuestion(FactKey.CHILDREN_UP_TO_23));
    }

    /** Noa from the demo: the last details by button and form, and the assistant (or server) says everything is known. */
    private void completeProfile(String id) {
        askForChildren(id);
        choices.choose(id, "fact:childrenUpTo23:2", "kids");
        choices.choose(id, "fact:currentCar:CAR-09", "car");
        workflow.setFact(id, FactKey.CURRENT_PARTICIPATION, "300", FactEntity.Source.FORM, null, null);
        workflow.addBotMessage(id, ChatSummaries.PROFILE_COMPLETE);
    }

    private static List<String> offered(CaseView view) {
        return lastChoices(view).stream().map(ChoiceView::id).toList();
    }

    private static List<ChoiceView> lastChoices(CaseView view) {
        return view.messages().get(view.messages().size() - 1).choices();
    }

    private static String lastText(CaseView view) {
        return view.messages().get(view.messages().size() - 1).text();
    }

    private static List<String> texts(CaseView view, String role) {
        return view.messages().stream().filter(m -> m.role().equals(role)).map(MessageView::text).toList();
    }

    private static ProfileField field(CaseView view, String key) {
        return view.profile().stream().filter(f -> f.key().equals(key)).findFirst().orElseThrow();
    }
}
