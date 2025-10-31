package org.otherband.lifeblood.alert;

public enum AlertLevel {
    ROUTINE(0, "Routine"),
    URGENT(1, "Urgent"),
    LIFE_OR_DEATH(2, "Life or Death");

    private final int ordinal;
    private final String displayName;


    AlertLevel(int ordinal, String displayName) {
        this.ordinal = ordinal;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int level() {
        return ordinal;
    }

    public static int minimumSeverity() {
        return 0;
    }

    public static int maximumSeverity() {
        return values().length;
    }
}
