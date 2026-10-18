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

    /**
     * The answer buttons that follow this message once the four questions are done: the next steps (what pays best,
     * electric cars, a few alternatives), only "confirm with the rep", or none. Buttons are offered only where they
     * help, not under every reply. Stored as text (null = none).
     */
    public enum Offers { NONE, NEXT_STEPS, REP }

    @Column(length = 20)
    private String offers;

    /** Catalog ids of cars shown as cards under this message (comma separated), or null. */
    @Column(length = 400)
    private String cars;

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

    public Offers offers() {
        return offers == null ? Offers.NONE : Offers.valueOf(offers);
    }

    void offer(Offers next) {
        this.offers = next == Offers.NONE ? null : next.name();
    }

    public java.util.List<String> cars() {
        return cars == null || cars.isBlank() ? java.util.List.of() : java.util.List.of(cars.split(","));
    }

    void attachCars(java.util.List<String> ids) {
        this.cars = ids.isEmpty() ? null : String.join(",", ids);
    }
}
