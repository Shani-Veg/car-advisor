package com.shani.caradvisor.agent;

import com.shani.caradvisor.workflow.ApiException;
import com.shani.caradvisor.workflow.CaseWorkflowService;
import com.shani.caradvisor.workflow.ChatSummaries;
import com.shani.caradvisor.workflow.Views.CaseView;
import com.shani.caradvisor.workflow.Views.MessageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * One employee chat turn: the model reads the case state, calls tools (which run the same workflow as the form),
 * and writes a short reply in words. Every number the employee sees in the chat is written by the server.
 */
@Service
public class AdvisorAgent {

    private static final Logger log = LoggerFactory.getLogger(AdvisorAgent.class);
    private static final int MAX_STEPS = 6;
    private static final int HISTORY_MESSAGES = 16;
    private static final String UNAVAILABLE = "העוזר לא זמין כרגע. אפשר להמשיך במילוי הטופס.";

    private final CaseWorkflowService workflow;
    private final AgentTools tools;
    private final LlmClient llm;
    private final RateLimiter rateLimiter;

    public AdvisorAgent(CaseWorkflowService workflow, AgentTools tools, LlmClient llm,
                        @Value("${advisor.assistant.max-messages-per-case-per-minute:6}") int perCase,
                        @Value("${advisor.assistant.max-messages-per-minute:30}") int overall) {
        this.workflow = workflow;
        this.tools = tools;
        this.llm = llm;
        this.rateLimiter = new RateLimiter(perCase, overall);
    }

    public CaseView reply(String caseId, String text, String clientRequestId) {
        if (!workflow.assistantAvailable()) {
            throw ApiException.unavailable("ASSISTANT_UNAVAILABLE", UNAVAILABLE);
        }
        if (!rateLimiter.tryAcquire(caseId, Instant.now())) {
            throw new ApiException(429, "RATE_LIMITED", "יותר מדי הודעות בזמן קצר. נסו שוב בעוד דקה.");
        }
        Optional<Long> messageId = workflow.recordEmployeeMessage(caseId, text, clientRequestId);
        if (messageId.isEmpty()) {
            return workflow.view(caseId);
        }

        List<Map<String, Object>> contents = history(workflow.view(caseId));
        AgentTools.TurnLog turnLog = new AgentTools.TurnLog();
        String reply;
        try {
            reply = converse(caseId, messageId.get(), text.trim(), contents, turnLog);
            reply = guarded(reply, contents);
        } catch (LlmClient.UnavailableException e) {
            log.warn("Assistant unavailable: {}", e.getMessage());
            workflow.addBotMessage(caseId, "העוזר לא זמין כרגע. מה שכתבת נשמר, ואפשר להמשיך במילוי הטופס.");
            throw ApiException.unavailable("ASSISTANT_UNAVAILABLE", UNAVAILABLE);
        }

        CaseView after = workflow.view(caseId);
        if (!turnLog.recorded.isEmpty()) {
            workflow.addBotMessage(caseId, ChatSummaries.confirmation(after, turnLog.recorded));
        }
        workflow.addBotMessage(caseId, reply.isBlank() ? ChatSummaries.fallback(after) : clean(reply));
        if (after.comparison() != null && (turnLog.alternativeChosen || !turnLog.recorded.isEmpty())) {
            workflow.addBotMessage(caseId, ChatSummaries.comparisonSummary(after));
        }
        if (turnLog.optionsListed && after.ranking() != null) {
            workflow.addBotMessage(caseId, ChatSummaries.rankingSummary(after));
        }
        return workflow.view(caseId);
    }

    /** Small models sometimes slip an Arabic waw into a Hebrew word. */
    private static String clean(String reply) {
        return reply.replace('و', 'ו').trim();
    }

    private String converse(String caseId, long messageId, String employeeText, List<Map<String, Object>> contents,
                            AgentTools.TurnLog turnLog) {
        for (int step = 0; step < MAX_STEPS; step++) {
            LlmClient.Turn turn = llm.next(Prompts.system(workflow.view(caseId)), contents, tools.declarations());
            contents.add(turn.rawContent());
            if (turn.calls().isEmpty()) {
                return turn.text();
            }
            List<Map<String, Object>> responses = new ArrayList<>();
            for (LlmClient.FunctionCall call : turn.calls()) {
                Map<String, Object> response = new LinkedHashMap<>();
                if (call.id() != null) {
                    response.put("id", call.id());
                }
                response.put("name", call.name());
                response.put("response", tools.execute(caseId, messageId, employeeText, call, turnLog));
                responses.add(Map.of("functionResponse", response));
            }
            contents.add(Map.of("role", "user", "parts", responses));
        }
        log.warn("Agent stopped after {} steps without a final reply", MAX_STEPS);
        return "";
    }

    /**
     * The model must not write amounts, and must stay in Hebrew. One chance to rewrite,
     * otherwise the server's fallback text is used.
     */
    private String guarded(String reply, List<Map<String, Object>> contents) {
        String text = clean(reply);
        if (acceptable(text)) {
            return text;
        }
        log.info("Reply contained an amount or foreign letters; asking the model to rewrite it");
        contents.add(Map.of("role", "user", "parts", List.of(Map.of("text",
                "[system] Your last reply contained numbers, amounts or non-Hebrew letters. Rewrite it in Hebrew only, without them."))));
        String rewritten = clean(llm.next(Prompts.RULES, contents, List.of()).text());
        return acceptable(rewritten) ? rewritten : "";
    }

    private boolean acceptable(String text) {
        return !ReplyGuard.containsAmount(text, tools.safeNames()) && !ReplyGuard.containsForeignScript(text);
    }

    /** Gemini wants the conversation to start with the user and alternate; merge neighbours with the same role. */
    private static List<Map<String, Object>> history(CaseView view) {
        List<MessageView> messages = view.messages();
        List<MessageView> recent = messages.subList(Math.max(0, messages.size() - HISTORY_MESSAGES), messages.size());
        List<Map<String, Object>> contents = new ArrayList<>();
        String role = null;
        StringBuilder text = new StringBuilder();
        for (MessageView m : recent) {
            String r = "bot".equals(m.role()) ? "model" : "user";
            String t = "system".equals(m.role()) ? "[עדכון מהמערכת] " + m.text() : m.text();
            if (contents.isEmpty() && role == null && "model".equals(r)) {
                continue;
            }
            if (r.equals(role)) {
                text.append("\n").append(t);
            } else {
                if (role != null) {
                    contents.add(content(role, text.toString()));
                }
                role = r;
                text = new StringBuilder(t);
            }
        }
        if (role != null) {
            contents.add(content(role, text.toString()));
        }
        return contents;
    }

    private static Map<String, Object> content(String role, String text) {
        return Map.of("role", role, "parts", List.of(Map.of("text", text)));
    }

}
