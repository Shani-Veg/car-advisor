package com.shani.caradvisor.engine;

import java.math.BigDecimal;
import java.util.Objects;

/** The car-related components of one option, each a monthly NIS amount. */
public record MonthlyBenefits(
        BigDecimal useValue,
        BigDecimal maintenance,
        BigDecimal participation,
        BigDecimal familyShare,
        BigDecimal evGrossUp) {

    /** Order matters: explanation factors are attributed in this order. */
    public enum Component {
        USE_VALUE("שווי שימוש"),
        MAINTENANCE("אחזקת רכב"),
        PARTICIPATION("השתתפות עובד"),
        FAMILY_SHARE("חלק העובד במסלול המשפחתי"),
        EV_REFUND("החזר חשמל מגולם");

        private final String hebrew;

        Component(String hebrew) {
            this.hebrew = hebrew;
        }

        public String hebrew() {
            return hebrew;
        }
    }

    public static final MonthlyBenefits NONE =
            new MonthlyBenefits(Money.ZERO, Money.ZERO, Money.ZERO, Money.ZERO, Money.ZERO);

    public MonthlyBenefits {
        Objects.requireNonNull(useValue, "useValue");
        Objects.requireNonNull(maintenance, "maintenance");
        Objects.requireNonNull(participation, "participation");
        Objects.requireNonNull(familyShare, "familyShare");
        Objects.requireNonNull(evGrossUp, "evGrossUp");
    }

    public BigDecimal get(Component component) {
        return switch (component) {
            case USE_VALUE -> useValue;
            case MAINTENANCE -> maintenance;
            case PARTICIPATION -> participation;
            case FAMILY_SHARE -> familyShare;
            case EV_REFUND -> evGrossUp;
        };
    }

    public MonthlyBenefits with(Component component, BigDecimal value) {
        return new MonthlyBenefits(
                component == Component.USE_VALUE ? value : useValue,
                component == Component.MAINTENANCE ? value : maintenance,
                component == Component.PARTICIPATION ? value : participation,
                component == Component.FAMILY_SHARE ? value : familyShare,
                component == Component.EV_REFUND ? value : evGrossUp);
    }
}
