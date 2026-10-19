package com.shani.caradvisor.workflow;

import com.shani.caradvisor.engine.Money;
import com.shani.caradvisor.workflow.Views.CaseView;
import com.shani.caradvisor.workflow.Views.ProfileField;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Every sentence in the chat that contains a number, or that the server must be able to say without a language
 * model: confirmations, summaries of a comparison, and the next question. Written here, in code, so the chat works
 * identically with the model on, off, or replaying a recording.
 */
public final class ChatSummaries {

    public static final String PROFILE_COMPLETE =
            "יש לי את כל מה שצריך. אפשר לבדוק רכב, או לשאול מה הכי משתלם.";

    private static final Map<FactKey, String> QUESTIONS = Map.ofEntries(
            Map.entry(FactKey.OCCUPATION, "מה התפקיד שלך?"),
            Map.entry(FactKey.GROUP, "באיזו קבוצת זכאות את/ה?"),
            Map.entry(FactKey.BASE_SALARY, "מה שכר הבסיס החודשי שלך (ברוטו)?"),
            Map.entry(FactKey.COMMISSIONS, "יש לך עמלות חודשיות? אם אין, כתבו 0."),
            Map.entry(FactKey.BONUS, "יש לך בונוס חודשי? אם אין, כתבו 0."),
            Map.entry(FactKey.PENSION_PERCENT, "מה אחוז ההפרשה שלך לפנסיה?"),
            Map.entry(FactKey.STUDY_FUND_PERCENT, "ומה אחוז ההפרשה שלך לקרן השתלמות? אם אין, כתבו 0."),
            Map.entry(FactKey.CREDIT_POINTS, "כמה נקודות זיכוי יש לך?"),
            Map.entry(FactKey.CHILDREN_UP_TO_23, "כמה ילדים עד גיל 23 יש לך?"),
            Map.entry(FactKey.CURRENT_CAR, "איזה רכב חברה יש לך היום?"),
            Map.entry(FactKey.CURRENT_PARTICIPATION, "ומה סכום ההשתתפות החודשי שלך ברכב הזה?"),
            Map.entry(FactKey.ALTERNATIVE_PARTICIPATION, "השתתפות ברכב החלופי נקבעת על ידי נציג התחבורה."));

    private ChatSummaries() {
    }

    /** Everything needed is known; says plainly which secondary details are still estimates. */
    public static String profileComplete(CaseView view) {
        List<String> assumed = view.profile().stream()
                .filter(f -> "DEFAULT".equals(f.status()) && !"group".equals(f.key()) && !"alternativeParticipation".equals(f.key()))
                .map(ProfileField::label)
                .toList();
        if (assumed.isEmpty()) {
            return PROFILE_COMPLETE;
        }
        return PROFILE_COMPLETE + " פרטים משניים הנחתי בינתיים (" + String.join(", ", assumed)
                + "), והם מסומנים כהערכה. אפשר לעדכן אותם ב\"המידע שלך\".";
    }

    /**
     * The assumed details, with their values, for the employee to confirm. again: the question was already asked and
     * the employee just corrected some of them.
     */
    public static String assumptionsReview(CaseView view, boolean again) {
        String items = view.assumptionsToConfirm().stream()
                .map(key -> field(view, key))
                .map(f -> f.label() + " " + f.display())
                .collect(Collectors.joining(", "));
        if (again) {
            return "עדכנתי. עוד הנחתי: " + items + ". נכון?";
        }
        return "יש לי מה שצריך. הנחתי כמה פרטים, כדאי לוודא שהם נכונים: " + items
                + (view.payslipEnabled() ? ". אפשר גם להעלות תלוש שכר ואמלא מהתלוש." : ".");
    }

    public static String assumptionsConfirmed() {
        return "מעולה, הפרטים אושרו. " + PROFILE_COMPLETE;
    }

    public static String assumptionsDeferred() {
        return "בסדר, הם נשארים מסומנים כהערכה. " + PROFILE_COMPLETE;
    }

    public static String repRequested(CaseView view) {
        return "שלחתי לנציג התחבורה: " + view.pendingQuestions().get(0).question()
                + " ההשוואה תתעדכן כשיענה.";
    }

    /** What the payslip changed, and which details are still the system's assumption. */
    public static String payslipApplied(CaseView view, List<FactKey> applied) {
        String items = applied.stream()
                .map(key -> field(view, key.apiName()))
                .map(f -> f.label() + " " + f.display())
                .collect(Collectors.joining(", "));
        List<String> stillAssumed = CaseState.REVIEWABLE.stream()
                .map(key -> field(view, key.apiName()))
                .filter(f -> "DEFAULT".equals(f.status()))
                .map(ProfileField::label)
                .toList();
        return "עדכנתי מהתלוש: " + items + "."
                + (stillAssumed.isEmpty() ? "" : " נשארו כהערכה: " + String.join(", ", stillAssumed) + ".");
    }

    public static String nextQuestion(FactKey key) {
        return QUESTIONS.get(key);
    }

    public static String confirmation(CaseView view, List<FactKey> recorded) {
        String items = recorded.stream().distinct().sorted()
                .map(key -> field(view, key.apiName()))
                .map(f -> f.label() + ": " + f.display())
                .collect(Collectors.joining(", "));
        return "רשמתי: " + items + ".";
    }

    public static String comparisonSummary(CaseView view) {
        String text = view.comparison().explanation();
        if (!view.pendingQuestions().isEmpty()) {
            text += " ממתין לתשובת נציג התחבורה על ההשתתפות.";
        } else if (view.comparison().eligible() && view.comparison().estimates().contains("alternativeParticipation")) {
            text += " ההשתתפות היא הערכה. אפשר לבקש מנציג התחבורה לאשר אותה.";
        }
        return text;
    }

    /** What the server says when the model produced nothing usable. */
    public static String fallback(CaseView view) {
        List<String> missing = view.profile().stream()
                .filter(f -> "MISSING".equals(f.status()) && !"alternativeParticipation".equals(f.key()))
                .limit(2)
                .map(ProfileField::label)
                .toList();
        if (!missing.isEmpty()) {
            return "כדי להמשיך אצטרך עוד כמה פרטים: " + String.join(" ו", missing) + ".";
        }
        return view.comparison() != null ? "ההשוואה מעודכנת במסך." : "איזו חלופה תרצה/י לבדוק?";
    }

    private static ProfileField field(CaseView view, String key) {
        return view.profile().stream().filter(f -> f.key().equals(key)).findFirst().orElseThrow();
    }
}
