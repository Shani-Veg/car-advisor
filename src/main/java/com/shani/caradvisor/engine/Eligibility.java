package com.shani.caradvisor.engine;

/** Whether the employee may take an option, and why, in plain Hebrew. */
public record Eligibility(boolean eligible, Code code, String reason) {

    public enum Code {
        OWN_OR_LOWER_GROUP,
        HIGHER_GROUP,
        FAMILY_TRACK,
        FAMILY_TRACK_NEEDS_CHILDREN,
        WAIVER_ALLOWED,
        WAIVER_NOT_ALLOWED,
        CAR_REQUIRED_BY_ROLE
    }

    static Eligibility yes(Code code, String reason) {
        return new Eligibility(true, code, reason);
    }

    static Eligibility no(Code code, String reason) {
        return new Eligibility(false, code, reason);
    }
}
