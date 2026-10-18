package com.shani.caradvisor.workflow;

import com.shani.caradvisor.engine.Drive;
import com.shani.caradvisor.workflow.Views.CarCard;
import com.shani.caradvisor.workflow.Views.CaseView;
import com.shani.caradvisor.workflow.Views.MessageView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** The chat shows real cars: which ones of a kind, what one means for the employee, and how a few compare. */
@SpringBootTest
class CarGuideTest {

    @Autowired
    private CarGuide guide;

    @Autowired
    private CaseWorkflowService workflow;

    @Autowired
    private ChoiceService choices;

    @Test
    void electricCarsAreListedBestForTheNetFirstWithTheirNumbers() {
        String id = noa();

        CarGuide.Answer answer = guide.find(id, Set.of(Drive.ELECTRIC), null, null);

        // MG4 +1,013 is the best electric option for Noa; ATTO 3 +661 is in the list too
        assertThat(answer.cars()).first().isEqualTo("CAR-06");
        assertThat(answer.cars()).contains("CAR-10", "CAR-03").doesNotContain("CAR-14", "CAR-17");
        // the text frames the cards; each card carries its own numbers (CarCard)
        assertThat(answer.text()).startsWith("רכבים חשמליים שזמינים לך, מהמשתלם ביותר לנטו:")
                // group D electric cars are named, without numbers
                .contains("MODEL 3 (2025) (קבוצה ד)").contains("מחוץ לזכאות שלך");
        assertThat(answer.forModel()).allSatisfy(car -> assertThat(car.toString()).doesNotContain("1,013").doesNotContain("1013"));
    }

    @Test
    void aKindTheEmployeeMayNotTakeIsSaidPlainly() {
        String id = noa();

        CarGuide.Answer answer = guide.find(id, Set.of(Drive.PLUG_IN), null, null);

        assertThat(answer.cars()).isEmpty();
        assertThat(answer.text()).startsWith("לא מצאתי רכבי פלאג-אין שזמינים לך.").contains("SPORTAGE HEV (2026) (קבוצה ד)");
    }

    @Test
    void priceAndNetLimitsFilterTheList() {
        String id = noa();

        CarGuide.Answer cheap = guide.find(id, Set.of(Drive.ELECTRIC), new BigDecimal("155000"), null);
        CarGuide.Answer noLoss = guide.find(id, Set.of(), null, BigDecimal.ZERO);

        assertThat(cheap.cars()).containsExactly("CAR-06", "CAR-03");
        assertThat(cheap.text()).contains("עד מחיר מחירון של 155,000 ₪");
        assertThat(noLoss.text()).contains("שהנטו שלך לא יורד בהם ביותר מ-0 ₪").doesNotContain("יורד ב-");
    }

    @Test
    void oneModelIsExplainedInTheTermsThatMatterForTheDecision() {
        String id = noa();

        CarGuide.Answer answer = guide.describe(id, "CAR-10");

        assertThat(answer.text()).startsWith("ATTO 3 (2025), בי ווי די:")
                .contains("הנעה: חשמלי").contains("החזר חשמל של 250 ₪ נטו בחודש")
                .contains("קבוצה ג (בזכאות שלך: קבוצה ג)")
                .contains("מחיר מחירון: 177,500 ₪")
                .contains("שווי שימוש: 3,130 ₪ בחודש").contains("ברכב שלך היום: 4,070 ₪")
                .contains("אצלך: הנטו עולה ב-661 ₪ בחודש");
        assertThat(guide.describe(id, "CAR-16").text()).contains("פלאג-אין").contains("רק על רכב חשמלי מלא")
                .contains("מעל הקבוצה שלך").contains("אין חישוב נטו");
    }

    @Test
    void modelsAreComparedAndTheBestForTheNetIsNamed() {
        String id = noa();

        CarGuide.Answer answer = guide.compare(id, List.of("CAR-10", "CAR-06", "CAR-11"));

        assertThat(answer.cars()).containsExactly("CAR-10", "CAR-06", "CAR-11");
        assertThat(answer.text()).contains("הכי משתלם לנטו מבין אלה: MG4 (2025)").contains("מיון לפי הנטו בלבד");
    }

    @Test
    void theElectricButtonAndTheCardButtonsWorkWithoutTheModel() {
        String id = noa();
        workflow.deferAssumptions(id);
        workflow.addBotMessage(id, ChatSummaries.PROFILE_COMPLETE);

        CaseView found = choices.choose(id, "find:ELECTRIC", "find-tap");
        MessageView last = found.messages().get(found.messages().size() - 1);
        assertThat(last.cars()).extracting(CarCard::vehicleId).first().isEqualTo("CAR-06");
        assertThat(last.cars().get(0).difference()).isEqualByComparingTo("1013");

        CaseView details = choices.carAction(id, "CAR-10", "details", "card-1");
        assertThat(texts(details)).contains("ספר/י לי על ATTO 3 (2025)");

        CaseView compared = choices.carAction(id, "CAR-10", "compare", "card-2");
        assertThat(compared.comparison().alternative().vehicleId()).isEqualTo("CAR-10");
        assertThat(compared.messages().get(compared.messages().size() - 1).text()).contains("661 ₪");
    }

    private String noa() {
        String id = workflow.create().caseId();
        Map.of(FactKey.OCCUPATION, "מנהל/ת מוצר", FactKey.BASE_SALARY, "18500", FactKey.COMMISSIONS, "0",
                        FactKey.BONUS, "0", FactKey.PENSION_PERCENT, "6", FactKey.STUDY_FUND_PERCENT, "2.5",
                        FactKey.CREDIT_POINTS, "2.75", FactKey.CHILDREN_UP_TO_23, "2", FactKey.CURRENT_CAR, "CAR-09",
                        FactKey.CURRENT_PARTICIPATION, "300")
                .forEach((key, value) -> workflow.setFact(id, key, value, FactEntity.Source.FORM, null, null));
        return id;
    }

    private static List<String> texts(CaseView view) {
        return view.messages().stream().map(MessageView::text).toList();
    }
}
