import React, { useState } from "react";
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  Alert,
  ActivityIndicator,
} from "react-native";
import { config } from "../config/config";
import { useUser } from "../Screens/UserContext";
import { useNavigation } from "@react-navigation/native";
import { NavigationProp } from "../Screens/navigationUtils";
import { LoginResponse } from "../generated-open-api";
import { saveToAsyncStorage } from "../utils/asyncStorageUtils";
import { login } from "../services/api";

function ActionButton({
  loading,
  onPress: onClick,
  buttonText,
}: {
  loading: boolean;
  onPress: () => Promise<void>;
  buttonText: string;
}) {
  return (
    <TouchableOpacity
      style={[styles.button, loading && styles.buttonDisabled]}
      onPress={onClick}
      disabled={loading}
    >
      {loading ? (
        <ActivityIndicator color="#fff" />
      ) : (
        <Text style={styles.buttonText}>{buttonText}</Text>
      )}
    </TouchableOpacity>
  );
}

const LoginScreen: React.FC = () => {
  const [phoneNumber, setPhoneNumber] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const userContext = useUser();
  const navigation = useNavigation<NavigationProp>();

  const handleLogin = async () => {
    if (!phoneNumber.trim() || !password.trim()) {
      Alert.alert("Error", "Please enter phone number and password");
      return;
    }

    setLoading(true);
    try {
      const loginResponse: LoginResponse = await login(phoneNumber, password);
      await saveToAsyncStorage("REFRESH_TOKEN", loginResponse.refreshToken!);
      await saveToAsyncStorage("USER_UUID", loginResponse.userUuid!);
      await saveToAsyncStorage("PHONE_NUMBER", loginResponse.phoneNumber!);
      userContext.setUserUuid(loginResponse.userUuid!);
      navigation.replace("summary");
    } catch (error) {
      console.error(`Login error: ${error}`);
      Alert.alert("Login Failed", "Invalid phone number or password");
    } finally {
      setLoading(false);
    }
  };

  function navigateToSignUp(): Promise<void> {
    navigation.navigate("signUp");
    return Promise.resolve();
  }

  return (
    <View style={styles.container}>
      <View style={styles.content}>
        <Text style={styles.title}>Lifeblood</Text>
        <Text style={styles.subtitle}>Login to continue</Text>

        <TextInput
          style={styles.input}
          placeholder="Phone number"
          value={phoneNumber}
          onChangeText={setPhoneNumber}
          autoCapitalize="none"
          editable={!loading}
        />

        <TextInput
          style={styles.input}
          placeholder="Password"
          value={password}
          onChangeText={setPassword}
          secureTextEntry
          editable={!loading}
        />

        <ActionButton
          loading={loading}
          buttonText="Login"
          onPress={handleLogin}
        />
        <ActionButton
          loading={loading}
          buttonText="Sign Up"
          onPress={navigateToSignUp}
        />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#f5f5f5",
    justifyContent: "center",
    padding: 20,
  },
  content: {
    backgroundColor: "#fff",
    borderRadius: 16,
    padding: 24,
  },
  title: {
    fontSize: 28,
    fontWeight: "bold",
    color: "#E53935",
    textAlign: "center",
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: "#666",
    textAlign: "center",
    marginBottom: 32,
  },
  input: {
    borderWidth: 1,
    borderColor: "#ddd",
    borderRadius: 8,
    padding: 16,
    fontSize: 16,
    marginBottom: 16,
    backgroundColor: "#fff",
  },
  button: {
    backgroundColor: "#E53935",
    borderRadius: 8,
    padding: 16,
    alignItems: "center",
    marginTop: 8,
  },
  buttonDisabled: {
    backgroundColor: "#ccc",
  },
  buttonText: {
    color: "#fff",
    fontSize: 16,
    fontWeight: "600",
  },
});

export default LoginScreen;
