package com.shani.caradvisor.workflow;

import java.util.Arrays;

/** The facts a case collects. The API exposes them by apiName, in this order. */
public enum FactKey {
    OCCUPATION("occupation", "עיסוק", ValueType.OCCUPATION),
    GROUP("group", "קבוצת זכאות", ValueType.GROUP),
    BASE_SALARY("baseSalary", "שכר בסיס", ValueType.MONEY),
    COMMISSIONS("commissions", "עמלות", ValueType.MONEY),
    BONUS("bonus", "בונוס", ValueType.MONEY),
    PENSION_PERCENT("pensionPercent", "הפרשת עובד לפנסיה", ValueType.PERCENT),
    STUDY_FUND_PERCENT("studyFundPercent", "הפרשת עובד לקרן השתלמות", ValueType.PERCENT),
    CREDIT_POINTS("creditPoints", "נקודות זיכוי", ValueType.POINTS),
    CHILDREN_UP_TO_23("childrenUpTo23", "ילדים עד גיל 23", ValueType.COUNT),
    CURRENT_CAR("currentCar", "רכב היום", ValueType.CURRENT_CAR),
    CURRENT_PARTICIPATION("currentParticipation", "השתתפות ברכב היום", ValueType.MONEY),
    ALTERNATIVE_PARTICIPATION("alternativeParticipation", "ההשתתפות החודשית שלך ברכב החדש", ValueType.MONEY);

    public enum ValueType { OCCUPATION, GROUP, MONEY, PERCENT, POINTS, COUNT, CURRENT_CAR }

    private final String apiName;
    private final String label;
    private final ValueType type;

    FactKey(String apiName, String label, ValueType type) {
        this.apiName = apiName;
        this.label = label;
        this.type = type;
    }

    public String apiName() {
        return apiName;
    }

    public String label() {
        return label;
    }

    public ValueType type() {
        return type;
    }

    public static FactKey fromApiName(String apiName) {
        return Arrays.stream(values())
                .filter(k -> k.apiName.equals(apiName))
                .findFirst()
                .orElseThrow(() -> ApiException.notFound("UNKNOWN_FIELD", "אין שדה בשם " + apiName));
    }
}
