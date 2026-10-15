package com.shani.caradvisor.workflow;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

/** One employee's conversation. Every change bumps the revision, which the API exposes as "version". */
@Entity
@Table(name = "advisor_case")
public class CaseEntity {

    public enum Alternative { NONE, CAR, WAIVER }

    @Id
    private String id;

    @Version
    private long lockVersion;

    private long revision;

    @Enumerated(EnumType.STRING)
    private Alternative alternative = Alternative.NONE;

    private String alternativeVehicleId;

    /** The employee answered (or moved past) "are these assumed details right?". Null in cases created before the question existed. */
    private Boolean assumptionsReviewed;

    private Instant createdAt;

    private Instant updatedAt;

    protected CaseEntity() {
    }

    public CaseEntity(String id, Instant now) {
        this.id = id;
        this.createdAt = now;
        this.updatedAt = now;
    }

    void touch(Instant now) {
        revision++;
        updatedAt = now;
    }

    void chooseCar(String vehicleId) {
        alternative = Alternative.CAR;
        alternativeVehicleId = vehicleId;
    }

    void chooseWaiver() {
        alternative = Alternative.WAIVER;
        alternativeVehicleId = null;
    }

    void markAssumptionsReviewed() {
        assumptionsReviewed = true;
    }

    boolean assumptionsReviewed() {
        return Boolean.TRUE.equals(assumptionsReviewed);
    }

    public String id() {
        return id;
    }

    public long revision() {
        return revision;
    }

    public Alternative alternative() {
        return alternative;
    }

    public String alternativeVehicleId() {
        return alternativeVehicleId;
    }

    /** What a question about the alternative refers to; a change makes such questions stale. */
    String alternativeSubject() {
        return alternative == Alternative.CAR ? alternativeVehicleId : alternative.name();
    }
}
