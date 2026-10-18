package com.shani.caradvisor.workflow;

import com.shani.caradvisor.agent.AdvisorAgent;
import com.shani.caradvisor.workflow.Views.CaseView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** A typed reply that plainly answers the open question is understood by code, with the assistant off (the test default). */
@SpringBootTest
class DirectAnswersTest {

    @Autowired
    private DirectAnswers direct;

    @Autowired
    private CaseWorkflowService workflow;

    @Autowired
    private AdvisorAgent agent;

    @Test
    void anOccupationIsRecognisedInEitherGenderAndInsideAShortSentence() {
        assertThat(direct.parse(FactKey.OCCUPATION, "מנהלת מוצר")).get().extracting(DirectAnswers.Answer::value)
                .isEqualTo("מנהל/ת מוצר");
        assertThat(direct.parse(FactKey.OCCUPATION, "אני מפתח תוכנה")).get().extracting(DirectAnswers.Answer::value)
                .isEqualTo("מפתח/ת תוכנה");
        assertThat(direct.parse(FactKey.OCCUPATION, "אנליסטית נתונים")).get().extracting(DirectAnswers.Answer::value)
                .isEqualTo("אנליסט/ית נתונים");
    }

    @Test
    void anOccupationTypedExactlyAsTheTableWritesItIsRecognised() {
        assertThat(direct.parse(FactKey.OCCUPATION, "מפתח/ת תוכנה")).get().extracting(DirectAnswers.Answer::value)
                .isEqualTo("מפתח/ת תוכנה");
        assertThat(direct.parse(FactKey.OCCUPATION, "אני מנהל/ת מוצר")).get().extracting(DirectAnswers.Answer::value)
                .isEqualTo("מנהל/ת מוצר");
    }

    @Test
    void anAmbiguousOrUnknownOccupationIsLeftToTheAssistant() {
        assertThat(direct.parse(FactKey.OCCUPATION, "מנהל")).isEmpty();
        assertThat(direct.parse(FactKey.OCCUPATION, "אסטרונאוט")).isEmpty();
    }

    @Test
    void aSalaryIsReadFromANumberWithOrWithoutFillerWords() {
        assertThat(direct.parse(FactKey.BASE_SALARY, "24000")).get().extracting(DirectAnswers.Answer::value).isEqualTo("24000");
        assertThat(direct.parse(FactKey.BASE_SALARY, "24,000 ש\"ח")).get().extracting(DirectAnswers.Answer::value).isEqualTo("24000");
        assertThat(direct.parse(FactKey.BASE_SALARY, "שכר הבסיס שלי 18500")).isEmpty();
        assertThat(direct.parse(FactKey.BASE_SALARY, "שכר 18,500 ברוטו")).get().extracting(DirectAnswers.Answer::value).isEqualTo("18500");
    }

    @Test
    void aSalaryThatCannotBeASalaryIsNotTaken() {
        assertThat(direct.parse(FactKey.BASE_SALARY, "880")).isEmpty();
        assertThat(direct.parse(FactKey.BASE_SALARY, "הרבה")).isEmpty();
        assertThat(direct.parse(FactKey.BASE_SALARY, "99999999")).isEmpty();
    }

    @Test
    void childrenCanBeADigitOrAWord() {
        assertThat(direct.parse(FactKey.CHILDREN_UP_TO_23, "2")).get().extracting(DirectAnswers.Answer::value).isEqualTo("2");
        assertThat(direct.parse(FactKey.CHILDREN_UP_TO_23, "יש לי שני ילדים")).get().extracting(DirectAnswers.Answer::value).isEqualTo("2");
        assertThat(direct.parse(FactKey.CHILDREN_UP_TO_23, "אין")).get().extracting(DirectAnswers.Answer::value).isEqualTo("0");
        assertThat(direct.parse(FactKey.CHILDREN_UP_TO_23, "הרבה")).isEmpty();
    }

    @Test
    void aCarIsMatchedByItsLatinModelNameLongestFirst() {
        assertThat(direct.parse(FactKey.CURRENT_CAR, "Corolla Cross")).get().extracting(DirectAnswers.Answer::value).isEqualTo("CAR-09");
        assertThat(direct.parse(FactKey.CURRENT_CAR, "אין לי רכב")).get().extracting(DirectAnswers.Answer::value).isEqualTo("NONE");
        assertThat(direct.parse(FactKey.CURRENT_CAR, "קורולה קרוס")).isEmpty();
    }

    @Test
    void aTypedSalaryWorksThroughTheChatWithTheAssistantDown() {
        String id = workflow.create().caseId();
        workflow.setFact(id, FactKey.OCCUPATION, "מנהל/ת מוצר", FactEntity.Source.FORM, null, null);
        workflow.addBotMessage(id, ChatSummaries.nextQuestion(FactKey.BASE_SALARY));

        CaseView view = agent.reply(id, "24000", "typed-1");

        assertThat(view.profile()).filteredOn(f -> f.key().equals("baseSalary")).singleElement()
                .satisfies(f -> {
                    assertThat(f.display()).contains("24,000");
                    assertThat(f.source().type()).isEqualTo("EMPLOYEE_MESSAGE");
                });
        assertThat(view.messages().get(view.messages().size() - 1).text()).isEqualTo("כמה ילדים עד גיל 23 יש לך?");
    }

    @Test
    void aRetriedTypedAnswerIsAppliedOnce() {
        String id = workflow.create().caseId();
        workflow.setFact(id, FactKey.OCCUPATION, "מנהל/ת מוצר", FactEntity.Source.FORM, null, null);

        agent.reply(id, "24000", "typed-2");
        CaseView again = agent.reply(id, "24000", "typed-2");

        assertThat(again.messages().stream().filter(m -> m.role().equals("employee"))).hasSize(1);
    }

    @Test
    void textThatIsNotAnAnswerStillNeedsTheAssistant() {
        String id = workflow.create().caseId();

        assertThatThrownBy(() -> agent.reply(id, "מה זה שווי שימוש?", "typed-3"))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.code()).isEqualTo("ASSISTANT_UNAVAILABLE"));
    }
}
