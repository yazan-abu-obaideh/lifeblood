import React, { useState } from "react";
import { View, Text, TextInput, TouchableOpacity } from "react-native";
import { ApiError } from "./services/api";
import { styles } from "./styles";
import { VerificationScreenProps } from "./types";
import { validateVerificationCode } from "./utils/validation";


export const VerificationScreen: React.FC<VerificationScreenProps> = ({
  phoneNumber, verifyCode, goBack, loading,
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
        placeholder="verification code"
        value={code}
        onChangeText={(text) => {
          setCode(text);
          setError("");
        } }
        keyboardType="number-pad"
        autoFocus
        editable={!loading} />

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
