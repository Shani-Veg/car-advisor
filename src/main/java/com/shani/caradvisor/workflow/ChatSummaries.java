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
            "יש לי את כל מה שצריך. אפשר לבחור חלופה להשוואה, או לשאול מה הכי משתלם.";

    private static final Map<FactKey, String> QUESTIONS = Map.ofEntries(
            Map.entry(FactKey.OCCUPATION, "באיזה תפקיד את/ה עובד/ת?"),
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

    public static String nextQuestion(FactKey key) {
        return QUESTIONS.get(key);
    }

    public static String confirmation(CaseView view, List<FactKey> recorded) {
        String items = recorded.stream().distinct().sorted()
                .map(key -> field(view, key.apiName()))
                .map(f -> f.label() + ": " + f.display())
                .collect(Collectors.joining(", "));
        return "רשמתי: " + items + ". אם משהו לא נכון, אפשר לתקן כאן או בטופס.";
    }

    public static String comparisonSummary(CaseView view) {
        String text = view.comparison().explanation();
        if (!view.pendingQuestions().isEmpty()) {
            text += " שלחתי שאלה לנציג התחבורה (" + view.pendingQuestions().get(0).question()
                    + "), וההשוואה תתעדכן כשתגיע תשובה.";
        }
        return text;
    }

    public static String rankingSummary(CaseView view) {
        String top = view.ranking().stream().limit(3)
                .map(item -> item.alternative().label() + " (" + signed(item.difference()) + " בחודש"
                        + (item.participationEstimated() ? ", לפי השתתפות משוערת" : "") + ")")
                .collect(Collectors.joining(", "));
        return "לפי המערכת, האפשרויות המשתלמות ביותר עבורך: " + top + ". כל האפשרויות מופיעות במסך.";
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

    private static String signed(BigDecimal amount) {
        String sign = amount.signum() > 0 ? "+" : amount.signum() < 0 ? "-" : "";
        return sign + Money.display(amount.abs());
    }

    private static ProfileField field(CaseView view, String key) {
        return view.profile().stream().filter(f -> f.key().equals(key)).findFirst().orElseThrow();
    }
}
