package com.shani.caradvisor.agent;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** The model may record an amount only if the employee wrote it. */
class StatedInMessageTest {

    @Test
    void writtenAmountsCount() {
        assertThat(AgentTools.statedInMessage(18500, "שכר הבסיס שלי 18,500")).isTrue();
        assertThat(AgentTools.statedInMessage(18000, "מרוויחה 18 אלף")).isTrue();
        assertThat(AgentTools.statedInMessage(18000, "מספר הילדים שלי 2, שכר 18000")).isTrue();
    }

    @Test
    void zeroNeedsTheEmployeeToSayThereIsNone() {
        assertThat(AgentTools.statedInMessage(0, "אין לי עמלות")).isTrue();
        assertThat(AgentTools.statedInMessage(0, "בונוס 0")).isTrue();
        assertThat(AgentTools.statedInMessage(0, "אני לא משלמת על הרכב")).isTrue();
        assertThat(AgentTools.statedInMessage(0, "אין לי מושג כמה אני משלם היום")).isFalse();
        assertThat(AgentTools.statedInMessage(0, "יש לי MG4")).isFalse();
    }

    @Test
    void anIdentifierIsNotAnAmount() {
        assertThat(AgentTools.statedInMessage(45000, "מספר העובד שלי 45000 ויש לי 2 ילדים")).isFalse();
        assertThat(AgentTools.statedInMessage(17600, "המשכורת שלי עלתה בעשרה אחוזים")).isFalse();
    }
}
