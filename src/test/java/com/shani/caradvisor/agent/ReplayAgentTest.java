package com.shani.caradvisor.agent;

import com.shani.caradvisor.workflow.ApiException;
import com.shani.caradvisor.workflow.CaseWorkflowService;
import com.shani.caradvisor.workflow.Views.CaseView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The whole agent with recorded turns instead of Gemini: same tools, same checks, no network, no key. */
@SpringBootTest(properties = {
        "advisor.llm.mode=replay",
        "advisor.llm.replay-file=src/test/resources/replay-sample.jsonl",
        "advisor.assistant.enabled=true"})
class ReplayAgentTest {

    @Autowired
    private AdvisorAgent agent;

    @Autowired
    private CaseWorkflowService workflow;

    @Test
    void aRecordedSentenceRunsTheRealToolsAndIsDeclaredAsReplay() {
        String id = workflow.create().caseId();

        CaseView view = agent.reply(id, "אני מנהלת מוצר   ושכר הבסיס שלי 18,500", "r-1");

        assertThat(view.assistantMode()).isEqualTo("REPLAY");
        assertThat(view.assistantAvailable()).isTrue();
        assertThat(view.profile()).filteredOn(f -> f.key().equals("baseSalary"))
                .singleElement().satisfies(f -> {
                    assertThat(f.status()).isEqualTo("PROVIDED");
                    assertThat(f.source().type()).isEqualTo("EMPLOYEE_MESSAGE");
                });
        assertThat(view.messages()).anySatisfy(m -> assertThat(m.text()).startsWith("רשמתי:").contains("18,500 ₪"));
        assertThat(view.messages()).anySatisfy(m -> assertThat(m.text()).isEqualTo("תודה! כמה אחוזים מפרישים לפנסיה?"));
    }

    @Test
    void aSentenceThatWasNeverRecordedIsUnavailableButTheMessageIsKept() {
        String id = workflow.create().caseId();

        assertThatThrownBy(() -> agent.reply(id, "משהו שלא הוקלט", "r-2"))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.code()).isEqualTo("ASSISTANT_UNAVAILABLE"));

        assertThat(workflow.view(id).messages()).anySatisfy(m -> assertThat(m.text()).isEqualTo("משהו שלא הוקלט"));
    }
}
