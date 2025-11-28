import React, { useState } from "react";
import { KeyboardAvoidingView, Platform, Alert } from "react-native";
import { registerVolunteer, verifyCode, ApiError } from "../../services/api";
import { styles } from "../../styles";
import { RegistrationScreen } from "./RegistrationScreen";
import { useNavigation } from "@react-navigation/native";
import { NavigationProp } from "../navigationUtils";
import { saveToAsyncStorage } from "../../utils/asyncStorageUtils";

const SignUp: React.FC = ({}) => {
  const [loading, setLoading] = useState<boolean>(false);
  const navigation = useNavigation<NavigationProp>();

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
