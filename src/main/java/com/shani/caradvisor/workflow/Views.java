package com.shani.caradvisor.workflow;

import com.shani.caradvisor.engine.NetResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** The JSON shapes of the API contract (docs/handoff-gpt-ui.md). */
public final class Views {

    private Views() {
    }

    public record CaseView(
            String caseId,
            long version,
            String status,
            boolean assistantAvailable,
            String assistantMode,
            List<ProfileField> profile,
            List<MessageView> messages,
            ComparisonView comparison,
            List<RankingItem> ranking,
            List<PendingQuestion> pendingQuestions,
            List<String> assumptionsToConfirm) {
    }

    /** What the payslip reader found, for the employee to approve. Nothing here is stored yet. */
    public record PayslipReading(List<PayslipField> fields, List<String> notFound) {
    }

    /** current: what the case holds now (empty when missing), so the screen can show what would change. */
    public record PayslipField(String key, String label, Object value, String display, String current,
                               String currentStatus) {
    }

    public record ProfileField(String key, String label, Object value, String display, String status, SourceView source) {
    }

    public record SourceView(String type, String ref, Instant at) {
    }

    public record MessageView(String id, String role, String text, Instant at, List<ChoiceView> choices) {
    }

    public record ChoiceView(String id, String label) {
    }

    public record ComparisonView(
            String rulesVersion,
            boolean eligible,
            String eligibilityReason,
            SideView today,
            SideView alternative,
            BigDecimal difference,
            String direction,
            List<FactorView> factors,
            BreakdownView breakdown,
            List<String> estimates,
            String explanation) {
    }

    /** officialUrl: the model's page on the importer's site; null for waiving the car or when there is none. */
    public record SideView(String label, BigDecimal net, String officialUrl, String type, String vehicleId) {
    }

    public record FactorView(String component, String label, BigDecimal from, BigDecimal to, BigDecimal netImpact) {
    }

    public record BreakdownView(NetResult today, NetResult alternative) {
    }

    public record RankingItem(RankingAlternative alternative, BigDecimal difference, boolean participationEstimated) {
    }

    public record RankingAlternative(String type, String vehicleId, String label, String group, String drive,
                                     String officialUrl) {
    }

    public record PendingQuestion(String id, String fieldKey, String question, String status, Instant createdAt) {
    }

    public record RepQuestionView(
            String id,
            String caseId,
            String employeeLabel,
            String fieldKey,
            String fieldLabel,
            String question,
            RepContext context,
            String status,
            boolean stale,
            Instant createdAt,
            RepAnswer answer) {
    }

    public record RepContext(List<KnownItem> known, String assumption, SuggestedAnswer suggestedAnswer) {
    }

    public record KnownItem(String label, String display) {
    }

    public record SuggestedAnswer(Object value, String display, String source) {
    }

    public record RepAnswer(Object value, String display, LocalDate effectiveFrom, String note, String answeredBy,
                            Instant answeredAt) {
    }

    public record CatalogItem(
            String id,
            String group,
            String maker,
            String commercialName,
            int regYear,
            String drive,
            BigDecimal monthlyUseValue,
            BigDecimal defaultParticipation,
            String officialUrl) {
    }

    public record OccupationItem(
            String name,
            String group,
            boolean carRequiredByRole,
            boolean waiverAllowed,
            BigDecimal waiverMaintenance) {
    }
}
