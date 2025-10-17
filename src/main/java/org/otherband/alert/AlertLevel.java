package org.otherband.alert;

public enum AlertLevel {
    ROUTINE(0),
    URGENT(1),
    LIFE_OR_DEATH(2);

    private final int ordinal;


    AlertLevel(int ordinal) {
        this.ordinal = ordinal;
    }

    public int level() {
        return ordinal;
    }

}
