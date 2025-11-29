import React, { useEffect, useState } from "react";
import { Alert, Text, TextInput, TouchableOpacity, View } from "react-native";
import { verifyCode } from "../../services/api";
import { ApiError } from "../../services/apiClientConfig";
import { styles } from "../../styles";
import { getFromAsyncStorage } from "../../utils/asyncStorageUtils";
import { validateVerificationCode } from "../../utils/validation";
import { getNavigation } from "../navigationUtils";

export const PhoneVerificationScreen: React.FC = () => {
  const [code, setCode] = useState<string>("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [error, setError] = useState<string>("");
  const [verified, setVerified] = useState(false);

  const [loading, setLoading] = useState(false);

  const navigation = getNavigation();

  useEffect(() => {
    getFromAsyncStorage("PHONE_NUMBER").then((result) => {
      setPhoneNumber(result ?? "");
    });
  }, []);

  const handleVerify = async (): Promise<void> => {
    // Validate verification code
    const validation = validateVerificationCode(code);
    if (!validation.valid) {
      setError(validation.error || "Invalid verification code");
      return;
    }

    setError("");

    try {
      setLoading(true);
      await verifyCode(phoneNumber, code);
      setVerified(true);
      Alert.alert("Verification successful! Redirecting to sign in screen.");
      navigation.navigate("signIn");
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError("An unexpected error occurred");
      }
    } finally {
      setLoading(false);
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
