import React, { useState, useEffect } from "react";
import {
  View,
  Text,
  ScrollView,
  TouchableOpacity,
  ActivityIndicator,
  Alert,
} from "react-native";
import { HospitalResponse } from "../../services/api";
import { styles } from "./VolunteerSettingsStyles";
import { config } from "../../config/config";
import { components } from "../../generated-open-api/open-api";

enum SeverityLevel {
  ROUTINE = "ROUTINE",
  URGENT = "URGENT",
  LIFE_OR_DEATH = "LIFE_OR_DEATH",
}

const SEVERITY_TO_NUMBER: Record<SeverityLevel, number> = {
  [SeverityLevel.ROUTINE]: 0,
  [SeverityLevel.URGENT]: 1,
  [SeverityLevel.LIFE_OR_DEATH]: 2,
};

const NUMBER_TO_SEVERITY: Record<number, SeverityLevel> = {
  0: SeverityLevel.ROUTINE,
  1: SeverityLevel.URGENT,
  2: SeverityLevel.LIFE_OR_DEATH,
};

const SEVERITY_LABELS: Record<SeverityLevel, string> = {
  [SeverityLevel.ROUTINE]: "Routine",
  [SeverityLevel.URGENT]: "Urgent",
  [SeverityLevel.LIFE_OR_DEATH]: "Life or Death",
};

const SEVERITY_DESCRIPTIONS: Record<SeverityLevel, string> = {
  [SeverityLevel.ROUTINE]: "Non-urgent blood requests",
  [SeverityLevel.URGENT]: "Time-sensitive requests",
  [SeverityLevel.LIFE_OR_DEATH]: "Critical emergencies only",
};

type Hospital = HospitalResponse;
type NotificationChannel = "" | "";

type UserSettings = components["schemas"]["VolunteerResponse"];

interface SettingsHeaderProps {
  onBackPress: () => void;
}

const SettingsHeader: React.FC<SettingsHeaderProps> = ({ onBackPress }) => {
  return (
    <View style={styles.header}>
      <TouchableOpacity onPress={onBackPress} style={styles.backButton}>
        <Text style={styles.backIcon}>←</Text>
      </TouchableOpacity>
      <Text style={styles.headerTitle}>Settings</Text>
      <View style={styles.headerSpacer} />
    </View>
  );
};

interface SeverityOptionProps {
  level: SeverityLevel;
  isSelected: boolean;
  onSelect: () => void;
}

const SeverityOption: React.FC<SeverityOptionProps> = ({
  level,
  isSelected,
  onSelect,
}) => {
  return (
    <TouchableOpacity
      style={[
        styles.severityOption,
        isSelected && styles.severityOptionSelected,
      ]}
      onPress={onSelect}
      activeOpacity={0.7}
    >
      <View style={styles.severityOptionContent}>
        <View style={styles.radioButton}>
          {isSelected && <View style={styles.radioButtonInner} />}
        </View>
        <View style={styles.severityTextContainer}>
          <Text
            style={[
              styles.severityLabel,
              isSelected && styles.severityLabelSelected,
            ]}
          >
            {SEVERITY_LABELS[level]}
          </Text>
          <Text style={styles.severityDescription}>
            {SEVERITY_DESCRIPTIONS[level]}
          </Text>
        </View>
      </View>
    </TouchableOpacity>
  );
};

interface MinimumSeveritySectionProps {
  selectedSeverity: SeverityLevel;
  onSeverityChange: (severity: SeverityLevel) => void;
}

const MinimumSeveritySection: React.FC<MinimumSeveritySectionProps> = ({
  selectedSeverity,
  onSeverityChange,
}) => {
  return (
    <View style={styles.section}>
      <View style={styles.sectionHeader}>
        <Text style={styles.sectionIcon}>🔔</Text>
        <Text style={styles.sectionTitle}>Minimum Alert Severity</Text>
      </View>
      <Text style={styles.sectionDescription}>
        Choose the minimum severity level for notifications you want to receive
      </Text>

      <View style={styles.severityOptions}>
        {Object.values(SeverityLevel).map((level) => (
          <SeverityOption
            key={level}
            level={level}
            isSelected={selectedSeverity === level}
            onSelect={() => onSeverityChange(level)}
          />
        ))}
      </View>
    </View>
  );
};

interface NotificationChannelProps {
  channel: string;
  isEnabled: boolean;
  onToggle: () => void;
}

const NotificationChannel: React.FC<NotificationChannelProps> = ({
  channel,
  isEnabled,
  onToggle,
}) => {
  const channelLabels: Record<string, string> = {
    PUSH_NOTIFICATIONS: "📱 Push Notifications",
    WHATSAPP_MESSAGES: "💬 WhatsApp Messages",
  };

  return (
    <TouchableOpacity
      style={styles.notificationChannel}
      onPress={onToggle}
      activeOpacity={0.7}
    >
      <Text style={styles.channelLabel}>
        {channelLabels[channel] || channel}
      </Text>
      <View style={[styles.toggle, isEnabled && styles.toggleActive]}>
        <View
          style={[styles.toggleThumb, isEnabled && styles.toggleThumbActive]}
        />
      </View>
    </TouchableOpacity>
  );
};

interface NotificationChannelsSectionProps {
  enabledChannels: string[];
  onChannelToggle: (channel: string) => void;
}

const NotificationChannelsSection: React.FC<
  NotificationChannelsSectionProps
> = ({ enabledChannels, onChannelToggle }) => {
  const availableChannels = ["PUSH_NOTIFICATIONS", "WHATSAPP_MESSAGES"];

  return (
    <View style={styles.section}>
      <View style={styles.sectionHeader}>
        <Text style={styles.sectionIcon}>📬</Text>
        <Text style={styles.sectionTitle}>Notification Channels</Text>
      </View>
      <Text style={styles.sectionDescription}>
        Select how you want to receive alerts
      </Text>

      <View style={styles.channelsList}>
        {availableChannels.map((channel) => (
          <NotificationChannel
            key={channel}
            channel={channel}
            isEnabled={enabledChannels.includes(channel)}
            onToggle={() => onChannelToggle(channel)}
          />
        ))}
      </View>
    </View>
  );
};

interface HospitalsSectionProps {
  selectedHospitals: Hospital[];
  onHospitalsChange: (hospitals: Hospital[]) => void;
}

const HospitalsSection: React.FC<HospitalsSectionProps> = ({
  selectedHospitals,
  onHospitalsChange,
}) => {
  return (
    <View style={styles.section}>
      <View style={styles.sectionHeader}>
        <Text style={styles.sectionIcon}>🏥</Text>
        <Text style={styles.sectionTitle}>Preferred Hospitals</Text>
      </View>
      <Text style={styles.sectionDescription}>
        Select hospitals you want to receive alerts from
      </Text>

      <TouchableOpacity
        style={styles.hospitalSelector}
        onPress={() => {}}
        activeOpacity={0.7}
      >
        <Text style={styles.hospitalSelectorText}>
          {selectedHospitals.length > 0
            ? `${selectedHospitals.length} hospital(s) selected`
            : "Select hospitals"}
        </Text>
        <Text style={styles.chevron}>▼</Text>
      </TouchableOpacity>

      {selectedHospitals.length > 0 && (
        <View style={styles.selectedHospitalsList}>
          {selectedHospitals.map((hospital) => (
            <View key={hospital.uuid} style={styles.selectedHospitalItem}>
              <Text style={styles.selectedHospitalName}>
                {hospital.hospitalName}
              </Text>
            </View>
          ))}
        </View>
      )}
    </View>
  );
};

interface SaveButtonProps {
  onPress: () => void;
  isSaving: boolean;
  hasChanges: boolean;
}

const SaveButton: React.FC<SaveButtonProps> = ({
  onPress,
  isSaving,
  hasChanges,
}) => {
  return (
    <TouchableOpacity
      style={[styles.saveButton, !hasChanges && styles.saveButtonDisabled]}
      onPress={onPress}
      disabled={isSaving || !hasChanges}
      activeOpacity={0.7}
    >
      {isSaving ? (
        <ActivityIndicator color="#fff" />
      ) : (
        <Text style={styles.saveButtonText}>
          {hasChanges ? "Save Changes" : "No Changes"}
        </Text>
      )}
    </TouchableOpacity>
  );
};

const LoadingView: React.FC = () => {
  return (
    <View style={styles.centerContainer}>
      <ActivityIndicator size="large" color="#E53935" />
    </View>
  );
};

interface VolunteerSettingsProps {
  setCurrScreen: (currScreen: string) => void;
}

const VolunteerSettings: React.FC<VolunteerSettingsProps> = ({
  setCurrScreen,
}) => {
  const [loading, setLoading] = useState<boolean>(true);
  const [saving, setSaving] = useState<boolean>(false);

  const [originalSettings, setOriginalSettings] = useState<UserSettings | null>(
    null
  );
  const [minimumSeverity, setMinimumSeverity] = useState<SeverityLevel>(
    SeverityLevel.ROUTINE
  );
  const [notificationChannels, setNotificationChannels] = useState<string[]>(
    []
  );
  const [selectedHospitals, setSelectedHospitals] = useState<Hospital[]>([]);

  useEffect(() => {
    fetchSettings();
  }, []);

  const fetchSettings = async (): Promise<void> => {
    try {
      const token = "your-jwt-token";
      const response = await fetch(
        `${config.apiBaseUrl}${config.endpoints.volunteer.replace(
          "{uuid}",
          "dce73dc4-85c4-4d17-a603-27ec6ce8734d"
        )}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      const data: UserSettings = await response.json();

      setOriginalSettings(data);
      setMinimumSeverity(NUMBER_TO_SEVERITY[data.minimumSeverity ?? 0]);
      setNotificationChannels(data.notificationChannels ?? []);
      setSelectedHospitals(data.alertableHospitals ?? []);
    } catch (error) {
      console.error("Error fetching settings:", error);
      Alert.alert("Error", "Failed to load settings");
    } finally {
      setLoading(false);
    }
  };

  const handleSeverityChange = (severity: SeverityLevel): void => {
    setMinimumSeverity(severity);
  };

  const handleChannelToggle = (channel: string): void => {
    setNotificationChannels((prev) =>
      prev.includes(channel)
        ? prev.filter((c) => c !== channel)
        : [...prev, channel]
    );
  };

  const handleHospitalsChange = (hospitals: Hospital[]): void => {
    setSelectedHospitals(hospitals);
  };

  const hasChanges = (): boolean => {
    if (!originalSettings) return false;

    const severityChanged =
      SEVERITY_TO_NUMBER[minimumSeverity] !== originalSettings.minimumSeverity;

    const channelsChanged =
      JSON.stringify([...notificationChannels].sort()) !==
      JSON.stringify([...(originalSettings.notificationChannels ?? [])].sort());

    const hospitalsChanged =
      JSON.stringify(selectedHospitals.map((h) => h.uuid).sort()) !==
      JSON.stringify(
        (originalSettings.alertableHospitals ?? []).map((h) => h.uuid).sort()
      );

    return severityChanged || channelsChanged || hospitalsChanged;
  };

  const handleSave = async (): Promise<void> => {
    if (!hasChanges()) return;

    setSaving(true);
    try {
      const token = "your-jwt-token";
      const payload = {
        minimumSeverity: SEVERITY_TO_NUMBER[minimumSeverity],
        notificationChannels,
        alertableHospitalIds: selectedHospitals.map((h) => h.uuid),
      };

      const response = await fetch("YOUR_API_URL/api/users/settings", {
        method: "PUT",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        throw new Error("Failed to save settings");
      }

      const updatedData: UserSettings = await response.json();
      setOriginalSettings(updatedData);

      Alert.alert("Success", "Settings saved successfully");
    } catch (error) {
      console.error("Error saving settings:", error);
      Alert.alert("Error", "Failed to save settings. Please try again.");
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <LoadingView />;
  }

  return (
    <View style={styles.container}>
      <SettingsHeader onBackPress={() => setCurrScreen("summary")} />

      <ScrollView style={styles.scrollView}>
        <NotificationChannelsSection
          enabledChannels={notificationChannels}
          onChannelToggle={handleChannelToggle}
        />

        <MinimumSeveritySection
          selectedSeverity={minimumSeverity}
          onSeverityChange={handleSeverityChange}
        />

        <HospitalsSection
          selectedHospitals={selectedHospitals}
          onHospitalsChange={handleHospitalsChange}
        />
      </ScrollView>

      <View style={styles.footer}>
        <SaveButton
          onPress={handleSave}
          isSaving={saving}
          hasChanges={hasChanges()}
        />
      </View>
    </View>
  );
};

export default VolunteerSettings;
