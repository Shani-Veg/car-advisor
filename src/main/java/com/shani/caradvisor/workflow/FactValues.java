package com.shani.caradvisor.workflow;

import com.shani.caradvisor.data.ReferenceData;
import com.shani.caradvisor.engine.Money;
import com.shani.caradvisor.engine.Vehicle;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Validates incoming values into one canonical string per fact, and turns them back into typed values and display text. */
@Component
public class FactValues {

    /** currentCar value for "no company car today (receives a maintenance allowance)". */
    public static final String NO_CAR = "NONE";

    private static final BigDecimal MAX_MONEY = BigDecimal.valueOf(1_000_000);
    private static final BigDecimal MAX_PERCENT = BigDecimal.valueOf(100);
    private static final BigDecimal MAX_POINTS = BigDecimal.valueOf(20);
    private static final BigDecimal MAX_CHILDREN = BigDecimal.valueOf(30);

    private final ReferenceData data;

    public FactValues(ReferenceData data) {
        this.data = data;
    }

    public String normalize(FactKey key, Object raw) {
        if (raw == null || raw.toString().isBlank()) {
            throw ApiException.badRequest("MISSING_VALUE", "חסר ערך עבור " + key.label());
        }
        String text = raw.toString().trim();
        return switch (key.type()) {
            case OCCUPATION -> {
                data.occupations().byName(text).orElseThrow(() ->
                        ApiException.badRequest("UNKNOWN_OCCUPATION", "העיסוק '" + text + "' לא מופיע בטבלת העיסוקים"));
                yield text;
            }
            case GROUP -> {
                if (!data.rules().groupOrder().contains(text)) {
                    throw ApiException.badRequest("UNKNOWN_GROUP", "קבוצת זכאות לא מוכרת: " + text);
                }
                yield text;
            }
            case MONEY -> number(key, text, MAX_MONEY).toPlainString();
            case PERCENT -> number(key, text, MAX_PERCENT).toPlainString();
            case POINTS -> number(key, text, MAX_POINTS).toPlainString();
            case COUNT -> {
                BigDecimal count = number(key, text, MAX_CHILDREN);
                if (count.scale() > 0) {
                    throw ApiException.badRequest("INVALID_VALUE", key.label() + " חייב להיות מספר שלם");
                }
                yield count.toPlainString();
            }
            case CURRENT_CAR -> {
                if (NO_CAR.equals(text)) {
                    yield NO_CAR;
                }
                data.catalog().byId(text).orElseThrow(() ->
                        ApiException.badRequest("UNKNOWN_VEHICLE", "הרכב " + text + " לא נמצא בקטלוג"));
                yield text;
            }
        };
    }

    /** The value as JSON should carry it: numbers as numbers, the rest as text. */
    public Object typed(FactKey key, String value) {
        return switch (key.type()) {
            case MONEY, PERCENT, POINTS -> new BigDecimal(value);
            case COUNT -> Integer.parseInt(value);
            default -> value;
        };
    }

    public String display(FactKey key, String value) {
        return switch (key.type()) {
            case MONEY -> Money.display(new BigDecimal(value));
            case PERCENT -> value + "%";
            case CURRENT_CAR -> NO_CAR.equals(value)
                    ? "אין רכב (מקבל/ת אחזקת רכב)"
                    : data.catalog().byId(value).map(Vehicle::label).orElse(value);
            default -> value;
        };
    }

    private static BigDecimal number(FactKey key, String text, BigDecimal max) {
        String cleaned = text.replace("₪", "").replace(",", "").replace("%", "").trim();
        BigDecimal value;
        try {
            value = new BigDecimal(cleaned).stripTrailingZeros();
        } catch (NumberFormatException e) {
            throw ApiException.badRequest("INVALID_VALUE", key.label() + ": '" + text + "' אינו מספר");
        }
        if (value.signum() < 0 || value.compareTo(max) > 0) {
            throw ApiException.badRequest("INVALID_VALUE", key.label() + " מחוץ לטווח המותר");
        }
        return value.scale() < 0 ? value.setScale(0) : value;
    }
}
