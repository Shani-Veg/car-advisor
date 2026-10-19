package com.shani.caradvisor.agent;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Only payroll terms and their numbers leave; nothing that identifies the employee does. */
class PayslipTextTest {

    @Test
    void theSamplePayslipGivesOnlyItsPayrollLines() throws IOException {
        List<String> lines = PayslipText.fromPdf(Files.readAllBytes(Path.of("demo/sample-payslip.pdf"))).orElseThrow();

        assertThat(lines).contains("שכר יסוד: 18,500.00", "עמלות: 0.00", "בונוס: 0.00", "נקודות זיכוי: 2.75",
                "קרן פנסיה תגמולי עובד: 1,110.00", "קרן השתלמות: 462.50", "השתתפות עובד ברכב: 300.00", "שווי רכב: 4,070.00");
        String sent = String.join("\n", lines);
        // name, ID, employee number, address, bank and account, employer, tax file, job, the car's name
        assertThat(sent).doesNotContain("נועה", "000000018", "000123", "רחוב", "בנק", "0000000", "חברה", "000000000",
                "מנהלת", "COROLLA", "מס הכנסה", "3,443.15", "11,031.82");
    }

    @Test
    void numbersNextToAPersonalLabelNeverLeave() {
        List<String> lines = PayslipText.payrollLines("""
                שכר יסוד 18,500.00 חשבון 123456
                מספר זהות 012345678 נקודות זיכוי 2.25 שם עובד ישראלי
                """);

        assertThat(lines).containsExactly("שכר יסוד: 18,500.00", "נקודות זיכוי: 2.25");
    }

    @Test
    void twoColumnsMergedIntoOneLineDoNotMix() {
        List<String> lines = PayslipText.payrollLines("""
                מס הכנסה 3,443.15 שכר יסוד 18,500.00
                ביטוח לאומי 1,131.59 עמלות 0.00
                קרן פנסיה 6% 1,110.00 בונוס 0.00
                """);

        assertThat(lines).containsExactly("שכר יסוד: 18,500.00", "עמלות: 0.00", "קרן פנסיה: 6% 1,110.00", "בונוס: 0.00");
    }

    @Test
    void identifiersAccountsAndYearsAreNotSafeNumbers() {
        assertThat(PayslipText.safe("012345678")).isFalse();
        assertThat(PayslipText.safe("000123")).isFalse();
        assertThat(PayslipText.safe("1234567")).isFalse();
        assertThat(PayslipText.safe("2026")).isFalse();
        assertThat(PayslipText.safe("18,500.00")).isTrue();
        assertThat(PayslipText.safe("18500")).isTrue();
        assertThat(PayslipText.safe("6%")).isTrue();
        assertThat(PayslipText.safe("0.00")).isTrue();
    }

    @Test
    void aScannedPdfWithoutTextIsNotSentAtAll() throws IOException {
        ByteArrayOutputStream scan = new ByteArrayOutputStream();
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage());
            document.save(scan);
        }

        assertThat(PayslipText.fromPdf(scan.toByteArray())).isEmpty();
    }
}
