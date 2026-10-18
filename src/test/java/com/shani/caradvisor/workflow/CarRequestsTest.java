package com.shani.caradvisor.workflow;

import com.shani.caradvisor.engine.Drive;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** Plain requests for cars are understood by code; anything more goes to the model. */
class CarRequestsTest {

    private final CarRequests requests = new CarRequests();

    @Test
    void driveTypesAreRecognised() {
        assertThat(requests.parse("אני רוצה רכב חשמלי. מה יש לי?").orElseThrow().drives()).containsExactly(Drive.ELECTRIC);
        assertThat(requests.parse("אז תן לי רכב היברידי").orElseThrow().drives()).containsExactly(Drive.HYBRID);
        assertThat(requests.parse("אילו רכבי פלאג אין יש?").orElseThrow().drives()).containsExactly(Drive.PLUG_IN);
        assertThat(requests.parse("חשמלי או היברידי").orElseThrow().drives()).containsExactlyInAnyOrder(Drive.ELECTRIC, Drive.HYBRID);
    }

    @Test
    void luxuryCheapAndPriceAreRecognised() {
        CarRequests.Request luxury = requests.parse("משהו יוקרתי").orElseThrow();
        assertThat(luxury.order()).isEqualTo(CarGuide.Order.MOST_LUXURIOUS);
        assertThat(luxury.drives()).isEmpty();

        assertThat(requests.parse("רכב חשמלי זול").orElseThrow().order()).isEqualTo(CarGuide.Order.CHEAPEST);
        assertThat(requests.parse("היברידי עד 180 אלף").orElseThrow().maxListPrice()).isEqualByComparingTo("180000");
        assertThat(requests.parse("רכב עד 200,000").orElseThrow().maxListPrice()).isEqualByComparingTo("200000");
    }

    @Test
    void whatPaysBestIsItsOwnRequest() {
        CarRequests.Request best = requests.parse("ומה הכי משתלם לי?").orElseThrow();

        assertThat(best.best()).isTrue();
        assertThat(requests.parse("מה הכי משתלם לי מהחשמליים?").orElseThrow().best()).isFalse();
    }

    @Test
    void questionsNegationsAndNamedModelsGoToTheModel() {
        assertThat(requests.parse("מה ההבדל בין היברידי לפלאג אין?")).isEmpty();
        assertThat(requests.parse("אני לא רוצה חשמלי")).isEmpty();
        assertThat(requests.parse("ספר לי על ה-EV3")).isEmpty();
        assertThat(requests.parse("האטו 3 חשמלי?")).isEmpty();
        assertThat(requests.parse("שלום, אני מנהלת מוצר")).isEmpty();
        assertThat(requests.parse("השכר שלי 26,520")).isEmpty();
    }

    @Test
    void givingUpTheCarIsComparedWhenTheEmployeeRaisesIt() {
        assertThat(requests.parse("אני רוצה לוותר על הרכב, כמה זה ישנה לי בנטו?").orElseThrow().waiver()).isTrue();
        assertThat(requests.parse("כדאי לי לוותר על הרכב?").orElseThrow().waiver()).isTrue();
        // not when it is negated, or weighed against a kind of car
        assertThat(requests.parse("אני לא רוצה לוותר על הרכב")).isEmpty();
        assertThat(requests.parse("לוותר או לקחת חשמלי?")).isEmpty();
    }

    @Test
    void anImplausiblePriceIsIgnored() {
        assertThat(requests.parse("רכב חשמלי עד 3").orElseThrow().maxListPrice()).isNull();
    }

    @Test
    void theRequestKeepsOnlyWhatWasAsked() {
        CarRequests.Request request = requests.parse("רכב בנזין").orElseThrow();

        assertThat(request.drives()).isEqualTo(Set.of(Drive.PETROL));
        assertThat(request.maxListPrice()).isNull();
        assertThat(request.order()).isEqualTo(CarGuide.Order.BEST_FOR_NET);
        assertThat(BigDecimal.ONE).isNotNull();
    }
}
