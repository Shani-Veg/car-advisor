package com.shani.caradvisor.api;

import com.shani.caradvisor.workflow.CaseWorkflowService;
import com.shani.caradvisor.workflow.FactEntity;
import com.shani.caradvisor.workflow.FactKey;
import com.shani.caradvisor.workflow.FleetReport;
import com.shani.caradvisor.workflow.FleetReport.Row;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The rep's Excel export: behind the login, a real workbook, every case, and no salary. */
@SpringBootTest(properties = {"advisor.rep.username=rep", "advisor.rep.password=test-pass"})
@AutoConfigureMockMvc
class ExportTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CaseWorkflowService workflow;

    @Autowired
    private FleetReport report;

    @Test
    void theExportNeedsTheRepLogin() throws Exception {
        mvc.perform(get("/api/rep/export.xlsx")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/rep/export.xlsx").with(httpBasic("rep", "guess"))).andExpect(status().isUnauthorized());
    }

    @Test
    void aComparedCaseIsARowWithTheEnginesNumbers() {
        String id = noaWithAtto3AskingTheRep();

        Row row = report.build().rows().stream()
                .filter(r -> "ATTO 3 (2025)".equals(r.alternative()) && "ממתינה".equals(r.repQuestion()))
                .reduce((first, second) -> second).orElseThrow();

        assertThat(row.occupation()).isEqualTo("מנהל/ת מוצר");
        assertThat(row.group()).isEqualTo("ג");
        assertThat(row.drive()).isEqualTo("חשמלי");
        assertThat(row.eligible()).isTrue();
        assertThat(row.children()).isEqualTo(2);
        // the same numbers the employee sees, straight from the engine
        assertThat(row.difference()).isEqualByComparingTo(workflow.view(id).comparison().difference());
        assertThat(row.netAlternative().subtract(row.netToday())).isEqualByComparingTo(row.difference());
        assertThat(row.participationStatus()).isEqualTo("הערכה");
        assertThat(row.alternativeCost()).isEqualByComparingTo(new BigDecimal("4100"));
    }

    @Test
    void theTrendsCountWhatWasCompared() {
        noaWithAtto3AskingTheRep();

        FleetReport.Trends trends = report.build().trends();

        assertThat(trends.compared()).isPositive();
        assertThat(trends.electric()).isPositive();
        assertThat(trends.repWaiting()).isPositive();
        assertThat(trends.byCar()).anySatisfy(b -> {
            assertThat(b.label()).isEqualTo("ATTO 3 (2025)");
            assertThat(b.detail()).isEqualTo("חשמלי");
        });
        assertThat(trends.byDrive()).extracting(FleetReport.Bucket::label).contains("חשמלי");
    }

    @Test
    void aCarAboveTheGroupIsCountedWithoutNumbers() {
        String id = workflow.create().caseId();
        form(id, FactKey.OCCUPATION, "מפתח/ת תוכנה");
        form(id, FactKey.BASE_SALARY, "16000");
        form(id, FactKey.CHILDREN_UP_TO_23, "1");
        form(id, FactKey.CURRENT_CAR, "CAR-06");
        form(id, FactKey.CURRENT_PARTICIPATION, "250");
        workflow.chooseAlternative(id, "car", "CAR-14", null);

        FleetReport.Report built = report.build();

        Row row = built.rows().stream().filter(r -> "MODEL 3 (2025)".equals(r.alternative()))
                .reduce((first, second) -> second).orElseThrow();
        assertThat(row.eligible()).isFalse();
        assertThat(row.difference()).isNull();
        assertThat(built.trends().aboveEligibility()).isPositive();
        assertThat(built.trends().byCar()).extracting(FleetReport.Bucket::label).doesNotContain("MODEL 3 (2025)");
    }

    @Test
    void theFileIsAnExcelWorkbookWithTwoRightToLeftSheetsAndNoSalary() throws Exception {
        noaWithAtto3AskingTheRep();

        byte[] file = mvc.perform(get("/api/rep/export.xlsx").with(httpBasic("rep", "test-pass")))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", ExportController.XLSX.toString()))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.startsWith("attachment; filename=")))
                .andReturn().getResponse().getContentAsByteArray();

        Map<String, String> parts = unzip(file);
        assertThat(parts).containsKeys("[Content_Types].xml", "xl/workbook.xml", "xl/styles.xml",
                "xl/worksheets/sheet1.xml", "xl/worksheets/sheet2.xml");
        assertThat(parts.get("xl/workbook.xml")).contains("name=\"מגמות\"", "name=\"פניות\"");
        assertThat(parts.get("xl/worksheets/sheet1.xml")).contains("rightToLeft=\"1\"", "ATTO 3 (2025)");
        assertThat(parts.get("xl/worksheets/sheet2.xml")).contains("rightToLeft=\"1\"", "<autoFilter", "ממתינה");
        // the base salary is never exported
        assertThat(parts.get("xl/worksheets/sheet2.xml")).doesNotContain(">18500<", "18,500");
    }

    @Test
    void textIsEscapedAndColumnsAreNamedLikeExcel() {
        assertThat(Xlsx.escape("<b>\"a\" & b</b>\u0001")).isEqualTo("&lt;b&gt;&quot;a&quot; &amp; b&lt;/b&gt;");
        assertThat(Xlsx.column(0)).isEqualTo("A");
        assertThat(Xlsx.column(25)).isEqualTo("Z");
        assertThat(Xlsx.column(26)).isEqualTo("AA");
    }

    private String noaWithAtto3AskingTheRep() {
        String id = workflow.create().caseId();
        form(id, FactKey.OCCUPATION, "מנהל/ת מוצר");
        form(id, FactKey.BASE_SALARY, "18500");
        form(id, FactKey.CHILDREN_UP_TO_23, "2");
        form(id, FactKey.CURRENT_CAR, "CAR-09");
        form(id, FactKey.CURRENT_PARTICIPATION, "300");
        workflow.chooseAlternative(id, "car", "CAR-10", null);
        workflow.requestRepConfirmation(id);
        return id;
    }

    private void form(String id, FactKey key, Object value) {
        workflow.setFact(id, key, value, FactEntity.Source.FORM, null, null);
    }

    private static Map<String, String> unzip(byte[] file) throws IOException {
        Map<String, String> parts = new HashMap<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(file), StandardCharsets.UTF_8)) {
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                parts.put(entry.getName(), new String(zip.readAllBytes(), StandardCharsets.UTF_8));
            }
        }
        return parts;
    }
}
