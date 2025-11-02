import React, { useState } from "react";
import { KeyboardAvoidingView, Platform, Alert } from "react-native";
import { registerVolunteer, verifyCode, ApiError } from "./services/api";
import { styles } from "./styles";
import { SignUpProps } from "./types";
import { VerificationScreen } from "./VerificationScreen";
import { PhoneInputScreen } from "./PhoneInputScreen";

const SignUp: React.FC<SignUpProps> = ({ onComplete }) => {
  const [step, setStep] = useState<"phone" | "verify">("phone");
  const [phoneNumber, setPhoneNumber] = useState<string>("");
  const [selectedHospitalIds, setSelectedHospitalIds] = useState<string[]>([]);
  const [loading, setLoading] = useState<boolean>(false);

  const sendVerificationCodeHandler = async (
    phone: string,
    hospitalIds: string[]
  ): Promise<void> => {
    setLoading(true);
    try {
      await registerVolunteer(phone, hospitalIds);
      setPhoneNumber(phone);
      setSelectedHospitalIds(hospitalIds);
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
        selectedHospitalIds,
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
    setSelectedHospitalIds([]);
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === "ios" ? "padding" : "height"}
    >
      {step === "phone" ? (
        <PhoneInputScreen
          sendVerificationCode={sendVerificationCodeHandler}
          loading={loading}
        />
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

export default SignUp;
