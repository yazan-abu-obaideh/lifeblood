import React, { useState, useEffect } from "react";
import {
  View,
  Text,
  ScrollView,
  TouchableOpacity,
  ActivityIndicator,
  Alert,
} from "react-native";
import { styles } from "./VolunteerSettingsStyles";
import {
  AlertLevel,
  HospitalResponse,
  NotificationChannel,
  VolunteerResponse,
} from "../../generated-open-api/models/all";
import { useNavigation } from "@react-navigation/native";
import { useUser } from "../UserContext";
import { getNavigation, NavigationProp } from "../navigationUtils";
import { fetchUserDetails, getHospitals } from "../../services/api";
import {
  SEVERITY_LABELS,
  SEVERITY_DESCRIPTIONS,
  NUMBER_TO_SEVERITY,
  SEVERITY_TO_NUMBER,
} from "../../utils/alertLevelUtils";
import { LoadingView } from "../AlertScreen/LoadingView";

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
  level: AlertLevel;
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
  selectedSeverity: AlertLevel;
  onSeverityChange: (severity: AlertLevel) => void;
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
        {Object.values(AlertLevel).map((level) => (
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

const NotificationChannelSlider: React.FC<NotificationChannelProps> = ({
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
  const availableChannels = Object.values(NotificationChannel);

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
          <NotificationChannelSlider
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
  selectedHospitals: HospitalResponse[];
  onHospitalsChange: (hospitals: HospitalResponse[]) => void;
}

function Hospital({
  selectedHospitals,
  hospital,
  onHospitalsChange,
}: {
  selectedHospitals: HospitalResponse[];
  hospital: HospitalResponse;
  onHospitalsChange: (hospitals: HospitalResponse[]) => void;
}) {
  const contained =
    selectedHospitals.filter((aHospital) => hospital.uuid === aHospital.uuid)
      .length > 0;
  return (
    <TouchableOpacity
      key={hospital.uuid}
      style={styles.selectedHospitalItem}
      onPress={() => {
        if (contained) {
          onHospitalsChange(
            selectedHospitals.filter(
              (aHospital) => aHospital.uuid !== hospital.uuid
            )
          );
        } else {
          onHospitalsChange([...selectedHospitals, hospital]);
        }
      }}
    >
      <Text style={styles.selectedHospitalName}>
        {hospital.hospitalName + (contained ? " (selected)" : "")}
      </Text>
    </TouchableOpacity>
  );
}

const HospitalsSection: React.FC<HospitalsSectionProps> = ({
  selectedHospitals,
  onHospitalsChange,
}) => {
  const [allHospitals, setAllHospitals] = useState<HospitalResponse[]>([]);

  useEffect(() => {
    getHospitals().then((hospitals) => setAllHospitals(hospitals));
  }, []);

  const numberSelectedHospitals = selectedHospitals.length;

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
          {numberSelectedHospitals > 0
            ? `${numberSelectedHospitals} hospital(s) selected`
            : "Select hospitals"}
        </Text>
        <Text style={styles.chevron}>▼</Text>
      </TouchableOpacity>

      {
        <View style={styles.selectedHospitalsList}>
          {allHospitals.map((hospital) => {
            return (
              <Hospital
                hospital={hospital}
                selectedHospitals={selectedHospitals}
                onHospitalsChange={onHospitalsChange}
              />
            );
          })}
        </View>
      }
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

const VolunteerSettings: React.FC = () => {
  const user = useUser();
  const navigation = getNavigation();

  const [loading, setLoading] = useState<boolean>(true);
  const [saving, setSaving] = useState<boolean>(false);

  const [originalSettings, setOriginalSettings] =
    useState<VolunteerResponse | null>(null);
  const [minimumSeverity, setMinimumSeverity] = useState<AlertLevel>(
    AlertLevel.Routine
  );
  const [notificationChannels, setNotificationChannels] = useState<string[]>(
    []
  );
  const [selectedHospitals, setSelectedHospitals] = useState<
    HospitalResponse[]
  >([]);

  useEffect(() => {
    fetchSettings();
  }, []);

  const fetchSettings = async (): Promise<void> => {
    try {
      if (!user.userUuid) {
        throw Error("User uuid not found");
      }
      const data: VolunteerResponse = await fetchUserDetails(user);

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

  const handleSeverityChange = (severity: AlertLevel): void => {
    setMinimumSeverity(severity);
  };

  const handleChannelToggle = (channel: string): void => {
    setNotificationChannels((prev) =>
      prev.includes(channel)
        ? prev.filter((c) => c !== channel)
        : [...prev, channel]
    );
  };

  const handleHospitalsChange = (hospitals: HospitalResponse[]): void => {
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

      const updatedData: VolunteerResponse = await response.json();
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
      <SettingsHeader onBackPress={() => navigation.goBack()} />

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
