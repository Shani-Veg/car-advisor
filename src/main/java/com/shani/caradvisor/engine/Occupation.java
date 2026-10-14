package com.shani.caradvisor.engine;

import java.math.BigDecimal;

public record Occupation(
        String name,
        String group,
        boolean carRequiredByRole,
        boolean waiverAllowed,
        BigDecimal waiverMaintenance) {
}
