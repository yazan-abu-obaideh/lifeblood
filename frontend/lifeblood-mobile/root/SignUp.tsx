import React, { useState } from "react";
import { KeyboardAvoidingView, Platform, Alert } from "react-native";
import { registerVolunteer, verifyCode, ApiError } from "./services/api";
import { styles } from "./styles";
import { SignUpProps } from "./types";
import { VerificationScreen } from "./VerificationScreen";
import { PhoneInputScreen } from "./PhoneInputScreen";
import { useNavigation } from "@react-navigation/native";
import { NavigationProp } from "./Screens/navigationUtils";

const SignUp: React.FC<SignUpProps> = ({ onComplete }) => {
  const [phoneNumber, setPhoneNumber] = useState<string>("");
  const [selectedHospitalIds, setSelectedHospitalIds] = useState<string[]>([]);
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
      setPhoneNumber(phone);
      setSelectedHospitalIds(hospitalIds);
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

  const verifyCodeHandler = async (code: string): Promise<void> => {
    setLoading(true);
    try {
      await verifyCode(phoneNumber, code);

      onComplete?.({
        phoneNumber,
        selectedHospitals: selectedHospitalIds,
      });
    } catch (error) {
      if (error instanceof ApiError) {
        Alert.alert("Error", error.message);
      } else {
        Alert.alert("Error", "Failed to verify code");
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
      <PhoneInputScreen
        registerVolunteer={registerVolunteerHandler}
        loading={loading}
      />
    </KeyboardAvoidingView>
  );
};

export default SignUp;
