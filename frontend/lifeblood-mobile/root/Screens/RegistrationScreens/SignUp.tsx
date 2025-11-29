import React, { useState } from "react";
import { Alert, KeyboardAvoidingView, Platform } from "react-native";
import { registerVolunteer } from "../../services/api";
import { ApiError } from "../../services/apiClientConfig";
import { styles } from "../../styles";
import { saveToAsyncStorage } from "../../utils/asyncStorageUtils";
import { getNavigation } from "../navigationUtils";
import { RegistrationScreen } from "./RegistrationScreen";

const SignUp: React.FC = ({}) => {
  const [loading, setLoading] = useState<boolean>(false);
  const navigation = getNavigation();

  const registerVolunteerHandler = async (
    phone: string,
    password: string,
    hospitalIds: string[]
  ): Promise<void> => {
    setLoading(true);
    try {
      await registerVolunteer(phone, password, hospitalIds);
      await saveToAsyncStorage("PHONE_NUMBER", phone);
      navigation.navigate("verifyNumber");
    } catch (error) {
      if (error instanceof ApiError) {
        Alert.alert("Error", error.message);
      } else {
        Alert.alert("Error", "Failed to send verification code");
      }
      throw error;
    } finally {
      setLoading(false);
    }
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === "ios" ? "padding" : "height"}
    >
      <RegistrationScreen
        registerVolunteer={registerVolunteerHandler}
        loading={loading}
      />
    </KeyboardAvoidingView>
  );
};

export default SignUp;
