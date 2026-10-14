package com.shani.caradvisor.engine;

import java.math.BigDecimal;

/** One month's pay for one option, rounded to agorot. */
public record NetResult(
        BigDecimal grossCash,
        BigDecimal taxableIncome,
        BigDecimal incomeTax,
        BigDecimal nationalInsurance,
        BigDecimal healthTax,
        BigDecimal pension,
        BigDecimal studyFund,
        BigDecimal participation,
        BigDecimal familyShare,
        BigDecimal net) {
}
