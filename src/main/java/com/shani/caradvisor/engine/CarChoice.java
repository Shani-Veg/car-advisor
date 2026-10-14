package com.shani.caradvisor.engine;

import java.math.BigDecimal;
import java.util.Objects;

/** What the employee has today, or the alternative being considered. */
public sealed interface CarChoice permits CarChoice.CompanyCar, CarChoice.NoCar {

    String label();

    /**
     * A company car. The participation must be known: a missing participation is asked
     * from the employee or the transport rep, never silently treated as 0.
     */
    record CompanyCar(Vehicle vehicle, BigDecimal participation) implements CarChoice {
        public CompanyCar {
            Objects.requireNonNull(vehicle, "vehicle");
            Objects.requireNonNull(participation, "participation is missing");
        }

        @Override
        public String label() {
            return vehicle.label();
        }
    }

    /** Waiving the car in exchange for the occupation's car-maintenance allowance. */
    record NoCar() implements CarChoice {
        @Override
        public String label() {
            return "ויתור על רכב";
        }
    }
}
