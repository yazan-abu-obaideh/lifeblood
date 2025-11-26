import React, { useEffect, useState } from "react";
import { View, Text, TextInput, TouchableOpacity } from "react-native";
import { ApiError, verifyCode } from "./services/api";
import { styles } from "./styles";
import { validateVerificationCode } from "./utils/validation";
import { useNavigation } from "@react-navigation/native";
import { NavigationProp } from "./Screens/navigationUtils";
import { getFromAsyncStorage } from "./utils/asyncStorageUtils";

export const VerificationScreen: React.FC = () => {
  const [code, setCode] = useState<string>("");
  const [error, setError] = useState<string>("");
  const [verified, setVerified] = useState(false);
  const [phoneNumber, setPhoneNumber] = useState("");
  const [loading, setLoading] = useState(false);

  const navigation = useNavigation<NavigationProp>();

  const handleVerify = async (): Promise<void> => {
    useEffect(() => {
      getFromAsyncStorage("PHONE_NUMBER").then((result) => {
        setPhoneNumber(result ?? "");
      });
    }, []);

    // Validate verification code
    const validation = validateVerificationCode(code);
    if (!validation.valid) {
      setError(validation.error || "Invalid verification code");
      return;
    }

    setError("");

    try {
      await verifyCode(code, phoneNumber);
      setVerified(true);
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError("An unexpected error occurred");
      }
    }
  };

  if (verified) {
    return (
      <>
        <Text>Verification successful!</Text>
        <TouchableOpacity
          onPress={() => {
            navigation.replace("signIn");
          }}
        >
          <Text>Tap here to log in</Text>
        </TouchableOpacity>
      </>
    );
  }

  return (
    <View style={styles.screen}>
      <Text style={styles.title}>Verify</Text>
      <Text style={styles.subtitle}>Enter the code sent to {phoneNumber}</Text>

      <TextInput
        style={[styles.input, error && styles.inputError]}
        placeholder="verification code"
        value={code}
        onChangeText={(text) => {
          setError("");
          setCode(text);
        }}
        keyboardType="number-pad"
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
    </View>
  );
};
