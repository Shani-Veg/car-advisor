package com.shani.caradvisor.workflow;

import com.shani.caradvisor.data.ReferenceData;
import com.shani.caradvisor.engine.CarChoice;
import com.shani.caradvisor.engine.Comparison;
import com.shani.caradvisor.engine.ComparisonEngine;
import com.shani.caradvisor.engine.Drive;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * The answer buttons offered under the bot's latest question, decided by the server from the state of the case:
 * a closed list while a detail is missing (job, current car, number of children), and the next steps once the
 * details are complete (what pays best, and the best alternatives). Ids are "fact:KEY:VALUE", "alt:CAR-ID",
 * "alt:waiver", "best", "review:ok" / "review:later" (the assumed details), "rep:ask" (confirm the participation
 * with the transport rep), and "ui:..." for actions the screen performs itself (choose a payslip file, open the form).
 */
@Component
public class ChoiceOptions {

    public record Offer(String id, String label) {
    }

    static final String CONTINUE = "continue";
    static final String REVIEW_OK = "review:ok";
    static final String REVIEW_LATER = "review:later";
    static final String REP_ASK = "rep:ask";
    /** "find:ELECTRIC": the cars of one drive type the employee may take (CarGuide.find). */
    static final String FIND = "find:";
    /** Prefix of buttons the screen handles without calling the server. */
    static final String CLIENT_ACTION = "ui:";

    private static final int MAX_CHILDREN_BUTTON = 3;
    private static final int MAX_NEXT_STEPS = 4;

    private final ReferenceData data;
    private final ComparisonEngine engine;

    public ChoiceOptions(ReferenceData data, ComparisonEngine engine) {
        this.data = data;
        this.engine = engine;
    }

    List<Offer> offeredFor(CaseState state) {
        List<FactKey> missing = state.missingForToday();
        if (!missing.isEmpty()) {
            return forMissing(missing.get(0));
        }
        if (state.reviewPending()) {
            return List.of(
                    new Offer(REVIEW_OK, "כן, הפרטים נכונים"),
                    new Offer(CLIENT_ACTION + "payslip", "העלאת תלוש שכר"),
                    new Offer(CLIENT_ACTION + "profile", "לעדכן בעצמי"),
                    new Offer(REVIEW_LATER, "אחר כך"));
        }
        return nextSteps(state);
    }

    private List<Offer> forMissing(FactKey next) {
        return switch (next) {
            case OCCUPATION -> data.occupations().all().stream()
                    .map(o -> new Offer("fact:occupation:" + o.name(), o.name()))
                    .toList();
            case CHILDREN_UP_TO_23 -> IntStream.rangeClosed(0, MAX_CHILDREN_BUTTON)
                    .mapToObj(i -> new Offer("fact:childrenUpTo23:" + i, String.valueOf(i)))
                    .toList();
            case CURRENT_CAR -> {
                List<Offer> offers = new ArrayList<>(data.catalog().vehicles().stream()
                        .map(v -> new Offer("fact:currentCar:" + v.id(), v.label()))
                        .toList());
                offers.add(new Offer("fact:currentCar:" + FactValues.NO_CAR, "אין לי רכב חברה"));
                yield offers;
            }
            default -> List.of();
        };
    }

    private List<Offer> nextSteps(CaseState state) {
        List<Offer> offers = new ArrayList<>();
        if (state.canAskRep()) {
            offers.add(new Offer(REP_ASK, "לאשר את ההשתתפות מול נציג"));
        }
        offers.add(new Offer("best", "מה הכי משתלם לי?"));
        offers.add(new Offer(FIND + Drive.ELECTRIC.name(), "אילו רכבים חשמליים יש לי?"));
        String today = state.fact(FactKey.CURRENT_CAR).map(FactEntity::value).orElse("");
        String selected = state.caseEntity().alternativeSubject();
        List<Comparison> ranked = engine.rankAlternatives(state.employee(), state.occupation(), state.todayChoice(),
                data.catalog().vehicles());
        for (Comparison c : ranked) {
            if (offers.size() >= MAX_NEXT_STEPS) {
                break;
            }
            CarChoice choice = c.alternative().choice();
            if (choice instanceof CarChoice.CompanyCar car) {
                String id = car.vehicle().id();
                if (!id.equals(today) && !id.equals(selected)) {
                    offers.add(new Offer("alt:" + id, car.vehicle().label()));
                }
            } else if (!"WAIVER".equals(selected)) {
                offers.add(new Offer("alt:waiver", choice.label()));
            }
        }
        return offers;
    }
}
