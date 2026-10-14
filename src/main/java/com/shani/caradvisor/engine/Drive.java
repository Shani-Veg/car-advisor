package com.shani.caradvisor.engine;

public enum Drive {
    PETROL("בנזין"),
    HYBRID("היברידי"),
    PLUG_IN("פלאג-אין"),
    ELECTRIC("חשמלי");

    private final String hebrew;

    Drive(String hebrew) {
        this.hebrew = hebrew;
    }

    public String hebrew() {
        return hebrew;
    }

    public static Drive fromHebrew(String value) {
        for (Drive drive : values()) {
            if (drive.hebrew.equals(value.trim())) {
                return drive;
            }
        }
        throw new IllegalArgumentException("Unknown drive: " + value);
    }
}
