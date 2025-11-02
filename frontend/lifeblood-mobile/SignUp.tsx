import React, { useState } from "react";
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  KeyboardAvoidingView,
  Platform,
  Alert,
} from "react-native";
import {
  validatePhoneNumber,
  validateVerificationCode,
} from "./utils/validation";
import { submitPhoneNumber, verifyCode, ApiError } from "./services/api";
import HospitalSelect from "./HospitalSelect";

// Types
interface SignUpData {
  phoneNumber: string;
  verified: boolean;
  token?: string;
}

interface PhoneInputScreenProps {
  submitPhoneNumber: (phoneNumber: string, hospitals: string[]) => Promise<void>;
  loading: boolean;
}

interface VerificationScreenProps {
  phoneNumber: string;
  verifyCode: (code: string) => Promise<void>;
  goBack: () => void;
  loading: boolean;
}

interface SignUpProps {
  onComplete?: (data: SignUpData) => void;
}

// Phone Input Screen Component
const PhoneInputScreen: React.FC<PhoneInputScreenProps> = ({
  submitPhoneNumber,
  loading,
}) => {
  const [phoneNumber, setPhoneNumber] = useState<string>("");
  const [hospitals, setHospitals] = useState<string[]>([]);
  const [error, setError] = useState<string>("");

  const handleSubmit = async (): Promise<void> => {
    // Validate phone number
    const validation = validatePhoneNumber(phoneNumber);
    if (!validation.valid) {
      setError(validation.error || "Invalid phone number");
      return;
    }

    if (hospitals.length === 0) {
      setError("Should select at least one hospital");
      return;
    }

    setError("");

    try {
      await submitPhoneNumber(phoneNumber, hospitals);
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError("An unexpected error occurred");
      }
    }
  };

  return (
    <View style={styles.screen}>
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
        autoFocus
        editable={!loading}
      />
      <HospitalSelect
        onSelectionComplete={(selection) => {
          setHospitals(selection);
        }}
      />

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
    </View>
  );
};

// Verification Screen Component
const VerificationScreen: React.FC<VerificationScreenProps> = ({
  phoneNumber,
  verifyCode,
  goBack,
  loading,
}) => {
  const [code, setCode] = useState<string>("");
  const [error, setError] = useState<string>("");

  const handleVerify = async (): Promise<void> => {
    // Validate verification code
    const validation = validateVerificationCode(code);
    if (!validation.valid) {
      setError(validation.error || "Invalid verification code");
      return;
    }

    setError("");

    try {
      await verifyCode(code);
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError("An unexpected error occurred");
      }
    }
  };

  return (
    <View style={styles.screen}>
      <Text style={styles.title}>Verify</Text>
      <Text style={styles.subtitle}>Enter the code sent to {phoneNumber}</Text>

      <TextInput
        style={[styles.input, error && styles.inputError]}
        placeholder="6-digit code"
        value={code}
        onChangeText={(text) => {
          setCode(text);
          setError("");
        }}
        keyboardType="number-pad"
        maxLength={6}
        autoFocus
        editable={!loading}
      />

      {error ? <Text style={styles.errorText}>{error}</Text> : null}

      <TouchableOpacity
        style={[styles.button, loading && styles.buttonDisabled]}
        onPress={handleVerify}
        disabled={loading}
      >
        <Text style={styles.buttonText}>
          {loading ? "Verifying..." : "Verify"}
        </Text>
      </TouchableOpacity>

      <TouchableOpacity onPress={goBack} disabled={loading}>
        <Text style={styles.backText}>Change phone number</Text>
      </TouchableOpacity>
    </View>
  );
};

// Main SignUp Component
const SignUp: React.FC<SignUpProps> = ({ onComplete }) => {
  const [step, setStep] = useState<"phone" | "verify">("phone");
  const [phoneNumber, setPhoneNumber] = useState<string>("");
  const [loading, setLoading] = useState<boolean>(false);

  const submitPhoneNumberHandler = async (phone: string): Promise<void> => {
    setLoading(true);
    try {
      await submitPhoneNumber(phone);
      setPhoneNumber(phone);
      setStep("verify");
    } catch (error) {
      // Error is already logged in api.ts
      // Just show alert to user
      if (error instanceof ApiError) {
        Alert.alert("Error", error.message);
      } else {
        Alert.alert("Error", "Failed to send verification code");
      }
      throw error; // Re-throw so child component can handle it
    } finally {
      setLoading(false);
    }
  };

  const verifyCodeHandler = async (code: string): Promise<void> => {
    setLoading(true);
    try {
      const response = await verifyCode(phoneNumber, code);

      // Call onComplete with the result
      onComplete?.({
        phoneNumber,
        verified: true,
        token: response.token,
      });
    } catch (error) {
      // Error is already logged in api.ts
      if (error instanceof ApiError) {
        Alert.alert("Error", error.message);
      } else {
        Alert.alert("Error", "Failed to verify code");
      }
      throw error; // Re-throw so child component can handle it
    } finally {
      setLoading(false);
    }
  };

  const goBack = (): void => {
    setStep("phone");
    setPhoneNumber("");
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === "ios" ? "padding" : "height"}
    >
      {step === "phone" ? (
        <>
          <PhoneInputScreen
            submitPhoneNumber={submitPhoneNumberHandler}
            loading={loading}
          />
        </>
      ) : (
        <VerificationScreen
          phoneNumber={phoneNumber}
          verifyCode={verifyCodeHandler}
          goBack={goBack}
          loading={loading}
        />
      )}
    </KeyboardAvoidingView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#fff",
  },
  screen: {
    flex: 1,
    justifyContent: "center",
    paddingHorizontal: 32,
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
    marginBottom: 32,
  },
  input: {
    borderWidth: 1,
    borderColor: "#ddd",
    borderRadius: 8,
    padding: 16,
    fontSize: 16,
    marginBottom: 8,
  },
  inputError: {
    borderColor: "#ff3b30",
  },
  errorText: {
    color: "#ff3b30",
    fontSize: 14,
    marginBottom: 16,
  },
  button: {
    backgroundColor: "#000",
    padding: 16,
    borderRadius: 8,
    alignItems: "center",
  },
  buttonDisabled: {
    opacity: 0.5,
  },
  buttonText: {
    color: "#fff",
    fontSize: 16,
    fontWeight: "600",
  },
  backText: {
    color: "#666",
    fontSize: 14,
    textAlign: "center",
    marginTop: 16,
  },
});

export default SignUp;
