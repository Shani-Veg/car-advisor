package com.shani.caradvisor.engine;

import java.math.BigDecimal;

public final class EligibilityChecker {

    private final Rules rules;

    public EligibilityChecker(Rules rules) {
        this.rules = rules;
    }

    public Eligibility check(Employee employee, Occupation occupation, CarChoice choice) {
        if (choice instanceof CarChoice.CompanyCar car) {
            return checkCar(employee, car.vehicle());
        }
        return checkWaiver(occupation);
    }

    private Eligibility checkCar(Employee employee, Vehicle vehicle) {
        if (vehicle.isFamilyTrack()) {
            Rules.FamilyTrack track = rules.familyTrack();
            if (employee.childrenUpTo23() < track.minChildren()) {
                return Eligibility.no(Eligibility.Code.FAMILY_TRACK_NEEDS_CHILDREN,
                        "מסלול ברוכת ילדים פתוח ל-" + track.minChildren() + " ילדים ומעלה עד גיל "
                                + track.childMaxAge() + " (רשומים " + employee.childrenUpTo23() + ")");
            }
            return Eligibility.yes(Eligibility.Code.FAMILY_TRACK,
                    "מסלול ברוכת ילדים (" + employee.childrenUpTo23() + " ילדים); על הפער מעל תקציב הקבוצה העובד משלם "
                            + percent(track.employeeShareOfGap()) + "%");
        }
        if (rules.rankOf(vehicle.group()) > rules.rankOf(employee.group())) {
            return Eligibility.no(Eligibility.Code.HIGHER_GROUP,
                    "הרכב מקבוצה " + vehicle.group() + ", גבוהה מהקבוצה שלך (" + employee.group() + ")");
        }
        return Eligibility.yes(Eligibility.Code.OWN_OR_LOWER_GROUP, "הרכב מהקבוצה שלך או נמוכה ממנה");
    }

    private Eligibility checkWaiver(Occupation occupation) {
        if (occupation.carRequiredByRole()) {
            return Eligibility.no(Eligibility.Code.CAR_REQUIRED_BY_ROLE,
                    "בתפקיד " + occupation.name() + " הרכב הוא חלק מהעבודה, ולכן אין אפשרות לוותר עליו");
        }
        if (!occupation.waiverAllowed()) {
            return Eligibility.no(Eligibility.Code.WAIVER_NOT_ALLOWED,
                    "העיסוק " + occupation.name() + " לא מאפשר ויתור על רכב");
        }
        return Eligibility.yes(Eligibility.Code.WAIVER_ALLOWED,
                "העיסוק מאפשר ויתור; במקום רכב משולמת אחזקת רכב של "
                        + Money.display(occupation.waiverMaintenance()) + " (חייבת במס)");
    }

    private static String percent(BigDecimal fraction) {
        return fraction.movePointRight(2).stripTrailingZeros().toPlainString();
    }
}
