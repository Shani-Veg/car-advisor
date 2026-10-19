package com.shani.caradvisor.agent;

import com.shani.caradvisor.data.ReferenceData;
import com.shani.caradvisor.observability.AssistantMetrics;
import com.shani.caradvisor.workflow.ApiException;
import com.shani.caradvisor.workflow.CaseWorkflowService;
import com.shani.caradvisor.workflow.FactKey;
import com.shani.caradvisor.workflow.FactValues;
import com.shani.caradvisor.workflow.Views.CaseView;
import com.shani.caradvisor.workflow.Views.PayslipField;
import com.shani.caradvisor.workflow.Views.PayslipReading;
import com.shani.caradvisor.workflow.Views.ProfileField;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads the few numbers the comparison needs from the employee's payslip, without the payslip itself ever reaching
 * the model. A PDF is read here, on our server, and only its payroll lines (terms and numbers, see PayslipText) are
 * sent; a scanned PDF without text is not sent at all. An image arrives already cropped by the employee in the
 * browser to the payments and deductions table, so the name, ID and address stay on their computer. The model only
 * reads; the code turns amounts into percentages, validates every value like a form entry, and nothing is stored
 * until the employee approves (CaseWorkflowService.applyPayslip). The file is kept in memory for this request only,
 * never saved or logged; a recording keeps only the numbers read, keyed by the hash of what was sent.
 */
@Service
public class PayslipReader {

    public static final int MAX_BYTES = 5 * 1024 * 1024;

    static final String INSTRUCTION = """
            This is part of an Israeli payslip (תלוש שכר): the table of payments and deductions. Return only the
            numbers below, as printed, for the month of this payslip. Use null for anything that is not on it.
            Never guess or calculate.
            """ + Fields.TEXT;

    static final String TEXT_INSTRUCTION = """
            These lines come from an Israeli payslip (תלוש שכר). Before sending, the system kept only payroll terms
            and the numbers next to them; every other word was removed. A line reads "term: numbers", and the numbers
            may be an amount in NIS, a rate in percent or a quantity, in any order. Return only the numbers below.
            Use null for anything that is not there. Never guess or calculate.
            """ + Fields.TEXT;

    private static final class Fields {
        static final String TEXT = """
            - baseSalary: monthly base salary (שכר יסוד, שכר בסיס, משכורת), in NIS.
            - commissions: commissions paid this month (עמלות), in NIS.
            - bonus: bonus or premium paid this month (בונוס, פרמיה, מענק), in NIS.
            - pensionEmployeePercent: the employee's own pension deduction rate in percent (תגמולי עובד, ניכוי עובד
              לקרן פנסיה), only if a rate is printed.
            - pensionEmployeeAmount: the employee's own pension deduction amount in NIS.
            - studyFundEmployeePercent: the employee's own study fund deduction rate in percent (קרן השתלמות עובד),
              only if a rate is printed.
            - studyFundEmployeeAmount: the employee's own study fund deduction amount in NIS.
            - creditPoints: tax credit points (נקודות זיכוי).
            - carParticipation: what the employee pays each month for the company car (השתתפות עובד ברכב), in NIS,
              as a positive number. This is NOT the car's use value (שווי רכב, שווי שימוש), which is added to income:
              never return the use value.
            Do not return names, ID numbers, addresses, bank details or any other text.
            """;
    }

    private static final List<String> NUMBERS = List.of("baseSalary", "commissions", "bonus", "pensionEmployeePercent",
            "pensionEmployeeAmount", "studyFundEmployeePercent", "studyFundEmployeeAmount", "creditPoints", "carParticipation");

    static final Map<String, Object> SCHEMA = schema();

    private final LlmClient llm;
    private final CaseWorkflowService workflow;
    private final FactValues values;
    private final ReferenceData data;
    private final RateLimiter rateLimiter = new RateLimiter(3, 20);
    private final AssistantMetrics metrics;
    private final boolean enabled;

    public PayslipReader(LlmClient llm, CaseWorkflowService workflow, FactValues values, ReferenceData data,
                         AssistantMetrics metrics, @Value("${advisor.payslip.enabled:true}") boolean enabled) {
        this.llm = llm;
        this.workflow = workflow;
        this.values = values;
        this.data = data;
        this.metrics = metrics;
        this.enabled = enabled;
    }

    public PayslipReading read(String caseId, byte[] file) {
        CaseView view = workflow.view(caseId);
        // Without a processing agreement with the model's provider, a real payslip must not leave the organisation.
        if (!enabled) {
            throw new ApiException(403, "PAYSLIP_DISABLED", "קריאת תלוש כבויה כאן. אפשר לעדכן את הפרטים ב\"המידע שלך\".");
        }
        if (file == null || file.length == 0) {
            throw ApiException.badRequest("EMPTY_FILE", "לא התקבל קובץ");
        }
        if (file.length > MAX_BYTES) {
            throw ApiException.badRequest("FILE_TOO_LARGE", "הקובץ גדול מדי. אפשר להעלות עד 5 מגה.");
        }
        String mimeType = mimeTypeOf(file);
        if (mimeType == null) {
            throw ApiException.badRequest("UNSUPPORTED_FILE", "אפשר להעלות תמונה (PNG, JPG, WEBP) או קובץ PDF");
        }
        if (!rateLimiter.tryAcquire(caseId, Instant.now())) {
            throw new ApiException(429, "RATE_LIMITED", "יותר מדי ניסיונות בזמן קצר. נסו שוב בעוד דקה.");
        }
        Map<String, Object> raw;
        List<String> sent = List.of();
        try {
            if (mimeType.equals("application/pdf")) {
                sent = PayslipText.fromPdf(file).orElseThrow(() -> ApiException.badRequest("SCANNED_PDF",
                        "בקובץ הזה אין טקסט (זו סריקה), ולכן הוא לא נשלח. אפשר לצלם את התלוש ולסמן בו את טבלת התשלומים, או לעדכן ב\"המידע שלך\"."));
                if (sent.isEmpty()) {
                    metrics.payslip("nothing_read");
                    throw ApiException.badRequest("NOTHING_READ",
                            "לא מצאתי בקובץ שורות שכר. אפשר לנסות קובץ אחר, או לעדכן ב\"המידע שלך\".");
                }
                raw = llm.extract(TEXT_INSTRUCTION, String.join("\n", sent).getBytes(StandardCharsets.UTF_8), "text/plain", SCHEMA);
            } else {
                raw = llm.extract(INSTRUCTION, file, mimeType, SCHEMA);
            }
        } catch (LlmClient.UnavailableException e) {
            metrics.payslip("unavailable");
            throw ApiException.unavailable("PAYSLIP_UNAVAILABLE", "REPLAY".equals(llm.mode())
                    ? "במצב השמעה אפשר לקרוא רק את תלוש הדוגמה שהוקלט. אפשר לעדכן את הפרטים ב\"המידע שלך\"."
                    : "קריאת התלוש לא זמינה כרגע. אפשר לעדכן את הפרטים ב\"המידע שלך\".");
        }
        Map<FactKey, BigDecimal> found = interpret(raw);
        List<PayslipField> fields = new ArrayList<>();
        List<String> notFound = new ArrayList<>();
        for (FactKey key : CaseWorkflowService.PAYSLIP_FIELDS) {
            String value = valid(key, found.get(key));
            if (value == null) {
                notFound.add(key.label());
                continue;
            }
            ProfileField current = view.profile().stream().filter(f -> f.key().equals(key.apiName())).findFirst().orElseThrow();
            fields.add(new PayslipField(key.apiName(), key.label(), values.typed(key, value), values.display(key, value),
                    current.display(), current.status()));
        }
        if (fields.isEmpty()) {
            metrics.payslip("nothing_read");
            throw ApiException.badRequest("NOTHING_READ",
                    "לא הצלחתי לקרוא פרטי שכר מהקובץ. אפשר לנסות תמונה ברורה יותר, או לעדכן ב\"המידע שלך\".");
        }
        metrics.payslip(sent.isEmpty() ? "read_image_crop" : "read_pdf_lines");
        return new PayslipReading(fields, notFound, sent);
    }

    /**
     * From what the model read to the case's fields. A rate the payslip does not print is computed by the code from
     * the deduction amount, on the same base the net calculation uses (rules file: whole gross, or base only).
     */
    Map<FactKey, BigDecimal> interpret(Map<String, Object> raw) {
        Map<FactKey, BigDecimal> found = new EnumMap<>(FactKey.class);
        BigDecimal base = number(raw, "baseSalary");
        BigDecimal commissions = number(raw, "commissions");
        BigDecimal bonus = number(raw, "bonus");
        put(found, FactKey.BASE_SALARY, base);
        put(found, FactKey.COMMISSIONS, commissions);
        put(found, FactKey.BONUS, bonus);
        put(found, FactKey.CREDIT_POINTS, number(raw, "creditPoints"));
        BigDecimal participation = number(raw, "carParticipation");
        put(found, FactKey.CURRENT_PARTICIPATION, participation == null ? null : participation.abs());

        BigDecimal contributionBase = base == null ? null
                : data.rules().netMethod().contributionsOnGross()
                ? base.add(orZero(commissions)).add(orZero(bonus))
                : base;
        put(found, FactKey.PENSION_PERCENT, rate(number(raw, "pensionEmployeePercent"), number(raw, "pensionEmployeeAmount"), contributionBase));
        put(found, FactKey.STUDY_FUND_PERCENT, rate(number(raw, "studyFundEmployeePercent"), number(raw, "studyFundEmployeeAmount"), contributionBase));
        return found;
    }

    private static BigDecimal rate(BigDecimal printed, BigDecimal amount, BigDecimal base) {
        if (printed != null) {
            return printed;
        }
        if (amount == null || base == null || base.signum() <= 0) {
            return null;
        }
        return amount.abs().multiply(BigDecimal.valueOf(100)).divide(base, 2, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    /** A value that would be refused in the form is dropped, not "fixed". */
    private String valid(FactKey key, BigDecimal value) {
        if (value == null) {
            return null;
        }
        try {
            String normalized = values.normalize(key, value.toPlainString());
            return key == FactKey.BASE_SALARY && new BigDecimal(normalized).compareTo(BigDecimal.valueOf(1000)) < 0 ? null : normalized;
        } catch (ApiException e) {
            return null;
        }
    }

    private static void put(Map<FactKey, BigDecimal> found, FactKey key, BigDecimal value) {
        if (value != null) {
            found.put(key, value);
        }
    }

    private static BigDecimal orZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static BigDecimal number(Map<String, Object> raw, String name) {
        Object value = raw.get(name);
        if (value instanceof Number n) {
            return new BigDecimal(n.toString());
        }
        if (value instanceof String s && !s.isBlank()) {
            try {
                return new BigDecimal(s.replace(",", "").replace("₪", "").replace("%", "").trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /** By content, not by the name or type the browser sent. */
    static String mimeTypeOf(byte[] file) {
        if (startsWith(file, 0x89, 'P', 'N', 'G')) {
            return "image/png";
        }
        if (startsWith(file, 0xFF, 0xD8, 0xFF)) {
            return "image/jpeg";
        }
        if (startsWith(file, '%', 'P', 'D', 'F')) {
            return "application/pdf";
        }
        if (startsWith(file, 'R', 'I', 'F', 'F') && file.length > 12 && file[8] == 'W' && file[9] == 'E' && file[10] == 'B' && file[11] == 'P') {
            return "image/webp";
        }
        return null;
    }

    private static boolean startsWith(byte[] file, int... prefix) {
        if (file.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if ((file[i] & 0xFF) != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private static Map<String, Object> schema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        for (String name : NUMBERS) {
            properties.put(name, Map.of("type", "NUMBER", "nullable", true));
        }
        return Map.of("type", "OBJECT", "properties", properties);
    }
}
