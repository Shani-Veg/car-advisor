package com.shani.caradvisor.engine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * Money helpers. All amounts are monthly NIS kept at full precision;
 * rounding happens only at the edges (results shown to people).
 */
public final class Money {

    public static final BigDecimal ZERO = BigDecimal.ZERO;

    private Money() {
    }

    public static BigDecimal of(long value) {
        return BigDecimal.valueOf(value);
    }

    public static BigDecimal of(String value) {
        return new BigDecimal(value.trim());
    }

    /** amount * percent / 100, exact (dividing by 100 never needs rounding). */
    public static BigDecimal percentOf(BigDecimal amount, BigDecimal percent) {
        return amount.multiply(percent).movePointLeft(2);
    }

    public static BigDecimal min(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) <= 0 ? a : b;
    }

    public static BigDecimal max(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    public static BigDecimal cents(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal shekels(BigDecimal value) {
        return value.setScale(0, RoundingMode.HALF_UP);
    }

    /** "18,500 ₪", or "439.91 ₪" when there are agorot. */
    public static String display(BigDecimal value) {
        BigDecimal stripped = value.stripTrailingZeros();
        String number = stripped.scale() <= 0
                ? String.format(Locale.ROOT, "%,d", value.setScale(0, RoundingMode.HALF_UP).longValueExact())
                : String.format(Locale.ROOT, "%,.2f", value);
        return number + " ₪";
    }
}
