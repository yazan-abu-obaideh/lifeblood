package org.otherband.lifeblood.alert;

import org.otherband.lifeblood.generated.model.AlertLevel;

public final class AlertLevelUtils {
    private AlertLevelUtils() {

    }

    public static int toLevel(AlertLevel alertLevel) {
        return switch (alertLevel) {
            case null -> -1;
            case ROUTINE -> 0;
            case URGENT -> 1;
            case LIFE_OR_DEATH -> 2;
        };
    }

    public static String toDisplayName(AlertLevel alertLevel) {
        return switch (alertLevel) {
            case null -> null;
            case ROUTINE -> "Routine";
            case URGENT -> "Urgent";
            case LIFE_OR_DEATH -> "Life or death";
        };
    }

    public static int minimumSeverity() {
        return 0;
    }

    public static int maximumSeverity() {
        return AlertLevel.values().length;
    }
}
