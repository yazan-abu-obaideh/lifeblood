import { AlertLevel } from "../generated-open-api";

export const SEVERITY_TO_NUMBER: Record<AlertLevel, number> = {
  [AlertLevel.Routine]: 0,
  [AlertLevel.Urgent]: 1,
  [AlertLevel.LifeOrDeath]: 2,
};

export const NUMBER_TO_SEVERITY: Record<number, AlertLevel> = {
  0: AlertLevel.Routine,
  1: AlertLevel.Urgent,
  2: AlertLevel.LifeOrDeath,
};

export const SEVERITY_LABELS: Record<AlertLevel, string> = {
  [AlertLevel.Routine]: "Routine",
  [AlertLevel.Urgent]: "Urgent",
  [AlertLevel.LifeOrDeath]: "Life or Death",
};

export const SEVERITY_DESCRIPTIONS: Record<AlertLevel, string> = {
  [AlertLevel.Routine]: "Non-urgent blood requests",
  [AlertLevel.Urgent]: "Time-sensitive requests",
  [AlertLevel.LifeOrDeath]: "Critical emergencies only",
};

export const getSeverityColor = (level: AlertLevel): string => {
  switch (level) {
    case "ROUTINE":
      return "#4CAF50";
    case "URGENT":
      return "#FF9800";
    case "LIFE_OR_DEATH":
      return "#E53935";
    default:
      return "#666";
  }
};

export const getSeverityLabel = (level: AlertLevel): string => {
  switch (level) {
    case "ROUTINE":
      return "Routine";
    case "URGENT":
      return "Urgent";
    case "LIFE_OR_DEATH":
      return "Life or Death";
    default:
      return level;
  }
};
