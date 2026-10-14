package com.shani.caradvisor.workflow;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

/**
 * A question for the transport rep about one fact of one case. It remembers its subject
 * (e.g. the alternative car); when the employee changes the subject, the question becomes stale
 * and can no longer be answered, so an old answer never lands on a new choice.
 */
@Entity
@Table(name = "rep_question")
public class RepQuestionEntity {

    public enum Status { OPEN, ANSWERED }

    @Id
    private String id;

    private String caseId;

    @Enumerated(EnumType.STRING)
    private FactKey factKey;

    @Column(length = 1000)
    private String question;

    private String subject;

    @Enumerated(EnumType.STRING)
    private Status status = Status.OPEN;

    private boolean stale;

    private Instant createdAt;

    private String answerValue;

    private LocalDate effectiveFrom;

    @Column(length = 1000)
    private String note;

    private String answerRequestId;

    private String answeredBy;

    private Instant answeredAt;

    protected RepQuestionEntity() {
    }

    public RepQuestionEntity(String id, String caseId, FactKey factKey, String question, String subject, Instant now) {
        this.id = id;
        this.caseId = caseId;
        this.factKey = factKey;
        this.question = question;
        this.subject = subject;
        this.createdAt = now;
    }

    void markStale() {
        stale = true;
    }

    void answer(String value, LocalDate effectiveFrom, String note, String requestId, String answeredBy, Instant now) {
        this.status = Status.ANSWERED;
        this.answerValue = value;
        this.effectiveFrom = effectiveFrom;
        this.note = note;
        this.answerRequestId = requestId;
        this.answeredBy = answeredBy;
        this.answeredAt = now;
    }

    public boolean isPending() {
        return status == Status.OPEN && !stale;
    }

    public String id() {
        return id;
    }

    public String caseId() {
        return caseId;
    }

    public FactKey factKey() {
        return factKey;
    }

    public String question() {
        return question;
    }

    public String subject() {
        return subject;
    }

    public Status status() {
        return status;
    }

    public boolean stale() {
        return stale;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public String answerValue() {
        return answerValue;
    }

    public LocalDate effectiveFrom() {
        return effectiveFrom;
    }

    public String note() {
        return note;
    }

    public String answerRequestId() {
        return answerRequestId;
    }

    public String answeredBy() {
        return answeredBy;
    }

    public Instant answeredAt() {
        return answeredAt;
    }
}
