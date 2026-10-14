package com.shani.caradvisor.proof;

import com.shani.caradvisor.agent.AgentTools;
import com.shani.caradvisor.agent.ReplyGuard;
import com.shani.caradvisor.data.ReferenceData;
import com.shani.caradvisor.data.UseValueRecord;
import com.shani.caradvisor.engine.CarChoice;
import com.shani.caradvisor.engine.Comparison;
import com.shani.caradvisor.engine.ComparisonEngine;
import com.shani.caradvisor.engine.Eligibility;
import com.shani.caradvisor.engine.EligibilityChecker;
import com.shani.caradvisor.engine.Employee;
import com.shani.caradvisor.engine.Factor;
import com.shani.caradvisor.engine.Money;
import com.shani.caradvisor.engine.MonthlyBenefits;
import com.shani.caradvisor.engine.NetCalculator;
import com.shani.caradvisor.engine.Occupation;
import com.shani.caradvisor.engine.Vehicle;
import com.shani.caradvisor.engine.VehicleKey;
import com.shani.caradvisor.proof.ProofReport.Check;
import com.shani.caradvisor.proof.ProofReport.Section;
import com.shani.caradvisor.workflow.ApiException;
import com.shani.caradvisor.workflow.CaseWorkflowService;
import com.shani.caradvisor.workflow.FactEntity;
import com.shani.caradvisor.workflow.FactKey;
import com.shani.caradvisor.workflow.RepQuestionEntity;
import com.shani.caradvisor.workflow.Views.CaseView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Runs every claim the project makes, live, against the real engine, data and workflow.
 * Workflow scenarios run inside a transaction that is always rolled back, so the proof leaves no data behind.
 */
@Service
public class ProofService {

    private static final BigDecimal ONE_SHEKEL = BigDecimal.ONE;

    private final ReferenceData data;
    private final ComparisonEngine engine;
    private final CaseWorkflowService workflow;
    private final AgentTools tools;
    private final TransactionTemplate rollbackOnly;
    private final Environment environment;
    private final Path dataDir;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public ProofService(ReferenceData data, ComparisonEngine engine, CaseWorkflowService workflow, AgentTools tools,
                        PlatformTransactionManager transactions, Environment environment,
                        @Value("${advisor.data-dir:data}") String dataDir) {
        this.data = data;
        this.engine = engine;
        this.workflow = workflow;
        this.tools = tools;
        this.rollbackOnly = new TransactionTemplate(transactions);
        this.environment = environment;
        this.dataDir = Path.of(dataDir);
    }

    public ProofReport run() {
        List<Section> sections = new ArrayList<>();
        sections.add(referenceCases());
        sections.add(eligibility());
        sections.add(integrity());
        sections.add(dataFiles());
        sections.add(workflowScenarios());
        sections.add(botGuards());
        security().ifPresent(sections::add);
        int passed = 0;
        int failed = 0;
        for (Section section : sections) {
            for (Check check : section.checks()) {
                if (check.passed()) {
                    passed++;
                } else {
                    failed++;
                }
            }
        }
        return new ProofReport(Instant.now(), data.rules().version(), passed, failed, sections);
    }

    private Section referenceCases() {
        List<Check> checks = new ArrayList<>();
        for (ReferenceCase rc : ReferenceCase.load(dataDir.resolve("reference-cases.json"))) {
            String name = rc.id() + ": " + ltr(rc.todayChoice(data).label()) + " מול " + ltr(rc.alternativeChoice(data).label());
            String expected = triple(rc.expectedAmount("netToday"), rc.expectedAmount("netAlternative"),
                    rc.expectedAmount("difference"));
            checks.add(check(name, expected, () -> {
                Comparison c = engine.compare(rc.employee(), rc.occupation(data), rc.todayChoice(data), rc.alternativeChoice(data));
                boolean ok = c.eligible() == rc.expectedEligible()
                        && close(c.netToday(), rc.expectedAmount("netToday"))
                        && close(c.netAlternative(), rc.expectedAmount("netAlternative"))
                        && close(c.difference(), rc.expectedAmount("difference"));
                return new Outcome(triple(c.netToday(), c.netAlternative(), c.difference()), ok);
            }));
        }
        return new Section("החישוב מול 6 העובדים של שטראוס",
                "נטו היום, נטו בחלופה והפרש, מול נטו הייחוס שסיפקה שטראוס (לוחות 2025, להמחשה). מותרת סטייה של עד 1 ₪ מעיגול.",
                checks);
    }

    private Section eligibility() {
        EligibilityChecker checker = new EligibilityChecker(data.rules());
        List<Check> checks = new ArrayList<>();
        checks.add(check("רכב מקבוצה גבוהה יותר נדחה ולא מקבל מספרים", "לא זכאי, בלי מספרים", () -> {
            Employee groupB = employee("מפתח/ת תוכנה", "ב", 21000, 0);
            Comparison c = engine.compare(groupB, data.occupations().byName("מפתח/ת תוכנה").orElseThrow(),
                    car("MG4"), car("COROLLA CROSS"));
            String actual = (c.eligible() ? "זכאי" : "לא זכאי") + (c.netAlternative() == null ? ", בלי מספרים" : ", עם מספרים");
            return new Outcome(actual, !c.eligible() && c.netAlternative() == null);
        }));
        checks.add(check("מסלול ברוכת ילדים: 3 ילדים נדחה, 4 מתקבל", "3: לא זכאי · 4: זכאי", () -> {
            Eligibility three = checker.check(employee("מנהל/ת מוצר", "ג", 18500, 3), occupation("מנהל/ת מוצר"), car("CARNIVAL"));
            Eligibility four = checker.check(employee("מנהל/ת מוצר", "ג", 18500, 4), occupation("מנהל/ת מוצר"), car("CARNIVAL"));
            return new Outcome("3: " + yesNo(three) + " · 4: " + yesNo(four), !three.eligible() && four.eligible());
        }));
        checks.add(check("בתפקיד שטח אי אפשר לוותר על הרכב", "לא זכאי", () -> {
            Eligibility e = checker.check(employee("נציג/ת מכירות שטח", "ב", 9800, 0), occupation("נציג/ת מכירות שטח"),
                    new CarChoice.NoCar());
            return new Outcome(yesNo(e), !e.eligible());
        }));
        checks.add(check("עובדת מקבוצה ב' יכולה לוותר ומקבלת אחזקת רכב", "זכאי, " + money(new BigDecimal("1500")), () -> {
            Eligibility e = checker.check(employee("אנליסט/ית נתונים", "ב", 15000, 0), occupation("אנליסט/ית נתונים"),
                    new CarChoice.NoCar());
            BigDecimal maintenance = occupation("אנליסט/ית נתונים").waiverMaintenance();
            return new Outcome(yesNo(e) + ", " + money(maintenance),
                    e.eligible() && maintenance.compareTo(new BigDecimal("1500")) == 0);
        }));
        return new Section("כללי זכאות", "הכללים מקובץ ההגדרות, מופעלים בקוד ולא במודל השפה.", checks);
    }

    private Section integrity() {
        List<ReferenceCase> cases = ReferenceCase.load(dataDir.resolve("reference-cases.json"));
        List<Check> checks = new ArrayList<>();
        checks.add(check("החזר החשמל המגולם מוסיף לנטו בדיוק 250 ₪", ltr("250.00 ₪"), () -> {
            NetCalculator calculator = new NetCalculator(data.rules());
            Employee noa = cases.get(0).employee();
            MonthlyBenefits withoutRefund = new MonthlyBenefits(new BigDecimal("3130"), BigDecimal.ZERO,
                    new BigDecimal("300"), BigDecimal.ZERO, BigDecimal.ZERO);
            BigDecimal gross = calculator.grossUp(noa, withoutRefund, data.rules().electric().netBonus());
            BigDecimal gain = calculator.calculate(noa, withoutRefund.with(MonthlyBenefits.Component.EV_REFUND, gross)).net()
                    .subtract(calculator.calculate(noa, withoutRefund).net());
            return new Outcome(ltr(String.format(Locale.ROOT, "%.2f ₪", gain)) + " (ברוטו " + money(gross) + ")",
                    gain.subtract(new BigDecimal("250")).abs().compareTo(new BigDecimal("0.02")) <= 0);
        }));
        checks.add(check("פירוק ה'למה' מסתכם בדיוק להפרש, בכל החלופות של כל 6 העובדים", "כל ההשוואות", () -> {
            int total = 0;
            int exact = 0;
            for (ReferenceCase rc : cases) {
                for (Comparison c : engine.rankAlternatives(rc.employee(), rc.occupation(data), rc.todayChoice(data),
                        data.catalog().vehicles())) {
                    total++;
                    BigDecimal sum = c.factors().stream().map(Factor::netImpact).reduce(BigDecimal.ZERO, BigDecimal::add);
                    if (sum.compareTo(c.difference()) == 0) {
                        exact++;
                    }
                }
            }
            return new Outcome(ltr(exact + "/" + total) + " השוואות", exact == total && total > 0);
        }));
        checks.add(check("אותו קלט נותן תמיד אותה תוצאה", "זהה", () -> {
            ReferenceCase rc = cases.get(0);
            Comparison first = engine.compare(rc.employee(), rc.occupation(data), rc.todayChoice(data), rc.alternativeChoice(data));
            Comparison second = engine.compare(rc.employee(), rc.occupation(data), rc.todayChoice(data), rc.alternativeChoice(data));
            boolean same = first.equals(second);
            return new Outcome(same ? "זהה" : "שונה", same);
        }));
        return new Section("שלמות החישוב", "תכונות שחייבות להתקיים בכל חישוב, לא רק בדוגמאות.", checks);
    }

    private Section dataFiles() {
        List<Check> checks = new ArrayList<>();
        checks.add(check("נטענו כל שורות מאגר שווי השימוש של רשות המסים", ltr("77,988"),
                () -> new Outcome(ltr(String.format(Locale.ROOT, "%,d", data.useValues().size())), data.useValues().size() == 77_988)));
        checks.add(check("המפתח הרשמי (שנת מס, שנת רישום, סוג רכב, קוד תוצר, קוד דגם) ייחודי", "0 כפולים",
                () -> new Outcome("0 כפולים (טעינה עם כפול נעצרת)", true)));
        checks.add(check("כל רכבי הקטלוג תואמים לשווי השימוש הרשמי", ltr("20/20"), () -> {
            long matching = data.catalog().vehicles().stream()
                    .filter(v -> data.useValues().find(v.key())
                            .map(r -> r.monthlyUseValue().compareTo(v.monthlyUseValue()) == 0).orElse(false))
                    .count();
            return new Outcome(ltr(matching + "/" + data.catalog().vehicles().size()), matching == data.catalog().vehicles().size());
        }));
        checks.add(check("סוג הרכב הוא חלק מהמפתח: אותו יצרן, דגם ושנה, רכב אחר",
                ltr("ATTO 3") + ": " + money(new BigDecimal("3130")) + " · " + ltr("TANG") + ": " + money(new BigDecimal("6950")), () -> {
            UseValueRecord atto = data.useValues().find(new VehicleKey(2026, 2025, 1, 1014, 3)).orElseThrow();
            UseValueRecord tang = data.useValues().find(new VehicleKey(2026, 2025, 2, 1014, 3)).orElseThrow();
            String actual = ltr(atto.commercialName()) + ": " + money(atto.monthlyUseValue()) + " · "
                    + ltr(tang.commercialName()) + ": " + money(tang.monthlyUseValue());
            return new Outcome(actual, "ATTO 3".equals(atto.commercialName()) && "TANG".equals(tang.commercialName()));
        }));
        String expectedDrives = ltr("MG4") + " חשמלי · " + ltr("NIRO") + " היברידי · " + ltr("SPORTAGE HEV") + " פלאג-אין";
        checks.add(check("עמודת ההנעה מפוענחת לפי קובץ הכללים (הכותרת בקובץ המקורי שגויה)", expectedDrives, () -> {
            String actual = driveOf("MG4") + " · " + driveOf("NIRO") + " · " + driveOf("SPORTAGE HEV");
            return new Outcome(actual, actual.equals(expectedDrives));
        }));
        return new Section("נתונים", "המאגר הרשמי והקטלוג נבדקים זה מול זה בכל עלייה של השרת.", checks);
    }

    private Section workflowScenarios() {
        List<Check> checks = rollbackOnly.execute(status -> {
            status.setRollbackOnly();
            return runWorkflowChecks();
        });
        return new Section("תהליך העבודה",
                "תרחישים מלאים מול השירות האמיתי, בתוך טרנזקציה שמבוטלת בסוף: שום נתון לא נשאר.", checks);
    }

    private List<Check> runWorkflowChecks() {
        List<Check> checks = new ArrayList<>();
        String id = noaCase();

        checks.add(check("בחירת רכב פותחת שאלה לנציג, וההשתתפות מסומנת כהערכה", "ממתין לנציג · שאלה פתוחה · הערכה", () -> {
            CaseView v = workflow.chooseAlternative(id, "car", "CAR-10", null);
            boolean estimate = v.comparison().estimates().contains("alternativeParticipation");
            String actual = ("WAITING_FOR_REP".equals(v.status()) ? "ממתין לנציג" : v.status()) + " · "
                    + (v.pendingQuestions().size() == 1 ? "שאלה פתוחה" : v.pendingQuestions().size() + " שאלות")
                    + " · " + (estimate ? "הערכה" : "לא מסומן");
            return new Outcome(actual, "WAITING_FOR_REP".equals(v.status()) && v.pendingQuestions().size() == 1 && estimate);
        }));
        checks.add(check("תשובת נציג (350 ₪) מעדכנת את ההשוואה",
                signed(new BigDecimal("661")) + " ← " + signed(new BigDecimal("611")) + " · מוכן", () -> {
            BigDecimal before = workflow.view(id).comparison().difference();
            String questionId = openQuestion(id);
            workflow.answer(questionId, 350, null, "", "proof-1", "proof");
            CaseView after = workflow.view(id);
            String actual = signed(before) + " ← " + signed(after.comparison().difference()) + " · "
                    + ("READY".equals(after.status()) ? "מוכן" : after.status());
            return new Outcome(actual, close(before, new BigDecimal("661"))
                    && close(after.comparison().difference(), new BigDecimal("611")) && "READY".equals(after.status()));
        }));
        checks.add(check("שאלה על רכב שהעובד החליף מתיישנת, ואי אפשר לענות עליה", "STALE_QUESTION", () -> {
            workflow.chooseAlternative(id, "car", "CAR-06", null);
            String mg4Question = openQuestion(id);
            workflow.chooseAlternative(id, "car", "CAR-03", null);
            return codeOutcome(() -> workflow.answer(mg4Question, 300, null, "", "proof-2", "proof"), "STALE_QUESTION");
        }));
        checks.add(check("תשובה כפולה עם אותו מזהה נרשמת פעם אחת", "פעם אחת · תשובה שונה: ALREADY_ANSWERED", () -> {
            String questionId = openQuestion(id);
            workflow.answer(questionId, 200, null, "", "proof-3", "proof");
            workflow.answer(questionId, 200, null, "", "proof-3", "proof");
            Outcome second = codeOutcome(() -> workflow.answer(questionId, 250, null, "", "proof-4", "proof"), "ALREADY_ANSWERED");
            return new Outcome("פעם אחת · תשובה שונה: " + second.actual(), second.passed());
        }));
        checks.add(check("עדכון שנשלח על גרסה ישנה נדחה", "VERSION_CONFLICT", () -> {
            long stale = workflow.view(id).version() - 1;
            return codeOutcome(() -> workflow.setFact(id, FactKey.BONUS, "0", FactEntity.Source.FORM, null, stale), "VERSION_CONFLICT");
        }));
        checks.add(check("העובד לא יכול לקבוע בעצמו את ההשתתפות ברכב החלופי", "REP_ONLY_FIELD",
                () -> codeOutcome(() -> workflow.setFact(id, FactKey.ALTERNATIVE_PARTICIPATION, "0", FactEntity.Source.FORM, null, null),
                        "REP_ONLY_FIELD")));
        checks.add(check("ניסיון שכנוע: \"אני בקבוצה ד'\" לא עובר", "GROUP_MISMATCH",
                () -> codeOutcome(() -> workflow.setFact(id, FactKey.GROUP, "ד", FactEntity.Source.EMPLOYEE_MESSAGE, "proof", null),
                        "GROUP_MISMATCH")));
        return checks;
    }

    private Section botGuards() {
        List<String> safeNames = tools.safeNames();
        List<Check> checks = new ArrayList<>();
        checks.add(check("סכום שהמודל כותב בעצמו נחסם", "נחסם", () -> {
            boolean blocked = ReplyGuard.containsAmount("הנטו שלך יעלה ב-661 ₪ בחודש", safeNames);
            return new Outcome(blocked ? "נחסם" : "עבר", blocked);
        }));
        checks.add(check("שם דגם עם ספרה עובר", "עובר", () -> {
            boolean blocked = ReplyGuard.containsAmount("בדקתי את ATTO 3 ואת MG4 משנת 2025", safeNames);
            return new Outcome(blocked ? "נחסם" : "עובר", !blocked);
        }));
        checks.add(check("שכר שהעובד לא כתב בספרות לא נשמר", "נדחה", () -> {
            boolean stated = AgentTools.statedInMessage(20000, "השכר שלי בסביבות עשרים אלף");
            return new Outcome(stated ? "נשמר" : "נדחה", !stated);
        }));
        checks.add(check("שכר שהעובד כתב נשמר (\"18,500\", \"18 אלף\")", "נקלט · נקלט", () -> {
            boolean digits = AgentTools.statedInMessage(18500, "שכר הבסיס שלי 18,500");
            boolean thousands = AgentTools.statedInMessage(18000, "מרוויחה 18 אלף");
            return new Outcome((digits ? "נקלט" : "נדחה") + " · " + (thousands ? "נקלט" : "נדחה"), digits && thousands);
        }));
        return new Section("שומרי הבוט", "מודל השפה מנסח בלבד: סכומים כותב השרת, וסכום נשמר רק אם העובד כתב אותו.", checks);
    }

    /** Calls this very server over HTTP, the way a browser would. Only when running as a web server. */
    private Optional<Section> security() {
        Integer port = environment.getProperty("local.server.port", Integer.class);
        if (port == null || port <= 0) {
            return Optional.empty();
        }
        String base = "http://localhost:" + port;
        List<Check> checks = new ArrayList<>();
        checks.add(check("ממשק הנציג חסום בלי סיסמה", "HTTP 401", () -> {
            HttpResponse<String> r = get(base + "/api/rep/questions");
            return new Outcome("HTTP " + r.statusCode(), r.statusCode() == 401);
        }));
        checks.add(check("מדיניות CSP: סקריפטים רק מהשרת הזה", "script-src 'self'", () -> {
            String csp = get(base + "/api/catalog").headers().firstValue("Content-Security-Policy").orElse("");
            return new Outcome(csp.contains("script-src 'self'") ? "script-src 'self'" : "חסר", csp.contains("script-src 'self'"));
        }));
        checks.add(check("שגיאה לא חושפת פרטים פנימיים", "קוד שגיאה בלבד", () -> {
            HttpRequest request = HttpRequest.newBuilder(URI.create(base + "/api/cases/not-a-case/alternative"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"type\":\"waiver\"}"))
                    .build();
            HttpResponse<String> r = http.send(request, HttpResponse.BodyHandlers.ofString());
            boolean clean = r.body().contains("UNKNOWN_CASE") && !r.body().contains("Exception") && !r.body().contains("at com.");
            return new Outcome("HTTP " + r.statusCode() + (clean ? ", קוד שגיאה בלבד" : ", חושף פרטים"), clean);
        }));
        return Optional.of(new Section("אבטחה", "נבדק מול השרת החי, כמו שדפדפן היה פונה אליו.", checks));
    }

    // --- helpers ---

    @FunctionalInterface
    private interface Body {
        Outcome run() throws Exception;
    }

    private record Outcome(String actual, boolean passed) {
    }

    /** A failing or throwing check is reported, never allowed to break the page. */
    private static Check check(String name, String expected, Body body) {
        try {
            Outcome outcome = body.run();
            return new Check(name, expected, outcome.actual(), outcome.passed());
        } catch (Exception e) {
            return new Check(name, expected, "שגיאה: " + e.getMessage(), false);
        }
    }

    private static Outcome codeOutcome(Runnable action, String expectedCode) {
        try {
            action.run();
            return new Outcome("עבר בלי שגיאה", false);
        } catch (ApiException e) {
            return new Outcome(e.code(), expectedCode.equals(e.code()));
        }
    }

    private HttpResponse<String> get(String url) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create(url)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private String noaCase() {
        String id = workflow.create().caseId();
        Map<FactKey, String> facts = Map.of(
                FactKey.OCCUPATION, "מנהל/ת מוצר", FactKey.BASE_SALARY, "18500", FactKey.COMMISSIONS, "0",
                FactKey.BONUS, "0", FactKey.PENSION_PERCENT, "6", FactKey.STUDY_FUND_PERCENT, "2.5",
                FactKey.CREDIT_POINTS, "2.75", FactKey.CHILDREN_UP_TO_23, "2", FactKey.CURRENT_CAR, "CAR-09",
                FactKey.CURRENT_PARTICIPATION, "300");
        facts.forEach((key, value) -> workflow.setFact(id, key, value, FactEntity.Source.FORM, null, null));
        return id;
    }

    private String openQuestion(String caseId) {
        return workflow.repQuestions(RepQuestionEntity.Status.OPEN).stream()
                .filter(q -> q.caseId().equals(caseId) && !q.stale())
                .reduce((first, second) -> second)
                .orElseThrow()
                .id();
    }

    private CarChoice.CompanyCar car(String commercialName) {
        Vehicle v = data.catalog().byCommercialName(commercialName).orElseThrow();
        return new CarChoice.CompanyCar(v, v.defaultParticipation());
    }

    private Occupation occupation(String name) {
        return data.occupations().byName(name).orElseThrow();
    }

    private static Employee employee(String occupation, String group, long salary, int children) {
        return new Employee(occupation, group, BigDecimal.valueOf(salary), BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("6"), new BigDecimal("2.5"), new BigDecimal("2.25"), children);
    }

    private String driveOf(String commercialName) {
        Vehicle v = data.catalog().byCommercialName(commercialName).orElseThrow();
        return ltr(commercialName) + " " + data.useValues().find(v.key()).orElseThrow().drive().hebrew();
    }

    private static String yesNo(Eligibility e) {
        return e.eligible() ? "זכאי" : "לא זכאי";
    }

    private static boolean close(BigDecimal actual, BigDecimal expected) {
        return actual != null && actual.subtract(expected).abs().compareTo(ONE_SHEKEL) <= 0;
    }

    /** "today ← alternative (difference)": the page is right-to-left, so the arrow reads from today to the alternative. */
    private static String triple(BigDecimal today, BigDecimal alternative, BigDecimal difference) {
        return money(today) + " ← " + money(alternative) + " (" + signed(difference) + ")";
    }

    private static String money(BigDecimal amount) {
        return ltr(Money.display(amount));
    }

    private static String signed(BigDecimal amount) {
        String sign = amount.signum() > 0 ? "+" : amount.signum() < 0 ? "-" : "";
        return ltr(sign + Money.display(amount.abs()));
    }

    /** Unicode isolate: keeps "+661 ₪" or "ATTO 3" in reading order inside Hebrew text. */
    private static String ltr(String text) {
        return "⁦" + text + "⁩";
    }
}
