package com.shani.caradvisor.agent;

import com.shani.caradvisor.workflow.ApiException;
import com.shani.caradvisor.workflow.CaseWorkflowService;
import com.shani.caradvisor.workflow.Views.CaseView;
import com.shani.caradvisor.workflow.Views.PayslipField;
import com.shani.caradvisor.workflow.Views.PayslipReading;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The model only reads the payslip: the code computes, validates, and stores nothing until the employee approves. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(AdvisorAgentTest.ScriptedModel.class)
class PayslipReaderTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10, 0, 0};

    @Autowired
    private PayslipReader reader;

    @Autowired
    private CaseWorkflowService workflow;

    @Autowired
    private ScriptedLlm llm;

    @Autowired
    private MockMvc mvc;

    @Test
    void ratesAreComputedFromTheDeductionAmountsByTheCode() {
        String id = workflow.create().caseId();
        llm.document = Map.of("baseSalary", 18500, "commissions", 0, "bonus", 0, "pensionEmployeeAmount", 1110,
                "studyFundEmployeeAmount", 462.5, "creditPoints", 2.75, "carParticipation", -300);

        PayslipReading reading = reader.read(id, PNG);

        assertThat(values(reading)).containsEntry("baseSalary", "18,500 ₪").containsEntry("pensionPercent", "6%")
                .containsEntry("studyFundPercent", "2.5%").containsEntry("creditPoints", "2.75")
                .containsEntry("currentParticipation", "300 ₪").containsEntry("commissions", "0 ₪");
        assertThat(reading.notFound()).isEmpty();
    }

    @Test
    void aValueTheFormWouldRefuseIsDroppedAndNothingIsStored() {
        String id = workflow.create().caseId();
        Map<String, Object> document = new HashMap<>();
        document.put("baseSalary", 18500);
        document.put("pensionEmployeePercent", 250);
        document.put("creditPoints", null);
        llm.document = document;

        PayslipReading reading = reader.read(id, PNG);

        assertThat(values(reading)).containsOnlyKeys("baseSalary");
        assertThat(reading.notFound()).contains("הפרשת עובד לפנסיה", "נקודות זיכוי");
        CaseView after = workflow.view(id);
        assertThat(after.profile()).filteredOn(f -> f.key().equals("baseSalary")).allMatch(f -> f.status().equals("MISSING"));
    }

    @Test
    void onlyImagesAndPdfsAreRead() {
        String id = workflow.create().caseId();
        llm.document = Map.of("baseSalary", 18500);

        assertThatThrownBy(() -> reader.read(id, "<html>not a payslip</html>".getBytes()))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.code()).isEqualTo("UNSUPPORTED_FILE"));
        assertThat(PayslipReader.mimeTypeOf("%PDF-1.7".getBytes())).isEqualTo("application/pdf");
        assertThat(PayslipReader.mimeTypeOf(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0})).isEqualTo("image/jpeg");
    }

    @Test
    void whenTheModelCannotReadTheFormStillWorks() {
        String id = workflow.create().caseId();
        llm.document = null;

        assertThatThrownBy(() -> reader.read(id, PNG))
                .isInstanceOfSatisfying(ApiException.class, e -> {
                    assertThat(e.status()).isEqualTo(503);
                    assertThat(e.code()).isEqualTo("PAYSLIP_UNAVAILABLE");
                });
    }

    @Test
    void theUploadNeedsTheAppsOwnHeader() throws Exception {
        String id = workflow.create().caseId();
        llm.document = Map.of("baseSalary", 18500);
        MockMultipartFile file = new MockMultipartFile("file", "slip.png", "image/png", PNG);

        mvc.perform(multipart("/api/cases/" + id + "/payslip").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("UPLOAD_HEADER_MISSING"));
        mvc.perform(multipart("/api/cases/" + id + "/payslip").file(file).header("X-Requested-With", "car-advisor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fields[0].key").value("baseSalary"));
    }

    private static Map<String, String> values(PayslipReading reading) {
        Map<String, String> map = new HashMap<>();
        for (PayslipField field : reading.fields()) {
            map.put(field.key(), field.display());
        }
        return map;
    }
}
