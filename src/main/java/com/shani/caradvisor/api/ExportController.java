package com.shani.caradvisor.api;

import com.shani.caradvisor.api.Xlsx.Sheet;
import com.shani.caradvisor.api.Xlsx.Style;
import com.shani.caradvisor.workflow.FleetReport;
import com.shani.caradvisor.workflow.FleetReport.Bucket;
import com.shani.caradvisor.workflow.FleetReport.Report;
import com.shani.caradvisor.workflow.FleetReport.Row;
import com.shani.caradvisor.workflow.FleetReport.Trends;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.shani.caradvisor.api.Xlsx.cell;
import static com.shani.caradvisor.api.Xlsx.text;

/**
 * The rep screen's "export to Excel": every case and the fleet trends, for Strauss to follow demand, check the
 * numbers and plan. Behind the rep login like the rest of /api/rep.
 */
@RestController
public class ExportController {

    static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    private static final ZoneId ISRAEL = ZoneId.of("Asia/Jerusalem");
    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ISRAEL);
    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ISRAEL);

    private final FleetReport report;

    public ExportController(FleetReport report) {
        this.report = report;
    }

    @GetMapping("/api/rep/export.xlsx")
    public ResponseEntity<byte[]> export() {
        Report built = report.build();
        byte[] file = Xlsx.write(List.of(trends(built), cases(built)));
        String name = "car-advisor-" + FILE_DATE.format(built.generatedAt()) + ".xlsx";
        return ResponseEntity.ok()
                .contentType(XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(name, StandardCharsets.UTF_8).build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(file);
    }

    private static Sheet trends(Report built) {
        Trends t = built.trends();
        Sheet sheet = new Sheet("מגמות", 34, 16, 16, 20)
                .row(cell("יועץ הרכב · דוח מגמות", Style.TITLE))
                .row(cell("הופק ב-" + WHEN.format(built.generatedAt())
                        + " · כל הפניות במערכת · בלי שכר ובלי תוכן השיחות", Style.NOTE))
                .blank()
                .row(cell("נתון", Style.HEADER), cell("ערך", Style.HEADER))
                .row(text("פניות"), text(t.cases()))
                .row(text("פניות שהגיעו להשוואה"), text(t.compared()))
                .row(text("מתוכן בדקו רכב חשמלי"), cell(share(t.electric(), t.compared()), Style.PERCENT))
                .row(text("מתוכן בדקו ויתור על הרכב"), cell(share(t.waivers(), t.compared()), Style.PERCENT))
                .row(text("ביקשו רכב מעל הזכאות"), text(t.aboveEligibility()))
                .row(text("שינוי ממוצע בנטו (לחודש)"), cell(t.averageDifference(), Style.SHEKEL))
                .row(text("שאלות לנציג"), text(t.repQuestions()))
                .row(text("נענו"), text(t.repAnswered()))
                .row(text("ממתינות"), text(t.repWaiting()))
                .row(text("זמן מענה ממוצע (שעות)"), cell(t.averageAnswerHours(), Style.DECIMAL))
                .blank();
        buckets(sheet, "הרכבים שנבדקו הכי הרבה", "רכב", "הנעה", t.byCar());
        buckets(sheet, "לפי סוג הנעה", "הנעה", null, t.byDrive());
        buckets(sheet, "לפי קבוצת זכאות", "קבוצה", null, t.byGroup());
        return sheet;
    }

    private static void buckets(Sheet sheet, String title, String label, String detail, List<Bucket> buckets) {
        sheet.row(cell(title, Style.TITLE));
        sheet.row(cell(label, Style.HEADER), cell(detail == null ? "" : detail, Style.HEADER),
                cell("פניות", Style.HEADER), cell("שינוי ממוצע בנטו", Style.HEADER));
        if (buckets.isEmpty()) {
            sheet.row(cell("אין עדיין פניות שהגיעו להשוואה", Style.NOTE));
        }
        for (Bucket b : buckets) {
            sheet.row(text(b.label()), text(b.detail()), text(b.cases()), cell(b.averageDifference(), Style.SHEKEL));
        }
        sheet.blank();
    }

    private static Sheet cases(Report built) {
        Sheet sheet = new Sheet("פניות", 17, 15, 20, 8, 9, 22, 22, 10, 9, 12, 12, 12, 13, 13, 13, 14, 11, 12, 12)
                .asTable()
                .row(header("נפתח"), header("מצב"), header("עיסוק"), header("קבוצה"), header("ילדים עד 23"),
                        header("רכב היום"), header("חלופה שנבדקה"), header("הנעה"), header("בזכאות"),
                        header("נטו היום"),
                        header("נטו בחלופה"), header("שינוי בנטו"), header("עלות החלופה לחברה"),
                        header("תקציב הקבוצה"), header("השתתפות בחלופה"), header("סטטוס ההשתתפות"),
                        header("שאלה לנציג"), header("ענה/תה"), header("זמן מענה (שעות)"));
        for (Row r : built.rows()) {
            sheet.row(text(r.createdAt() == null ? null : WHEN.format(r.createdAt())), text(r.status()),
                    text(r.occupation()), text(r.group()), text(r.children()), text(r.currentCar()),
                    text(r.alternative()), text(r.drive()),
                    text(r.eligible() == null ? null : r.eligible() ? "כן" : "לא"), cell(r.netToday(), Style.SHEKEL),
                    cell(r.netAlternative(), Style.SHEKEL), cell(r.difference(), Style.SHEKEL),
                    cell(r.alternativeCost(), Style.SHEKEL), cell(r.budget(), Style.SHEKEL),
                    cell(r.participation(), Style.SHEKEL), text(r.participationStatus()), text(r.repQuestion()),
                    text(r.answeredBy()), cell(r.answerHours(), Style.DECIMAL));
        }
        return sheet;
    }

    private static Xlsx.Cell header(String title) {
        return cell(title, Style.HEADER);
    }

    private static Double share(int part, int whole) {
        return whole == 0 ? null : (double) part / whole;
    }
}
