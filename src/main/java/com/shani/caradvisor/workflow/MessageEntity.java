package com.shani.caradvisor.workflow;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "case_message")
public class MessageEntity {

    public enum Role { EMPLOYEE, BOT, SYSTEM }

    @Id
    @GeneratedValue
    private Long id;

    private String caseId;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(length = 4000)
    private String text;

    private String clientRequestId;

    private Instant createdAt;

    /** True for a reply that did not move the case forward: the only button offered is "continue the questions". */
    private Boolean continueOnly;

    protected MessageEntity() {
    }

    public MessageEntity(String caseId, Role role, String text, String clientRequestId, Instant now) {
        this.caseId = caseId;
        this.role = role;
        this.text = text;
        this.clientRequestId = clientRequestId;
        this.createdAt = now;
    }

    public Long id() {
        return id;
    }

    public Role role() {
        return role;
    }

    public String text() {
        return text;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public boolean continueOnly() {
        return Boolean.TRUE.equals(continueOnly);
    }

    void markContinueOnly() {
        this.continueOnly = true;
    }
}
