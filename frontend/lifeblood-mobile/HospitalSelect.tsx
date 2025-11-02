import React, { useState, useEffect } from "react";
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  FlatList,
  ActivityIndicator,
} from "react-native";
import { getHospitals, ApiError, type Hospital } from "./services/api";

interface HospitalSelectProps {
  onSelectionComplete: (selectedHospitalIds: string[]) => void;
}

const HospitalSelect: React.FC<HospitalSelectProps> = ({
  onSelectionComplete,
}) => {
  const [hospitals, setHospitals] = useState<Hospital[]>([]);
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>("");

  useEffect(() => {
    loadHospitals();
  }, []);

  const loadHospitals = async (): Promise<void> => {
    setLoading(true);
    setError("");

    try {
      const data = await getHospitals();
      setHospitals(data);
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError("Failed to load hospitals");
      }
    } finally {
      setLoading(false);
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

  const handleContinue = (): void => {
    onSelectionComplete(Array.from(selectedIds));
  };

  if (loading) {
    return (
      <View style={styles.centerContainer}>
        <ActivityIndicator size="large" color="#000" />
        <Text style={styles.loadingText}>Loading hospitals...</Text>
      </View>
    );
  }

  if (error) {
    return (
      <View style={styles.centerContainer}>
        <Text style={styles.errorText}>{error}</Text>
        <TouchableOpacity style={styles.retryButton} onPress={loadHospitals}>
          <Text style={styles.retryButtonText}>Retry</Text>
        </TouchableOpacity>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Select Hospitals</Text>
      <Text style={styles.subtitle}>
        Choose the hospitals you're interested in
      </Text>

      <FlatList
        data={hospitals}
        keyExtractor={(item) => item.uuid}
        renderItem={({ item }) => (
          <TouchableOpacity
            style={[
              styles.hospitalItem,
              selectedIds.has(item.uuid) && styles.hospitalItemSelected,
            ]}
            onPress={() => toggleHospital(item.uuid)}
          >
            <Text
              style={[
                styles.hospitalText,
                selectedIds.has(item.uuid) && styles.hospitalTextSelected,
              ]}
            >
              {item.hospitalName}
            </Text>
            {selectedIds.has(item.uuid) && (
              <Text style={styles.checkmark}>✓</Text>
            )}
          </TouchableOpacity>
        )}
        contentContainerStyle={styles.listContent}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#fff",
    paddingHorizontal: 24,
    paddingTop: 60,
  },
  centerContainer: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#fff",
    paddingHorizontal: 24,
  },
  title: {
    fontSize: 32,
    fontWeight: "600",
    marginBottom: 8,
    color: "#000",
  },
  subtitle: {
    fontSize: 16,
    color: "#666",
    marginBottom: 24,
  },
  listContent: {
    paddingBottom: 100,
  },
  hospitalItem: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    padding: 16,
    borderWidth: 1,
    borderColor: "#ddd",
    borderRadius: 8,
    marginBottom: 12,
  },
  hospitalItemSelected: {
    backgroundColor: "#000",
    borderColor: "#000",
  },
  hospitalText: {
    fontSize: 16,
    color: "#000",
  },
  hospitalTextSelected: {
    color: "#fff",
  },
  checkmark: {
    fontSize: 20,
    color: "#fff",
    fontWeight: "600",
  },
  continueButton: {
    position: "absolute",
    bottom: 40,
    left: 24,
    right: 24,
    backgroundColor: "#000",
    padding: 16,
    borderRadius: 8,
    alignItems: "center",
  },
  continueButtonDisabled: {
    opacity: 0.3,
  },
  continueButtonText: {
    color: "#fff",
    fontSize: 16,
    fontWeight: "600",
  },
  loadingText: {
    marginTop: 16,
    fontSize: 16,
    color: "#666",
  },
  errorText: {
    fontSize: 16,
    color: "#ff3b30",
    textAlign: "center",
    marginBottom: 16,
  },
  retryButton: {
    backgroundColor: "#000",
    paddingVertical: 12,
    paddingHorizontal: 24,
    borderRadius: 8,
  },
  retryButtonText: {
    color: "#fff",
    fontSize: 16,
    fontWeight: "600",
  },
});

export default HospitalSelect;
