package com.shani.caradvisor.engine;

/**
 * The tax authority's key for a use-value row. All five parts are needed:
 * dropping the vehicle type silently returns a different car
 * (2025 / maker 1014 / model 3 is ATTO 3 as type 1 but TANG as type 2).
 */
public record VehicleKey(int taxYear, int registrationYear, int vehicleType, int makerCode, int modelCode) {
}
