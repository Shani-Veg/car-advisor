package com.shani.caradvisor.data;

import com.shani.caradvisor.engine.Drive;
import com.shani.caradvisor.engine.VehicleKey;

import java.math.BigDecimal;

/** One row of the tax authority's extended use-value file. */
public record UseValueRecord(
        VehicleKey key,
        String maker,
        String modelDescription,
        String commercialName,
        Drive drive,
        String effectiveDate,
        BigDecimal listPrice,
        BigDecimal monthlyUseValue) {
}
