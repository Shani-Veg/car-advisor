package com.shani.caradvisor.workflow;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.time.LocalDate;

/**
 * One known fact with where it came from. "Missing" is the absence of a row, never a 0:
 * a 0 the employee typed is PROVIDED, a catalog value is DEFAULT, a rep's answer is CONFIRMED.
 */
@Entity
@Table(name = "case_fact", uniqueConstraints = @UniqueConstraint(columnNames = {"caseId", "factKey"}))
public class FactEntity {

    public enum Status { PROVIDED, DEFAULT, CONFIRMED }

    public enum Source { EMPLOYEE_MESSAGE, FORM, REP, CATALOG_DEFAULT }

    @Id
    @GeneratedValue
    private Long id;

    private String caseId;

    @Enumerated(EnumType.STRING)
    private FactKey factKey;

    @Column(name = "fact_value")
    private String value;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Source source;

    private String sourceRef;

    private LocalDate effectiveFrom;

    private Instant updatedAt;

    protected FactEntity() {
    }

    FactEntity(String caseId, FactKey factKey) {
        this.caseId = caseId;
        this.factKey = factKey;
    }

    void set(String value, Source source, String sourceRef, LocalDate effectiveFrom, Instant now) {
        this.value = value;
        this.source = source;
        this.sourceRef = sourceRef;
        this.effectiveFrom = effectiveFrom;
        this.updatedAt = now;
        this.status = switch (source) {
            case REP -> Status.CONFIRMED;
            case CATALOG_DEFAULT -> Status.DEFAULT;
            case EMPLOYEE_MESSAGE, FORM -> Status.PROVIDED;
        };
    }

    public FactKey factKey() {
        return factKey;
    }

    public String value() {
        return value;
    }

    public Status status() {
        return status;
    }

    public Source source() {
        return source;
    }

    public String sourceRef() {
        return sourceRef;
    }

    public LocalDate effectiveFrom() {
        return effectiveFrom;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
