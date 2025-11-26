import React, { useState, useEffect } from "react";
import {
  ScrollView,
  Text,
  TextInput,
  View,
  ActivityIndicator,
  TouchableOpacity,
} from "react-native";
import { getHospitals, ApiError } from "./services/api";
import { styles } from "./styles";
import { PhoneInputScreenProps } from "./types";
import { validatePhoneNumber } from "./utils/validation";
import { HospitalResponse } from "./generated-open-api/models/HospitalResponse";

export const PhoneInputScreen: React.FC<PhoneInputScreenProps> = ({
  registerVolunteer: registerVolunteer,
  loading,
}) => {
  const [phoneNumber, setPhoneNumber] = useState<string>("");
  const [password, setPassword] = useState<string>("");
  const [error, setError] = useState<string>("");
  const [hospitals, setHospitals] = useState<HospitalResponse[]>([]);
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());
  const [loadingHospitals, setLoadingHospitals] = useState<boolean>(true);
  const [hospitalError, setHospitalError] = useState<string>("");

  useEffect(() => {
    loadHospitals();
  }, []);

  const loadHospitals = async (): Promise<void> => {
    setLoadingHospitals(true);
    setHospitalError("");

    try {
      const data = await getHospitals();
      setHospitals(data);
    } catch (err) {
      if (err instanceof ApiError) {
        setHospitalError(err.message);
      } else {
        setHospitalError("Failed to load hospitals");
      }
    } finally {
      setLoadingHospitals(false);
    }
  };

  const toggleHospital = (hospitalId: string): void => {
    setSelectedIds((prev) => {
      const newSet = new Set(prev);
      if (newSet.has(hospitalId)) {
        newSet.delete(hospitalId);
      } else {
        newSet.add(hospitalId);
      }
      return newSet;
    });
  };

  const handleSubmit = async (): Promise<void> => {
    // Validate phone number
    const validation = validatePhoneNumber(phoneNumber);
    if (!validation.valid) {
      setError(validation.error || "Invalid phone number");
      return;
    }

    // Validate hospital selection
    if (selectedIds.size === 0) {
      setError("Please select at least one hospital");
      return;
    }

    setError("");

    try {
      await registerVolunteer(phoneNumber, password, Array.from(selectedIds));
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError("An unexpected error occurred");
      }
    }
  };

  return (
    <ScrollView
      style={styles.screen}
      contentContainerStyle={styles.screenContent}
    >
      <Text style={styles.title}>Sign Up</Text>
      <Text style={styles.subtitle}>Enter your phone number</Text>

      <TextInput
        style={[styles.input, error && styles.inputError]}
        placeholder="Phone number"
        value={phoneNumber}
        onChangeText={(text) => {
          setPhoneNumber(text);
          setError("");
        }}
        keyboardType="phone-pad"
        editable={!loading}
      />
      <Text style={styles.subtitle}>Enter your password</Text>

      <TextInput
        style={[styles.input, error && styles.inputError]}
        placeholder="Password"
        value={password}
        onChangeText={(text) => {
          setPassword(text);
          setError("");
        }}
        editable={!loading}
      />

      <Text style={styles.sectionTitle}>Select Hospitals</Text>
      <Text style={styles.sectionSubtitle}>
        Choose the hospitals you're interested in
      </Text>

      {loadingHospitals ? (
        <View style={styles.hospitalLoading}>
          <ActivityIndicator size="small" color="#000" />
          <Text style={styles.hospitalLoadingText}>Loading hospitals...</Text>
        </View>
      ) : hospitalError ? (
        <View style={styles.hospitalError}>
          <Text style={styles.errorText}>{hospitalError}</Text>
          <TouchableOpacity style={styles.retryButton} onPress={loadHospitals}>
            <Text style={styles.retryButtonText}>Retry</Text>
          </TouchableOpacity>
        </View>
      ) : (
        <>
          {hospitals.map((hospital) => (
            <TouchableOpacity
              key={hospital.uuid}
              style={[
                styles.hospitalItem,
                selectedIds.has(hospital.uuid) && styles.hospitalItemSelected,
              ]}
              onPress={() => toggleHospital(hospital.uuid)}
              disabled={loading}
            >
              <Text
                style={[
                  styles.hospitalText,
                  selectedIds.has(hospital.uuid) && styles.hospitalTextSelected,
                ]}
              >
                {hospital.hospitalName}
              </Text>
              {selectedIds.has(hospital.uuid) && (
                <Text style={styles.checkmark}>✓</Text>
              )}
            </TouchableOpacity>
          ))}
        </>
      )}

      {error ? <Text style={styles.errorText}>{error}</Text> : null}

      <TouchableOpacity
        style={[styles.button, loading && styles.buttonDisabled]}
        onPress={handleSubmit}
        disabled={loading}
      >
        <Text style={styles.buttonText}>
          {loading ? "Sending..." : "Continue"}
        </Text>
      </TouchableOpacity>
    </ScrollView>
  );
};
