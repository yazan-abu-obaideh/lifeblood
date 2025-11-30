import React, { useEffect, useState } from "react";
import { ScrollView, Text, TouchableOpacity, View } from "react-native";
import {
  HospitalResponse,
  VolunteerResponse,
} from "../../generated-open-api";
import { fetchUserDetails } from "../../services/api";
import { LoadingView } from "../AlertScreen/LoadingView";
import { getNavigation } from "../navigationUtils";
import { useUser } from "../UserContext";
import { styles } from "./VolunteerSummaryStyles";

interface ProfileHeaderProps {
  onSettingsPress: () => void;
}

const ProfileHeader: React.FC<ProfileHeaderProps> = ({ onSettingsPress }) => {
  return (
    <View style={styles.header}>
      <Text style={styles.headerTitle}>My Data</Text>
      <TouchableOpacity onPress={onSettingsPress} style={styles.settingsButton}>
        <Text style={styles.settingsIcon}>⚙️</Text>
      </TouchableOpacity>
    </View>
  );
};

interface VerifiedBadgeProps {
  text: string;
}

const VerifiedBadge: React.FC<VerifiedBadgeProps> = ({ text }) => {
  return (
    <View style={styles.verifiedBadge}>
      <Text style={styles.verifiedIcon}>✓</Text>
      <Text style={styles.verifiedText}>{text}</Text>
    </View>
  );
};

interface PhoneNumberSectionProps {
  phoneNumber: string;
  isVerified: boolean;
}

const PhoneNumberSection: React.FC<PhoneNumberSectionProps> = ({
  phoneNumber,
  isVerified,
}) => {
  return (
    <View style={styles.section}>
      <View style={styles.sectionHeader}>
        <Text style={styles.sectionIcon}>📞</Text>
        <Text style={styles.sectionLabel}>Phone Number</Text>
      </View>
      <Text style={styles.sectionValue}>{phoneNumber}</Text>
      {isVerified && <VerifiedBadge text="Verified" />}
    </View>
  );
};

interface LastDonationSectionProps {
  lastDonationDate: string | undefined;
  isVerifiedDonor: boolean;
}

const LastDonationSection: React.FC<LastDonationSectionProps> = ({
  lastDonationDate,
  isVerifiedDonor,
}) => {
  const formatDate = (dateString: string | undefined): string => {
    if (!dateString) return "Never";
    const date = new Date(dateString);
    return date.toLocaleDateString("en-US", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };

  return (
    <View style={styles.section}>
      <View style={styles.sectionHeader}>
        <Text style={styles.sectionIcon}>🩸</Text>
        <Text style={styles.sectionLabel}>Last Donation</Text>
      </View>
      <Text style={styles.sectionValue}>{formatDate(lastDonationDate)}</Text>
      {isVerifiedDonor && <VerifiedBadge text="Verified Donor" />}
    </View>
  );
};

interface HospitalItemProps {
  hospital: HospitalResponse;
}

const HospitalItem: React.FC<HospitalItemProps> = ({ hospital }) => {
  return (
    <View style={styles.hospitalItem}>
      <Text style={styles.hospitalIcon}>📍</Text>
      <Text style={styles.hospitalName}>{hospital.hospitalName}</Text>
    </View>
  );
};

interface HospitalsListProps {
  hospitals: HospitalResponse[];
}

const HospitalsList: React.FC<HospitalsListProps> = ({ hospitals }) => {
  if (!hospitals || hospitals.length === 0) {
    return <Text style={styles.emptyText}>No hospitals selected</Text>;
  }

  return (
    <View style={styles.hospitalsList}>
      {hospitals.map((hospital) => (
        <HospitalItem key={hospital.uuid} hospital={hospital} />
      ))}
    </View>
  );
};

interface HospitalsSectionProps {
  hospitals: HospitalResponse[];
}

const HospitalsSection: React.FC<HospitalsSectionProps> = ({ hospitals }) => {
  const [expanded, setExpanded] = useState<boolean>(false);

  return (
    <View style={styles.section}>
      <TouchableOpacity
        style={styles.sectionHeader}
        onPress={() => setExpanded(!expanded)}
        activeOpacity={0.7}
      >
        <Text style={styles.sectionIcon}>🏥</Text>
        <Text style={styles.sectionLabel}>Hospitals of Interest</Text>
        <Text style={styles.chevron}>{expanded ? "▲" : "▼"}</Text>
      </TouchableOpacity>

      <Text style={styles.hospitalCount}>
        {hospitals?.length || 0} hospital(s) selected
      </Text>

      {expanded && <HospitalsList hospitals={hospitals} />}
    </View>
  );
};

interface EditProfileButtonProps {
  onPress: () => void;
}

const ViewAlertsButton: React.FC<EditProfileButtonProps> = ({ onPress }) => {
  return (
    <TouchableOpacity style={styles.editButton} onPress={onPress}>
      <Text style={styles.editButtonText}>View Alerts</Text>
    </TouchableOpacity>
  );
};

const ErrorView: React.FC = () => {
  return (
    <View style={styles.centerContainer}>
      <Text style={styles.errorText}>Unable to load profile</Text>
    </View>
  );
};

const VolunteerSummary: React.FC = () => {
  const user = useUser();
  const navigation = getNavigation();

  const [userData, setUserData] = useState<VolunteerResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    fetchUserData();
  }, []);

  const fetchUserData = async (): Promise<void> => {
    try {
      if (!user.userUuid) {
        throw Error(`User uuid is not present`);
      }
      const data: VolunteerResponse = await fetchUserDetails(user);
      setUserData(data);
      console.log(`Found user data ${JSON.stringify(data)}`);
    } catch (error) {
      console.error("Error fetching user data:", error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <LoadingView />;
  }

  if (!userData) {
    return <ErrorView />;
  }

  return (
    <ScrollView style={styles.container}>
      <ProfileHeader onSettingsPress={() => navigation.navigate("settings")} />

      <PhoneNumberSection
        phoneNumber={userData.phoneNumber}
        isVerified={userData.verifiedPhoneNumber}
      />

      <LastDonationSection
        lastDonationDate={userData.lastDonationDate?.toDateString()}
        isVerifiedDonor={userData.verifiedDonor}
      />

      <HospitalsSection hospitals={userData.alertableHospitals ?? []} />

      <ViewAlertsButton onPress={() => navigation.navigate("alerts")} />
    </ScrollView>
  );
};

export default VolunteerSummary;
