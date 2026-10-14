package com.shani.caradvisor.engine;

import java.math.BigDecimal;

/**
 * A car from the company catalog, already matched to its tax-authority use value.
 * officialUrl is the model's page on the importer's site, or null when there is no reliable one.
 */
public record Vehicle(
        String id,
        String group,
        String maker,
        String commercialName,
        String modelDescription,
        VehicleKey key,
        Drive drive,
        BigDecimal listPrice,
        BigDecimal monthlyUseValue,
        BigDecimal monthlyCost,
        BigDecimal defaultParticipation,
        String officialUrl) {

    public static final String FAMILY_TRACK_GROUP = "משפחתי";

    public boolean isFamilyTrack() {
        return FAMILY_TRACK_GROUP.equals(group);
    }

    public String label() {
        return commercialName + " (" + key.registrationYear() + ")";
    }
}
