package com.shani.caradvisor.workflow;

import com.shani.caradvisor.data.ReferenceData;
import com.shani.caradvisor.engine.Drive;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * The answer buttons offered under the bot's latest question, decided by the server from the state of the case:
 * a closed list while a detail is missing (job, current car, number of children), the review of the assumed details,
 * and, where the latest message asks for them, two next steps or "confirm with the rep". Ids are "fact:KEY:VALUE",
 * "best", "find:DRIVE", "review:ok" / "review:later", "rep:ask", "continue", and "ui:..." for actions the screen
 * performs itself (choose a payslip file, open the form). Cars are shown as cards, never as bare buttons.
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

    private final ReferenceData data;
    public ChoiceOptions(ReferenceData data) {
        this.data = data;
    }

    /** after: the buttons the latest message asked for, used once the questions and the review are done. */
    List<Offer> offeredFor(CaseState state, MessageEntity.Offers after) {
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
        return switch (after) {
            case NEXT_STEPS -> nextSteps();
            case REP -> state.canAskRep() ? List.of(new Offer(REP_ASK, "לאשר את ההשתתפות מול נציג")) : List.of();
            case NONE -> List.of();
        };
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

    /**
     * Two questions the employee can ask with a tap. Cars appear only once they ask: a few car names out of context
     * explain nothing, and giving up the car is never pushed as a next step.
     */
    private static List<Offer> nextSteps() {
        return List.of(
                new Offer("best", "מה הכי משתלם לי?"),
                new Offer(FIND + Drive.ELECTRIC.name(), "אילו רכבים חשמליים יש לי?"));
    }
}
